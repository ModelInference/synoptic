package synoptic.algorithms.graph;

import java.util.HashSet;
import java.util.Set;

import synoptic.model.MessageEvent;
import synoptic.model.Partition;
import synoptic.model.PartitionGraph;
import synoptic.model.interfaces.IModifiableGraph;


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
	private Set<MessageEvent> eventsToSplitOut = null;
	private Partition partitionToInsert = null;

	/** 
	 * Creates a partition split. The split is defined by the messages added using {@code addEventToSplit}.
	 * @param partition the partition to split
	 */
	public PartitionSplit(Partition partition) {
		this.partitionToSplit = partition;
		eventsToSplitOut = new HashSet<MessageEvent>(partition.size());
	}

	/**
	 * Creates a partition split, forcing the inserted node to be {@code removed}. Provided for undo functionality.
	 * @param partitionToSplit the partition split
	 * @param partitionToInsert the partition that will be inserted
	 */
	public PartitionSplit(Partition partitionToSplit, Partition partitionToInsert) {
		this.partitionToSplit = partitionToSplit;
		this.partitionToInsert = partitionToInsert;
		eventsToSplitOut = new HashSet<MessageEvent>(partitionToSplit.size());
	}

	@Override
	public Operation commit(PartitionGraph g,
			IModifiableGraph<Partition> partitionGraph) {
		Partition newPartition = partitionToInsert;
		if (newPartition == null) {
			newPartition = new Partition(getSplitEvents());
		} else {
			newPartition = partitionToInsert;
			partitionToInsert.addAllMessages(getSplitEvents());
		}
		partitionToSplit.removeMessages(getSplitEvents());
		partitionGraph.add(newPartition);
		return new PartitionMerge(getPartition(), newPartition);
	}

	/**
	 * Check whether this partition split is valid to perform. This should
	 * be used prior to applying the operation on a graph.
	 * 
	 * @return true if valid
	 */
	public boolean isValid() {
		return partitionToSplit != null && eventsToSplitOut.size() > 0 && partitionToSplit.getMessages().size() > eventsToSplitOut.size();
	}

	@Override
	public String toString() {
		// NOTE: this string only makes sense before the operation is committed,
		// after a commit() the partition may have a different # of messages!
		return "S." + partitionToSplit.getLabel() + "."
			+ eventsToSplitOut.size() + "/"
			+ (partitionToSplit.getMessages().size() - eventsToSplitOut.size());
	}

	/**
	 * Mark a message for splitting into a separate node.
	 * @param node the node to mark
	 */
	public void addEventToSplit(MessageEvent event) {
		eventsToSplitOut.add(event);
	}

	/**
	 * Retrieve the set of messages marked so far.
	 * @return the set of marked nodes
	 */
	public Set<MessageEvent> getSplitEvents() {
		return eventsToSplitOut;
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
	 * @return the newly created partition split
	 */
	public static PartitionSplit newSplitWithAllEvents(Partition partition) {
		PartitionSplit s = new PartitionSplit(partition);
		s.eventsToSplitOut = partition.getMessages();
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