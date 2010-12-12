package invariants.fsmcheck;

import invariants.BinaryInvariant;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>Abstract class to provide functionality for simulating nondeterministic
 * finite state machines, utilizing BitSets as a method for evaluating the
 * transitions of many instances in parallel.</p>
 * 
 * <p>Note the "nondeterministic".  This means that each machine can be in
 * multiple states at once.  This allows us to associate with each node in
 * the model a set of states which can be inhabited (by some path from an
 * initial node).</p>
 * 
 * <p>If you imagine the BitSets as creating a matrix, where each row is a set,
 * then each row corresponds to a state of the machine, and each column
 * corresponds to an individual state machine.  1 in this matrix indicates that
 * that particular state is active in that particular machine.</p>
 * 
 * <p>The input which drives the transition of the state machines consists of a
 * list of BitSets, each corresponding to a logical input of the machine. With
 * all of the current implementations of this interface (AlwaysFollowedSet,
 * AlwaysPrecedesSet, NeverFollowedSet), there are two inputs, as these are
 * binary invariants.  In other words, each individual machine being simulated
 * is watching for just two events.  1 in the nth bit of the first input
 * indicates that the first event that the nth invariant is watching for
 * ("A" in A afby B) is being used as the input for the transition.</p>
 * 
 * <p>Unless an invariant is watching for the same event on each of its inputs,
 * the inputs can be thought of as a one-hot encoding: input[0] & input[1] = 0.
 * The only time this doesn't happen in practice is "A NFby A", which indicates
 * that A is singleton in the traces.  Furthermore, if all input bits are 0,
 * then the corresponding machines should not change state.</p>
 * 
 * @author Michael Sloan (mgsloan@gmail.com)
 * 
 * @see AFbyInvFsms
 * @see APInvFsms
 * @see NFbyInvFsms
 */
public abstract class FsmStateSet {
	protected List<BitSet> sets;
	protected int count;
	
	/**
	 * Initializes the data structures, taking the number of instances of
	 * the machines to simulate in parallel.
	 * 
	 * @param numSimulators
	 */
	protected FsmStateSet(int numSimulators) {
		this.count = numSimulators;
		// Initial capacity set to 4 as none of our current inheritors exceed.
		sets = new ArrayList<BitSet>(4);
	}
	
	/**
	 * Mutates the stateset, to reflect the states which may be inhabited after
	 * processing the input.
	 * 
	 * @param inputs The input bitsets to provide for transitioning the FSMs
	 */
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
	public void mergeWith(FsmStateSet other) {
		assert(sets.size() == other.sets.size());
		assert(count == other.count);
		for (int i = 0; i < sets.size(); i++) {
			sets.get(i).or(other.sets.get(i));
		}
	}
	
	/**
	 * Checks if one StateSet's inhabited states is a subset of another.
	 * In other words, if every onebit in this StateSet has a corresponding
	 * onebit in the other bitset, then it is a subset.  This query is used
	 * for determining when a particular state transition propagation changes
	 * the state at a node.  This allows the graph to arrive at a fixpoint. 
	 * 
	 * @param other The other set.
	 * @return Returns true if the otherset is a superset.
	 */
	public boolean isSubset(FsmStateSet other) {
		if (other == null) return false;
		if (sets.size() != other.sets.size()) return false;
		for (int j = 0; j < sets.size(); j++) {
			BitSet thisSet = sets.get(j);
			BitSet s = (BitSet) thisSet.clone();
			s.and(other.sets.get(j));
			s.xor(thisSet);     // (intersection != this) == 0 for subset
			if (!s.isEmpty()) return false;
		}
		return true;
	}
	
	/**
	 * Clones this set of states, using reflection to ascertain the actual type
	 * to construct.
	 * 
	 * @see java.lang.Object#clone()
	 */
	public FsmStateSet clone() {
		FsmStateSet result;
		try {
			result = (FsmStateSet)this.getClass().getConstructors()[0].newInstance(sets.size());
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
	
	/**
	 * Helper to perform nor, for (neither = input[0] nor input[1])
	 * A B result
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

	/* Helpers for dealing with mapping from some event representation to BitSets,
	 * which indicate which boolean inputs to provide to each of the machines which
	 * are being simulated.
	 */
	
	public static final BitSet zero = new BitSet();

	/**
	 * Helper function to visit an event, giving a mapping from it to inputs.
	 * 
	 * @param mappings A list of mapping from event to bitset, one for each input.
	 * @param event The event to visit.
	 */
	public <T> void visit(List<Map<T, BitSet>> mappings, T event) {
		List<BitSet> inputs = new ArrayList<BitSet>();
		for (Map<T, BitSet> mapping : mappings) {
			BitSet input = mapping.get(event);
			inputs.add(input == null ? zero : input);
		}
		transition(inputs);
	}
	
	/**
	 * Converts a list of binary invariants, presumably used to construct a
	 * stateset simulation of the invariants, to the mapping used for converting
	 * from events to input bitsets.  (see visit, above)
	 * 
	 * @param invariants A list of binary invariants to convert to a list of
	 *     mappings from 
	 * @return A list of two mappings from node labels to the first and second
	 *     inputs, respectively, of the invariants in the list.
	 */
	public static List<Map<String, BitSet>> getInvEventFsmDeps (List<BinaryInvariant> invariants) {
		List<Map<String, BitSet>> result = new ArrayList<Map<String, BitSet>>(2);
		Map<String, BitSet> amap = new HashMap<String, BitSet>(),
		                    bmap = new HashMap<String, BitSet>();
		result.add(amap);
		result.add(bmap);
		for (int i = 0; i < invariants.size(); i++) {
			String first = invariants.get(i).getFirst();
			String second = invariants.get(i).getSecond();
			BitSet aset = amap.get(first);
			BitSet bset = bmap.get(second);
			if (aset == null) amap.put(first,  aset = new BitSet());
			if (bset == null) bmap.put(second, bset = new BitSet());
			aset.set(i);
			bset.set(i);
		}
		return result;
	}
}
