package invariants.fsmcheck;

import invariants.AlwaysFollowedInvariant;
import invariants.AlwaysPrecedesInvariant;
import invariants.BinaryInvariant;
import invariants.NeverFollowedInvariant;
import invariants.TemporalInvariant;
import invariants.TemporalInvariantSet.RelationPath;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import model.interfaces.IGraph;
import model.interfaces.INode;
import model.interfaces.ITransition;

/**
 * Implements two different finite-state-machine based model checkers.  The first
 * is implemented using bitsets, and therefore can evaluate many invariants at once
 * in one relatively efficient pass.  Following this pass, a less efficient model
 * is invoked, which keeps track of the path required to end up in the failing
 * state.
 * 
 * @author Michael Sloan (mgsloan@gmail.com)
 *
 * @param <T> The nodetype of the graphs used for model checking.
 */
public class FsmModelChecker<T extends INode<T>> {
	public List<List<BinaryInvariant>> invariants;
	IGraph<T> graph;
	
	/*
	 * Constructs an model checking environment from a set of invariants and a graph
	 * to check.
	 */
	@SuppressWarnings("unchecked")
	public FsmModelChecker(Iterable<TemporalInvariant> invariants, IGraph<T> graph) {
		this.graph = graph;
		
		// TODO: store the set this way instead of needing to process it.
		// Filter the elements of the set into categorized lists.
		List<BinaryInvariant> alwaysFollowed = new ArrayList<BinaryInvariant>();
		List<BinaryInvariant> alwaysPrecedes = new ArrayList<BinaryInvariant>();
		List<BinaryInvariant> neverFollowed  = new ArrayList<BinaryInvariant>();
		for (TemporalInvariant inv : invariants) {
			Class<Object> clazz = (Class<Object>) inv.getClass();
			if (clazz.equals(AlwaysFollowedInvariant.class)) {
				alwaysFollowed.add((BinaryInvariant)inv);
			} else if (clazz.equals(AlwaysPrecedesInvariant.class)) {
				alwaysPrecedes.add((BinaryInvariant)inv);
			} else if (clazz.equals(NeverFollowedInvariant.class)) {
				neverFollowed.add((BinaryInvariant)inv);
			}
		}
		
		this.invariants = new ArrayList<List<BinaryInvariant>>();
		this.invariants.add(alwaysFollowed);
		this.invariants.add(alwaysPrecedes);
		this.invariants.add(neverFollowed);
	}
	
	public int invariantCount() {
		int result = 0;
		for (int i = 0; i < this.invariants.size(); i++) {
			result += this.invariants.get(i).size();
		}
		return result;
	}
	
	/**
	 * Yields an invariant out of the lists of invariants stored by this checker.
	 * Since it stores lists of different types, this indexing is equivalent to
	 * indexing the concatenated version of the invariants list.
	 */
	public BinaryInvariant getInvariant(int index) {
		for (int i = 0; i < invariants.size(); i++) {
			int sz = invariants.get(i).size();
			if (index < sz) {
				return invariants.get(i).get(index);
			} else {
				index -= sz;
			}
		}
		return null;
	}
	
	/*********************************************
	 * Helper functions utilized by whichFail(): */
	
	// Clones every stateset in the given list, placing them on a new list.
	protected List<StateSet> newMachines(List<StateSet> old) {
		List<StateSet> results = new ArrayList<StateSet>();
		for (StateSet ss : old) {
			results.add(ss != null ? ss.clone() : null);
		}
		return results;
	}
	
	// Given a list of machines, and a list of input mappings (one for each),
	// this calls the 'visit' method on each stateset.  This effectively
	protected List<StateSet> visit(T node, List<List<Map<String, BitSet>>> mapping, List<StateSet> states) {
		String label = node.getLabel();
		for (int i = 0; i < states.size(); i++) {
			StateSet state = states.get(i);
			if (state != null) state.visit(mapping.get(i), label);
		}
		return states;
	}
	
	/**
	 * Predicate for determining if one set of states is the subset of another.
	 * 
	 * @param as  The first list of statesets, true is yielded if it's a subset / equal
	 * @param bs  The second list of statesets, true is yielded if it's a superset / equal
	 */
	protected boolean isSubset(List<StateSet> as, List<StateSet> bs) {
		assert(as.size() == bs.size());
		for (int i = 0; i < as.size(); i++) {
			StateSet a = as.get(i), b = bs.get(i);
			// if a is null, then the value of b doesn't matter, and it doesn't
			// tell us that this is a subset.
			if (a != null && !a.isSubset(b)) return false;
		}
		return true;
	}

