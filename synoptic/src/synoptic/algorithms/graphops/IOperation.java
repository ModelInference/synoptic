package synoptic.algorithms.graphops;

import synoptic.model.PartitionGraph;

/**
 * Interface for mutating operations on PartitionGraphs
 * 
 */
public interface IOperation {
    /**
     * Commit the changes to graphs partitionGraph and stateGraph, and return an
     * operation that will rewind them.
     * 
     * @param g
     *            the partition graph to apply to
     * @return an operation that will undo the changes
     */
    IOperation commit(PartitionGraph g);
}
