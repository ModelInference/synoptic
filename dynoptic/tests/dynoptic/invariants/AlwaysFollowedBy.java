package dynoptic.invariants;

import dynoptic.model.alphabet.EventType;

public class AlwaysFollowedBy extends BinaryInvariant {

    public AlwaysFollowedBy(EventType typeFirst, EventType typeSecond) {
        super(typeFirst, typeSecond, "AFby");
    }

}
