package invariants.fsmcheck;

import invariants.AlwaysFollowedInvariant;
import invariants.AlwaysPrecedesInvariant;
import invariants.BinaryInvariant;
import invariants.NeverFollowedInvariant;
import invariants.TemporalInvariant;
import invariants.TemporalInvariantSet;
import invariants.TemporalInvariantSet.RelationPath;
import invariants.fsmcheck.FsmWorker.HistoryNode;

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

public class FsmModelChecker<T extends INode<T>> {
	public List<List<BinaryInvariant>> invariants;
	Queue<FsmWorker<T>> workList;
	Map<T, Set<FsmWorker<T>>> cachedStates;
	IGraph<T> graph;
	List<List<Map<String, BitSet>>> inputMappings;
	
	public FsmModelChecker(Iterable<TemporalInvariant> invariants, IGraph<T> graph) {
		this.graph = graph;
		this.workList = new LinkedList<FsmWorker<T>>();
		this.cachedStates = new HashMap<T, Set<FsmWorker<T>>>();
		for (T node : graph.getNodes()) {
			cachedStates.put(node, new HashSet<FsmWorker<T>>());
		}
		
		// TODO: store this instead of post processing the set
		List<BinaryInvariant> alwaysFollowed = new ArrayList<BinaryInvariant>();
		List<BinaryInvariant> alwaysPrecedes = new ArrayList<BinaryInvariant>();
		List<BinaryInvariant> neverFollowed  = new ArrayList<BinaryInvariant>();
		for (TemporalInvariant inv : invariants) {
			if (inv.getClass().equals(AlwaysFollowedInvariant.class)) {
				alwaysFollowed.add((BinaryInvariant)inv);
			} else if (inv.getClass().equals(AlwaysPrecedesInvariant.class)) {
				alwaysPrecedes.add((BinaryInvariant)inv);
			} else if (inv.getClass().equals(NeverFollowedInvariant.class)) {
				alwaysFollowed.add((BinaryInvariant)inv);
			}
		}
		
		inputMappings = new ArrayList<List<Map<String, BitSet>>>();
		inputMappings.add(StateSet.getMapping(alwaysFollowed));
		inputMappings.add(StateSet.getMapping(alwaysPrecedes));
		inputMappings.add(StateSet.getMapping(neverFollowed));
		
		List<StateSet> machines = new ArrayList<StateSet>(3);
		machines.add(new AlwaysFollowedSet(alwaysFollowed.size()));
		machines.add(new AlwaysPrecedesSet(alwaysPrecedes.size()));
		machines.add(new NeverFollowedSet(neverFollowed.size()));
		
		this.invariants = new ArrayList<List<BinaryInvariant>>();
		this.invariants.add(alwaysFollowed);
		this.invariants.add(alwaysPrecedes);
		this.invariants.add(neverFollowed);
		
		FsmWorker<T> initialWorker = new FsmWorker<T>(machines, inputMappings);
		for (T initial : graph.getInitialNodes()) {
			FsmWorker<T> newWorker = new FsmWorker<T>(initialWorker);
			newWorker.resetHistory(initial);
			workList.add(newWorker);
			cachedStates.get(initial).add(newWorker);
		}
	}
	
	public void runToCompletion() {
		while (makeProgress()) { System.out.println("Worklist size: " + workList.size()); }
		//printAllHistory(true);
	}
	
	public boolean makeProgress() {
		while (!workList.isEmpty()) {
			FsmWorker<T> worker = workList.remove();
			//TODO: use iterator?
			for (ITransition<T> adjacent : worker.history.node.getTransitions()) {
				FsmWorker<T> nextWorker = new FsmWorker<T>(worker);
				T target = adjacent.getTarget();
				nextWorker.next(target);
				
				boolean visited = false;
				Set<FsmWorker<T>> states = cachedStates.get(target);
				for (FsmWorker<T> priorState : states) {
					if (nextWorker.isSubset(priorState)) {
						visited = true;
						break;
					}
				}
				
				if (!visited) {
					workList.add(nextWorker);
					//TODO: this throws away shorter counter examples. ugh.
					for (FsmWorker<T> priorState : states) {
						if (priorState.isSubset(nextWorker)) {
							states.remove(priorState);
						}
					}
					states.add(nextWorker);
					return true;
				}
			}
		}
		return false;
	}
	
	List<BitSet> oldFailures = new ArrayList<BitSet>();
	
	/* Returns counterexamples for invariants which have not been found to fail in prior calls.
	 * TODO: integrate into makeprogress so that the whole graph isn't re-scanned.
	 * TODO: consider storing unioned fail vectors in worker, and seeing if it's a subset.
	 */
	public List<RelationPath<T>> newFailures() {
		List<RelationPath<T>> results = new ArrayList<RelationPath<T>>();
		for (int i = 0; i < invariants.size(); i++) {
			BitSet old = oldFailures.get(i);
			List<BinaryInvariant> invs = invariants.get(i);
			for (T elem : graph.getNodes()) {
				Set<FsmWorker<T>> states = cachedStates.get(elem);
				if (states == null) continue;
				for (FsmWorker<T> state : states) {
					StateSet machine = state.machines.get(i);
					BitSet isFail = machine.isPermanentFail();
					if (elem.isFinal()) isFail.or(machine.isFail());
					isFail.andNot(old);
					if (!isFail.isEmpty()) {
						List<T> path = new ArrayList<T>();
						for (FsmWorker<T>.HistoryNode cur = state.history; cur != null; cur = cur.previous) {
							path.add(cur.node);
						}
						//TODO: should the path be reversed?
						// Iterate new failures, recording counterexamples.
						for (int j = isFail.nextSetBit(0); j >= 0; j = isFail.nextSetBit(j + 1)) {
							//TODO: make this more efficient by making relationpath use the linked representation
							RelationPath<T> counterexample = new RelationPath<T>();
							counterexample.invariant = invs.get(j);
							//NOTE: These should be cloned before being mutated, at any point henceforth.
							counterexample.path = counterexample.invariant.shorten(path);
							results.add(counterexample);
						}
						old.or(isFail);
					}
				}
			}
		}
		return results;
	}
	
	/*
	public void printAllHistory(boolean matchFailures) {
		for (T elem : graph.getNodes()) {
			for (FsmWorker<T> worker : this.cachedStates.get(elem)) {
				if (worker.history == null) {
					System.out.println("null history!!!"); continue;
				}
				if (!worker.permanentFail.isEmpty() ||
					(worker.history.node.getTransitions().isEmpty() && !worker.fail.isEmpty())) {
					System.out.println(worker.fail.cardinality() + " invariants invalid " + worker.history.fullHistory());
				}
			}
		}
	} */
}
