package synoptic.algorithms.graphops;

import java.util.LinkedHashSet;
import java.util.Set;

import synoptic.model.EventNode;
import synoptic.model.Partition;
import synoptic.model.PartitionGraph;

/**
 * A operation that splits a partition into two -- creating one new partition.
 * 
 */
public class PartitionSplit implements IOperation {
    /**
     * Partition that will be split by this operation.
     */
    private Partition partitionToSplit = null;

    /**
     * The messages that will be split out into a separate node.
     */
    private Set<EventNode> eventsToSplitOut = null;

    /**
     * Partition that will be used for storing eventsToSplitOut once this
     * operation commits.
     */
    private Partition newPartition = null;

    /**
     * Creates a partition split. The split is defined by the messages added
     * using {@code addEventToSplit}.
     * 
     * @param partitionToSplit
     *            the partition to split
     */
    public PartitionSplit(Partition partitionToSplit) {
        this.partitionToSplit = partitionToSplit;
        eventsToSplitOut = new LinkedHashSet<EventNode>(partitionToSplit.size());
        newPartition = null;
    }

    /**
     * Creates a partition split. The split is defined by the messages added
     * using {@code addEventToSplit}. Will use newPartitionArg for the messages
     * that will be split out (instead of creating a new partition) -- this is
     * important in cases when splits need to preserve the partition set of a
     * graph.
     * 
     * @param partitionToSplit
     *            the partition to split
     * @param newPartitionArg
     *            the partition to use for events to split out
     */
    public PartitionSplit(Partition partitionToSplit, Partition newPartitionArg) {
        this(partitionToSplit);
        newPartition = newPartitionArg;
    }

    @Override
    public IOperation commit(PartitionGraph g) {
        assert isValid();

        if (newPartition == null) {
            newPartition = new Partition(getSplitEvents());
        }

        newPartition.addEventNodes(getSplitEvents());
        partitionToSplit.removeEventNodes(getSplitEvents());
        g.add(newPartition);

        // //////////////
        // Invalidate the appropriate elements in the graph's transitionCache

        g.clearNodeAdjacentsCache(partitionToSplit);
        g.clearNodeAdjacentsCache(newPartition);

        // //////////////

        return new PartitionMerge(partitionToSplit, newPartition);
    }

    /**
     * Check whether this partition split is valid to perform. This should be
     * used prior to applying the operation on a graph.
     * 
     * @return true if valid
     */
    public boolean isValid() {
        // A split is valid if:
        // 1. it splits out at least one message
        // 2. it does not split out all messages
        return partitionToSplit != null
                && eventsToSplitOut.size() > 0
                && partitionToSplit.getEventNodes().size() > eventsToSplitOut
                        .size();
    }

    @Override
    public String toString() {
        // NOTE: this string only makes sense BEFORE the operation is committed,
        // after a commit() the partition may have a different # of messages!
        return "S."
                + partitionToSplit.getEType()
                + "."
                + eventsToSplitOut.size()
                + "/"
                + (partitionToSplit.getEventNodes().size() - eventsToSplitOut
                        .size());
    }

    /**
     * Mark an event for splitting into a separate node.
     * 
     * @param event
     *            the event to mark
     */
    public void addEventToSplit(EventNode event) {
        eventsToSplitOut.add(event);
    }

    /**
     * Retrieve the set of messages marked so far.
     * 
     * @return the set of marked nodes
     */
    public Set<EventNode> getSplitEvents() {
        return eventsToSplitOut;
    }

    /**
     * Gets the partition to split.
     * 
     * @return the partition to split.
     */
    public Partition getPartition() {
        return partitionToSplit;
    }

    /**
     * Create a partition split that would split all messages of
     * {@code partition} into a separate node.
     * 
     * @param partition
     * @return the newly created partition split
     */
    public static PartitionSplit newSplitWithAllEvents(Partition partition) {
        PartitionSplit s = new PartitionSplit(partition);
        s.eventsToSplitOut = partition.getEventNodes();
        return s;
    }

    /**
     * Incorporates {@code candidateSplit} into this split, yielding a multi
     * split.
     * 
     * @param candidateSplit
     *            the candidate split to incorporate
     * @return the resulting multi split
     */
    public PartitionMultiSplit incorporate(PartitionSplit candidateSplit) {
        if (candidateSplit.getPartition() != partitionToSplit) {
            throw new IllegalArgumentException();
        }
        PartitionMultiSplit multiSplit = new PartitionMultiSplit(this);
        multiSplit.incorporate(candidateSplit);
        return multiSplit;
    }
}
