package dynoptic.invariants;

import synoptic.model.event.DistEventType;

/**
 * Represents an "X eventually happens" invariant type, which corresponds to the
 * "INITIAL AFby X" Synoptic invariant type. We consider this invariant as an
 * AFby invariant.
 */
public class EventuallyHappens extends AlwaysFollowedBy {
    public EventuallyHappens(DistEventType event) {
        super(DistEventType.INITIALEventType, event);
    }
}
