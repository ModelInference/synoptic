package synoptic.invariants.constraints;

import synoptic.util.time.ITime;

/**
 * An inequality constraint that evaluates to true if the given time t is
 * greater than the internal time threshold.
 */
public class LowerBoundConstraint implements IThresholdConstraint {
    private ITime bound;

    public LowerBoundConstraint(ITime bound) {
        this.bound = bound;
    }

    @Override
    public ITime getThreshold() {
        return bound;
    }

    @Override
    public boolean evaluate(ITime t) {
        return !(this.bound.lessThan(t));
    }
}
