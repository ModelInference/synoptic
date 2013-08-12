package dynoptic.invariants.checkers;

import dynoptic.invariants.BinaryInvariant;

import synoptic.model.event.DistEventType;

enum AFbyState {
    // INITIAL (accepting):
    // initial state and the state after having observed y.
    INITIAL,

    // SAW_X (rejecting):
    // state after having observed x.
    SAW_X;
}

public class AFbyChecker extends BinChecker<AFbyState> {

    /**
     * @param inv
     *            x AFby y
     */
    public AFbyChecker(BinaryInvariant inv) {
        super(inv, AFbyState.INITIAL);
    }

    public AFbyChecker(AFbyChecker ch) {
        super(ch.inv, ch.s);
    }

    /**
     * @return whether or not the new state is an accepting state.
     */
    @Override
    public Validity transition(DistEventType e) {
        if (inv.getFirst().equals(e)) {
            s = AFbyState.SAW_X;
            return Validity.TEMP_FAIL;
        }

        if (inv.getSecond().equals(e)) {
            s = AFbyState.INITIAL;
            return Validity.TEMP_SUCCESS;
        }

        // Otherwise state is unchanged.
        if (s == AFbyState.INITIAL) {
            return Validity.TEMP_SUCCESS;
        }
        return Validity.TEMP_FAIL;
    }

    @Override
    public boolean isFail() {
        return s == AFbyState.SAW_X;
    }

    @Override
    public BinChecker<AFbyState> getClone() {
        return new AFbyChecker(this);
    }
}
