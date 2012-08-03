package dynoptic.invariants;

import dynoptic.model.alphabet.EventType;

public class AlwaysPrecedes extends BinaryInvariant {

    public AlwaysPrecedes(EventType typeFirst, EventType typeSecond) {
        super(typeFirst, typeSecond, "AP");
    }

}
