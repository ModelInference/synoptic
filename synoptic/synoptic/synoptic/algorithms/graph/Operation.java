package synoptic.algorithms.graph;

import synoptic.model.Partition;
import synoptic.model.PartitionGraph;
import synoptic.model.interfaces.IModifiableGraph;
/**
 * Interface for operations on PartitionGraphs 
 * @author sigurd
 */
public interface Operation {
	/**
	 * Commit the changes to graphs partitionGraph and stateGraph, and return an operation that will rewind them.
	 * @param partitionGraph the partition graph to apply to
	 * @return an operation that will undo the changes
	 */
	Operation commit(PartitionGraph g, IModifiableGraph<Partition> partitionGraph);
} 