package algorithms.graph;

import java.util.ArrayList;

import model.Partition;
import model.PartitionGraph;
import model.SystemState;
import model.interfaces.IModifiableGraph;
/**
 * An operation that provides a multi merge, i.e. merging multiple partitions into another partition.
 * @author Sigurd Schneider
 *
 */
public class PartitionMultiMerge implements Operation {
	private Partition retainedPartition;
	private ArrayList<Partition> partitionsToMerge;

	/**
	 * Creates a partition multi merge. 
	 * @param partition the partition to merge into
	 * @param partitionsToMerge the partitions to merge into {@code partition}
	 */
	public PartitionMultiMerge(Partition partition,
			ArrayList<Partition> partitionsToMerge) {
		this.retainedPartition = partition;
		this.partitionsToMerge = partitionsToMerge;
	}

	@Override
	public Operation commit(PartitionGraph g,
			IModifiableGraph<Partition> partitionGraph,
			IModifiableGraph<SystemState<Partition>> stateGraph) {
		for (Partition removed : partitionsToMerge) {
			retainedPartition.addAllMessages(removed.getMessages());
			stateGraph.remove(removed.getTarget());
			removed.removeMessages(removed.getMessages());
			partitionGraph.remove(removed);
		}
		// TODO: Provide undo
		return null;
	}
}
