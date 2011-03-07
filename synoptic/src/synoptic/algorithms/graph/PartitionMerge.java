package synoptic.algorithms.graph;

import synoptic.model.LogEvent;
import synoptic.model.Partition;
import synoptic.model.PartitionGraph;
import synoptic.util.InternalSynopticException;

/**
 * An operation that merges two partitions into one.
 * 
 * @author Sigurd Schneider
 */
public class PartitionMerge implements IOperation {
    Partition retained;
    Partition removed;

    /**
     * Creates a PartitionMerge.
     * 
     * @param retained
     *            the partition that remains in the graph
     * @param removed
     *            the partition that gets removed. Its messages are moved to the
     *            partition {@code retained}
     */
    public PartitionMerge(Partition retained, Partition removed) {
        // TODO: check that retained label is the same as removed label?
        this.retained = retained;
        this.removed = removed;
        if (retained.size() == 0 || removed.size() == 0) {
            throw new InternalSynopticException("merging empty partitions: "
                    + retained.size() + ", " + removed.size());
        }
    }

    @Override
    public IOperation commit(PartitionGraph g) {
        int retainedSize = retained.size();
        int removedSize = removed.size();
        PartitionSplit split = new PartitionSplit(retained, removed);
        for (LogEvent m : removed.getMessages()) {
            split.addEventToSplit(m);
        }
        retained.addAllMessages(removed.getMessages());
        // TODO: do we have to call removed.removeMessages() prior to calling
        // partitionGraph.remove() ?
        removed.removeMessages(removed.getMessages());
        g.remove(removed);
        if (removedSize + retainedSize != retained.size()) {
            throw new InternalSynopticException("lost messages!: "
                    + removedSize + "+" + retainedSize + "!= "
                    + retained.size());
        }

        // //////////////
        // Invalidate the appropriate elements in the graph's transitionCache

        // g.mergeAdjacentsCache(removed, retained);

        // if (g.transitionCache.get(removed) == null
        // || g.transitionCache.get(retained) == null) {
        // g.transitionCache.remove(retained);
        // } else {
        // g.transitionCache.get(retained).addAll(
        // g.transitionCache.get(removed));
        // }

        // g.transitionCache.remove(removed);

        g.clearNodeAdjacentsCache(retained);
        g.clearNodeAdjacentsCache(removed);

        // //////////////

        return split;
    }

    /**
     * Get the partition that was removed from the graph.
     * 
     * @return the partition that was removed
     */
    public Partition getRemoved() {
        return removed;
    }

}
