package algorithms.graph;

import java.util.HashSet;
import java.util.Set;

import model.MessageEvent;
import model.Partition;
import model.PartitionGraph;
import model.interfaces.IModifiableGraph;

/**
 * A operation for a partition split.
 * @author Sigurd Schneider
 *
 */
public class PartitionSplit implements Operation {
	private Partition partitionToSplit = null;
	/**
	 * The messages that will be split out into a separate node.
	 */
	private Set<MessageEvent> fulfills = null;
	private Partition partitionToInsert = null;

	/** 
	 * Creates a partition split. The split is defined by the messages added using {@code addFulfills}.
	 * @param partition the partition to split
	 */
	public PartitionSplit(Partition partition) {
		this.partitionToSplit = partition;
		fulfills = new HashSet<MessageEvent>(partition.size());
	}

	/**
	 * Creates a partition split, forcing the inserted node to be {@code removed}. Provided for undo functionality.
	 * @param partitionToSplit the partition split
	 * @param partitionToInsert the partition that will be inserted
	 */
	public PartitionSplit(Partition partitionToSplit, Partition partitionToInsert) {
		this.partitionToSplit = partitionToSplit;
		this.partitionToInsert = partitionToInsert;
		fulfills = new HashSet<MessageEvent>(partitionToSplit.size());
	}

	@Override
	public Operation commit(PartitionGraph g,
			IModifiableGraph<Partition> partitionGraph) {
		Partition newPartition = partitionToInsert;
		if (newPartition == null) {
			newPartition = new Partition(getFulfills());
		} else {
			newPartition = partitionToInsert;
			partitionToInsert.addAllMessages(getFulfills());
		}
		partitionToSplit.removeMessages(getFulfills());
		partitionGraph.add(newPartition);
		return new PartitionMerge(getPartition(), newPartition);
	}

	/**
	 * Check whether this partition split is valid.
	 * @return true if valid
	 */
	public boolean isValid() {
		return partitionToSplit != null && fulfills.size() > 0 && partitionToSplit.getMessages().size() > fulfills.size();
	}

	@Override
	public String toString() {
		return fulfills.size() + "/" + (partitionToSplit.getMessages().size()-fulfills.size());
	}

	/**
	 * Mark a message for splitting into a separate node.
	 * @param node the node to mark
	 */
	public void addFulfills(MessageEvent node) {
		fulfills.add(node);
	}

	/**
	 * Retrieve the set of messages marked so far.
	 * @return the set of marked nodes
	 */
	public Set<MessageEvent> getFulfills() {
		return fulfills;
	}

	/**
	 * Gets the partition to split.
	 * @return the partition to split.
	 */
	public Partition getPartition() {
		return partitionToSplit;
	}

	/**
	 * Create a partition split that would split all messages of {@code partition} into a separate node.
 	 * @param partition
	 * @return
	 */
	public static PartitionSplit onlyFulfills(Partition partition) {
		PartitionSplit s = new PartitionSplit(partition);
		s.fulfills = partition.getMessages();
		return s;
	}

	/**
	 * Incorporates {@code candidateSplit} into this split, yielding a multi split.
	 * @param candidateSplit the candidate split to incorporate
	 * @return the resulting multi split
	 */
	public PartitionMultiSplit incorporate(PartitionSplit candidateSplit) {
		if (partitionToInsert == null || candidateSplit.getPartition() != partitionToSplit)
			throw new IllegalArgumentException();
		PartitionMultiSplit multiSplit = new PartitionMultiSplit(this);
		multiSplit.incorporate(candidateSplit);
		return multiSplit;
	}
}