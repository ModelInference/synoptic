package synoptic.invariants.constraints;

import synoptic.model.resource.Resource;

/**
 * Represents a resource threshold constraint on some resource invariant.
 */
public interface IResourceConstraint {
    /**
     * @return resource constraint
     */
    Resource<?> getThreshold();

    /**
     * @return true if given resource satisfies internal inequality with respect
     *         to the threshold.
     */
    boolean evaluate(Resource<?> t);
}
