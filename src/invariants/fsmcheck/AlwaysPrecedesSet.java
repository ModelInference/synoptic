package invariants.fsmcheck;

import java.util.BitSet;
import java.util.List;

/**
 * Represents a set of "A always precedes B" invariants to simulate.
 * 
 * This finite state machine enters a permanent success state upon encountering
 * A, and enters a permanent failure state upon encountering B.  This reflects
 * the fact that the first of the two events encountered is the only thing
 * relevant to the failure state of the invariant.
 * 
 * @author Michael Sloan (mgsloan@gmail.com)
 * 
 * @see AlwaysPrecedesTracingSet
 * @see StateSet
 */
public class AlwaysPrecedesSet extends StateSet {
	public AlwaysPrecedesSet(int size) {
		super(size);
		addState(true);		// State 1: Accept state (no A or B seen)
		addState(false);    // State 2: Permanent accept state (A seen first)
		addState(false);    // State 3: Permanent fail state   (B seen first)
	}

	// State 4 indicates failure, failure is permanent.
	@Override
	public BitSet isFail() { return (BitSet)sets.get(2).clone(); }
	@Override
	public BitSet isPermanentFail() { return (BitSet)sets.get(2).clone(); }
	
	@Override
	public void transition(List<BitSet> inputs) {
		/*
		 * (non-a/b preserves state)
		 *  1 -a-> 2
		 *  1 -b-> 3
		 *
		 * n = !(isA | isB)
		 *
		 * s1 = s1 & n
		 * s2 = s2 | (s1 & isA)
		 * s3 = s3 | (s1 & isB)
		 */
		
		// inputs cloned so that they can be mutated.
		BitSet isA = (BitSet)inputs.get(0).clone(), isB = (BitSet)inputs.get(1).clone(),
		       neither = nor(isA, isB, count),
		       s1 = sets.get(0), s2 = sets.get(1), s3 = sets.get(2);
		
		s1.and(neither);
		
		isA.and(s1);
		s2.or(isA);
		
		isB.and(s1);
		s3.or(isB);
	}
}