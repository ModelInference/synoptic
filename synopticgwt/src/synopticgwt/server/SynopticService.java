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
import synoptic.model.Graph;
import synoptic.model.Partition;
import synoptic.model.PartitionGraph;
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
    private Set<ITemporalInvariant> unsatisfiedInvariants;
    private TemporalInvariantSet minedInvs;
    private Graph<EventNode> iGraph;

    // //////////////////////////////////////////////////////////////////////////////
    // Helper methods.

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
        if (session.getAttribute("model") == null) {
            // TODO: throw appropriate exception
            throw new Exception();
        }
        pGraph = (PartitionGraph) session.getAttribute("model");
        if (session.getAttribute("numSplitSteps") == null) {
            // TODO: throw appropriate exception
            throw new Exception();
        }
        numSplitSteps = (Integer) session.getAttribute("numSplitSteps");
        if (session.getAttribute("unsatInvs") == null) {
            // TODO: throw appropriate exception
            throw new Exception();
        }
        unsatisfiedInvariants = (Set<ITemporalInvariant>) session
                .getAttribute("unsatInvs");
        if (session.getAttribute("invariants") == null) {
            // TODO: throw appropriate exception
            throw new Exception();
        }
        minedInvs = (TemporalInvariantSet) session.getAttribute("invariants");
        if (session.getAttribute("inputGraph") == null) {
            // TODO: throw appropriate exception
            throw new Exception();
        }
        iGraph = (Graph<EventNode>) session.getAttribute("inputGraph");
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
            TemporalInvariantSet invs) {
        GWTInvariantSet GWTinvs = new GWTInvariantSet();
        for (ITemporalInvariant inv : invs) {
            String invKey = inv.getShortName();
            GWTInvariant<String, String> invVal;
            if (inv instanceof BinaryInvariant) {
                invVal = new GWTInvariant<String, String>(
                        ((BinaryInvariant) inv).getFirst().toString(),
                        ((BinaryInvariant) inv).getSecond().toString(),
                        ((BinaryInvariant) inv).getShortName());

                // Set a unique identification id.
                invVal.setID(inv.hashCode());

                GWTinvs.addInv(invKey, invVal);
            } else {
                // TODO: throw an exception
            }
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
        Graph<EventNode> inputGraph = parser
                .generateDirectTemporalRelation(parsedEvents);

        // Mine invariants, and convert them to GWTInvariants.
        InvariantMiner miner;
        if (parser.logTimeTypeIsTotallyOrdered()) {
            miner = new ChainWalkingTOInvMiner();
        } else {
            miner = new TransitiveClosureInvMiner();
        }
        TemporalInvariantSet minedInvs = miner.computeInvariants(inputGraph);

        return convertToGWT(minedInvs, inputGraph);
    }

    /**
     * Removes invariants from the server's collection. Invariants are specified
     * for removal based on whether their hash code matches any of the hashes
     * passed to the method. The method requires that the input graph and
     * invariant set fields have been initialized, and alters the
     * 
     * @param hashes
     *            The hash codes for the invariants to be removed.
     */
    @Override
    public GWTPair<GWTInvariantSet, GWTGraph> removeInvs(Set<Integer> hashes)
            throws Exception {

        // Set up current state.
        retrieveSessionState();

        LinkedHashSet<ITemporalInvariant> invariantsForRemoval = new LinkedHashSet<ITemporalInvariant>();
        // Get the actual set of invariants to be removed.
        for (ITemporalInvariant inv : minedInvs) {
            if (hashes.contains(inv.hashCode())) {
                invariantsForRemoval.add(inv);
            }

        }

        // Remove all invariants.
        minedInvs.removeAll(invariantsForRemoval);

        return convertToGWT(minedInvs, iGraph);
    }

    /**
     * Helper method for parseLog and removInvs methods
     */

    private GWTPair<GWTInvariantSet, GWTGraph> convertToGWT(
            TemporalInvariantSet minedInvs, Graph<EventNode> inputGraph) {
        GWTInvariantSet invs = TemporalInvariantSetToGWTInvariants(minedInvs);

        // Create a PartitionGraph and convert it into a GWTGraph.
        PartitionGraph pGraph = new PartitionGraph(inputGraph, true, minedInvs);
        GWTGraph graph = PGraphToGWTGraph(pGraph);

        // Retrieve HTTP session for storage.
        HttpServletRequest request = getThreadLocalRequest();
        HttpSession session = request.getSession();

        // Save stuff into session.

        // Compute and save unsatisfied invariants (they are just the mined
        // invariants)
        Set<ITemporalInvariant> unsatisfiedInvariants;
        unsatisfiedInvariants = new LinkedHashSet<ITemporalInvariant>();
        unsatisfiedInvariants.addAll(pGraph.getInvariants().getSet());
        session.setAttribute("invariants", minedInvs);
        session.setAttribute("inputGraph", inputGraph);
        session.setAttribute("unsatInvs", unsatisfiedInvariants);
        session.setAttribute("model", pGraph);
        session.setAttribute("numSplitSteps", 0);

        return new GWTPair<GWTInvariantSet, GWTGraph>(invs, graph);
    }

    /**
     * Performs a single step of refinement on the cached model.
     */
    @Override
    public GWTGraphDelta refineOneStep() {
        // Retrieve HTTP session to access storage.
        HttpServletRequest request = getThreadLocalRequest();
        HttpSession session = request.getSession();
        // Retrieve stuff from storage, and if we can't find something then we
        // throw an error since we can't continue with refinement.
        if (session.getAttribute("model") == null) {
            // TODO: throw appropriate exception
            return null;
        }
        PartitionGraph pGraph = (PartitionGraph) session.getAttribute("model");

        if (session.getAttribute("numSplitSteps") == null) {
            // TODO: throw appropriate exception
            return null;
        }
        Integer numSplitSteps = (Integer) session.getAttribute("numSplitSteps");
        if (session.getAttribute("unsatInvs") == null) {
            // TODO: throw appropriate exception
            return null;
        }
        @SuppressWarnings("unchecked")
        Set<ITemporalInvariant> unsatisfiedInvariants = (Set<ITemporalInvariant>) session
                .getAttribute("unsatInvs");

        if (unsatisfiedInvariants.size() != 0) {
            // Retrieve the counter-examples for the unsatisfied invariants.
            List<RelationPath<Partition>> counterExampleTraces = new TemporalInvariantSet(
                    unsatisfiedInvariants).getAllCounterExamples(pGraph);

            // TODO: raise an appropriate Exception when the assert condition is
            // violated.
            assert (counterExampleTraces != null && counterExampleTraces.size() > 0);

            // Perform a single refinement step.
            numSplitSteps = Bisimulation.performOneSplitPartitionsStep(
                    numSplitSteps, pGraph, counterExampleTraces);

            // Recompute the unsatisfied invariants based on
            // counterExampleTraces (set by reference above).
            unsatisfiedInvariants.clear();
            for (RelationPath<Partition> relPath : counterExampleTraces) {
                unsatisfiedInvariants.add(relPath.invariant);
            }

            PartitionMultiSplit last = pGraph.getMostRecentSplit();

            int refinedNode = last.getPartition().hashCode(); // until
                                                              // determined
            // Return the new model.
            return new GWTGraphDelta(PGraphToGWTGraph(pGraph), refinedNode);
        }
        // We did not need to perform refinement.
        return null;
    }

    /**
     * Performs coarsening of the completely refined model in one single step.
     */
    @Override
    public GWTGraph coarsenOneStep() throws Exception {
        // Set up state.
        retrieveSessionState();

        // TODO: raise an appropriate Exception when the assert condition is
        // violated.
        assert (unsatisfiedInvariants.size() == 0);

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
        unsatisfiedInvariants.clear();

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
     * <<<<<<< local Exports the current model and downloads it as a .png and
     * .dot file. Returns the filename/directory. ======= Exports the current
     * model as a .dot file. Returns the filename/directory. >>>>>>> other
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
