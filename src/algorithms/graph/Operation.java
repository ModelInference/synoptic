package algorithms.graph;

import model.Partition;
import model.PartitionGraph;
import model.SystemState;
import model.interfaces.IModifiableGraph;
/**
 * Interface for operations on PartitionGraphs 
 * @author sigurd
 */
public interface Operation {
	/**
	 * Commit the changes to graphs partitionGraph and stateGraph, and return an operation that will rewind them.
	 * @param partitionGraph the partition graph to apply to
	 * @param stateGraph the partition graph that corresponds to the state graph
	 * @return an operation that will undo the changes
	 */
	Operation commit(PartitionGraph g, IModifiableGraph<Partition> partitionGraph, IModifiableGraph<SystemState<Partition>> stateGraph);
} 