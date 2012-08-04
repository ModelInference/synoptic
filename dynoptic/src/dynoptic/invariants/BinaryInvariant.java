package dynoptic.invariants;

import dynoptic.model.alphabet.EventType;
import dynoptic.model.alphabet.FSMAlphabet;

abstract public class BinaryInvariant {
    // The two event types that are related by this binary invariant.
    protected EventType first;
    protected EventType second;

    // A string such as "AFby", or "NFby", or "AP".
    protected String connectorStr;

    public BinaryInvariant(EventType typeFirst, EventType typeSecond, String str) {
        first = typeFirst;
        second = typeSecond;
        this.connectorStr = str;
    }

    @Override
    public String toString() {
        return "(" + first + ") " + connectorStr + " (" + second + ")";
    }

    public EventType getFirst() {
        return first;
    }

    public EventType getSecond() {
        return second;
    }

    public String scmBadStateQRe(FSMAlphabet globalAlphabet) {
        assert globalAlphabet.contains(first);
        assert globalAlphabet.contains(second);
        return null;
    }
}
