package algorithms.graph;

import java.util.HashSet;
import java.util.Set;


import model.Partition;
import model.SystemState;
import model.PartitionGraph;
import model.interfaces.IModifiableGraph;
import model.interfaces.ISuccessorProvider;

/**
 * An operation that splits a state.
 * @author Sigurd Schneider
 *
 */
public class StateSplit implements Operation {
	private SystemState<Partition> stateToSplit = null;
	private Set<ISuccessorProvider<Partition>> positives = null;
	private Set<ISuccessorProvider<Partition>> negatives = null;
	private SystemState<Partition> stateToInsert = null;

	/**
	 * Create a state split. 
	 * @param stateToSplit the state to be split
	 * @param positives the successor providers to remain in the state
	 * @param negatives the successor providers to move to a new state
	 */
	public StateSplit(SystemState<Partition> stateToSplit,
			Set<ISuccessorProvider<Partition>> positives, Set<ISuccessorProvider<Partition>> negatives) {
		this.stateToSplit = stateToSplit;
		this.positives = new HashSet<ISuccessorProvider<Partition>>(positives);
		this.negatives = new HashSet<ISuccessorProvider<Partition>>(negatives);
	}

	/**
	 * Creates a state split. (Provided for undo operations)
	 * @param stateToSplit the state that gets split
	 * @param stateToInsert the state that should be introduced newly
	 */
	public StateSplit(SystemState<Partition> stateToSplit,
			SystemState<Partition> stateToInsert) {
		this.stateToSplit = stateToSplit;
		this.stateToInsert = stateToInsert;
		positives = stateToInsert.getSuccessorProviders();
	}

	/**
	 * Returns the state that gets split
	 * @return the state that gets split
	 */
	public SystemState<Partition> getState() {
		return stateToSplit;
	}

	/**
	 * Get the set of successor providers that remain in the state.
	 * @return set of successor providers that remain in the state
	 */
	private Set<ISuccessorProvider<Partition>> getPositive() {
		return positives;
	}
	
	/**
	 * Get the set of successor providers that are moved to a new state.
	 * @return set of successor providers that are moved to a new state
	 */
	private Set<ISuccessorProvider<Partition>> getNegatives() {
		return negatives;
	}

	@Override
	public Operation commit(PartitionGraph g, IModifiableGraph<Partition> partitionGraph,
			IModifiableGraph<SystemState<Partition>> stateGraph) {
		SystemState<Partition> newState = null;
		if (stateToInsert == null) {
			newState = new SystemState<Partition>("");
			newState.addSuccessorProviders(getPositive());
			newState.setParent(getState().getParent());
			
		} else {
			newState = stateToInsert;
		}
		stateGraph.add(newState);
		getState().removeSuccessorProviders(getNegatives());
		return new StateMerge(stateToSplit, newState);
	}
}
