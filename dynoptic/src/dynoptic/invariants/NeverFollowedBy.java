package dynoptic.invariants;

import synoptic.model.event.DistEventType;

/** A Dynoptic representation of the NFby invariant. */
public class NeverFollowedBy extends BinaryInvariant {

    public NeverFollowedBy(DistEventType typeFirst, DistEventType typeSecond) {
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
