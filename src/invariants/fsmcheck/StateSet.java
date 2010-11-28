package invariants.fsmcheck;

import invariants.BinaryInvariant;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract class to provide functionality for nondeterministic finite state
 * machines which utilize BitSets as a method for evaluating the transitions
 * of many instances in parallel.
 */
public abstract class StateSet {
	protected List<BitSet> sets;
	protected int count;
	
	/**
	 * Initializes the data structures, taking the number of instances of
	 * the machines to simulate in parallel.
	 * 
	 * @param numSimulators
	 */
	protected StateSet(int numSimulators) {
		this.count = numSimulators;
		// TODO: explain why sets is initialized to be a list of length 4?
		sets = new ArrayList<BitSet>(4);
	}
	
	public abstract void transition(List<BitSet> inputs);
	
	/**
	 * At final states (partitions which contain ending nodes of some sample
	 * traces), this indicates which of the invariants maintained by this
	 * stateset would be considered to not be satisfied.
	 */
	public abstract BitSet isFail();
	
	/**
	 * When machines are permanently locked in a failure state, i.e. no recovery
	 * is possible, that fact is indicated as a 1 in this bitset. This can be
	 * used to preemptively halt search.
	 */
	public abstract BitSet isPermanentFail();
	
	/**
	 * Adds a state, with a given initial value
	 * 
	 * @param initialValue true = 1, false = 0
	 */
	protected void addState(boolean initialValue) {
		BitSet newState = new BitSet(count);
		newState.set(0, count, initialValue);
		sets.add(newState);
	}
	
	/**
	 * Merges this stateset with another, by ORing all of the state vectors.
	 * Exceptions are thrown if
	 * 	- statesets have different sizes
	 * 	- statesets have different number of parallel instances
	 *  
	 * @param other
	 */
	public void mergeWith(StateSet other) {
		assert(sets.size() == other.sets.size());
		assert(count == other.count);
		for (int i = 0; i < sets.size(); i++) {
			sets.get(i).or(other.sets.get(i));
		}
	}
	
	/**
	 * Clones this set of states, using reflection to ascertain the actual type
	 * to construct.
	 * 
	 * @see java.lang.Object#clone()
	 */
	public StateSet clone() {
		StateSet result;
		try {
			result = (StateSet)this.getClass().getConstructors()[0].newInstance(sets.size());
		} catch (Exception e) {
			System.out.println("ERROR: Failed to clone stateset.");
			return null;
		}
		result.count = count;
		ArrayList<BitSet> newSets = new ArrayList<BitSet>();
		for (int i = 0; i < sets.size(); i++) {
			newSets.add((BitSet)sets.get(i).clone());
		}
		result.sets = newSets;
		return result;
	}
	
	/* A B result
	 * 0 0 1
	 * 0 1 0
	 * 1 0 0
	 * 1 1 0
	 */
	public static BitSet nor(BitSet a, BitSet b, int count) {
		BitSet result = (BitSet) a.clone();
		result.or(b);
		result.flip(0, count);
		return result;
	}

	// Helpers for dealing with mapping from some event representation to BitSets,
	// which indicate which boolean inputs to provide to each of the machines which
	// are being simulated.
	
	public static final BitSet zero = new BitSet();

	public <T> void visit(List<Map<T, BitSet>> mappings, T x) {
		List<BitSet> inputs = new ArrayList<BitSet>();
		for (Map<T, BitSet> mapping : mappings) {
			BitSet input = mapping.get(x);
			inputs.add(input == null ? zero : input);
		}
		transition(inputs);
	}
	
	public static List<Map<String, BitSet>> getMapping(List<BinaryInvariant> invariants) {
		List<Map<String, BitSet>> result = new ArrayList<Map<String, BitSet>>(2);
		Map<String, BitSet> amap = new HashMap<String, BitSet>(),
		                    bmap = new HashMap<String, BitSet>();
		result.add(amap);
		result.add(bmap);
		for (int i = 0; i < invariants.size(); i++) {
			BitSet aset = amap.get(invariants.get(i).getFirst());
			BitSet bset = bmap.get(invariants.get(i).getSecond());
			if (aset == null) aset = new BitSet();
			if (bset == null) bset = new BitSet();
			aset.set(i);
			bset.set(i);
		}
		return result;
	}
}
