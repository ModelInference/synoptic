package dynoptic.invariants;

import dynoptic.model.alphabet.EventType;
import dynoptic.model.alphabet.FSMAlphabet;

public class NeverFollowedBy extends BinaryInvariant {

    public NeverFollowedBy(EventType typeFirst, EventType typeSecond) {
        super(typeFirst, typeSecond, "NFby");
    }

    @Override
    public String scmBadStateQRe(FSMAlphabet globalAlphabet) {
        // There is an 'a' that is followed by a 'b'.
        return globalAlphabet.anyEventScmQRe() + "^* . " + first + " . "
                + globalAlphabet.anyEventScmQRe() + "^* . " + second + " . "
                + globalAlphabet.anyEventScmQRe() + "^*";
    }
}
