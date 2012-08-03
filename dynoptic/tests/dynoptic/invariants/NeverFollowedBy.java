package dynoptic.invariants;

import dynoptic.model.alphabet.EventType;

public class NeverFollowedBy extends BinaryInvariant {

    public NeverFollowedBy(EventType typeFirst, EventType typeSecond) {
        super(typeFirst, typeSecond, "NFby");
    }

}
