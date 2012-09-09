package dynoptic.invariants;

import dynoptic.model.alphabet.EventType;

/**
 * Represents an "X eventually happens" invariant type, which corresponds to the
 * "INITIAL AFby X" Synoptic invariant type. We consider this invariant as an
 * AFby invariant.
 */
public class EventuallyHappens extends AlwaysFollowedBy {
    public EventuallyHappens(EventType event) {
        super(EventType.INITIALEventType, event);
    }
}
