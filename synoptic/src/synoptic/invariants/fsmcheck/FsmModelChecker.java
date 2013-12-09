package synoptic.invariants.fsmcheck;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import synoptic.invariants.AlwaysFollowedInvariant;
import synoptic.invariants.AlwaysPrecedesInvariant;
import synoptic.invariants.BinaryInvariant;
import synoptic.invariants.CExamplePath;
import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.InterruptedByInvariant;
import synoptic.invariants.NeverFollowedInvariant;
import synoptic.invariants.constraints.IThresholdConstraint;
import synoptic.invariants.constraints.LowerBoundConstraint;
import synoptic.invariants.constraints.TempConstrainedInvariant;
import synoptic.invariants.constraints.UpperBoundConstraint;
import synoptic.model.interfaces.IGraph;
import synoptic.model.interfaces.INode;

/**
 * Implements two different finite-state-machine based synoptic.model checkers.
 * The first is implemented using bitsets, and therefore can evaluate many
 * synoptic.invariants at once in one relatively efficient pass. Following this
 * pass, a less efficient synoptic.model is invoked, which keeps track of the
 * path required to end up in the failing state.
 */
public class FsmModelChecker {
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
    public static <Node extends INode<Node>, StateSet extends IStateSet<Node, StateSet>> Map<Node, StateSet> runChecker(
            IStateSet<Node, StateSet> initial, IGraph<Node> graph,
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
                StateSet oldTargetStates = states.get(target);
                StateSet updatesToTargetStates = current.copy();
                updatesToTargetStates.transition(target);

                // Evaluate isSubset _before_ the merge.
                boolean isSubset = updatesToTargetStates
                        .isSubset(oldTargetStates);
                oldTargetStates.mergeWith(updatesToTargetStates);
                if (earlyExit && oldTargetStates.isFail()
                        && target.isTerminal()) {
                    return states;
                }

                // If updatesToTargetStates is subset of targetStates, then NOT
                // re-exploring the graph starting from the current node
                // prevents infinitely traversing loops
                if (!isSubset && !workList.contains(target)) {
                    workList.add(target);
                }
            }
        }

        return states;
    }

    // Helper which invokes runChecker given an fsm state set, and process the
    // resulting states into a summary failure-indicating BitSet.
    protected static <T extends INode<T>> BitSet whichFail(
            FsmStateSet<T> initial, IGraph<T> graph) {
        Map<T, FsmStateSet<T>> states = runChecker(initial, graph, false);
        BitSet result = new BitSet();
        for (Entry<T, FsmStateSet<T>> entry : states.entrySet()) {
            if (entry.getKey().isTerminal()) {
                result.or(entry.getValue().whichFail());
            }
        }
        return result;
    }

    // Helper to append the elements of a list corresponding to 1s in a BitSet
    // to another list, which is accumulating results.
    protected static <E> void bitFilter(BitSet set, List<E> list,
            List<E> results) {
        for (int i = set.nextSetBit(0); i >= 0; i = set.nextSetBit(i + 1)) {
            if (i >= list.size()) {
                break;
            }
            results.add(list.get(i));
        }
    }

    /**
     * Use the BitSet checker to evaluate, and return which synoptic.invariants
     * failed.
     */
    @SuppressWarnings("rawtypes")
    public static <T extends INode<T>> List<BinaryInvariant> runBitSetChecker(
            Iterable<BinaryInvariant> invariants, IGraph<T> graph) {

        // TODO: store the TemporalInvariantSet in this way instead of needing
        // to process it here.
        // Filter the elements of the set into categorized lists.
        List<BinaryInvariant> alwaysFollowed = new ArrayList<BinaryInvariant>();
        List<BinaryInvariant> alwaysPrecedes = new ArrayList<BinaryInvariant>();
        List<BinaryInvariant> neverFollowed = new ArrayList<BinaryInvariant>();
        for (ITemporalInvariant inv : invariants) {
            @SuppressWarnings("unchecked")
            Class<Object> invClass = (Class) inv.getClass();
            if (invClass.equals(AlwaysFollowedInvariant.class)) {
                alwaysFollowed.add((BinaryInvariant) inv);
            } else if (invClass.equals(AlwaysPrecedesInvariant.class)) {
                alwaysPrecedes.add((BinaryInvariant) inv);
            } else if (invClass.equals(NeverFollowedInvariant.class)) {
                neverFollowed.add((BinaryInvariant) inv);
            }
        }

        BitSet afs = whichFail(new AFbyInvFsms<T>(alwaysFollowed), graph);
        BitSet aps = whichFail(new APInvFsms<T>(alwaysPrecedes), graph);
        BitSet nfs = whichFail(new NFbyInvFsms<T>(neverFollowed), graph);

        List<BinaryInvariant> results = new ArrayList<BinaryInvariant>();
        bitFilter(afs, alwaysFollowed, results);
        bitFilter(aps, alwaysPrecedes, results);
        bitFilter(nfs, neverFollowed, results);
        return results;
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
            BinaryInvariant invariant, IGraph<Node> graph) {

        TracingStateSet<Node> stateset = null;
        if (invariant == null) {
            return null;
        }
        Class<BinaryInvariant> invClass = (Class<BinaryInvariant>) invariant
                .getClass();
        if (invClass.equals(AlwaysFollowedInvariant.class)) {
            stateset = new AFbyTracingSet<Node>(invariant);
        } else if (invClass.equals(AlwaysPrecedesInvariant.class)) {
            stateset = new APTracingSet<Node>(invariant);
        } else if (invClass.equals(NeverFollowedInvariant.class)) {
            stateset = new NFbyTracingSet<Node>(invariant);
        } else if (invClass.equals(InterruptedByInvariant.class)) {
            stateset = new IntrByTracingSet<Node>(invariant);
        } else if (invClass.equals(TempConstrainedInvariant.class)) {

            BinaryInvariant constInvInv = ((TempConstrainedInvariant<?>) invariant)
                    .getInv();
            IThresholdConstraint constInvConst = ((TempConstrainedInvariant<?>) invariant)
                    .getConstraint();

            if (constInvInv instanceof AlwaysFollowedInvariant) {
                // AFby Upper
                if (constInvConst instanceof UpperBoundConstraint) {
                    stateset = new AFbyUpperTracingSet<Node>(invariant);
                }
                // AFby Lower
                else if (constInvConst instanceof LowerBoundConstraint) {
                    stateset = new AFbyLowerTracingSet<Node>(invariant);
                }
            } else if (constInvInv instanceof AlwaysPrecedesInvariant) {
                // AP Upper
                if (constInvConst instanceof UpperBoundConstraint) {
                    stateset = new APUpperTracingSet<Node>(invariant);
                }
                // AP Lower
                else if (constInvConst instanceof LowerBoundConstraint) {
                    stateset = new APLowerTracingSet<Node>(invariant);
                }
            } else if (constInvInv instanceof InterruptedByInvariant) {
                // IntrBy Upper
                if (constInvConst instanceof UpperBoundConstraint) {
                    stateset = new IntrByUpperTracingSet<Node>(invariant);
                }
                // IntrBy Lower
                else if (constInvConst instanceof LowerBoundConstraint) {
                    stateset = new IntrByLowerTracingSet<Node>(invariant);
                }
            }
        }

        // Return the shortest path, ending on a final node, which causes the
        // invariant to fail.
        HistoryNode<Node> shortestPath = null;
        Set<Entry<Node, TracingStateSet<Node>>> entrySet = runChecker(stateset,
                graph, true).entrySet();
        for (Entry<Node, TracingStateSet<Node>> e : entrySet) {
            TracingStateSet<Node> stateSet = e.getValue();
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
