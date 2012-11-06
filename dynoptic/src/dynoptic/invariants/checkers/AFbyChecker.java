package dynoptic.invariants.checkers;

import dynoptic.invariants.BinaryInvariant;

import synoptic.model.event.DistEventType;

public class AFbyChecker extends BinChecker {

    private enum State {
        // INITIAL (accepting):
        // initial state and the state after having observed y.
        INITIAL,

        // SAW_X (rejecting):
        // state after having observed x.
        SAW_X;
    }

    State s;

    /**
     * @param inv
     *            x AFby y
     */
    public AFbyChecker(BinaryInvariant inv) {
        super(inv);
        s = State.INITIAL;
    }

    /**
     * @return whether or not the new state is an accepting state.
     */
    @Override
    public Validity transition(DistEventType e) {
        if (inv.getFirst().equals(e)) {
            s = State.SAW_X;
            return Validity.TEMP_FAIL;
        }

        if (inv.getSecond().equals(e)) {
            s = State.INITIAL;
            return Validity.TEMP_SUCCESS;
        }

        // Otherwise state is unchanged.
        if (s == State.INITIAL) {
            return Validity.TEMP_SUCCESS;
        }
        return Validity.TEMP_FAIL;
    }
}
