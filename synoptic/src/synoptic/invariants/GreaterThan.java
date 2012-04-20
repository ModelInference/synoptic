package synoptic.invariants;

import synoptic.model.Partition;
import synoptic.model.Transition;
import synoptic.util.time.ITime;

public class GreaterThan implements IInequality {
	private ITime time;
	
	public GreaterThan(ITime constraint) {
		time = constraint;
	}
	
	public boolean isSatisfyConstraint() {
		return false;
	}
	
	/**
	 * @return time constraint
	 */
	@Override
	public ITime getConstraint() {
		return time;
	}
}
