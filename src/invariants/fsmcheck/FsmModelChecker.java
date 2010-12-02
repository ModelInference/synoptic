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
				alwaysFollowed.add((BinaryInvariant)inv);
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
			results.add(ss.clone());
		}
		return results;
	}
	
	protected List<StateSet> visit(T node, List<List<Map<String, BitSet>>> mapping, List<StateSet> states) {
		for (int i = 0; i < states.size(); i++) {
			states.get(i).visit(mapping.get(i), node.getLabel());
		}
		return states;
	}
	
	protected boolean isSubset(List<StateSet> a, List<StateSet> b) {
		for (int i = 0; i < a.size(); i++) {
			if (!a.get(i).isSubset(b.get(i))) return false;
		}
		return true;
	}
	
	protected BitSet failureBits(List<StateSet> states, boolean isFinal) {
		BitSet result = new BitSet();
		int pos = 0;
		for (StateSet state : states) {
			BitSet fail = isFinal ? state.isFail() : state.isPermanentFail();
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
					for (int i = 0; i < newMachines.size(); i++) {
						newMachines.get(i).mergeWith(other.get(i));
					}
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
		
		for (int i = failures.nextSetBit(0); i >= 0; i++)
			invariantCounterexamples(results, i);
		
		return results;
	}
	
	public BinaryInvariant getInvariant(int index) {
		int i = 0;
		while (i < invariants.size() && invariants.get(i).size() <= index) {
			int sz = invariants.get(i).size();
			if (index < sz) {
				return invariants.get(i).get(index);
			} else {
				index -= sz;
				i++;
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	protected void invariantCounterexamples(List<RelationPath<T>> results, int index) {
		TracingStateSet<T> stateset = null;
		BinaryInvariant invariant = getInvariant(index);
		Class<BinaryInvariant> clazz = (Class<BinaryInvariant>) invariant.getClass();
		if (clazz.equals(AlwaysFollowedInvariant.class)) {
			stateset = new AlwaysFollowedTracingSet<T>(invariant);
		} else if (clazz.equals(AlwaysPrecedesInvariant.class)) {
			stateset = new AlwaysPrecedesTracingSet<T>(invariant);
		} else if (clazz.equals(NeverFollowedInvariant.class)) {
			stateset = new NeverFollowedTracingSet<T>(invariant);
		}
		
		Set<T> onWorklist = new HashSet<T>();
		Queue<T> workList = new LinkedList<T>();
		Map<T, TracingStateSet> states = new HashMap<T, TracingStateSet>();
		
		for (T node : graph.getNodes()) {
			states.put(node, stateset.clone());
		}
		
		for (T node : graph.getInitialNodes()) {
			onWorklist.add(node);
			workList.add(node);
			states.get(node).setInitial(node);
		}
		
		while (!workList.isEmpty()) {
			T node = workList.remove();
			TracingStateSet current = states.get(node);
			for (ITransition<T> adjacent : node.getTransitions()) {
				T target = adjacent.getTarget();
				TracingStateSet other = states.get(target);
				TracingStateSet temp = current.clone();
				temp.transition(target);
				if (temp.isSubset(other)) continue;
				other.merge(temp);
				workList.add(target);
			}
		}
		
		for (T node : graph.getNodes()) {
			TracingStateSet<T> state = states.get(node);
			if (node.isFinal()) {
				results.add(state.failstate().toCounterexample((TemporalInvariant)invariant));
			}
		}
	}
}