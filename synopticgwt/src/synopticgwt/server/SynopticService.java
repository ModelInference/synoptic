package synopticgwt.server;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import synoptic.algorithms.bisim.Bisimulation;
import synoptic.algorithms.graph.PartitionMultiSplit;
import synoptic.invariants.BinaryInvariant;
import synoptic.invariants.CExamplePath;
import synoptic.invariants.ConcurrencyInvariant;
import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.invariants.miners.ChainWalkingTOInvMiner;
import synoptic.invariants.miners.POInvariantMiner;
import synoptic.invariants.miners.TOInvariantMiner;
import synoptic.invariants.miners.TransitiveClosureInvMiner;
import synoptic.main.ParseException;
import synoptic.main.TraceParser;
import synoptic.model.ChainsTraceGraph;
import synoptic.model.DAGsTraceGraph;
import synoptic.model.EventNode;
import synoptic.model.Partition;
import synoptic.model.PartitionGraph;
import synoptic.model.WeightedTransition;
import synoptic.model.export.GraphExporter;
import synoptic.util.InternalSynopticException;
import synopticgwt.client.ISynopticService;
import synopticgwt.shared.GWTGraph;
import synopticgwt.shared.GWTGraphDelta;
import synopticgwt.shared.GWTInvariant;
import synopticgwt.shared.GWTInvariantSet;
import synopticgwt.shared.GWTPair;
import synopticgwt.shared.LogLine;
import synopticgwt.shared.SerializableParseException;

/**
 * Implements the Synoptic service which does:
 * 
 * <pre>
 * - Log parsing
 * - Model refinement
 * - Model coarsening
 * </pre>
 */
