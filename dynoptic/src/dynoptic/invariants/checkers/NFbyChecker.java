package dynoptic.invariants.checkers;

import dynoptic.invariants.BinaryInvariant;

import synoptic.model.event.DistEventType;

enum NFbyState {
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

public class NFbyChecker extends BinChecker<NFbyState> {

    /**
     * @param inv
     *            x NFby y
     */
    public NFbyChecker(BinaryInvariant inv) {
        super(inv, NFbyState.INITIAL);
    }

    public NFbyChecker(NFbyChecker ch) {
        super(ch.inv, ch.s);
    }

    /**
     * @return whether or not the new state is an accepting state.
     */
    @Override
    public Validity transition(DistEventType e) {
        if (s == NFbyState.SAW_XY) {
            // Permanently rejecting.
            return Validity.PERM_FAIL;
        }

        if (s == NFbyState.SAW_X) {
            if (inv.getSecond().equals(e)) {
                s = NFbyState.SAW_XY;
                return Validity.PERM_FAIL;
            }
            return Validity.TEMP_SUCCESS;
        }

        assert s == NFbyState.INITIAL;

        if (inv.getFirst().equals(e)) {
            s = NFbyState.SAW_X;
        }
        return Validity.TEMP_SUCCESS;
    }

    @Override
    public boolean isFail() {
        return s == NFbyState.SAW_XY;
    }

    @Override
    public NFbyChecker getClone() {
        return new NFbyChecker(this);
    }

}
