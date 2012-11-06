package dynoptic.invariants.checkers;

import dynoptic.invariants.BinaryInvariant;

import synoptic.model.event.DistEventType;

public class NFbyChecker extends BinChecker {

    private enum State {
        // INITIAL (accepting):
        // initial state.
        INITIAL,

        // SAW_X (accepting):
        // state after having observed x.
        SAW_X,

        // SAW_XY (permanently rejecting):
        // state after observing x, and then y.
        SAW_XY;
    }

    State s;

    /**
     * @param inv
     *            x NFby y
     */
    public NFbyChecker(BinaryInvariant inv) {
        super(inv);
        s = State.INITIAL;
    }

    /**
     * @return whether or not the new state is an accepting state.
     */
    @Override
    public Validity transition(DistEventType e) {
        if (s == State.SAW_XY) {
            // Permanently rejecting.
            return Validity.PERM_FAIL;
        }

        if (s == State.SAW_X) {
            if (inv.getSecond().equals(e)) {
                s = State.SAW_XY;
                return Validity.PERM_FAIL;
            }
            return Validity.TEMP_SUCCESS;
        }

        assert s == State.INITIAL;

        if (inv.getFirst().equals(e)) {
            s = State.SAW_X;
        }
        return Validity.TEMP_SUCCESS;
    }

    @Override
    public boolean isFail() {
        return s == State.SAW_XY;
    }

}