public class SynopticService extends RemoteServiceServlet implements
        ISynopticService {

    private static final long serialVersionUID = 1L;

    public static Logger logger = Logger.getLogger("SynopticService");

    // The directory in which files are currently exported to.
    private static final String userExport = "userexport/";

    // Variables corresponding to session state.
    private PartitionGraph pGraph;
    private Integer numSplitSteps;
    private Set<ITemporalInvariant> unsatInvs;
    private TemporalInvariantSet minedInvs;
    private Set<ITemporalInvariant> activeInvs;
    private List<CExamplePath<Partition>> counterExampleTraces;
    private ChainsTraceGraph traceGraph;

    // //////////////////////////////////////////////////////////////////////////////
    // Helper methods.

    /**
     * Initializes the server state.
     */
    private void initializeRefinementState(TemporalInvariantSet invs) {
        pGraph = new PartitionGraph(traceGraph, true, invs);
        numSplitSteps = 0;

        unsatInvs = new LinkedHashSet<ITemporalInvariant>();
        unsatInvs.addAll(pGraph.getInvariants().getSet());

        counterExampleTraces = new TemporalInvariantSet(unsatInvs)
                .getAllCounterExamples(pGraph);

        unsatInvs.clear();
        if (counterExampleTraces != null) {
            logger.info("counterExampleTraces : "
                    + counterExampleTraces.toString());
            for (CExamplePath<Partition> relPath : counterExampleTraces) {
                unsatInvs.add(relPath.invariant);
            }
        } else {
            logger.info("counterExampleTraces : NONE");
        }

        activeInvs = new LinkedHashSet<ITemporalInvariant>();
        activeInvs.addAll(pGraph.getInvariants().getSet());
    }

    /**
     * Save server state into the session object.
     */
    private void storeSessionState() {
        // Retrieve HTTP session and store all the state in the session.
        HttpServletRequest request = getThreadLocalRequest();
        HttpSession session = request.getSession();

        session.setAttribute("partitionGraph", pGraph);
        session.setAttribute("numSplitSteps", 0);
        session.setAttribute("unsatInvs", unsatInvs);
        session.setAttribute("minedInvs", minedInvs);
        session.setAttribute("activeInvs", activeInvs);
        session.setAttribute("traceGraph", traceGraph);
        session.setAttribute("counterExampleTraces", counterExampleTraces);
    }

    /**
     * Retrieves session state and sets/reconstructs the local variables.
     */
    @SuppressWarnings("unchecked")
    private void retrieveSessionState() throws Exception {
        // Retrieve HTTP session to access storage.
        HttpServletRequest request = getThreadLocalRequest();
        HttpSession session = request.getSession();

        // Retrieve stuff from storage, and if we can't find something then we
        // throw an error since we can't continue with refinement.

        if (session.getAttribute("partitionGraph") == null) {
            // TODO: throw appropriate exception
            throw new Exception();
        }
        pGraph = (PartitionGraph) session.getAttribute("partitionGraph");

        if (session.getAttribute("numSplitSteps") == null) {
            // TODO: throw appropriate exception
            throw new Exception();
        }
        numSplitSteps = (Integer) session.getAttribute("numSplitSteps");

        if (session.getAttribute("unsatInvs") == null) {
            // TODO: throw appropriate exception
            throw new Exception();
        }
        unsatInvs = (Set<ITemporalInvariant>) session.getAttribute("unsatInvs");

        if (session.getAttribute("minedInvs") == null) {
            // TODO: throw appropriate exception
            throw new Exception();
        }
        minedInvs = (TemporalInvariantSet) session.getAttribute("minedInvs");

        if (session.getAttribute("activeInvs") == null) {
            // TODO: throw appropriate exception
            throw new Exception();
        }
        activeInvs = (Set<ITemporalInvariant>) session
                .getAttribute("activeInvs");

        if (session.getAttribute("traceGraph") == null) {
            // TODO: throw appropriate exception
            throw new Exception();
        }
        traceGraph = (ChainsTraceGraph) session.getAttribute("traceGraph");

        // NOTE: counterExampleTraces is allowed to be null.

        // TODO: create the ability to detect when counterExampleTraces state
        // has not been initialized.

        // if (session.getAttribute("counterExampleTraces") == null) {
        // TODO: throw appropriate exception
        // throw new Exception();
        // }
        counterExampleTraces = (List<CExamplePath<Partition>>) session
                .getAttribute("counterExampleTraces");
        return;
    }

    /**
     * Converts a partition graph into a GWTGraph
     * 
     * @param partGraph
     *            partition graph
     * @return Equivalent GWTGraph
     */
    private GWTGraph PGraphToGWTGraph(PartitionGraph partGraph) {
        GWTGraph graph = new GWTGraph();

        Set<Partition> nodes = partGraph.getNodes();
        HashMap<Partition, Integer> nodeIds = new HashMap<Partition, Integer>();
        int pNodeId, adjPNodeId = 0;

        // Iterate through all the nodes in the pGraph
        for (Partition pNode : nodes) {
            // Add the pNode to the GWTGraph
            if (nodeIds.containsKey(pNode)) {
                pNodeId = nodeIds.get(pNode);
            } else {
                pNodeId = pNode.hashCode();
                nodeIds.put(pNode, pNodeId);
                graph.addNode(pNodeId, pNode.getEType().toString());
            }

            /*
             * Get the list of adjacent nodes that have the current pNode as the
             * source.
             */
            List<WeightedTransition<Partition>> adjacents = pNode
                    .getWeightedTransitions();

            // For every adjacent node, calculate the likelihood of the
            // transition, and add that to the graph's edge.
            for (WeightedTransition<Partition> wTransition : adjacents) {
                // The current adjacent partition.
                Partition adjPNode = wTransition.getTarget();

                if (nodeIds.containsKey(adjPNode.hashCode())) {
                    adjPNodeId = nodeIds.get(adjPNode);
                } else {
                    // Add the node to the graph so it can be connected
                    // if it doesn't exist.
                    adjPNodeId = adjPNode.hashCode();
                    nodeIds.put(adjPNode, adjPNodeId);
                    graph.addNode(adjPNodeId, adjPNode.getEType().toString());
                }

                // Truncate the last three digits to make the weight more
                // readable
                Double transitionFrac = Math
                        .ceil(wTransition.getFraction() * 1000) / 1000;

                // Add the complete weighted edge
                graph.addEdge(pNodeId, adjPNodeId, transitionFrac);
            }
        }
        return graph;
    }

    /**
     * Calls the TemporalInvariantSetToGWTInvariants below, but first determines
     * if there are any concurrency invariants in the input set.
     */
    private GWTInvariantSet TemporalInvariantSetToGWTInvariants(
            Set<ITemporalInvariant> invs) {
        boolean containsConcurrencyInvs = false;
        for (ITemporalInvariant inv : invs) {
            if (inv instanceof ConcurrencyInvariant) {
                containsConcurrencyInvs = true;
            }
        }
        return TemporalInvariantSetToGWTInvariants(containsConcurrencyInvs,
                invs);
    }

    /**
     * Converts a TemporalInvariantSet into GWTInvariants
     * 
     * @param containsConcurrencyInvs
     *            whether or not the set of invariants contains any
     *            ConcurrencyInvariant instances
     * @param invs
     * @return Equivalent GWTInvariants
     */
    private GWTInvariantSet TemporalInvariantSetToGWTInvariants(
            boolean containsConcurrencyInvs, Set<ITemporalInvariant> invs) {
        GWTInvariantSet GWTinvs = new GWTInvariantSet();
        for (ITemporalInvariant inv : invs) {
            assert (inv instanceof BinaryInvariant);
            BinaryInvariant bInv = ((BinaryInvariant) inv);
            String invKey = inv.getShortName();
            GWTInvariant invVal = new GWTInvariant(bInv.getFirst().toString(),
                    bInv.getSecond().toString(), bInv.getShortName());

            // Set a unique identification id.
            invVal.setID(inv.hashCode());

            GWTinvs.addInv(invKey, invVal);

        }
        return GWTinvs;
    }

    // //////////////////////////////////////////////////////////////////////////////

    /**
     * Parses the input log, and sets up and stores Synoptic session state for
     * refinement\coarsening. For a PO log this returns a null GWTGraph.
     * 
     * @throws Exception
     */
    @Override
    public GWTPair<GWTInvariantSet, GWTGraph> parseLog(String logLines,
            List<String> regExps, String partitionRegExp, String separatorRegExp)
            throws Exception {

        // Set up some static variables in Main that are necessary to use the
        // Synoptic library.
        synoptic.main.Main.setUpLogging();
        synoptic.main.Main.recoverFromParseErrors = false;
        synoptic.main.Main.ignoreNonMatchingLines = false;
        synoptic.main.Main.debugParse = false;
        synoptic.main.Main.logLvlVerbose = false;
        synoptic.main.Main.logLvlExtraVerbose = false;
        synoptic.main.Main.graphExportFormatter = new synoptic.model.export.DotExportFormatter();
        synoptic.main.Main.randomSeed = System.currentTimeMillis();
        synoptic.main.Main.random = new java.util.Random(
                synoptic.main.Main.randomSeed);

        // Instantiate the parser and parse the log lines.
        TraceParser parser = null;
        ArrayList<EventNode> parsedEvents = null;
        try {
            parser = synoptic.main.Main.newTraceParser(regExps,
                    partitionRegExp, separatorRegExp);
            parsedEvents = parser.parseTraceString(logLines, new String(
                    "traceName"), -1);
        } catch (InternalSynopticException ise) {
            throw serializeException(ise);

        } catch (ParseException pe) {
            throw serializeException(pe);

        }

        // Code below mines invariants, and converts them to GWTInvariants.
        // TODO: refactor synoptic main so that it does all of this most of this
        // for the client.
        GWTGraph graph;
        if (parser.logTimeTypeIsTotallyOrdered()) {
            ChainsTraceGraph inputGraph = parser
                    .generateDirectTORelation(parsedEvents);
            TOInvariantMiner miner = new ChainWalkingTOInvMiner();
            minedInvs = miner.computeInvariants(inputGraph);

            // Since we're in the TO case then we also initialize and store
            // refinement state.
            initializeRefinementState(minedInvs);
            storeSessionState();
            graph = PGraphToGWTGraph(pGraph);

        } else {
            // TODO: expose to the user the option of using another kind of
            // PO invariant miner.
            DAGsTraceGraph inputGraph = parser
                    .generateDirectPORelation(parsedEvents);
            POInvariantMiner miner = new TransitiveClosureInvMiner();
            minedInvs = miner.computeInvariants(inputGraph);
            graph = null;
        }

        GWTInvariantSet invs = TemporalInvariantSetToGWTInvariants(
                !parser.logTimeTypeIsTotallyOrdered(), minedInvs.getSet());

        return new GWTPair<GWTInvariantSet, GWTGraph>(invs, graph);
    }

    private SerializableParseException serializeException(ParseException pe) {
        SerializableParseException exception = new SerializableParseException(
                pe.getMessage());
        if (pe.hasRegex()) {
            exception.setRegex(pe.getRegex());
        }
        if (pe.hasLogLine()) {
            exception.setLogLine(pe.getLogLine());
        }
        return exception;

    }

    private SerializableParseException serializeException(
            InternalSynopticException ise) {
        if (ise.hasParseException()) {
            return serializeException(ise.getParseException());
        }
        return new SerializableParseException(ise.getMessage());
    }

    /**
     * Restarts refinement by re-creating the partition graph with the active
     * invariants that the user selected.
     * 
     * @throws Exception
     */
    @Override
    public GWTGraph commitInvariants(Set<Integer> activeInvsHashes)
            throws Exception {
        // Set up current state.
        retrieveSessionState();

        activeInvs.clear();
        // Get the actual set of invariants to be removed.
        for (ITemporalInvariant inv : minedInvs) {
            if (activeInvsHashes.contains(inv.hashCode())) {
                activeInvs.add(inv);
            }
        }

        initializeRefinementState(new TemporalInvariantSet(activeInvs));
        storeSessionState();

        return PGraphToGWTGraph(pGraph);
    }

    /**
     * Performs a single step of refinement on the cached model.
     * 
     * @throws Exception
     */
    @Override
    public GWTGraphDelta refineOneStep() throws Exception {
        // Set up state.
        retrieveSessionState();

        if (counterExampleTraces == null) {
            // We do not need to perform refinement.
            return null;
        }
        assert (counterExampleTraces.size() > 0);

        // Perform a single refinement step.
        numSplitSteps = Bisimulation.performOneSplitPartitionsStep(
                numSplitSteps, pGraph, counterExampleTraces);

        // Recompute the counter-examples for the unsatisfied invariants.
        counterExampleTraces = new TemporalInvariantSet(unsatInvs)
                .getAllCounterExamples(pGraph);

        unsatInvs.clear();
        if (counterExampleTraces != null) {
            for (CExamplePath<Partition> relPath : counterExampleTraces) {
                unsatInvs.add(relPath.invariant);
            }
        }
        PartitionMultiSplit last = pGraph.getMostRecentSplit();

        int refinedNode = last.getPartition().hashCode();

        // Because we've created new objects on top of older objects we need to
        // store the state explicitly.
        storeSessionState();

        // Return the new model.
        return new GWTGraphDelta(PGraphToGWTGraph(pGraph), refinedNode,
                TemporalInvariantSetToGWTInvariants(unsatInvs));

    }

    /**
     * Performs coarsening of the completely refined model in one single step.
     */
    @Override
    public GWTGraph coarsenOneStep() throws Exception {
        // Set up state.
        retrieveSessionState();

        if (unsatInvs.size() != 0) {
            return null;
        }

        Bisimulation.mergePartitions(pGraph);
        return PGraphToGWTGraph(pGraph);
    }

    /**
     * Completes any refinement left to be done and then coarsens the graph into
     * a final model.
     */
    @Override
    public GWTGraph getFinalModel() throws Exception {
        // Set up state.
        retrieveSessionState();

        // Refine.
        Bisimulation.splitPartitions(pGraph);
        unsatInvs.clear();

        // Coarsen.
        Bisimulation.mergePartitions(pGraph);
        return PGraphToGWTGraph(pGraph);
    }

    /**
     * Find the requested partition and returns a list of log lines, each in the
     * form [line #, line, filename]
     */
    @Override
    public List<LogLine> handleLogRequest(int nodeID) throws Exception {
        // Set up state.
        retrieveSessionState();

        // Find partition
        Partition requested = null;
        for (Partition p : pGraph.getNodes()) {
            if (p.hashCode() == nodeID) {
                requested = p;
                break;
            }
        }

        // Fetch log lines
        List<LogLine> validLines = new ArrayList<LogLine>();
        if (requested != null) {
            for (EventNode event : requested.getEventNodes()) {
                validLines.add(new LogLine(event.getLineNum(), event.getLine(),
                        event.getShortFileName()));
            }
        }

        return validLines;
    }

    /**
     * Exports the current model as a .dot file. Returns the filename/directory.
     */
    @Override
    public String exportDot() throws Exception {
        retrieveSessionState();
        Calendar now = Calendar.getInstance();
        // Naming convention for the file can be improved
        String fileString = userExport + now.getTimeInMillis()
                + "exportmodel.dot";
        GraphExporter.exportGraph(fileString, pGraph, true);
        return fileString;
    }

    /**
     * Exports the current model as a .png file. Returns the filename/directory.
     */
    @Override
    public String exportPng() throws Exception {
        String fileString = exportDot();
        GraphExporter.generatePngFileFromDotFile(fileString);
        return fileString + ".png";
    }

}
