package invariants.fsmcheck;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

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
	public class HistoryNode {
		T node;
		HistoryNode previous;
		HistoryNode(T node, HistoryNode previous) {
			this.node = node;
			this.previous = previous;
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
	
	public List<StateSet> machines;
	public final List<List<Map<String, BitSet>>> inputMappings;
	public HistoryNode history;
	
	// Constructs the worker given an initial 
	public FsmWorker(List<StateSet> machines, List<List<Map<String, BitSet>>> mappings) {
		this.machines = machines;
		this.inputMappings = mappings;
		this.history       = null;
	}
	
	// Clone constructor, to avoid initializing empty structures which are duplicated
	public FsmWorker(FsmWorker<T> other) { 
		List<StateSet> newMachines = new ArrayList<StateSet>();
		for (StateSet machine : other.machines) {
			newMachines.add(machine.clone());
		}
		this.machines = newMachines;
		this.inputMappings = other.inputMappings;
		this.history       = other.history;
	}
	
	/*
	 * Given a node, this mutates the worker
	 * TODO: consider passing mappings in here?
	 */
	public void next(T x) {
		for (int i = 0; i < machines.size(); i++) {
			machines.get(i).visit(inputMappings.get(i), x.getLabel());
		}
		this.history = new HistoryNode(x, this.history);
	}
	
	/*
	 * Indicates that this state is a subset of another.  If so, and the other
	 * worker is found in the cache of a node being transferred to,  then that
	 * set of potential states (and more) has already been considered.
	 */
	public boolean isSubset(FsmWorker<T> other) {
		for (int i = 0; i < machines.size(); i++) {
			if(!machines.get(i).isSubset(other.machines.get(i))) return false;
		}
		return true;
	}
	
	// Sets the history to null.
	public void resetHistory(T node) {
		this.history = new HistoryNode(node, null);
	}
	
	// Equality & hashing on machine state, ignoring other fields.
	
	public int hashCode() {
		return machines.hashCode();
	}
	
	public boolean equals(FsmWorker<T> other) {
		if (other == null) return false;
		return this.machines.equals(other.machines);
	}
}
