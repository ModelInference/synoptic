package dynoptic.invariants.checkers;

import dynoptic.invariants.BinaryInvariant;

import synoptic.model.event.DistEventType;

/**
 * Factory of binary invariant checkers, as well as the base class for all of
 * these checkers.
 */
public abstract class BinChecker<State> {

    public enum Validity {
        // Temporarily failing -- failed if at end of trace, but might
        // recover/succeed if trace is incomplete.
        TEMP_FAIL,

        // Temporarily successful -- successful if at end of trace, but might
        // fail if trace is incomplete.
        TEMP_SUCCESS,

        // (optimization) Permanently failing -- no reason to continue checking
        // the invariant.
        PERM_FAIL,

        // (optimization) Permanently successful -- no reason to continue
        // checking the invariant.
        PERM_SUCCESS,
    }

    // ////////////////////////////////////////////////////////////////

    /** The invariant that this checker corresponds to. */
    protected final BinaryInvariant inv;

    /** Represents the state of this invariant checker instance. */
    protected State s;

    public BinChecker(BinaryInvariant inv, State initS) {
        this.inv = inv;
        this.s = initS;
    }

    /** Updates the state of this checker with state of otherChecker */
    // public <InvChecker extends BinChecker<State>> void inheritState(
    public void inheritState(BinChecker<State> otherChecker) {
        assert otherChecker.s.getClass() == this.s.getClass();
        this.s = otherChecker.s;
    }

    // ////////////////////////////////////////////////////////////////

    /** @return whether or not the new state is an accepting state. */
    abstract public Validity transition(DistEventType e);

    /** @return whether or not the current state is a rejecting state. */
    abstract public boolean isFail();

    /** Returns a clone of this checker. */
    abstract public BinChecker<State> getClone();
}
