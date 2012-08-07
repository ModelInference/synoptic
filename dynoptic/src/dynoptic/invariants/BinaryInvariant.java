package dynoptic.invariants;

import dynoptic.model.alphabet.EventType;
import dynoptic.model.alphabet.FSMAlphabet;

abstract public class BinaryInvariant {
    // The two event types that are related by this binary invariant.
    protected EventType first;
    protected EventType second;

    // The corresponding synthetic events used for invariant checking.
    protected EventType firstSynth;
    protected EventType secondSynth;

    // A string such as "AFby", or "NFby", or "AP".
    protected String connectorStr;

    public BinaryInvariant(EventType typeFirst, EventType typeSecond, String str) {
        assert typeSecond != null;
        assert typeFirst != null;

        first = typeFirst;
        second = typeSecond;

        // Initialize these to null -- use set*SynthTracer below to set these
        // appropriately.
        firstSynth = null;
        secondSynth = null;

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

    /** Sets the synthetic tracing event corresponding to first. */
    public void setFirstSynthTracer(EventType fSynth) {
        firstSynth = fSynth;
    }

    /** Sets the synthetic tracing event corresponding to second. */
    public void setSecondSynthTracer(EventType sSynth) {
        secondSynth = sSynth;
    }

    public String scmBadStateQRe(FSMAlphabet globalAlphabet) {
        assert globalAlphabet.contains(first);
        assert globalAlphabet.contains(second);
        assert firstSynth != null;
        assert secondSynth != null;

        return null;
    }
}
