package invariants.fsmcheck;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import model.interfaces.INode;

/**
 * Stores a set of states potentially held after traversing some path through
 * the graph.  This path is stored in the history field, as a linked list
 * referencing each node it passes through.  This linked list actually forms an
 * inverted DAG structure, as prefixes are shared.
 * 
 * @author mgsloan
 *
 * @param <T> The node type of the graph this worker traverses.
 */
public class FsmWorker<T extends INode<T>> {
	public List<StateSet<String>> machines;
	public HistoryNode history;
	public BitSet fail, permanentFail;
	
	public class HistoryNode {
		T node;
		HistoryNode previous;
		boolean knownFails;
		HistoryNode(T node, HistoryNode previous, boolean knownFail) {
			this.node = node;
			this.previous = previous;
			this.knownFails = knownFail;
		}
		public String fullHistory() {
			StringBuilder result = new StringBuilder();
			HistoryNode current = this;
			while (current != null) {
				result.append(current.node.getLabel());
				result.append(" ~~> ");
				current = current.previous;
			}
			return result.toString();
		}
	}
	
	public FsmWorker(List<StateSet<String>> machines) {
		this.machines = machines;
		this.history = null;
		this.fail = new BitSet();
		this.permanentFail = new BitSet();
	}
	
	public FsmWorker(FsmWorker<T> other) { 
		List<StateSet<String>> newMachines = new ArrayList<StateSet<String>>();
		for (StateSet<String> machine : other.machines) {
			newMachines.add(machine.clone());
		}
		this.machines = newMachines;
		this.fail = other.fail;
		this.permanentFail = other.permanentFail;
		this.history = other.history;
	}
	
	// Can't believe this isn't in the API.
	private static void setRange(BitSet destination, int from, BitSet source) {
		for (int i = from; i < from + source.size(); i++)
			destination.set(i, source.get(i));
	}
	
	/*
	 * Given a node, this mutates the worker
	 */
	public void next(T x, boolean isFinal) {
		int offset = 0;
		for (int i = 0; i < machines.size(); i++) {
			StateSet<String> machine = machines.get(i);
			machine.next(x.getLabel());
			setRange(fail, offset, machine.isFail());
			setRange(permanentFail, offset, machine.isPermanentFail());
			offset += machine.count;
		}
		this.history = new HistoryNode(x, this.history,
				!(isFinal ? fail : permanentFail).isEmpty());
	}
	
	/*
	 * Indicates that this state is a subset of another.  If so, and the other
	 * worker is found in the cache of a node being transferred to,  then that
	 * set of potential states (and more) has already been considered.
	 */
	public boolean isSubset(FsmWorker<T> other) {
		for (int i = 0; i < machines.size(); i++) {
			List<BitSet> sets = machines.get(i).sets;
			List<BitSet> osets = other.machines.get(i).sets;
			if (sets.size() != osets.size()) return false;
			for (int j = 0; j < sets.size(); j++) {
				BitSet thisSet = sets.get(j);
				BitSet s = (BitSet) thisSet.clone();
				s.and(osets.get(j));
				s.xor(thisSet);     // (intersection != this) == 0 for subset
				if (!s.isEmpty()) return false;
			}
		}
		return true;
	}
	
	public void resetHistory(T node) {
		this.history = new HistoryNode(node, null, false);
	}
	
	// Equality / hashing on machine state, not history.
	public int hashCode() {
		return machines.hashCode();
	}
	
	public boolean equals(FsmWorker<T> other) {
		if (other == null) return false;
		return this.machines.equals(other.machines);
	}
}
