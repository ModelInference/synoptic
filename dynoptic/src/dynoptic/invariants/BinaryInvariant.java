package dynoptic.invariants;

import java.util.List;

import dynoptic.invariants.checkers.BinChecker;

import synoptic.model.event.DistEventType;

/**
 * A Dynoptic representation a binary temporal invariant, inv(first,second),
 * where first/second are the related event types. An invariant also includes
 * synthetic event types that are solely used to track and specify when the
 * first/second events occur during model checking.
 */
abstract public class BinaryInvariant {
    // The two event types that are related by this binary invariant.
    protected DistEventType first;
    protected DistEventType second;

    // Synthetic events used for invariant checking.
    protected DistEventType firstSynth1, firstSynth2;
    protected DistEventType secondSynth1, secondSynth2;

    // A string such as "AFby", "NFby", or "AP".
    protected String connectorStr;

    public BinaryInvariant(DistEventType typeFirst, DistEventType typeSecond,
            String str) {
        assert typeSecond != null;
        assert typeFirst != null;
        assert str != null;

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

    /**
     * This method has to be override all subclasses. It's purpose is to return
     * an RE that encodes the queue bad states corresponding to the invariant.
     */
    abstract public String scmBadStateQRe();

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

    public DistEventType getFirst() {
        return first;
    }

    public DistEventType getSecond() {
        return second;
    }

    public String getConnectorString() {
        return connectorStr;
    }

    /** Sets the synthetic tracing event corresponding to first. */
    public void setFirstSynthTracers(DistEventType fSynth1,
            DistEventType fSynth2) {
        firstSynth1 = fSynth1;
        firstSynth2 = fSynth2;
    }

    /** Sets the synthetic tracing event corresponding to second. */
    public void setSecondSynthTracers(DistEventType sSynth1,
            DistEventType sSynth2) {
        secondSynth1 = sSynth1;
        secondSynth2 = sSynth2;
    }

    /**
     * Checks that the invariant has been properly initialized -- that all of
     * the synthetic events are initialized.
     */
    public void checkInitialized() {
        assert firstSynth1 != null;
        assert secondSynth1 != null;
        assert firstSynth2 != null;
        assert secondSynth2 != null;
    }

    /**
     * Returns an RE encoding an occurrence of any number of the synthetic
     * events, in arbitrary order.
     */
    public String someSynthEventsQRe() {
        checkInitialized();

        return "(" + firstSynth1.getScmEventString() + " | "
                + firstSynth2.getScmEventString() + " | "
                + secondSynth1.getScmEventString() + " | "
                + secondSynth2.getScmEventString() + ")^*";
    }

    /**
     * Returns an RE encoding exactly one occurrence of the _first_ pair of
     * synthetic events in the appropriate order (synth1, and then synth2).
     */
    public String firstSynthEventsQRe() {
        assert firstSynth1 != null;
        assert firstSynth2 != null;

        return "(" + firstSynth1.getScmEventString() + " . "
                + firstSynth2.getScmEventString() + ")";
    }

    /**
     * Returns an RE encoding exactly one occurrence of the _second_ pair of
     * synthetic events in the appropriate order (synth1, and then synth2).
     */
    public String secondSynthEventsQRe() {
        assert secondSynth1 != null;
        assert secondSynth2 != null;

        return "(" + secondSynth1.getScmEventString() + " . "
                + secondSynth2.getScmEventString() + ")";
    }

    /** Whether or not the passed eventsPath satisfied this invariant type. */
    public abstract boolean satisfies(List<DistEventType> eventsPath);

    /** Returns an invariant checker instance corresponding to this invariant. */
    public abstract BinChecker<?> newChecker();
}
