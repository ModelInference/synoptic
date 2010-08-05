package algorithms.graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import model.MessageEvent;
import model.Partition;
import model.PartitionGraph;
import model.SystemState;
import model.interfaces.IModifiableGraph;

public class PartitionMultiSplit implements Operation {
	private ArrayList<Set<MessageEvent>> partitioning = new ArrayList<Set<MessageEvent>>();
	private Partition partition;

	public PartitionMultiSplit(PartitionSplit split) {
		partition = split.getPartition();
		partitioning.add(split.getFulfills());
		Set<MessageEvent> otherMessages = new HashSet<MessageEvent>(partition.getMessages());
		otherMessages.removeAll(split.getFulfills());
		partitioning.add(otherMessages);
	}

	public Operation commit(PartitionGraph g,
			IModifiableGraph<Partition> partitionGraph,
			IModifiableGraph<SystemState<Partition>> stateGraph) {
		// We have to remove one of the sets, because the partition currently in the graph will hold exactly that set of message events.
		partitioning.remove(0);
		for (Set<MessageEvent> set : partitioning) {
			SystemState<Partition> newState = new SystemState<Partition>("");
			Partition newPartition = new Partition(set,
					partition.getSources(), newState);
			newState.addSuccessorProvider(newPartition);
			partition.removeMessages(set);
			newPartition.addAllMessages(set);
			partitionGraph.add(newPartition);
			stateGraph.add(newState);
		}
		g.checkSanity();
		return null;
	}

	public boolean isValid() {
		// TODO
		return true;
	}

	public String toString() {
		// TODO
		return "";
	}

	public PartitionMultiSplit incorporate(PartitionSplit candidateSplit) {
		if (candidateSplit.getPartition() != partition)
			throw new IllegalArgumentException();
		ArrayList<Set<MessageEvent>> newSets = new ArrayList<Set<MessageEvent>>();
		for (Set<MessageEvent> set : partitioning) {
			Set<MessageEvent> newSet = new HashSet<MessageEvent>(set);
			set.removeAll(candidateSplit.getFulfills());
			newSet.retainAll(candidateSplit.getFulfills());
			newSets.add(newSet);
		}
		partitioning.addAll(newSets);
		for (Iterator<Set<MessageEvent>> iter = partitioning.iterator(); iter.hasNext();) {
			if (iter.next().size() == 0)
				iter.remove();
		}
		System.out.println(partitioning);
		return this;
	}
}
