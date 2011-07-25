package synoptic.algorithms.graph;

import java.util.ArrayList;

import synoptic.model.Partition;
import synoptic.model.PartitionGraph;

/**
 * An operation that provides a multi merge, i.e. merging multiple partitions
 * into another partition.
 * 
 * @author Sigurd Schneider
 */
public class PartitionMultiMerge implements IOperation {
    private final Partition retainedPartition;
    private final ArrayList<Partition> partitionsToMerge;

    /**
     * Creates a partition multi merge.
     * 
     * @param partition
     *            the partition to merge into
     * @param partitionsToMerge
     *            the partitions to merge into {@code partition}
     */
    public PartitionMultiMerge(Partition partition,
            ArrayList<Partition> partitionsToMerge) {
        retainedPartition = partition;
        this.partitionsToMerge = partitionsToMerge;
    }

    @Override
    public IOperation commit(PartitionGraph g) {
        // boolean invalidateRetained = false;
        // if (g.transitionCache.get(retainedPartition) == null) {
        // invalidateRetained = true;
        // }

        for (Partition removed : partitionsToMerge) {
            retainedPartition.addEventNodes(removed.getEventNodes());
            removed.removeAllEventNodes();
            g.remove(removed);

            // //////////////
            // Invalidate the appropriate elements in the graph's
            // transitionCache

            // g.clearNodeAdjacentsCache(removed);
            // g.mergeAdjacentsCache(removed, retainedPartition);
            // if (!invalidateRetained) {
            // if (g.transitionCache.get(removed) == null) {
            // invalidateRetained = true;
            // } else {
            // g.transitionCache.get(retainedPartition).addAll(
            // g.transitionCache.get(removed));
            // }
            // }
            // g.transitionCache.remove(removed);
            g.clearNodeAdjacentsCache(removed);
            // //////////////
        }

        // if (invalidateRetained) {
        g.removeFromCache(retainedPartition);
        // }
        // g.transitionCache.remove(removed);

        // g.clearNodeAdjacentsCache(retainedPartition);

        // TODO: Provide undo
        return null;
    }
}
