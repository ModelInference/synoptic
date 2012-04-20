package synoptic.invariants;

import synoptic.util.time.ITime;

public class LessThan implements IInequality {
	private ITime time;
	
	public LessThan(ITime constraint) {
		this.time = constraint;
	}
	
	@Override
	public ITime getConstraint() {
		return time;
	}
	
	@Override
	public boolean isSatisfyConstraint(ITime t) {
		return this.time.lessThan(t);
	}
	
}
