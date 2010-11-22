package invariants.fsmcheck;

import invariants.BinaryInvariant;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class StateSet<T> {
	protected List<BitSet> sets;
	protected int count;
	protected List<Map<T, BitSet>> inputCache;
	protected List<Map<T, Integer>> inputMaps;
	
	public StateSet(int n) {
		this.count = n;
		sets = new ArrayList<BitSet>(4);
		inputMaps = new ArrayList<Map<T, Integer>>(2);
		inputCache = new ArrayList<Map<T, BitSet>>(2);
	}
	
	protected void addState(boolean initialValue) {
		BitSet newState = new BitSet(count);
		newState.set(0, count, initialValue);
		sets.add(newState);
	}
	
	protected void addInput(Map<T, Integer> m) {
		inputMaps.add(m);
		inputCache.add(new HashMap<T, BitSet>());
	}
	
	public static void addBinaryInvariants(StateSet<String> set, List<BinaryInvariant> invariants) {
		Map<String, Integer> amap = new HashMap<String, Integer>(),
		                     bmap = new HashMap<String, Integer>();
		for (int i = 0; i < invariants.size(); i++) {
			amap.put(invariants.get(i).getFirst(), i);
			bmap.put(invariants.get(i).getSecond(), i);
		}
		set.addInput(amap);
		set.addInput(bmap);
	}
	
	public void mergeWith(StateSet<T> other) {
		assert(sets.size() == other.sets.size());
		assert(count == other.count);
		for (int i = 0; i < sets.size(); i++) {
			sets.get(i).or(other.sets.get(i));
		}
	}
	
	@SuppressWarnings("unchecked")
	public StateSet<T> clone() {

		StateSet<T> result;
		try {
			result = (StateSet<T>)this.getClass().getConstructors()[0].newInstance(sets.size());
		} catch (Exception e) {
			System.out.println("ERROR: Failed to clone stateset.");
			return null;
		}
		result.count = count;
		result.inputMaps = inputMaps;
		result.inputCache = inputCache;
		ArrayList<BitSet> newSets = new ArrayList<BitSet>();
		for (int i = 0; i < sets.size(); i++) {
			newSets.add((BitSet)sets.get(i).clone());
		}
		result.sets = newSets;
		return result;
	}
	
	public static zero = new BitSet();
	
	public BitSet inputBits(T input, int ix) {
		Map<T, BitSet> cacheMap = this.inputCache.get(ix); 
		BitSet result = cacheMap.get(input);
		if (result == null) {
			Integer index = inputMaps.get(ix).get(input);
			if (index != null) {
			    // Only cache non-zero sets
				result = new BitSet();
				result.set(index);
				cacheMap.put(input, result);  
			} else {
				result = zero;
			}
		}
		return result;
	}
	
	public abstract void next(T x);
	
	// At the "end" of evaluation, indicates if 
	public abstract BitSet isFail();
	// Indicates if failstate is known to be permanent.
	public abstract BitSet isPermanentFail();
	
	
	public static void flip(BitSet o) { o.flip(0, o.size()); }
}
