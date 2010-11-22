package invariants.fsmcheck;

import java.util.BitSet;
import java.util.List;

// Indicates that A always precedes B.
public class AlwaysPrecedesSet extends StateSet {
	public AlwaysPrecedesSet(int size) {
		super(size);
		addState(true);		// State 1: Accept state (no A or B seen)
		addState(false);    // State 2: Accept state (A seen)
		addState(false);    // State 3: Accept state (B seen after A)
		addState(false);    // State 4: Fail state   (A seen after B)
	}

	// State 4 indicates failure, failure is permanent.
	public BitSet isFail() { return (BitSet)sets.get(3).clone(); }
	public BitSet isPermanentFail() { return (BitSet)sets.get(3).clone(); }
	
	public void transition(List<BitSet> inputs) {
		/*
		 *  1 -a-> 2
		 *  1 -b-> 4
		 *  2 -a-> 2
		 *  2 -b-> 3
		 *  3 -a-> 4
		 *  3 -b-> 3
		 *
		 * n = !(isA | isB)
		 *
		 * s1 = s1 & n
		 * s2 = (s2 & n) | ((s1 | s2) & isA)
		 * s3 = (s3 & n) | ((s2 | s3) & isB)
		 * s4 = s4 | (s1 & isB) | (s3 & isA)
		 * 
		 * Possibly useful:
		 * s2 = (s2 & !isB) | (s1 & isA)
		 * s3 = (s3 & !isA) | (s2 & isB)
		 */
		BitSet isA = inputs.get(0), isB = inputs.get(1),
		       s1 = sets.get(0), s2 = sets.get(1), s3 = sets.get(2), s4 = sets.get(3);
		
		// Temporary sets
		BitSet t1, t2;
		
		BitSet neither = (BitSet)isA.clone();
		neither.or(isB);
		StateSet.flip(neither);
		
		                   // name = expression in terms of original values
		
		t1 = (BitSet)s1.clone();
		t1.and(isB);         // t1 = s1 & isB
		t2 = (BitSet)s3.clone();
		t2.and(isA);         // t2 = s3 & isA
		s4.or(t1);           // s4 = s4 | (s1 & isB)
		s4.or(t2);           // s4 = s4 | (s1 & isB) | (s3 &isA)
		
		t1 = (BitSet)s3.clone();
		t1.or(s2);           // t1 = s2 | s3
		t1.and(isB);         // t1 = (s2 | s3) & isB
		s3.and(neither);     // s3 = s3 & n
		s3.or(t1);           // s3 = (s3 & n) | ((s2 | s3) & isB)
		
		t1 = (BitSet)s2.clone();
		t1.or(s1);           // t1 = s1 | s2
		t1.and(isA);         // t1 = (s1 | s2) & isA
		s2.and(neither);     // s2 = s2 & n
		s2.or(t1);           // s2 = (s2 & n) | ((s1 | s2) & isA)
		
		s1.and(neither);     // s1 = s1 & n
	}
}