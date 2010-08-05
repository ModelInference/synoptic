package algorithms.graph;

import model.Partition;
import model.SystemState;
import model.interfaces.IModifiableGraph;
import model.interfaces.ISuccessorProvider;
import model.PartitionGraph;

/**
 * An operation that merges two states (of the state based representation).
 * @author Sigurd Schneider
 *
 */
public class StateMerge implements Operation {
	SystemState<Partition> retained;
	SystemState<Partition> removed;
	
	/**
	 * Creates a state merge operation.
	 * @param retained the state that will be retained in the graph
	 * @param removed the state that gets removed in the graph
	 */
	public StateMerge(SystemState<Partition> retained,
			SystemState<Partition> removed) {
		this.retained = retained;
		this.removed = removed;
	}

	@Override
	public Operation commit(PartitionGraph g, IModifiableGraph<Partition> partitionGraph,
			IModifiableGraph<SystemState<Partition>> stateGraph) {
			for (ISuccessorProvider<Partition> sp : removed.getSuccessorProviders()) {
				sp.setTarget(retained);
				retained.addSuccessorProvider(sp);
			}
			stateGraph.remove(removed);
			return new StateSplit(retained, removed);
	}
}
