package synoptic.invariants.constraints;

import synoptic.util.time.ITime;

/**
 * Represents a time threshold constraint on some temporal invariant.
 *
 * A constrained invariant is defined as follows:
 * <pre>
 *  Whenever an event a_i is encountered in trace t:
 *
 *      1. a_i is followed by at least one c_i before TERMINAL.
 *
 *      2. Either one of the following is true:
 *          a.) The first c_i, c_j, follows a_i in at most n time.
 *          b.) The first c_i, c_j, follows a_i in at leas n time.
 * </pre>
 */
public interface IThresholdConstraint {
    /**
     * @return time constraint
     */
    ITime getThreshold();

    /**
     * @return true if given time satisfies internal inequality with respect to
     *         the threshold.
     */
    boolean evaluate(ITime t);

}
