package synoptic.algorithms.graph;

import synoptic.model.Partition;
import synoptic.model.PartitionGraph;
import synoptic.model.interfaces.IModifiableGraph;

/**
 * Interface for operations on PartitionGraphs
 * 
 * @author sigurd
 */
public interface IOperation {
    /**
     * Commit the changes to graphs partitionGraph and stateGraph, and return an
     * operation that will rewind them.
     * 
     * @param partitionGraph
     *            the partition graph to apply to
     * @return an operation that will undo the changes
     */
    IOperation commit(PartitionGraph g,
            IModifiableGraph<Partition> partitionGraph);
}