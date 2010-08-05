package algorithms.graph;

import model.Partition;
import model.PartitionGraph;
import model.SystemState;
import model.interfaces.IModifiableGraph;
import model.interfaces.ISuccessorProvider;

public class CascadingStateMerge extends StateMerge {

	public CascadingStateMerge(SystemState<Partition> retained,
			SystemState<Partition> removed) {
		super(retained, removed);
	}

	@Override
	public Operation commit(PartitionGraph g,
			IModifiableGraph<Partition> partitionGraph,
			IModifiableGraph<SystemState<Partition>> stateGraph) {
		OperationSequence rewindOperation = new OperationSequence();
		// merge the states
		rewindOperation.addFirst(super.commit(g, partitionGraph, stateGraph)); 

		// now check if this induces merging partitions
		for (Partition a : retained.getSuccessorIterator()) {
			for (ISuccessorProvider<Partition> sp : retained.getSuccessorProviders()) {
				for (Partition b : sp.getSuccessorIterator()) {
					if (a == b || !a.getAction().equals(b.getAction()))
						continue;
					PartitionMerge m = new PartitionMerge(a, b);
					//System.out.println("merge " + a + " <- " + b);
					rewindOperation.addFirst(g.apply(m));
				}
			}
		}

		return rewindOperation;
	}
}
