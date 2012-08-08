package dynoptic.invariants;

import dynoptic.model.alphabet.EventType;

abstract public class BinaryInvariant {
    // The two event types that are related by this binary invariant.
    protected EventType first;
    protected EventType second;

    // The corresponding synthetic events used for invariant checking.
    protected EventType firstSynth1, firstSynth2;
    protected EventType secondSynth1, secondSynth2;

    // A string such as "AFby", or "NFby", or "AP".
    protected String connectorStr;

    public BinaryInvariant(EventType typeFirst, EventType typeSecond, String str) {
        assert typeSecond != null;
        assert typeFirst != null;

        first = typeFirst;
        second = typeSecond;

        // Initialize these to null -- use set*SynthTracer below to set these
        // appropriately.
        firstSynth1 = null;
        firstSynth2 = null;
        secondSynth1 = null;
        secondSynth2 = null;

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
    public void setFirstSynthTracers(EventType fSynth1, EventType fSynth2) {
        firstSynth1 = fSynth1;
        firstSynth2 = fSynth2;
    }

    /** Sets the synthetic tracing event corresponding to second. */
    public void setSecondSynthTracers(EventType sSynth1, EventType sSynth2) {
        secondSynth1 = sSynth1;
        secondSynth2 = sSynth2;
    }

    /**
     * This method assumes that the channel alphabet is made up of just
     * secondSynth and firstSynth events.
     */
    public String scmBadStateQRe() {
        assert firstSynth1 != null;
        assert secondSynth1 != null;
        assert firstSynth2 != null;
        assert secondSynth2 != null;

        return null;
    }

    public String someSynthEventsQRe() {
        assert firstSynth1 != null;
        assert secondSynth1 != null;
        assert firstSynth2 != null;
        assert secondSynth2 != null;

        return "(" + firstSynth1.getScmEventString() + " | "
                + firstSynth2.getScmEventString() + " | "
                + secondSynth1.getScmEventString() + " | "
                + secondSynth2.getScmEventString() + ")^*";
    }

    public String firstSynthEventsQRe() {
        assert firstSynth1 != null;
        assert firstSynth2 != null;

        return "(" + firstSynth1.getScmEventString() + " . "
                + firstSynth2.getScmEventString() + ")";
    }

    public String secondSynthEventsQRe() {
        assert secondSynth1 != null;
        assert secondSynth2 != null;

        return "(" + secondSynth1.getScmEventString() + " . "
                + secondSynth2.getScmEventString() + ")";
    }
}
