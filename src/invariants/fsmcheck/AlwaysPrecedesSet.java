package invariants.fsmcheck;

import java.util.BitSet;
import java.util.List;

// Indicates that A always precedes B.
public class AlwaysPrecedesSet extends StateSet {
	public AlwaysPrecedesSet(int size) {
		super(size);
		addState(true);		// State 1: Accept state (no A or B seen)
		addState(false);    // State 2: Permanent accept state (A seen first)
		addState(false);    // State 3: Permanent fail state   (B seen first)
	}

	// State 4 indicates failure, failure is permanent.
	public BitSet isFail() { return (BitSet)sets.get(2).clone(); }
	public BitSet isPermanentFail() { return (BitSet)sets.get(2).clone(); }
	
	public void transition(List<BitSet> inputs) {
		/*
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