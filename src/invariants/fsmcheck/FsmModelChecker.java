package invariants.fsmcheck;

import invariants.AlwaysFollowedInvariant;
import invariants.AlwaysPrecedesInvariant;
import invariants.BinaryInvariant;
import invariants.NeverFollowedInvariant;
import invariants.TemporalInvariant;
import invariants.TemporalInvariantSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import model.Graph;
import model.interfaces.INode;
import model.interfaces.ITransition;

public class FsmModelChecker<T extends INode<T>> {
	public List<BinaryInvariant> alwaysFollowed, alwaysPrecedes, neverFollowed;
	Queue<FsmWorker<T>> workList;
	Map<T, Set<FsmWorker<T>>> cachedStates;
	Graph<T> graph;
	
	public FsmModelChecker(TemporalInvariantSet invariants, Graph<T> graph) {
		// TODO: store this instead of post processing the set
		alwaysFollowed = new ArrayList<BinaryInvariant>();
		alwaysPrecedes = new ArrayList<BinaryInvariant>();
		neverFollowed  = new ArrayList<BinaryInvariant>();
		for (TemporalInvariant inv : invariants) {
			if (inv.getClass().equals(AlwaysFollowedInvariant.class)) {
				alwaysFollowed.add((BinaryInvariant)inv);
			} else if (inv.getClass().equals(AlwaysPrecedesInvariant.class)) {
				alwaysPrecedes.add((BinaryInvariant)inv);
			} else if (inv.getClass().equals(NeverFollowedInvariant.class)) {
				alwaysFollowed.add((BinaryInvariant)inv);
			}
		}
		this.graph = graph;
		this.workList = new LinkedList<FsmWorker<T>>();
		this.cachedStates = new HashMap<T, Set<FsmWorker<T>>>();
		for (T node : graph.getNodes()) {
			cachedStates.put(node, new HashSet<FsmWorker<T>>());
		}
		
		List<StateSet<String>> machines = new ArrayList<StateSet<String>>(3);
		machines.add(new AlwaysFollowedSet(alwaysFollowed));
		machines.add(new AlwaysPrecedesSet(alwaysPrecedes));
		machines.add(new NeverFollowedSet(neverFollowed));
		FsmWorker<T> initialWorker = new FsmWorker<T>(machines);
		for (T initial : graph.getInitialNodes()) {
			FsmWorker<T> newWorker = new FsmWorker<T>(initialWorker);
			newWorker.resetHistory(initial);
			workList.add(newWorker);
			cachedStates.get(initial).add(newWorker);
		}
	}
	
	public void runToCompletion() {
		while (makeProgress()) { System.out.println("Worklist size: " + workList.size()); }
		printAllHistory(true);
	}
	
	public boolean makeProgress() {
		while (!workList.isEmpty()) {
			FsmWorker<T> worker = workList.remove();
			//TODO: use iterator?
			for (ITransition<T> adjacent : worker.history.node.getTransitions()) {
				FsmWorker<T> nextWorker = new FsmWorker<T>(worker);
				T target = adjacent.getTarget();
				nextWorker.next(target, false /* TODO */);
				
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
					//TODO: this throws away shorter errors. ugh.
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
	
	
	public void printAllHistory(boolean matchFailures) {
		Set<T> visited = new HashSet<T>();
		Queue<T> toVisit = new LinkedList<T>();
		toVisit.addAll(this.graph.getInitialNodes());
		while (!toVisit.isEmpty()) {
			T elem = toVisit.remove();
			for (ITransition<T> trans : elem.getTransitions()) {
				T targ = trans.getTarget();
				if (!visited.contains(targ)) toVisit.add(targ);
			}
			for (FsmWorker<T> worker : this.cachedStates.get(elem)) {
				if (worker.history == null) {
					System.out.println("null history!!!"); continue;
				}
				if (!worker.permanentFail.isEmpty() ||
					(worker.history.node.getTransitions().isEmpty() && !worker.fail.isEmpty())) {
					System.out.println(worker.fail.cardinality() + " invariants invalid " + worker.history.fullHistory());
					//result.add(worker.history);
				}
			}
			visited.add(elem);
		}
		//return result;
	}
}
