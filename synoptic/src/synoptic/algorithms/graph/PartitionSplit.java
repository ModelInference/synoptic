package synoptic.algorithms.graph;

import java.util.LinkedHashSet;
import java.util.Set;

import synoptic.model.LogEvent;
import synoptic.model.Partition;
import synoptic.model.PartitionGraph;

/**
 * A operation for a partition split.
 * 
 * @author Sigurd Schneider
 */
public class PartitionSplit implements IOperation {
    /**
     * Partition that will be split by this operation.
     */
    private Partition partitionToSplit = null;

    /**
     * The messages that will be split out into a separate node.
     */
    private Set<LogEvent> eventsToSplitOut = null;

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
        eventsToSplitOut = new LinkedHashSet<LogEvent>(partitionToSplit.size());
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

        newPartition.addAllMessages(getSplitEvents());
        partitionToSplit.removeMessages(getSplitEvents());
        g.add(newPartition);

        // //////////////
        // Invalidate the appropriate elements in the graph's transitionCache

        g.clearNodeAdjacentsCache(partitionToSplit);
        g.clearNodeAdjacentsCache(newPartition);

        // //////////////

        // g.transitionCache.clear();

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
                && partitionToSplit.getEvents().size() > eventsToSplitOut
                        .size();
    }

    @Override
    public String toString() {
        // NOTE: this string only makes sense BEFORE the operation is committed,
        // after a commit() the partition may have a different # of messages!
        return "S."
                + partitionToSplit.getLabel()
                + "."
                + eventsToSplitOut.size()
                + "/"
                + (partitionToSplit.getEvents().size() - eventsToSplitOut
                        .size());
    }

    /**
     * Mark a message for splitting into a separate node.
     * 
     * @param node
     *            the node to mark
     */
    public void addEventToSplit(LogEvent event) {
        eventsToSplitOut.add(event);
    }

    /**
     * Retrieve the set of messages marked so far.
     * 
     * @return the set of marked nodes
     */
    public Set<LogEvent> getSplitEvents() {
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
        s.eventsToSplitOut = partition.getEvents();
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