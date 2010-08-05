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

/**
 * Implements a partition split that splits a partition into multiple others.
 * 
 * @author Sigurd Schneider
 * 
 */
public class PartitionMultiSplit implements Operation {
	private ArrayList<Set<MessageEvent>> partitioning = new ArrayList<Set<MessageEvent>>();
	private Partition partition;

	/**
	 * Creates a partition multi split. At first it will just behave like the
	 * {@code split} passed here. Afterwards other splits may be {@code
	 * incorporate}d.
	 * 
	 * @param split
	 *            the split that this multi split is based on.
	 */
	public PartitionMultiSplit(PartitionSplit split) {
		partition = split.getPartition();
		partitioning.add(split.getFulfills());
		Set<MessageEvent> otherMessages = new HashSet<MessageEvent>(partition
				.getMessages());
		otherMessages.removeAll(split.getFulfills());
		partitioning.add(otherMessages);
	}

	@Override
	public Operation commit(PartitionGraph g,
			IModifiableGraph<Partition> partitionGraph,
			IModifiableGraph<SystemState<Partition>> stateGraph) {
		// We have to remove one of the sets, because the partition currently in
		// the graph will hold exactly that set of message events.
		partitioning.remove(0);
		for (Set<MessageEvent> set : partitioning) {
			SystemState<Partition> newState = new SystemState<Partition>("");
			Partition newPartition = new Partition(set, partition.getSources(),
					newState);
			newState.addSuccessorProvider(newPartition);
			partition.removeMessages(set);
			newPartition.addAllMessages(set);
			partitionGraph.add(newPartition);
			stateGraph.add(newState);
		}
		g.checkSanity();
		return null;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("MultiSplit");
		for (Set<MessageEvent> m : partitioning) {
			sb.append(" " + m.size());
		}
		return sb.toString();
	}

	/**
	 * Perform the {@code split} on all partitions that would be created upon
	 * commitment of this multi split. In general, this will double the number
	 * of partitions this multi spit creates (although in practice, newly
	 * introduced partitions are often empty, and thus discarded)
	 * 
	 * @param split the split to incorporate
	 */
	public void incorporate(PartitionSplit split) {
		if (split.getPartition() != partition)
			throw new IllegalArgumentException();
		ArrayList<Set<MessageEvent>> newSets = new ArrayList<Set<MessageEvent>>();
		for (Set<MessageEvent> set : partitioning) {
			Set<MessageEvent> newSet = new HashSet<MessageEvent>(set);
			set.removeAll(split.getFulfills());
			newSet.retainAll(split.getFulfills());
			newSets.add(newSet);
		}
		partitioning.addAll(newSets);
		for (Iterator<Set<MessageEvent>> iter = partitioning.iterator(); iter
				.hasNext();) {
			if (iter.next().size() == 0)
				iter.remove();
		}
	}
}
