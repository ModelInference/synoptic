package synoptic.invariants;

import synoptic.util.time.ITime;

public interface IInequality {
	/**
	 * @return time constraint
	 */
	ITime getConstraint();
	
	/**
	 * @return true if given time satisfies inequality
	 */
	boolean isSatisfyConstraint(ITime t);
	
}
