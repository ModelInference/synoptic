package synoptic.algorithms.graphops;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import synoptic.main.SynopticMain;
import synoptic.model.EventNode;
import synoptic.model.Partition;
import synoptic.model.PartitionGraph;

/**
 * Implements a partition split that splits a partition into multiple others.
 * 
 * @author Sigurd Schneider
 */
public class PartitionMultiSplit implements IOperation {
    private final ArrayList<Set<EventNode>> partitioning = new ArrayList<Set<EventNode>>();
    private final Partition partition;

    /**
     * Creates a partition multi split. At first it will just behave like the
     * {@code split} passed here. Afterwards other splits may be
     * {@code incorporate}d.
     * 
     * @param split
     *            the split that this multi split is based on.
     */
    public PartitionMultiSplit(PartitionSplit split) {
        partition = split.getPartition();
        partitioning.add(split.getSplitEvents());
        Set<EventNode> otherMessages = new LinkedHashSet<EventNode>(
                partition.getEventNodes());
        otherMessages.removeAll(split.getSplitEvents());
        partitioning.add(otherMessages);
    }

    @Override
    public IOperation commit(PartitionGraph g) {
        // We have to remove one of the sets, because the partition currently in
        // the graph will hold exactly that set of message events.
        boolean skippedFirst = false;
        ArrayList<Partition> newPartitions = new ArrayList<Partition>();
        for (Set<EventNode> set : partitioning) {
            if (!skippedFirst) {
                skippedFirst = true;
                continue;
            }
            Partition newPartition = new Partition(set);
            newPartitions.add(newPartition);
            partition.removeEventNodes(set);
            newPartition.addEventNodes(set);
            g.add(newPartition);

            g.clearNodeAdjacentsCache(newPartition);
        }

        // //////////////
        // Invalidate the appropriate elements in the graph's transitionCache

        g.clearNodeAdjacentsCache(partition);

        // //////////////

        if (SynopticMain.getInstanceWithExistenceCheck().options.performExtraChecks) {
            g.checkSanity();
        }

        return new PartitionMultiMerge(partition, newPartitions);
    }

    @Override
    public String toString() {
        // NOTE: this string only makes sense before the operation is committed,
        // after a commit() the partition may have a different # of messages!
        StringBuilder sb = new StringBuilder("S." + partition.getEType() + ".");
        for (Set<EventNode> m : partitioning) {
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

        ArrayList<Set<EventNode>> newSets = new ArrayList<Set<EventNode>>();
        for (Set<EventNode> set : partitioning) {
            Set<EventNode> newSet = new LinkedHashSet<EventNode>(set);
            set.removeAll(split.getSplitEvents());
            newSet.retainAll(split.getSplitEvents());
            newSets.add(newSet);
        }
        partitioning.addAll(newSets);

        // Remove all the partitions that are empty as a result of the
        // incorporation.
        for (Iterator<Set<EventNode>> iter = partitioning.iterator(); iter
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
        ArrayList<Set<EventNode>> newSets = new ArrayList<Set<EventNode>>();
        for (Set<EventNode> set : partitioning) {
            for (Set<EventNode> otherSet : split.partitioning) {
                Set<EventNode> newSet = new LinkedHashSet<EventNode>(set);
                set.removeAll(otherSet);
                newSet.retainAll(otherSet);
                newSets.add(newSet);
            }
        }
        partitioning.addAll(newSets);

        // Remove all the partitions that are empty as a result of the
        // incorporation.
        for (Iterator<Set<EventNode>> iter = partitioning.iterator(); iter
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
