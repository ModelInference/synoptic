package algorithms.graph;

import model.MessageEvent;
import model.Partition;
import model.SystemState;
import model.PartitionGraph;
import model.interfaces.IModifiableGraph;

public class PartitionMerge implements Operation {
	Partition retained;
	Partition removed;

	public PartitionMerge(Partition retained, Partition removed) {
		this.retained = retained;
		this.removed = removed;
	}

	@Override
	public Operation commit(PartitionGraph g, IModifiableGraph<Partition> partitionGraph,
			IModifiableGraph<SystemState<Partition>> stateGraph) {
		retained.addAllMessages(removed.getMessages());
		stateGraph.remove(removed.getTarget());
		for (SystemState<Partition> s : removed.getSources()) {
			//stateGraph.remove(s);
		}
		partitionGraph.remove(removed);
		PartitionSplit split = new PartitionSplit(retained, removed);
		for (MessageEvent m : removed.getMessages())
			split.addFulfills(m);
		return split;
	}

}
