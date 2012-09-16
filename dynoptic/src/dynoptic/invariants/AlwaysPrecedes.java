package dynoptic.invariants;

import synoptic.model.event.DistEventType;

/** A Dynoptic representation of the AP invariant. */
public class AlwaysPrecedes extends BinaryInvariant {

    public AlwaysPrecedes(DistEventType typeFirst, DistEventType typeSecond) {
        super(typeFirst, typeSecond, "AP");
    }

    @Override
    public String scmBadStateQRe() {
        checkInitialized();

        // There is a 'b' that was never preceded by an 'a'. This 'b' is
        // followed by any number of a's or b's.
        return secondSynthEventsQRe() + "^+ . " + someSynthEventsQRe();
    }
}
