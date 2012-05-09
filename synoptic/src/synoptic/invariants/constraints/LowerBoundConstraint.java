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
         
         LowerBoundConstraint other = (LowerBoundConstraint) obj;
         return bound.equals(other.getThreshold());
    }
    
    @Override
    public String toString() {
    	return "lowerbound = " + bound.toString();
    }
}
