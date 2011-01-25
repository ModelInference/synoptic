package synoptic.invariants.fsmcheck;


import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import synoptic.invariants.AlwaysFollowedInvariant;
import synoptic.invariants.AlwaysPrecedesInvariant;
import synoptic.invariants.BinaryInvariant;
import synoptic.invariants.NeverFollowedInvariant;
import synoptic.invariants.TemporalInvariant;
import synoptic.invariants.RelationPath;
import synoptic.model.interfaces.IGraph;
import synoptic.model.interfaces.INode;
import synoptic.model.interfaces.ITransition;


/**
 * Implements two different finite-state-machine based synoptic.model checkers.  The first
 * is implemented using bitsets, and therefore can evaluate many synoptic.invariants at once
 * in one relatively efficient pass.  Following this pass, a less efficient synoptic.model
 * is invoked, which keeps track of the path required to end up in the failing
 * state.
 * 
 * @author Michael Sloan (mgsloan@gmail.com)
 *
 * @param <T> The nodetype of the graphs used for synoptic.model checking.
 */
public class FsmModelChecker {
	/**
	 * Given an initial StateSet, and graph to check, this yields the fixpoint
	 * states eventually reached.  The states in the graph are transitioned
	 * along edges, and merged at the nodes.  Once every merge causes no change
	 * to the graph, the resulting association between nodes in the graph and
	 * states is yielded.
	 * 
	 * @param <S> The type of StateSet we are propagating.
	 * @param initial The initial state of each node.
	 * @param graph The graph to analyze.
	 * @return The associations between node and stateset.
	 */
	public static <T extends INode<T>, S extends IStateSet<T, S>>
	Map<T, S> runChecker(IStateSet<T, S> initial, IGraph<T> graph, boolean earlyExit) {
		
		Set<T> onWorkList = new HashSet<T>();
		Queue<T> workList = new LinkedList<T>();
		Map<T, S> states = new HashMap<T, S>();
		
		// Populate the state map with initial states.s
		for (T node : graph.getNodes()) {
			states.put(node, initial.copy());
		}
		
		// Populate the worklist with the initial nodes, and set the initial
		// path history on each.
		for (T node : graph.getInitialNodes()) {
			onWorkList.add(node);
			workList.add(node);
			states.get(node).setInitial(node);
		}
		
		// Actual synoptic.model checking step - takes an item off the worklist, and
		// transitions the state found at that node, using the labels of all
		// of the adjacent nodes as input.  The resulting state is then checked
		// for subset with the stateset cached at the destination node. If it is
		// found to be a subset, then merging in the new state would cause no
		// change.  Therefore, only in the case where it's not a subset is the
		// merge performed and the destination node added to the worklist
		// (the changed states need to be propagated).
		while (!workList.isEmpty()) {
			T node = workList.remove();
			onWorkList.remove(node);
			S current = states.get(node);
		/*	System.out.println();
			System.out.println(node.getLabel() + " " + node.toString());
			System.out.println(current.toString()); */
			for (ITransition<T> adjacent : node.getTransitionsIterator()) {
				T target = adjacent.getTarget();
				S other = states.get(target);
				S temp = current.copy();
				temp.transition(target);
				boolean isSubset = temp.isSubset(other);
				other.mergeWith(temp);
				if (earlyExit && other.isFail() && target.isFinal()) {
					return states;
				}
				if (!isSubset && !onWorkList.contains(target)) {
					/*System.out.println("propogated to " + 
							target.getLabel() + " " + target.toString() + (target.isFinal() ? "final" : ""));
					System.out.println(other); */
					workList.add(target);
					onWorkList.add(target);
				} /*else {
					System.out.println("subset found on " +
							target.getLabel() + " " + target.toString() + (target.isFinal() ? "final" : ""));
					System.out.println(other);
				}*/
			}
		}
		
		return states;
	}
	
	// Helper which invokes runChecker given an fsm state set, and process the
	// resulting states into a summary failure-indicating BitSet.
	protected static <T extends INode<T>>
	BitSet whichFail(FsmStateSet<T> initial, IGraph<T> graph) {
		Map<T, FsmStateSet<T>> states = runChecker(initial, graph, false);
		BitSet result = new BitSet();
		for (Entry<T, FsmStateSet<T>> entry : states.entrySet()) {
			if (entry.getKey().isFinal()) result.or(entry.getValue().whichFail()); 
		}
		return result;
	}
	
