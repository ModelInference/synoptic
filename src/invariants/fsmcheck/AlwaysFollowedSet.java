package invariants.fsmcheck;

import invariants.BinaryInvariant;

import java.util.BitSet;
import java.util.List;

// Indicates that A is always followed by B.
public class AlwaysFollowedSet extends StateSet<String> {
	public AlwaysFollowedSet(int size) {
		super(size);
	}
	public AlwaysFollowedSet(List<BinaryInvariant> invariants) {
		super(invariants.size());
		addState(true);		// State 1: Accept state
		addState(false);    // State 2: Fail state
		StateSet.addBinaryInvariants(this, invariants);
	}
	
	public BitSet isFail() { return (BitSet)sets.get(1).clone(); }
	public BitSet isPermanentFail() { return new BitSet(); }
	
	public void next(String x) {
		/*
		 * 1 -a-> 2
		 * 1 -b-> 1
		 * 2 -a-> 2
		 * 2 -b-> 1
		 * 
		 * neither = !(isA | isB)
		 * 
		 * s1 = (s1 & neither) | isB
		 * s2 = (s2 & neither) | isA 
		 */
		
		BitSet isA = inputBits(x, 0), isB = inputBits(x, 1),
		       s1 = sets.get(0), s2 = sets.get(1);
		
		BitSet neither = (BitSet) isA.clone();
		neither.or(isB);
		StateSet.flip(neither);
		
		s1.and(neither);
		s1.or(isB);
		
		s2.and(neither);
		s2.or(isA);
	}
}