package dynoptic.invariants;

import dynoptic.model.alphabet.EventType;

public class AlwaysFollowedBy extends BinaryInvariant {

    public AlwaysFollowedBy(EventType typeFirst, EventType typeSecond) {
        super(typeFirst, typeSecond, "AFby");
    }

    @Override
    public String scmBadStateQRe() {
        super.scmBadStateQRe();

        // There is an 'a' that is preceded by 'a' or 'b' but is not followed
        // any later by any 'b', though it might be followed by more a's.
        return someSynthEventsQRe() + " . " + firstSynthEventsQRe() + "^+";
    }
}