	// Helper to append the elements of a list corresponding to 1s in a BitSet
	// to another list, which is accumulating results.
	protected static <E> void bitFilter(BitSet set, List<E> list, List<E> results) {
		for (int i = set.nextSetBit(0); i >= 0; i = set.nextSetBit(i+1)) {
			if (i >= list.size()) break;
			results.add(list.get(i));
		}
	}
	
	/**
	 * Use the BitSet checker to evaluate, and return which synoptic.invariants failed.
	 * @param synoptic.invariants
	 */
	public static <T extends INode<T>> List<BinaryInvariant>
	  runBitSetChecker(Iterable<BinaryInvariant> invariants, IGraph<T> graph) {
	
		// TODO: store the TemporalInvariantSet in this way instead of needing to process it here.
		// Filter the elements of the set into categorized lists.
		List<BinaryInvariant> alwaysFollowed = new ArrayList<BinaryInvariant>();
		List<BinaryInvariant> alwaysPrecedes = new ArrayList<BinaryInvariant>();
		List<BinaryInvariant> neverFollowed  = new ArrayList<BinaryInvariant>();
		for (TemporalInvariant inv : invariants) {
			@SuppressWarnings("unchecked")
			Class<Object> invClass = (Class<Object>) (Class) inv.getClass();
			if (invClass.equals(AlwaysFollowedInvariant.class)) {
				alwaysFollowed.add((BinaryInvariant)inv);
			} else if (invClass.equals(AlwaysPrecedesInvariant.class)) {
				alwaysPrecedes.add((BinaryInvariant)inv);
			} else if (invClass.equals(NeverFollowedInvariant.class)) {
				neverFollowed.add((BinaryInvariant)inv);
			}
		}
		
		BitSet afs = whichFail(new AFbyInvFsms<T>(alwaysFollowed), graph),
		       aps = whichFail(new APInvFsms<T>  (alwaysPrecedes), graph),
		       nfs = whichFail(new NFbyInvFsms<T>(neverFollowed),  graph);
		
		List<BinaryInvariant> results = new ArrayList<BinaryInvariant>();
		bitFilter(afs, alwaysFollowed, results);
		bitFilter(aps, alwaysPrecedes, results);
		bitFilter(nfs, neverFollowed,  results);
		return results;
	}
	
	
	/**
	 * Runs invariant-checking finite state machines over the synoptic.model graph,
	 * while keeping history paths which justify any particular state.  This
	 * allows us to report counterexample paths, where a failure state is
	 * reached on a final node.
	 * 
	 * @param invariant The invariant to test.
	 * @return The shortest counterexample path for this invariant.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends INode<T>> RelationPath<T>
	invariantCounterexample(BinaryInvariant invariant, IGraph<T> graph) {
		
		TracingStateSet<T> stateset = null;
		if (invariant == null) return null;
		Class<BinaryInvariant> invClass = (Class<BinaryInvariant>) invariant.getClass();
		if (invClass.equals(AlwaysFollowedInvariant.class)) {
			stateset = new AFbyTracingSet<T>(invariant);
		} else if (invClass.equals(AlwaysPrecedesInvariant.class)) {
			stateset = new APTracingSet<T>(invariant);
		} else if (invClass.equals(NeverFollowedInvariant.class)) {
			stateset = new NFbyTracingSet<T>(invariant);
		}

		// Return the shortest path, ending on a final node, which causes the
		// invariant to fail.
		TracingStateSet<T>.HistoryNode shortestPath = null;
		for(Entry<T, TracingStateSet<T>> e : runChecker(stateset, graph, true).entrySet()) {
			//if (!invClass.equals(AlwaysFollowedInvariant.class) || e.getKey().isFinal()) {
			TracingStateSet<T>.HistoryNode path = e.getValue().failpath();
			boolean fin = e.getKey().isFinal();
			if (fin && path != null && (shortestPath == null || shortestPath.count > path.count)) {
				shortestPath = path;
			}
		}
		
		// Convert to RelationPath
		if (shortestPath == null) return null;
		return shortestPath.toCounterexample((TemporalInvariant)invariant);
	}
}