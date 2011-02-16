package synoptic.algorithms.graph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import synoptic.model.LogEvent;
import synoptic.model.Partition;
import synoptic.model.PartitionGraph;

/**
 * Implements a partition split that splits a partition into multiple others.
 * 
 * @author Sigurd Schneider
 */
public class PartitionMultiSplit implements IOperation {
    private final ArrayList<Set<LogEvent>> partitioning = new ArrayList<Set<LogEvent>>();
    private final Partition partition;

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
        partitioning.add(split.getSplitEvents());
        Set<LogEvent> otherMessages = new LinkedHashSet<LogEvent>(partition
                .getMessages());
        otherMessages.removeAll(split.getSplitEvents());
        partitioning.add(otherMessages);
    }

    @Override
    public IOperation commit(PartitionGraph g) {
        // We have to remove one of the sets, because the partition currently in
        // the graph will hold exactly that set of message events.
        boolean skippedFirst = false;
        ArrayList<Partition> newPartitions = new ArrayList<Partition>();
        for (Set<LogEvent> set : partitioning) {
            if (!skippedFirst) {
                skippedFirst = true;
                continue;
            }
            Partition newPartition = new Partition(set);
            newPartitions.add(newPartition);
            partition.removeMessages(set);
            newPartition.addAllMessages(set);
            g.add(newPartition);
        }
        g.checkSanity();
        return new PartitionMultiMerge(partition, newPartitions);
    }

    @Override
    public String toString() {
        // NOTE: this string only makes sense before the operation is committed,
        // after a commit() the partition may have a different # of messages!
        StringBuilder sb = new StringBuilder("S." + partition.getLabel() + ".");
        for (Set<LogEvent> m : partitioning) {
            sb.append(m.size() + "/");
        }
        return sb.toString().substring(0, sb.length() - 1);
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
        if (split.getPartition() != partition) {
            throw new IllegalArgumentException();
        }

        ArrayList<Set<LogEvent>> newSets = new ArrayList<Set<LogEvent>>();
        for (Set<LogEvent> set : partitioning) {
            Set<LogEvent> newSet = new LinkedHashSet<LogEvent>(set);
            set.removeAll(split.getSplitEvents());
            newSet.retainAll(split.getSplitEvents());
            newSets.add(newSet);
        }
        partitioning.addAll(newSets);

        // Remove all the partitions that are empty as a result of the
        // incorporation.
        for (Iterator<Set<LogEvent>> iter = partitioning.iterator(); iter
                .hasNext();) {
            if (iter.next().size() == 0) {
                iter.remove();
            }
        }
    }

    /**
     * Gets the partition that will be split.
     * 
     * @return the partition that will be split
     */
    public Partition getPartition() {
        return partition;
    }

    /**
     * Incorporates a partition multi split.
     * 
     * @param split
     *            the multi split to incorporate
     */
    public void incorporate(PartitionMultiSplit split) {
        if (split.getPartition() != partition) {
            throw new IllegalArgumentException();
        }
        ArrayList<Set<LogEvent>> newSets = new ArrayList<Set<LogEvent>>();
        for (Set<LogEvent> set : partitioning) {
            for (Set<LogEvent> otherSet : split.partitioning) {
                Set<LogEvent> newSet = new LinkedHashSet<LogEvent>(set);
                set.removeAll(otherSet);
                newSet.retainAll(otherSet);
                newSets.add(newSet);
            }
        }
        partitioning.addAll(newSets);

        // Remove all the partitions that are empty as a result of the
        // incorporation.
        for (Iterator<Set<LogEvent>> iter = partitioning.iterator(); iter
                .hasNext();) {
            if (iter.next().size() == 0) {
                iter.remove();
            }
        }
    }

    public boolean isValid() {
        return partitioning.size() > 1;
    }
}
