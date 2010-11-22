package invariants.fsmcheck;

import java.util.BitSet;
import java.util.List;

// A never followed by B
public class NeverFollowedSet extends StateSet {	
	public NeverFollowedSet(int size) {
		super(size);
		addState(true);		// State 1: Accept state (no A seen, maybe B seen)
		addState(false);    // State 2: Accept state (A seen)
		addState(false);    // State 3: Accept state (B seen after A)
	}
	
	// State 3 indicates failure, failure is permanent.
	public BitSet isFail() { return (BitSet)sets.get(2).clone(); }
	public BitSet isPermanentFail() { return (BitSet)sets.get(2).clone(); }
	
	public void transition(List<BitSet> inputs) {
		/*
		 * state 1 (no A seen, maybe some B seen), 2 (A seen) are accepting states
		 * state 3 (B after A seen) fail state
		 * 1 -a-> 2
		 * 1 -b-> 1
		 * 2 -a-> 2
		 * 2 -b-> 3
		 * 
		 * s1 = s1 & (n | isB)
		 * s2 = (s2 & n) | (!s3 & isA)
		 * s3 = s3 | (s2 & isB)
		 */
		BitSet isA = inputs.get(0), isB = inputs.get(1),
		       s1 = sets.get(0), s2 = sets.get(1), s3 = sets.get(2);

		BitSet neither = (BitSet)isA.clone();
		neither.or(isB); 
		StateSet.flip(neither);
		
        // name = expression in terms of original values
		
		BitSet t = (BitSet)s2.clone();
		t.and(isB);          // t = s2 & isB
		s3.or(t);            // s3 = s3 | (s2 & isB)
		
		s2.and(neither);     // s2 = s2 & n
		StateSet.flip(isA);  // isA = !isA
		isA.or(s3);          // isA = s3 | !isA
		StateSet.flip(isA);  // isA = !(s3 | !isA)
		s2.or(isA);          // s2 = (s2 & n) | !(s3 | !isA)
		
		neither.or(isB);     // n = n | isB
		s1.and(neither);     // s1 = s1 & (n | isB)
	}
}