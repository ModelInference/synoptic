package algorithms.graph;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import model.MessageEvent;
import model.Partition;
import model.SystemState;
import model.PartitionGraph;
import model.interfaces.IModifiableGraph;

public class PartitionSplit implements Operation {
	private Partition partition = null;
	private Set<MessageEvent> fulfills = null;
	private Set<MessageEvent> fulfillsNot = null;
	private Partition removed = null;

	public PartitionSplit(Partition partition) {
		this.partition = partition;
		fulfills = new HashSet<MessageEvent>(partition.size());
		fulfillsNot = new HashSet<MessageEvent>(partition.size());
	}

	public PartitionSplit(Partition retained, Partition removed) {
		this.partition = retained;
		this.removed = removed;
		fulfills = new HashSet<MessageEvent>(partition.size());
		fulfillsNot = new HashSet<MessageEvent>(partition.size());
	}

	public Operation commit(PartitionGraph g,
			IModifiableGraph<Partition> partitionGraph,
			IModifiableGraph<SystemState<Partition>> stateGraph) {
		SystemState<Partition> newState = null;
		Partition newPartition = null;
		if (newPartition == null) {
			newState = new SystemState<Partition>("");
			newPartition = new Partition(getFulfills(), partition.getSources(), newState);
			newState.addSuccessorProvider(newPartition);
		} else {
			newPartition = removed;
			removed.addAllMessages(getFulfills());
			newState = removed.getTarget();
		}
		partition.removeMessages(getFulfills());
		partitionGraph.add(newPartition);
		stateGraph.add(newState);
		return new PartitionMerge(getPartition(), newPartition);
	}

	public boolean isValid() {
		return partition != null && fulfills.size() > 0
				&& fulfillsNot.size() > 0;
	}

	public String toString() {
		return fulfills.size() + "/" + fulfillsNot.size();
	}

	public void addFulfills(MessageEvent node) {
		fulfills.add(node);
	}

	public void addFulfillsNot(MessageEvent node) {
		fulfillsNot.add(node);
	}

	public Set<MessageEvent> getFulfillsNot() {
		return fulfillsNot;
	}

	public Set<MessageEvent> getFulfills() {
		return fulfills;
	}

	public Partition getPartition() {
		return partition;
	}

	public static PartitionSplit onlyFulfills(Partition partition) {
		PartitionSplit s = new PartitionSplit(partition);
		s.fulfills = partition.getMessages();
		s.fulfillsNot = Collections.emptySet();
		return s;
	}
}