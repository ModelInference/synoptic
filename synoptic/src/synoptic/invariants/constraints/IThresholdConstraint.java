package synoptic.invariants.constraints;

import synoptic.util.resource.AbstractResource;

/**
 * Represents a time threshold constraint on some temporal invariant.
 *
 * 
 */
public interface IThresholdConstraint {
    /**
     * @return time constraint
     */
    AbstractResource getThreshold();

    /**
     * @return true if given time satisfies internal inequality with respect to
     *         the threshold.
     */
    boolean evaluate(AbstractResource t);

}