	/**
	 * This helper merges the states from one list of sets
	 * 
	 * @param as The list of states the merge is being applied to
	 * @param bs The list of states to be merged in
	 */
	protected void merge(List<StateSet> as, List<StateSet> bs) {
		for (int i = 0; i < as.size(); i++) {
			StateSet a = as.get(i), b = bs.get(i);
			if (a == null && b != null) {
				as.set(i, b.clone());
			} else {
				a.mergeWith(b);
			}
		}
	}
	
	/**
	 * Concatenates the failure bits of a list of statesets into one unified
	 * failure bitset.
	 * 
	 * @param states  The statesets from which to take failure bits.
	 * @return The collective failure bitset, with fail bits from each set
	 *     appended end-to-end.
	 */
	protected BitSet failureBits(List<StateSet> states) {
		BitSet result = new BitSet();
		int pos = 0;
		for (StateSet state : states) {
			if (state == null) continue;
			BitSet fail = state.isFail();
			for (int i = fail.nextSetBit(0); i >= 0; i = fail.nextSetBit(i + 1)) {
				result.set(i + pos);
			}
			pos += state.count;
		}
		return result;
	}
	
	
	/**
	 * Runs a bitset-based finite state machine invariant checkers over the
	 * graph, and yields a BitSet representing which invariants are found to
	 * have paths, from an initial to final node, which cause them to fail.
	 *  
	 * @return A bitset with 1-bits at each index corresponding to a failed
	 *     machine (see getInvariant)
	 */
	@SuppressWarnings("unchecked")
	public BitSet whichFail() {
		/* TODO: I realized that it may be more efficient to do a type of StateSet
		 * at a time, because then we will be more likely to reach subset, and
		 * therefore reach fixpoint and terminate sooner.  This also saves a lot of
		 * the above helper function tom-foolery.
		 * 
		 * You can also imagine creating an interface to StateSet / TracingStateSet,
		 * and having the model checker operate using either.  The for each is
		 * already nearly the same.
		 */
		
		int count = invariants.size();
		List<List<Map<String, BitSet>>> mappings = new ArrayList<List<Map<String, BitSet>>>(count);
		
		// Construct StateSet machines from each type of invariant, via type-based dispatch.
		List<StateSet> machines = new ArrayList<StateSet>(count);
		for (List<BinaryInvariant> invs : invariants) {
			mappings.add(StateSet.getMapping(invs));
			if (invs.isEmpty()) { machines.add(null); continue; }
			Class<BinaryInvariant> clazz = (Class<BinaryInvariant>) invs.get(0).getClass();
			if (clazz.equals(AlwaysFollowedInvariant.class)) {
				machines.add(new AlwaysFollowedSet(invs.size()));
			} else if (clazz.equals(AlwaysPrecedesInvariant.class)) {
				machines.add(new AlwaysPrecedesSet(invs.size()));
			} else if (clazz.equals(NeverFollowedInvariant.class)) {
				machines.add(new NeverFollowedSet(invs.size()));
			}
		}
		
		// Stores which set of states we currently inhabit.  The list, is of
		// course because we have multiple types of state machines.
		Map<T, List<StateSet>> states = new HashMap<T, List<StateSet>>();
		
		// Populate the worklist and states map, with initial machines at each
		// initial node.
		Queue<T> workList = new LinkedList<T>();
		for (T initial : graph.getInitialNodes()) {
			workList.add(initial);
			states.put(initial, newMachines(machines));
		}
		
		// Actual model checking step - takes an item off the worklist, and
		// transitions the state found at that node, using the labels of all
		// of the adjacent nodes as input.  The resulting statesets are then
		// checked for subset with the stateset cached at the destination node.
		// If it is found to be a subset, then merging in the new states would
		// cause no change.  Therefore, only in the case where they are not a
		// subset is the merge performed and the destination node added to the
		// worklist (the changed states need to be propagated).
		while(!workList.isEmpty()) {
			T node = workList.remove();
			List<StateSet> current = states.get(node);
			for (ITransition<T> adjacent : node.getTransitions()) {
				T target = adjacent.getTarget();
				List<StateSet> other = states.get(target);
				List<StateSet> newMachines = visit(target, mappings, newMachines(current));
				if (other != null) {
					if (isSubset(newMachines, other)) continue;
					merge(newMachines, other);
				}
				states.put(target, newMachines);
				workList.add(target);
			}
		}
		
		//Map<T, BitSet> result = new HashMap<T, BitSet>();
		BitSet result = new BitSet();
		for (T node : graph.getNodes()) {
			if (node.isFinal()) result.or(failureBits(states.get(node)));
			//result.put(node, failureBits(states.get(node)));
		}
		return result;
	}
	
