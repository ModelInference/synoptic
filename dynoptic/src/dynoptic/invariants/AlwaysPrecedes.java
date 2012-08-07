package dynoptic.invariants;

import dynoptic.model.alphabet.EventType;
import dynoptic.model.alphabet.FSMAlphabet;

public class AlwaysPrecedes extends BinaryInvariant {

    public AlwaysPrecedes(EventType typeFirst, EventType typeSecond) {
        super(typeFirst, typeSecond, "AP");
    }

    @Override
    public String scmBadStateQRe(FSMAlphabet globalAlphabet) {
        super.scmBadStateQRe(globalAlphabet);

        // There is a 'b' that was never preceded by an 'a'.
        return globalAlphabet.anyEventExceptOneScmQRe(firstSynth) + "^* . "
                + secondSynth.getScmEventString() + " . "
                + globalAlphabet.anyEventScmQRe() + "^*";
    }
}
