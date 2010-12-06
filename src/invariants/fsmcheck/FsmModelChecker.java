package invariants.fsmcheck;

import invariants.AlwaysFollowedInvariant;
import invariants.AlwaysPrecedesInvariant;
import invariants.BinaryInvariant;
import invariants.NeverFollowedInvariant;
import invariants.TemporalInvariant;
import invariants.TemporalInvariantSet.RelationPath;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
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

public class FsmModelChecker<T extends INode<T>> {
	public List<List<BinaryInvariant>> invariants;
	IGraph<T> graph;
	
	@SuppressWarnings("unchecked")
	public FsmModelChecker(Iterable<TemporalInvariant> invariants, IGraph<T> graph) {
		this.graph = graph;
		
		// TODO: store this instead of post processing the set
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
	
	protected List<StateSet> newMachines(List<StateSet> old) {
		List<StateSet> results = new ArrayList<StateSet>();
		for (StateSet ss : old) {
			results.add(ss != null ? ss.clone() : null);
		}
		return results;
	}
	
	protected List<StateSet> visit(T node, List<List<Map<String, BitSet>>> mapping, List<StateSet> states) {
		String label = node.getLabel();
		for (int i = 0; i < states.size(); i++) {
			StateSet state = states.get(i);
			if (state != null) state.visit(mapping.get(i), label);
		}
		return states;
	}
	
	/**
	 * Predicate for determining if one set of states is the strict subset of another.
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
	
	/*
	 * Merges the failure bits of a list of statesets into one unified failure bitset.
	 */
	protected BitSet failureBits(List<StateSet> states, boolean isFinal) {
		BitSet result = new BitSet();
		if (!isFinal) return result;
		int pos = 0;
		for (StateSet state : states) {
			if (state == null) continue;
			BitSet fail = state.isFail(); //isFinal ? state.isFail() : state.isPermanentFail();
			for (int i = fail.nextSetBit(0); i >= 0; i = fail.nextSetBit(i + 1)) {
				result.set(i + pos);
			}
			pos += state.count;
		}
		return result;
	}
	
	public List<RelationPath<T>> getCounterexamples() {
		return findFailures(mergeBitSets(whichFail().values()));
	}
	
	@SuppressWarnings("unchecked")
	public Map<T, BitSet> whichFail() {
		int count = invariants.size();
		List<List<Map<String, BitSet>>> mappings = new ArrayList<List<Map<String, BitSet>>>(count);
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
		
		Map<T, List<StateSet>> states = new HashMap<T, List<StateSet>>();
		Queue<T> workList = new LinkedList<T>();
		for (T initial : graph.getInitialNodes()) {
			workList.add(initial);
			states.put(initial, newMachines(machines));
		}
		
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
		
		Map<T, BitSet> result = new HashMap<T, BitSet>();
		for (T node : graph.getNodes()) {
			List<StateSet> xs = states.get(node);
			if (states != null) result.put(node, failureBits(xs, node.isFinal()));
		}
		return result;
	}
	
	public static BitSet mergeBitSets(Collection<BitSet> failures) {
		BitSet result = new BitSet();
		for (BitSet b : failures)
			result.or(b);
		return result;
	}
	
	public List<RelationPath<T>> findFailures(BitSet failures) {
		List<RelationPath<T>> results = new ArrayList<RelationPath<T>>();
		
		for (int i = failures.nextSetBit(0); i >= 0; i = failures.nextSetBit(i + 1)) {
			System.out.println(results.size() + " size before");
			invariantCounterexamples(results, i);
		}
		
		return results;
	}
	
	public int invariantCount() {
		int result = 0;
		for (int i = 0; i < this.invariants.size(); i++) {
			result += this.invariants.get(i).size();
		}
		return result;
	}
	
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
	
	@SuppressWarnings("unchecked")
	protected void invariantCounterexamples(List<RelationPath<T>> results, int index) {
		TracingStateSet<T> stateset = null;
		BinaryInvariant invariant = getInvariant(index);
		if (invariant == null) return;
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
		Map<T, TracingStateSet> states = new HashMap<T, TracingStateSet>();
		
		for (T node : graph.getNodes()) {
			states.put(node, stateset.clone());
		}
		
		for (T node : graph.getInitialNodes()) {
			onWorkList.add(node);
			workList.add(node);
			states.get(node).setInitial(node);
		}
		
		while (!workList.isEmpty()) {
			T node = workList.remove();
			onWorkList.remove(node);
			TracingStateSet current = states.get(node);
			for (ITransition<T> adjacent : node.getTransitions()) {
				T target = adjacent.getTarget();
				TracingStateSet other = states.get(target);
				TracingStateSet temp = current.clone();
				temp.transition(target);
				boolean isSubset = temp.isSubset(other);
				other.merge(temp);
				if (!isSubset && !onWorkList.contains(target)) {
					workList.add(target);
					onWorkList.add(target);
				}
			}
		}
		
		for (T node : graph.getNodes()) {
			TracingStateSet<T> state = states.get(node);
			if (node.isFinal()) {
				if (state.failstate() != null) {
					results.add(state.failstate().toCounterexample((TemporalInvariant)invariant));
				}
			}
		}
	}
}