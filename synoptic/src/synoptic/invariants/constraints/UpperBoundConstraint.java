package synoptic.invariants.constraints;

import synoptic.util.time.ITime;

/**
 * An inequality constraint that evaluates to true if the given time t is less
 * than the internal time threshold.
 */
public class UpperBoundConstraint implements IThresholdConstraint {
    private ITime bound;

    public UpperBoundConstraint(ITime bound) {
        this.bound = bound;
    }

    @Override
    public ITime getThreshold() {
        return bound;
    }

    @Override
    public boolean evaluate(ITime t) {
        return this.bound.lessThan(t);
    }

    @Override
    public int hashCode() {
        return bound.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return bound.equals(obj);
    }
    
    @Override
    public String toString() {
    	return "upperbound = " + bound.toString();
    }
}
