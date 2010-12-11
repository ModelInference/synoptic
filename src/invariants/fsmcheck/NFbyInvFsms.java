package invariants.fsmcheck;

import java.util.BitSet;
import java.util.List;

/**
 * Represents a set of "A never followed by B" invariants to simulate.
 * 
 * This finite state machine enters a particular state (s2) when A is provided.
 * If we are in this state when a B is provided, then we enter into a failure
 * state.
 * 
 * @author Michael Sloan (mgsloan@gmail.com)
 *  
 * @see NFbyTracingSet
 * @see FsmStateSet
 */
public class NFbyInvFsms extends FsmStateSet {	
	public NFbyInvFsms(int size) {
		super(size);
		addState(true);		// State 1: Accept state (no A seen, maybe B seen)
		addState(false);    // State 2: Accept state (A seen)
		addState(false);    // State 3: Failure state (B seen after A)
	}
	
	// State 3 indicates failure, failure is permanent.
	@Override
	public BitSet isFail() { return (BitSet)sets.get(2).clone(); }
	@Override
	public BitSet isPermanentFail() { return (BitSet)sets.get(2).clone(); }

	@Override
	public void transition(List<BitSet> inputs) {
		/*
		 * state 1 (no A seen, maybe some B seen) : accepting state
		 * state 2 (A seen) : accepting states
		 * state 3 (B after A seen) : failing state
		 * 
		 * (non-a/b preserves state)
		 * 1 -a-> 2
		 * 1 -b-> 1
		 * 2 -a-> 2
		 * 2 -b-> 3
		 * 
		 * s1 = s1 & !isA
		 * s2 = (s1 & isA) | (s2 & !isB) 
 		 * s3 = s3 | (s2 & isB)
 		 */
		 
		// isA is cloned so that it can be mutated.
		BitSet isA = (BitSet)inputs.get(0).clone(),
			   isB = inputs.get(1),
 		       s1 = sets.get(0),
 		       s2 = sets.get(1),
 		       s3 = sets.get(2);
		 
		//                      var = expression in terms of original values
		
		BitSet t = (BitSet)s2.clone();
		t.and(isB);          // s3  = s2 & isB
		s3.or(t);            // s3  = s3 | (s2 & isB)
		
		s1.andNot(isA);      // s1  = s1 & !isA
		
		s2.andNot(isB);      // s2  = s2 & !isB
		isA.and(s1);         // isA = s1 & isA
		s2.or(isA);          // s2  = (s1 & isA) | (s2 & !isB)
	}
}