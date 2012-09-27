package dynoptic.invariants;

import synoptic.model.event.DistEventType;

/**
 * Represents an "X eventually happens" invariant type, which corresponds to the
 * "INITIAL AFby X" Synoptic invariant type. We consider this invariant as an
 * AFby invariant for expedience.<br/>
 * TODO: refactor invariant class hierarchy.
 */
public class EventuallyHappens extends AlwaysFollowedBy {
    public EventuallyHappens(DistEventType event) {
        super(DistEventType.INITIALEventType, event);
    }

    public DistEventType getEvent() {
        return this.getSecond();
    }

    @Override
    public void checkInitialized() {
        // Override the initialization checking to just check for the first
        // synthetic events pair, since this invariant type does not use the
        // second pair.
        assert firstSynth1 != null;
        assert secondSynth1 != null;
    }

    @Override
    public String scmBadStateQRe() {
        // There are 0 occurrences of 'X'.
        return "_";
    }
}
