package synoptic.invariants.constraints;

import synoptic.util.time.ITime;

/**
 * An inequality constraint that evaluates to true if the given time t is less
 * than or equal to the internal time threshold.
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
        // If bound >= t return true, else return false.
        // == If bound < t return false, else return true.
        return !this.bound.lessThan(t);
    }

    @Override
    public int hashCode() {
        return bound.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        UpperBoundConstraint other = (UpperBoundConstraint) obj;
        return bound.equals(other.getThreshold());
    }

    @Override
    public String toString() {
        return "upperbound = " + bound.toString();
    }
}
