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
		boolean skippedFirst = false;
		ArrayList<Partition> newPartitions = new  ArrayList<Partition>();
		for (Set<MessageEvent> set : partitioning) {
			if (!skippedFirst) {
				skippedFirst = true;
				continue;
			}
			SystemState<Partition> newState = new SystemState<Partition>("");
			Partition newPartition = new Partition(set, partition.getSources(),
					newState);
			newPartitions.add(newPartition);
			newState.addSuccessorProvider(newPartition);
			partition.removeMessages(set);
			newPartition.addAllMessages(set);
			partitionGraph.add(newPartition);
			stateGraph.add(newState);
		}
		g.checkSanity();
		return new PartitionMultiMerge(partition, newPartitions);
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
	 * @param split
	 *            the split to incorporate
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

	/**
	 * Gets the partition that will be split.
	 * @return the partition that will be split
	 */
	public Partition getPartition() {
		return this.partition;
	}

	/**
	 * Incorporates a partition multi split.
	 * @param split the multi split to incorporate
	 */
	public void incorporate(PartitionMultiSplit split) {
		if (split.getPartition() != partition)
			throw new IllegalArgumentException();
		ArrayList<Set<MessageEvent>> newSets = new ArrayList<Set<MessageEvent>>();
		for (Set<MessageEvent> set : partitioning) {
			for (Set<MessageEvent> otherSet : split.partitioning) {
				Set<MessageEvent> newSet = new HashSet<MessageEvent>(set);
				set.removeAll(otherSet);
				newSet.retainAll(otherSet);
				newSets.add(newSet);
			}
		}
		partitioning.addAll(newSets);
		for (Iterator<Set<MessageEvent>> iter = partitioning.iterator(); iter
				.hasNext();) {
			if (iter.next().size() == 0)
				iter.remove();
		}
		System.out.println(partitioning);
	}

	public boolean isValid() {
		return partitioning.size() > 1;
	}
}
