package synoptic.invariants;

import synoptic.util.time.ITime;

public interface IInequality {
	
	ITime getConstraint();
	boolean isSatisfyConstraint();
	
}
