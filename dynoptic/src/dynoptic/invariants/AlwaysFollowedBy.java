package dynoptic.invariants;

import synoptic.model.event.DistEventType;

/** A Dynoptic representation of the AFby invariant. */
public class AlwaysFollowedBy extends BinaryInvariant {

    public AlwaysFollowedBy(DistEventType typeFirst, DistEventType typeSecond) {
        super(typeFirst, typeSecond, "AFby");
    }

    @Override
    public String scmBadStateQRe() {
        checkInitialized();

        // There is an 'a' that is preceded by 'a' or 'b' but is not followed
        // any later by any 'b', though it might be followed by more a's.
        return someSynthEventsQRe() + " . " + firstSynthEventsQRe() + "^+";
    }
}
