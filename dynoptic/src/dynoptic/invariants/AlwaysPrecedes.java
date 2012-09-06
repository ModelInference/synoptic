package dynoptic.invariants;

import dynoptic.model.alphabet.EventType;

/** A Dynoptic representation of the AP invariant. */
public class AlwaysPrecedes extends BinaryInvariant {

    public AlwaysPrecedes(EventType typeFirst, EventType typeSecond) {
        super(typeFirst, typeSecond, "AP");
    }

    @Override
    public String scmBadStateQRe() {
        super.scmBadStateQRe();

        // There is a 'b' that was never preceded by an 'a'. This 'b' is
        // followed by any number of a's or b's.
        return secondSynthEventsQRe() + "^+ . " + someSynthEventsQRe();
    }
}
