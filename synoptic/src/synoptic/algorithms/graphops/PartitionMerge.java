package synoptic.algorithms.graphops;

import synoptic.model.EventNode;
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
        assert retained.getEType().equals(removed.getEType()) : "merging partitions of different types";
        assert (retained.size() != 0 && removed.size() != 0) : "merging two empty partitions";

        this.retained = retained;
        this.removed = removed;
    }

    @Override
    public IOperation commit(PartitionGraph g) {
        int retainedSize = retained.size();
        int removedSize = removed.size();
        PartitionSplit split = new PartitionSplit(retained, removed);
        for (EventNode m : removed.getEventNodes()) {
            split.addEventToSplit(m);
        }
        retained.addEventNodes(removed.getEventNodes());
        removed.removeAllEventNodes();
        g.removePartition(removed);
        if (removedSize + retainedSize != retained.size()) {
            throw new InternalSynopticException("lost messages!: "
                    + removedSize + "+" + retainedSize + "!= "
                    + retained.size());
        }

        // //////////////
        // Invalidate the appropriate elements in the graph's transitionCache

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
