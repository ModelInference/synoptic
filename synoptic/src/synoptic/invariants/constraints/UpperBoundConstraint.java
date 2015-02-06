package synoptic.invariants.constraints;

import synoptic.util.resource.AbstractResource;

/**
 * An inequality constraint that evaluates to true if the given time t is less
 * than the internal time threshold.
 */
public class UpperBoundConstraint implements IThresholdConstraint {
    private AbstractResource bound;

    public UpperBoundConstraint(AbstractResource bound) {
        this.bound = bound;
    }

    @Override
    public AbstractResource getThreshold() {
        return bound;
    }

    @Override
    public boolean evaluate(AbstractResource t) {
        return this.bound.lessThan(t);
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
