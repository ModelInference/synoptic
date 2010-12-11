package invariants.fsmcheck;

import java.util.BitSet;
import java.util.List;

/**
 * Represents a set of "A always followed by B" invariants to simulate.
 * 
 * This finite state machine enters a failure state when A is encountered,
 * and enters a success state when B is encountered.  This means that the
 * failure state upon encountering a final node indicates which, of A
 * and B, was last encountered.
 * 
 * NOTE: ensure this documentation stays consistent with AlwaysFollowedTracingSet.
 * 
 * @author Michael Sloan (mgsloan@gmail.com)
 * 
 * @see AFbyTracingSet
 * @see FsmStateSet
 */
public class AFbyInvFsms extends FsmStateSet {
	public AFbyInvFsms(int size) {
		super(size);
		addState(true);		// State 1: Accept state
		addState(false);    // State 2: Fail state
	}

	@Override
	public BitSet isFail() { return (BitSet)sets.get(1).clone(); }

	@Override
	public BitSet isPermanentFail() { return new BitSet(); }

	@Override
	public void transition(List<BitSet> inputs) {
		/*
		 * (non-a/b preserves state)
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
		
		BitSet isA = inputs.get(0),
			   isB = inputs.get(1),
		       neither = nor(isA, isB, count),
		       s1 = sets.get(0),
		       s2 = sets.get(1);
		
		s1.and(neither);
		s1.or(isB);
		
		s2.and(neither);
		s2.or(isA);
	}
}