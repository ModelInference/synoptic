package invariants.fsmcheck;

import java.util.BitSet;
import java.util.List;

/**
 * Represents the "A never followed by B" FSM
 */
public class NeverFollowedSet extends StateSet {	
	public NeverFollowedSet(int size) {
		super(size);
		addState(true);		// State 1: Accept state (no A seen, maybe B seen)
		addState(false);    // State 2: Accept state (A seen)
		addState(false);    // State 3: Failure state (B seen after A)
	}
	
	// State 3 indicates failure, failure is permanent.
	public BitSet isFail() { return (BitSet)sets.get(2).clone(); }
	public BitSet isPermanentFail() { return (BitSet)sets.get(2).clone(); }
	
	public void transition(List<BitSet> inputs) {
		/*
		 * state 1 (no A seen, maybe some B seen) : accepting state
		 * state 2 (A seen) : accepting states
		 * state 3 (B after A seen) : failing state
		 * 1 -a-> 2
		 * 1 -b-> 1 // TODO: shouldn't this be 1 - (all except a) -> 1 ?
		 * 2 -a-> 2 // TODO: shouldn't this be 2 - (all except b) -> 2 ?
		 * 2 -b-> 3
		 * 
		 * s1 = s1 & (n | isB)
		 * s2 = (s2 & n) | (!s3 & isA)
		 * s3 = s3 | (s2 & isB)
		 */

		// isA is cloned so that it can be mutated.
		BitSet isA = (BitSet)inputs.get(0).clone(), isB = inputs.get(1),
		       neither = nor(isA, isB, count),
		       s1 = sets.get(0), s2 = sets.get(1), s3 = sets.get(2);

        //                      var = expression in terms of original values
		
		BitSet t = (BitSet)s2.clone();
		t.and(isB);          // t   = s2 & isB
		s3.or(t);            // s3  = s3 | (s2 & isB)
		
		neither.or(isB);     // n   = n | isB
		s1.and(neither);     // s1  = s1 & (n | isB)
		
		s2.and(neither);     // s2  = s2 & n
		isA.andNot(s3);      // isA = !s3 & isA
		s2.or(isA);          // s2  = (s2 & n) | (!s3 & isA)
		
	}
}