package dynoptic.invariants;

import dynoptic.model.alphabet.EventType;
import dynoptic.model.alphabet.FSMAlphabet;

public class AlwaysFollowedBy extends BinaryInvariant {

    public AlwaysFollowedBy(EventType typeFirst, EventType typeSecond) {
        super(typeFirst, typeSecond, "AFby");
    }

    @Override
    public String scmBadStateQRe(FSMAlphabet globalAlphabet) {
        // There is an 'a' that is not followed any time later by a 'b'.
        return globalAlphabet.anyEventScmQRe() + "^* . " + first
                + globalAlphabet.anyEventExceptOneScmQRe(second) + "^*";
    }
}
