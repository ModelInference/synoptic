package dynoptic.invariants;

import dynoptic.model.alphabet.EventType;

/**
 * A Dynoptic representation a binary temporal invariant, inv(first,second),
 * where first/second are the related event types. An invariant also includes
 * synthetic event types that are solely used to track and specify when the
 * first/second events occur during model checking.
 */
abstract public class BinaryInvariant {
    // The two event types that are related by this binary invariant.
    protected EventType first;
    protected EventType second;

    // Synthetic events used for invariant checking.
    protected EventType firstSynth1, firstSynth2;
    protected EventType secondSynth1, secondSynth2;

    // A string such as "AFby", "NFby", or "AP".
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

    // //////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        return "(" + first + ") " + connectorStr + " (" + second + ")";
    }

    @Override
    public int hashCode() {
        int ret = 17;
        ret = 31 * ret + first.hashCode();
        ret = 31 * ret + second.hashCode();
        ret = 31 * ret + connectorStr.hashCode();
        return ret;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (this == other) {
            return true;
        }
        if (!(other instanceof BinaryInvariant)) {
            return false;

        }
        BinaryInvariant otherInv = (BinaryInvariant) other;
        if (!otherInv.getFirst().equals(first)) {
            return false;
        }

        if (!otherInv.getSecond().equals(second)) {
            return false;
        }

        if (!otherInv.connectorStr.equals(connectorStr)) {
            return false;
        }
        return true;
    }

    // //////////////////////////////////////////////////////////////////

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
