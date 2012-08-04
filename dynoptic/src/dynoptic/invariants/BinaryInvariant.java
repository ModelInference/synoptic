package dynoptic.invariants;

import dynoptic.model.alphabet.EventType;
import dynoptic.model.alphabet.FSMAlphabet;

abstract public class BinaryInvariant {
    protected EventType first;
    protected EventType second;
    protected String str;

    public BinaryInvariant(EventType typeFirst, EventType typeSecond, String str) {
        first = typeFirst;
        second = typeSecond;
        this.str = str;
    }

    @Override
    public String toString() {
        return first + " " + str + " " + second;
    }

    public EventType getFirst() {
        return first;
    }

    public EventType getSecond() {
        return second;
    }

    abstract public String scmBadStateQRe(FSMAlphabet globalAlphabet);
}
