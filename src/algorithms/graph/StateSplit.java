package algorithms.graph;

import java.util.HashSet;
import java.util.Set;


import model.Partition;
import model.SystemState;
import model.PartitionGraph;
import model.interfaces.IModifiableGraph;
import model.interfaces.ISuccessorProvider;

public class StateSplit implements Operation {
	private SystemState<Partition> state = null;
	private Set<ISuccessorProvider<Partition>> positives = null;
	private Set<ISuccessorProvider<Partition>> negatives = null;
	private SystemState<Partition> removed = null;

	public StateSplit(SystemState<Partition> state,
			Set<ISuccessorProvider<Partition>> positives, Set<ISuccessorProvider<Partition>> negatives) {
		this.state = state;
		this.positives = new HashSet<ISuccessorProvider<Partition>>(positives);
		this.negatives = new HashSet<ISuccessorProvider<Partition>>(negatives);
	}

	public StateSplit(SystemState<Partition> retained,
			SystemState<Partition> removed) {
		this.state = retained;
		this.removed = removed;
		positives = removed.getSuccessorProviders();
	}

	public SystemState<Partition> getState() {
		return state;
	}

	private Set<ISuccessorProvider<Partition>> getPositive() {
		return positives;
	}
	
	private Set<ISuccessorProvider<Partition>> getNegatives() {
		return negatives;
	}

	@Override
	public Operation commit(PartitionGraph g, IModifiableGraph<Partition> partitionGraph,
			IModifiableGraph<SystemState<Partition>> stateGraph) {
		SystemState<Partition> newState = null;
		if (removed == null) {
			newState = new SystemState<Partition>("");
			newState.addSuccessorProviders(getPositive());
			newState.setParent(getState().getParent());
			
		} else {
			newState = removed;
		}
		stateGraph.add(newState);
		getState().removeSuccessorProviders(getNegatives());
		return new StateMerge(state, newState);
	}
}
