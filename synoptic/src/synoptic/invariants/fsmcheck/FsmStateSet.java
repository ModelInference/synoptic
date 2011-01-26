package synoptic.invariants.fsmcheck;


import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import synoptic.invariants.BinaryInvariant;
import synoptic.model.interfaces.INode;


/**
 * <p>Abstract class to provide functionality for simulating nondeterministic
 * finite state machines, utilizing BitSets as a method for evaluating the
 * transitions of many instances in parallel.</p>
 * 
 * <p>Note the "nondeterministic".  This means that each machine can be in
 * multiple states at once.  This allows us to associate with each node in
 * the synoptic.model a set of states which can be inhabited (by some path from an
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
 * binary synoptic.invariants.  In other words, each individual machine being simulated
 * is watching for just two events.  1 in the nth bit of the first input
 * indicates that the first event that the nth invariant is watching for
 * ("A" in A afby B) is being used as the input for the transition.</p>
 * 
 * <p>Unless an invariant is watching for the same event on each of its inputs,
 * the inputs can be thought of as a one-hot encoding: input[0] & input[1] = 0.
 * The only time this doesn't happen in practice is "A NFby A", which indicates
 * that A is a singleton in the traces.  Furthermore, if all input bits are 0,
 * then the corresponding machines should not change state.</p>
 * 
 * @author Michael Sloan (mgsloan@gmail.com)
 * 
 * @see AFbyInvFsms
 * @see APInvFsms
 * @see NFbyInvFsms
 */
public abstract class FsmStateSet<T extends INode<T>> implements IStateSet<T, FsmStateSet<T>> {
	protected List<BitSet> sets;
	protected int count;
	public List<Map<String, BitSet>> mappings;  // public for testing.
	
	/**
	 * Initializes the bitsets, taking the number of instances of the machines
	 * to simulate in parallel.
	 * 
	 * TODO: necessary?
	 * 
	 * @param numSimulators Number of machines to simulate in parallel.
	 */
	protected FsmStateSet(int numSimulators, int numStates) {
		this.count = numSimulators;
		sets = new ArrayList<BitSet>(numStates);
		for (int i = 0; i < numStates; i++)
		    sets.add(new BitSet());
	}
	
	/**
	 * Initializes the bitsets, and assigns the input mapping, based on the
	 * passed synoptic.invariants.  NOTE: this assumes that all of the passed synoptic.invariants
	 * are of the appropriate type.
	 */
	protected FsmStateSet(List<BinaryInvariant> invariants, int numStates) {
		this(invariants.size(), numStates);
		
		mappings = new ArrayList<Map<String, BitSet>>(2);
		Map<String, BitSet> amap = new HashMap<String, BitSet>(),
		                    bmap = new HashMap<String, BitSet>();
		mappings.add(amap);
		mappings.add(bmap);
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
	}

	/**
	 * At final states (partitions which contain ending nodes of some sample
	 * traces), this indicates which of the synoptic.invariants maintained by this
	 * stateset would be considered to not be satisfied.
	 */
	public abstract BitSet whichFail();
	
	/**
	 * When machines are permanently locked in a failure state, i.e. no recovery
	 * is possible, that fact is indicated as a 1 in this bitset. This can be
	 * used to preemptively halt search.
	 */
	public abstract BitSet whichPermanentFail();
	
	/**
	 * Merges this stateset with another, by ORing all of the state vectors.
	 * Exceptions are thrown if
	 * 	- statesets have different sizes
	 * 	- statesets have different number of parallel instances
	 *
	 * @param other
	 */
	public void mergeWith(FsmStateSet<T> other) {
		assert(other.mappings == mappings);
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
	public boolean isSubset(FsmStateSet<T> other) {
		if (other == null) return false;
		assert(other.mappings == mappings);
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
	@SuppressWarnings("unchecked")
	public FsmStateSet<T> copy() {
		FsmStateSet<T> result;
		try {
			Constructor cons = null;
			for (Constructor c : this.getClass().getConstructors()) {
				Class[] params = c.getParameterTypes();
				if (params.length == 1  && params[0].toString().equals("int")) cons = c;
			}
			result = (FsmStateSet<T>)cons.newInstance(sets.size());
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
		result.mappings = mappings;
		return result;
	}
	
	public BitSet getInput(int ix, T input) {
		BitSet result = mappings.get(ix).get(input.getLabel());
		if (result == null) return new BitSet();
		return result;
	}
	
	public BitSet getInputCopy(int ix, T input) {
		String label = input.getLabel();
		BitSet result = mappings.get(ix).get(label);
		if (result == null) return new BitSet();
		return (BitSet)result.clone();
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
}
