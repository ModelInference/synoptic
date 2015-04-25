package synoptic.invariants.constraints;

import synoptic.util.resource.AbstractResource;

/**
 * Represents a resource threshold constraint on some resource invariant.
 */
public interface IResourceConstraint<E extends Comparable<E>> {
    /**
     * @return resource constraint
     */
    AbstractResource getThreshold();

    /**
     * @return true if given resource satisfies internal inequality with respect
     *         to the threshold.
     */
    boolean evaluate(AbstractResource t);
}