	/*  These might be useful if we go back to using a map from nodes to
	 * failure bits.  This can be useful if it's possible to implement a more
	 * efficient path-tracking checker given knowledge of the final state
	 * associated with a particular invariant failure.
	 * /
	
	public static BitSet mergeBitSets(Collection<BitSet> failures) {
		BitSet result = new BitSet();
		for (BitSet b : failures)
			result.or(b);
		return result;
	}
	public List<RelationPath<T>> getCounterexamples() {
		return findFailures(mergeBitSets(whichFail().values()));
	}
	*/
	
	/**
	 * This is a convenience function, which, given a set of invariants to check,
	 * returns a list of the shortest counterexample paths for each.
	 */
	public List<RelationPath<T>> findFailures(BitSet which) {
		List<RelationPath<T>> results = new ArrayList<RelationPath<T>>();
		
		for (int i = which.nextSetBit(0); i >= 0; i = which.nextSetBit(i + 1)) {
			System.out.println(results.size() + " size before");
			invariantCounterexamples(results, i);
		}
		
		return results;
	}
	
	
	/**
	 * Runs path history tracking finite state machines over the model graph,
	 * @param results
	 * @param index
	 * @return 
	 */
	@SuppressWarnings("unchecked")
	protected RelationPath<T> invariantCounterexamples(List<RelationPath<T>> results, int index) {
		TracingStateSet<T> stateset = null;
		BinaryInvariant invariant = getInvariant(index);
		if (invariant == null) return null;
		Class<BinaryInvariant> clazz = (Class<BinaryInvariant>) invariant.getClass();
		if (clazz.equals(AlwaysFollowedInvariant.class)) {
			stateset = new AlwaysFollowedTracingSet<T>(invariant);
		} else if (clazz.equals(AlwaysPrecedesInvariant.class)) {
			stateset = new AlwaysPrecedesTracingSet<T>(invariant);
		} else if (clazz.equals(NeverFollowedInvariant.class)) {
			stateset = new NeverFollowedTracingSet<T>(invariant);
		}
		
		Set<T> onWorkList = new HashSet<T>();
		Queue<T> workList = new LinkedList<T>();
		Map<T, TracingStateSet<T>> states = new HashMap<T, TracingStateSet<T>>();
		
		// Populates the state map with blank initial states.
		for (T node : graph.getNodes()) {
			states.put(node, stateset.clone());
		}
		
		// Populate the worklist with the initial nodes, and set the initial
		// path history on each.
		for (T node : graph.getInitialNodes()) {
			onWorkList.add(node);
			workList.add(node);
			states.get(node).setInitial(node);
		}
		
		// Actual model checking step - takes an item off the worklist, and
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
			TracingStateSet<T> current = states.get(node);
			for (ITransition<T> adjacent : node.getTransitions()) {
				T target = adjacent.getTarget();
				TracingStateSet<T> other = states.get(target);
				TracingStateSet<T> temp = current.clone();
				temp.transition(target);
				boolean isSubset = temp.isSubset(other);
				other.merge(temp);
				if (!isSubset && !onWorkList.contains(target)) {
					workList.add(target);
					onWorkList.add(target);
				}
			}
		}
		
		// Return the shortest path, ending on a final node, which causes the
		// invariant to fail.
		TracingStateSet<T>.HistoryNode shortestPath = null; 
		for (T node : graph.getNodes()) {
			TracingStateSet<T> state = states.get(node);
			if (node.isFinal()) {
				TracingStateSet<T>.HistoryNode path = state.failstate(); 
				if (path != null && (shortestPath == null || shortestPath.count > path.count)) {
					shortestPath = path;
				}
			}
		}
		if (shortestPath == null) return null;
		return shortestPath.toCounterexample((TemporalInvariant)invariant);
	}
}