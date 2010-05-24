package algorithms.graph;

import java.util.LinkedList;

import model.Partition;
import model.PartitionGraph;
import model.SystemState;
import model.interfaces.IModifiableGraph;

public class OperationSequence implements Operation {
	LinkedList<Operation> sequence = new LinkedList<Operation>();

	@Override
	public Operation commit(PartitionGraph g,
			IModifiableGraph<Partition> partitionGraph,
			IModifiableGraph<SystemState<Partition>> stateGraph) {
		OperationSequence rewindOperation = new OperationSequence();
		for (Operation op : sequence)
			rewindOperation.addFirst(g.apply(op));
		return rewindOperation;
	}

	public void addFirst(Operation operation) {
		sequence.addFirst(operation);
	}
}
