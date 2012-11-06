package dynoptic.invariants.checkers;

import dynoptic.invariants.BinaryInvariant;

import synoptic.model.event.DistEventType;

public class APChecker extends BinChecker {

    private enum State {
        // INITIAL (accepting):
        // initial state.
        INITIAL,

        // SAW_X (permanently accepting):
        // state after having observed x.
        SAW_X,

        // SAW_Y (permanently rejecting):
        // state after having observed y.
        SAW_Y;
    }

    State s;

    /**
     * @param inv
     *            x AP y
     */
    public APChecker(BinaryInvariant inv) {
        super(inv);
        s = State.INITIAL;
    }

    /**
     * @return whether or not the new state is an accepting state.
     */
    @Override
    public Validity transition(DistEventType e) {
        if (s == State.SAW_X) {
            return Validity.PERM_SUCCESS;
        }

        if (s == State.SAW_Y) {
            return Validity.PERM_FAIL;
        }

        assert s == State.INITIAL;

        if (inv.getFirst().equals(e)) {
            s = State.SAW_X;
            return Validity.PERM_SUCCESS;
        }

        if (inv.getSecond().equals(e)) {
            s = State.SAW_Y;
            return Validity.PERM_FAIL;
        }

        // Remain at INITIAL
        return Validity.TEMP_SUCCESS;
    }

    @Override
    public boolean isFail() {
        return s == State.SAW_Y;
    }

    @Override
    public void inheritState(BinChecker otherChecker) {
        assert otherChecker instanceof APChecker;
        this.s = ((APChecker) otherChecker).s;
    }

    @Override
    public BinChecker getClone() {
        APChecker ch = new APChecker(inv);
        ch.s = this.s;
        return ch;
    }

}
