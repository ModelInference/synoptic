package synopticgwt.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import synoptic.algorithms.bisim.Bisimulation;
import synoptic.invariants.BinaryInvariant;
import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.RelationPath;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.invariants.miners.ChainWalkingTOInvMiner;
import synoptic.invariants.miners.InvariantMiner;
import synoptic.invariants.miners.TransitiveClosureTOInvMiner;
import synoptic.main.TraceParser;
import synoptic.model.EventNode;
import synoptic.model.Graph;
import synoptic.model.Partition;
import synoptic.model.PartitionGraph;
import synopticgwt.client.ISynopticService;
import synopticgwt.shared.GWTGraph;
import synopticgwt.shared.GWTInvariants;
import synopticgwt.shared.GWTPair;

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

    // Variables corresponding to session state.
    private PartitionGraph pGraph;
    private Integer numSplitSteps;
    private Set<ITemporalInvariant> unsatisfiedInvariants;

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
        int pNodeId, adjPNodeId, nextId = 0;

        // Iterate through all the nodes in the pGraph
        for (Partition pNode : nodes) {
            // Add the pNode to the GWTGraph
            if (nodeIds.containsKey(pNode)) {
                pNodeId = nodeIds.get(pNode);
            } else {
                pNodeId = nextId;
                nextId += 1;
                nodeIds.put(pNode, pNodeId);
                graph.addNode(pNodeId, pNode.getEType().toString());
            }

            // Add all the edges corresponding to pNode to the GWTGraph
            Set<Partition> adjacents = pGraph.getAdjacentNodes(pNode);
            for (Partition adjPNode : adjacents) {
                if (nodeIds.containsKey(adjPNode)) {
                    adjPNodeId = nodeIds.get(adjPNode);
                } else {
                    adjPNodeId = nextId;
                    nextId += 1;
                    nodeIds.put(adjPNode, adjPNodeId);
                    graph.addNode(adjPNodeId, adjPNode.getEType().toString());
                }
                graph.addEdge(pNodeId, adjPNodeId);
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
    private GWTInvariants TemporalInvariantSetToGWTInvariants(
            TemporalInvariantSet invs) {
        GWTInvariants GWTinvs = new GWTInvariants();
        for (ITemporalInvariant inv : invs) {
            String invKey = inv.getShortName();
            GWTPair<String, String> invVal;
            if (inv instanceof BinaryInvariant) {
                invVal = new GWTPair<String, String>(((BinaryInvariant) inv)
                        .getFirst().toString(), ((BinaryInvariant) inv)
                        .getSecond().toString());
                GWTinvs.addInv(invKey, invVal);
            } else {
                // TODO: throw an exception
            }
        }
        return GWTinvs;
    }

    // //////////////////////////////////////////////////////////////////////////////

    @Override
    public GWTPair<GWTInvariants, GWTGraph> parseLog(String logLines,
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
            miner = new TransitiveClosureTOInvMiner();
        }
        TemporalInvariantSet minedInvs = miner.computeInvariants(inputGraph);
        GWTInvariants invs = TemporalInvariantSetToGWTInvariants(minedInvs);

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
        session.setAttribute("unsatInvs", unsatisfiedInvariants);
        session.setAttribute("model", pGraph);
        session.setAttribute("numSplitSteps", 0);

        return new GWTPair<GWTInvariants, GWTGraph>(invs, graph);
    }

    @Override
    public GWTGraph refineOneStep() throws Exception {
        retrieveSessionState();
        // Retrieve the counter-examples for the unsatisfied invariants.
        List<RelationPath<Partition>> counterExampleTraces = new TemporalInvariantSet(
                unsatisfiedInvariants).getAllCounterExamples(pGraph);

        if (counterExampleTraces != null && counterExampleTraces.size() > 0) {
            // Perform a single refinement step.
            numSplitSteps = Bisimulation.performOneSplitPartitionsStep(
                    numSplitSteps, pGraph, counterExampleTraces);

            // Recompute the unsatisfied invariants based on
            // counterExampleTraces (set by reference above).
            unsatisfiedInvariants.clear();
            for (RelationPath<Partition> relPath : counterExampleTraces) {
                unsatisfiedInvariants.add(relPath.invariant);
            }
            // Return the new model.
            return PGraphToGWTGraph(pGraph);
        }
        // We did not need to perform refinement (model is final).
        return null;
    }

    @Override
    public GWTGraph coarsenOneStep() throws Exception {
        retrieveSessionState();
        Bisimulation.mergePartitions(pGraph);
        return PGraphToGWTGraph(pGraph);
    }
}
