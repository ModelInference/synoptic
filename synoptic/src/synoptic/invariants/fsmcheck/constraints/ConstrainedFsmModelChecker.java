package synoptic.invariants.fsmcheck.constraints;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Map.Entry;

import synoptic.invariants.AlwaysFollowedInvariant;
import synoptic.invariants.AlwaysPrecedesInvariant;
import synoptic.invariants.BinaryInvariant;
import synoptic.invariants.CExamplePath;
import synoptic.invariants.constraints.TempConstrainedInvariant;
import synoptic.invariants.fsmcheck.HistoryNode;
import synoptic.model.interfaces.IGraph;
import synoptic.model.interfaces.INode;
import synoptic.model.interfaces.ITransition;

public class ConstrainedFsmModelChecker {
	/**
     * Given an initial StateSet, and graph to check, this yields the fixpoint
     * states eventually reached. The states in the graph are transitioned along
     * edges, and merged at the nodes. Once every merge causes no change to the
     * graph, the resulting association between nodes in the graph and states is
     * yielded.
     * 
     * @param <StateSet>
     *            The type of StateSet we are propagating.
     * @param initial
     *            The initial state of each node.
     * @param graph
     *            The graph to analyze.
     * @return The associations between node and stateset.
     */
    public static <Node extends INode<Node>, StateSet extends IConstrainedStateSet<Node, StateSet>> Map<Node, StateSet> runChecker(
            IConstrainedStateSet<Node, StateSet> initial, IGraph<Node> graph,
            boolean earlyExit) {

        // A queue of nodes that we should process.
        Queue<Node> workList = new LinkedList<Node>();

        // Maps a node to a set of states.
        Map<Node, StateSet> states = new LinkedHashMap<Node, StateSet>();

        // Populate the state map with initial states.
        for (Node node : graph.getNodes()) {
            states.put(node, initial.copy());
        }

        // Add initial node to the worklist.
        Node node = graph.getDummyInitialNode();
        workList.add(node);
        states.get(node).setInitial(node);

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
            StateSet current = states.get(node);
            
            // Process all the nodes that are adjacent to the current node.
            for (Node target : graph.getAdjacentNodes(node)) {
            	ITransition<Node> trans = getTransition(node, target);
            	
                StateSet oldTargetStates = states.get(target);
                StateSet updatesToTargetStates = current.copy();
                updatesToTargetStates.transition(target, trans);

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
    
    // Given a source node and target node, return the transition between the two.
    private static <Node extends INode<Node>> ITransition<Node> getTransition(Node source, Node target) {
    	//System.out.println("source = " + source);
    	for (ITransition<Node> trans : source.getAllTransitions()) {
    		//System.out.println("curTarget = " + trans.getTarget() + ", realTarget = " + target);
    		if (trans.getTarget().equals(target)) {
    			return trans;
    		}
    	}
    	return null;
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
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <Node extends INode<Node>> CExamplePath<Node> getCounterExample(
            TempConstrainedInvariant invariant, IGraph<Node> graph) {

        ConstrainedTracingStateSet<Node> stateset = null;
        if (invariant == null) {
            return null;
        }
        
        BinaryInvariant bInv = invariant.getInv();
        
        Class<BinaryInvariant> invClass = (Class<BinaryInvariant>) bInv.getClass();
        if (invClass.equals(AlwaysFollowedInvariant.class)) {
            stateset = new ConstrainedAFbyTracingSet<Node>(invariant);
        } else if (invClass.equals(AlwaysPrecedesInvariant.class)) {
            stateset = new ConstrainedAPTracingSet<Node>(invariant);
        } 

        // Return the shortest path, ending on a final node, which causes the
        // invariant to fail.
        HistoryNode<Node> shortestPath = null;
        Set<Entry<Node, ConstrainedTracingStateSet<Node>>> entrySet = runChecker(stateset,
                graph, true).entrySet();
        for (Entry<Node, ConstrainedTracingStateSet<Node>> e : entrySet) {
            ConstrainedTracingStateSet<Node> stateSet = e.getValue();
            Node node = e.getKey();

            HistoryNode<Node> path = stateSet.failpath();

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
