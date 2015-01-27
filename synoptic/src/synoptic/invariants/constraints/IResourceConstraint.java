package synoptic.invariants.constraints;

import synoptic.model.resource.IResource;

/**
 * Represents a resource threshold constraint on some resource invariant.
 */
public interface IResourceConstraint<E extends Comparable<E>> {
    /**
     * @return resource constraint
     */
    IResource<E> getThreshold();

    /**
     * @return true if given resource satisfies internal inequality with respect
     *         to the threshold.
     */
    boolean evaluate(IResource<E> t);
}
