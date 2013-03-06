package dynoptic.invariants.checkers;

import dynoptic.invariants.BinaryInvariant;

import synoptic.model.event.DistEventType;

enum APState {
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

public class APChecker extends BinChecker<APState> {

    /**
     * @param inv
     *            x AP y
     */
    public APChecker(BinaryInvariant inv) {
        super(inv, APState.INITIAL);
    }

    public APChecker(APChecker ch) {
        super(ch.inv, ch.s);
    }

    /**
     * @return whether or not the new state is an accepting state.
     */
    @Override
    public Validity transition(DistEventType e) {
        if (s == APState.SAW_X) {
            return Validity.PERM_SUCCESS;
        }

        if (s == APState.SAW_Y) {
            return Validity.PERM_FAIL;
        }

        assert s == APState.INITIAL;

        if (inv.getFirst().equals(e)) {
            s = APState.SAW_X;
            return Validity.PERM_SUCCESS;
        }

        if (inv.getSecond().equals(e)) {
            s = APState.SAW_Y;
            return Validity.PERM_FAIL;
        }

        // Remain at INITIAL
        return Validity.TEMP_SUCCESS;
    }

    @Override
    public boolean isFail() {
        return s == APState.SAW_Y;
    }

    @Override
    public APChecker getClone() {
        return new APChecker(this);
    }

}
