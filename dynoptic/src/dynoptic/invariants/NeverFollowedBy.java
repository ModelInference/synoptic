package dynoptic.invariants;

import dynoptic.model.alphabet.EventType;

public class NeverFollowedBy extends BinaryInvariant {

    public NeverFollowedBy(EventType typeFirst, EventType typeSecond) {
        super(typeFirst, typeSecond, "NFby");
    }

    @Override
    public String scmBadStateQRe() {
        super.scmBadStateQRe();

        // There is an 'a', preceded by any number of 'a' or 'b', but which is
        // followed by at least one 'b'. This last 'b' can be intermingled in
        // any way with 'a' and other 'b' instances.
        return someSynthEventsQRe() + " . " + firstSynthEventsQRe() + " . "
                + someSynthEventsQRe() + " . " + secondSynthEventsQRe() + " . "
                + someSynthEventsQRe();
    }
}
