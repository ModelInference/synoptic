package synoptic.invariants.fsmcheck.birelational;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import synoptic.invariants.AlwaysFollowedInvariant;
import synoptic.invariants.AlwaysPrecedesInvariant;
import synoptic.invariants.BinaryInvariant;
import synoptic.invariants.CExamplePath;
import synoptic.invariants.NeverFollowedInvariant;
import synoptic.invariants.birelational.AFBiRelationInvariant;
import synoptic.invariants.birelational.APBiRelationInvariant;
import synoptic.invariants.birelational.BiRelationalInvariant;
import synoptic.invariants.birelational.NFBiRelationInvariant;
import synoptic.invariants.fsmcheck.birelational.tracing.AFBiTracingSet;
import synoptic.invariants.fsmcheck.birelational.tracing.APBiTracingSet;
import synoptic.invariants.fsmcheck.birelational.tracing.NFBiTracingSet;
import synoptic.invariants.fsmcheck.birelational.tracing.TracingBiRelationalStateSet;
import synoptic.main.SynopticMain;
import synoptic.main.options.SynopticOptions;
import synoptic.model.interfaces.IGraph;
import synoptic.model.interfaces.INode;

public class BiFsmModelChecker {
    
    public BiFsmModelChecker() {
        super();
        SynopticMain synopticMain = synoptic.main.SynopticMain.getInstanceWithExistenceCheck();
        SynopticOptions options = synopticMain.options;
        boolean multipleRelations = options.multipleRelations;
        
        if (!multipleRelations) {
            throw new IllegalStateException("Multiple Relations not enabled");
        }
    }
    
    /**
     * Given an initial StateSet, and graph to check, this yields the fixpoint
     * states eventually reached. The states in the graph are transitioned along
     * edges, and merged at the nodes. Once every merge causes no change to the
     * graph, the resulting association between nodes in the graph and states is
     * yielded.
     * 
     * @param <StateSet>
     *            The type of StateSet we are propagating.
     * @param stateset
     *            The initial state of each node.
     * @param graph
     *            The graph to analyze.
     * @return The associations between node and stateset.
     */
    public static <Node extends INode<Node>> Map<Node, TracingBiRelationalStateSet<Node>> runChecker(
            TracingBiRelationalStateSet<Node> stateset, IGraph<Node> graph,
            boolean earlyExit) {

        
        // A queue of nodes that we should process.
        Queue<Node> workList = new LinkedList<Node>();

        // Maps a node to a set of states.
        Map<Node, TracingBiRelationalStateSet<Node>> states = new LinkedHashMap<Node, TracingBiRelationalStateSet<Node>>();

        // Populate the state map with initial states.
        for (Node node : graph.getNodes()) {
            states.put(node, stateset.copy());
        }

        // Add initial node to the worklist.
        Node node = graph.getDummyInitialNode();
        workList.add(node);

        TracingBiRelationalStateSet<Node> stateSet = states.get(node);
        // stateSet.setInitial(node);


        // Actual model checking step - takes an item off the worklist, and
        // transitions the state found at that node, using the labels of all
        // of the adjacent nodes as input. The resulting state is then checked
        // for subset with the stateset cached at the destination node. If it is
        // found to be a subset, then merging in the new state would cause no
        // change. Therefore, only in the case where it's not a subset is the
        // merge performed and the destination node added to the worklist
        // (the changed states need to be propagated).
        while (!workList.isEmpty()) {
            node = workList.remove();
            TracingBiRelationalStateSet<Node> current = states.get(node);

            // Process all the nodes that are adjacent to the current node.
            for (Node target : graph.getAdjacentNodes(node)) {
                TracingBiRelationalStateSet<Node> oldTargetStates = states.get(target);
                TracingBiRelationalStateSet<Node> updatesToTargetStates = current.copy();
                
                // updatesToTargetStates.transition(target);

                // Evaluate isSubset _before_ the merge.
                boolean isSubset = updatesToTargetStates
                        .isSubset(oldTargetStates);
                oldTargetStates.mergeWith(updatesToTargetStates);
                if (earlyExit && oldTargetStates.isFail()
                        && target.isTerminal()) {
                    return states;
                }

                // Optimization: if updatesToTargetStates is subset of
                // targetStates then I do not need to re-explore the graph
                // starting from the current node.
                if (!isSubset && !workList.contains(target)) {
                    workList.add(target);
                }
            }
        }

        return states;
    }

    /**
     * Runs invariant-checking finite state machines over the synoptic.model
     * graph, while keeping history paths which justify any particular state.
     * This allows us to report counterexample paths, where a failure state is
     * reached on a final node.
     * 
     * @param invariant
     *            The invariant to test.
     * @return The shortest counterexample path for this invariant.
     */
    @SuppressWarnings("unchecked")
    public static <Node extends INode<Node>> CExamplePath<Node> getCounterExample(
            BiRelationalInvariant invariant, IGraph<Node> graph) {

        
        
        TracingBiRelationalStateSet<Node> stateset = null;
        if (invariant == null) {
            return null;
        }
        Class<BiRelationalInvariant> invClass = (Class<BiRelationalInvariant>) invariant
                .getClass();
                    
        if (invClass.equals(AFBiRelationInvariant.class)) {
            stateset = new AFBiTracingSet<Node>(invariant);
        } else if (invClass.equals(APBiRelationInvariant.class)) {
            stateset = new APBiTracingSet<Node>(invariant);
        } else if (invClass.equals(NFBiRelationInvariant.class)) {
            stateset = new NFBiTracingSet<Node>(invariant);
        }
        
        stateset.addRelation(invariant.getRelation());
        stateset.addClosureRelation(invariant.getOrderingRelation());

        // Return the shortest path, ending on a final node, which causes the
        // invariant to fail.
        TracingBiRelationalStateSet<Node>.HistoryNode shortestPath = null;
        Set<Entry<Node, TracingBiRelationalStateSet<Node>>> entrySet = runChecker(stateset,
                graph, true).entrySet();
        for (Entry<Node, TracingBiRelationalStateSet<Node>> e : entrySet) {
            TracingBiRelationalStateSet<Node> stateSet = e.getValue();
            Node node = e.getKey();

            TracingBiRelationalStateSet<Node>.HistoryNode path = stateSet.failpath();

            // 1. We must have ended up at the terminal node.
            // 2. Invariant is not satisfied, so we have a history path for it.
            // 3. If we had counter-example path in the past, that path is
            // longer (because we want the shortest).
            if (node.isTerminal()
                    && path != null
                    && (shortestPath == null || shortestPath.count > path.count)) {
                shortestPath = path;
            }
        }

        // Convert to RelationPath
        if (shortestPath == null) {
            return null;
        }

        return shortestPath.toCounterexample(invariant);
    }
}