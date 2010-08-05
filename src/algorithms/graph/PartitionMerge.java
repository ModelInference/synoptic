package algorithms.graph;

import model.MessageEvent;
import model.Partition;
import model.SystemState;
import model.PartitionGraph;
import model.interfaces.IModifiableGraph;

/**
 * An operation that merges two states into one.
 * @author Sigurd Schneider
 *
 */
public class PartitionMerge implements Operation {
	Partition retained;
	Partition removed;

	/**
	 * Creates a PartitionMerge.
	 * @param retained the partition that remains in the graph
	 * @param removed the partition that gets removed. Its messages are moved to the partition {@code retained}
	 */
	public PartitionMerge(Partition retained, Partition removed) {
		this.retained = retained;
		this.removed = removed;
		if (retained.size() == 0 || removed.size() == 0)
			throw new RuntimeException("merging empty partitions: " + retained.size() + ", " + removed.size());
	}
	
	@Override
	public Operation commit(PartitionGraph g, IModifiableGraph<Partition> partitionGraph,
			IModifiableGraph<SystemState<Partition>> stateGraph) {
		int retainedSize = retained.size();
		int removedSize = removed.size();
		PartitionSplit split = new PartitionSplit(retained, removed);
		for (MessageEvent m : removed.getMessages())
			split.addFulfills(m);
	//	for (MessageEvent m : retained.getMessages())
	//		split.addFulfillsNot(m);
		retained.addAllMessages(removed.getMessages());
		stateGraph.remove(removed.getTarget());
		for (SystemState<Partition> s : removed.getSources()) {
			//stateGraph.remove(s);
		}
		removed.removeMessages(removed.getMessages());
		partitionGraph.remove(removed);
		//System.out.println("merge rewind: " + split);
		if (removedSize + retainedSize != retained.size())
			throw new RuntimeException("lost messages!: " + removedSize+ "+" + retainedSize + "!= " + retained.size());
		return split;
	}
	
	/**
	 * Get the partition that was removed from the graph.
	 * @return the partition that was removed
	 */
	public Partition getRemoved() {
		return removed;
	}

}
