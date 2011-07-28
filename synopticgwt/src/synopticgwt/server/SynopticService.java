package synopticgwt.server;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import synoptic.algorithms.bisim.Bisimulation;
import synoptic.algorithms.graph.PartitionMultiSplit;
import synoptic.invariants.BinaryInvariant;
import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.RelationPath;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.invariants.miners.ChainWalkingTOInvMiner;
import synoptic.invariants.miners.InvariantMiner;
import synoptic.invariants.miners.TransitiveClosureInvMiner;
import synoptic.main.TraceParser;
import synoptic.model.EventNode;
import synoptic.model.Partition;
import synoptic.model.PartitionGraph;
import synoptic.model.TraceGraph;
import synoptic.model.WeightedTransition;
import synoptic.model.export.GraphExporter;
import synopticgwt.client.ISynopticService;
import synopticgwt.shared.GWTGraph;
import synopticgwt.shared.GWTGraphDelta;
import synopticgwt.shared.GWTInvariant;
import synopticgwt.shared.GWTInvariantSet;
import synopticgwt.shared.GWTPair;
import synopticgwt.shared.LogLine;

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
    // The directory in which files are currently exported to.
    private static final String userExport = "userexport/";

    // Variables corresponding to session state.
    private PartitionGraph pGraph;
    private Integer numSplitSteps;
    private Set<ITemporalInvariant> unsatInvs;
    private TemporalInvariantSet minedInvs;
    private Set<ITemporalInvariant> activeInvs;
    private List<RelationPath<Partition>> counterExampleTraces;
    private TraceGraph traceGraph;

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
        for (RelationPath<Partition> relPath : counterExampleTraces) {
            unsatInvs.add(relPath.invariant);
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
        traceGraph = (TraceGraph) session.getAttribute("traceGraph");

        // NOTE: counterExampleTraces is allowed to be null.

        // TODO: create the ability to detect when counterExampleTraces state
        // has not been initialized.

        // if (session.getAttribute("counterExampleTraces") == null) {
        // TODO: throw appropriate exception
        // throw new Exception();
        // }
        counterExampleTraces = (List<RelationPath<Partition>>) session
                .getAttribute("counterExampleTraces");
        return;
    }

    /**
     * Converts a partition graph into a GWTGraph
     * 
     * @param pGraph
     *            partition graph
     * @return Equivalent GWTGraph
     */
    private GWTGraph PGraphToGWTGraph(PartitionGraph pGraph) {
        GWTGraph graph = new GWTGraph();

        Set<Partition> nodes = pGraph.getNodes();
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
     * Converts a TemporalInvariantSet into GWTInvariants
     * 
     * @param invs
     *            TemporalInvariantSet
     * @return Equivalent GWTInvariants
     */
    private GWTInvariantSet TemporalInvariantSetToGWTInvariants(
            Set<ITemporalInvariant> invs) {
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
     * refinement\coarsening.
     */
    @Override
    public GWTPair<GWTInvariantSet, GWTGraph> parseLog(String logLines,
            List<String> regExps, String partitionRegExp, String separatorRegExp)
            throws synoptic.main.ParseException {

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

        // Instantiate the parser.
        TraceParser parser = synoptic.main.Main.newTraceParser(regExps,
                partitionRegExp, separatorRegExp);

        // Parse the log lines.
        ArrayList<EventNode> parsedEvents = parser.parseTraceString(logLines,
                new String("traceName"), -1);
        traceGraph = parser.generateDirectTemporalRelation(parsedEvents);

        // Mine invariants, and convert them to GWTInvariants.
        InvariantMiner miner;
        if (parser.logTimeTypeIsTotallyOrdered()) {
            miner = new ChainWalkingTOInvMiner();
        } else {
            miner = new TransitiveClosureInvMiner();
        }
        minedInvs = miner.computeInvariants(traceGraph);

        initializeRefinementState(minedInvs);
        storeSessionState();

        GWTGraph graph = PGraphToGWTGraph(pGraph);
        GWTInvariantSet invs = TemporalInvariantSetToGWTInvariants(minedInvs
                .getSet());
        return new GWTPair<GWTInvariantSet, GWTGraph>(invs, graph);
    }

    /**
     * Removes invariants from the server's collection. Invariants are specified
     * for removal based on whether their hash code matches any of the hashes
     * passed to the method. The method requires that the input graph and
     * invariant set fields have been initialized.
     * 
     * @param invHash
     *            The hash codes for the invariants to be removed.
     */
    @Override
    public Integer deactivateInvariant(Integer invHash) throws Exception {
        // Set up current state.
        retrieveSessionState();

        LinkedHashSet<ITemporalInvariant> invsToRemove = new LinkedHashSet<ITemporalInvariant>();
        // Get the actual set of invariants to be removed.
        for (ITemporalInvariant inv : minedInvs) {
            if (invHash.equals(inv.hashCode())) {
                invsToRemove.add(inv);
            }
        }

        // Remove all the specified invariants.
        activeInvs.removeAll(invsToRemove);
        return invHash;
    }

    /**
     * Adds invariants to the server's collection. Invariants are specified for
     * removal based on whether their hash code matches any of the hashes passed
     * to the method. The method requires that the input graph and invariant set
     * fields have been initialized.
     * 
     * @param invHash
     *            The hash codes for the invariants to be removed.
     */
    @Override
    public Integer activateInvariant(Integer invHash) throws Exception {
        // Set up current state.
        retrieveSessionState();

        LinkedHashSet<ITemporalInvariant> invsToAdd = new LinkedHashSet<ITemporalInvariant>();
        // Get the actual set of invariants to be removed.
        for (ITemporalInvariant inv : minedInvs) {
            if (invHash.equals(inv.hashCode())) {
                invsToAdd.add(inv);
            }
        }

        // Remove all the specified invariants.
        activeInvs.addAll(invsToAdd);
        return invHash;
    }

    /**
     * Restarts refinement by re-creating the partition graph with the active
     * invariants that the user selected.
     */
    @Override
    public GWTGraph commitInvariants() throws Exception {
        // Set up current state.
        retrieveSessionState();

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
            for (RelationPath<Partition> relPath : counterExampleTraces) {
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
