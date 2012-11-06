package dynoptic.invariants.checkers;

import dynoptic.invariants.BinaryInvariant;

import synoptic.model.event.DistEventType;

public class EventuallyChecker extends BinChecker {

    private enum State {
        // INITIAL (rejecting):
        // initial state.
        INITIAL,

        // SAW_X (permanently accepting):
        // state after having observed x.
        SAW_X;
    }

    State s;

    /**
     * @param inv
     *            Eventually x
     */
    public EventuallyChecker(BinaryInvariant inv) {
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

        assert s == State.INITIAL;

        if (inv.getSecond().equals(e)) {
            s = State.SAW_X;
            return Validity.PERM_SUCCESS;
        }
        // Remain at INITIAL.
        return Validity.TEMP_FAIL;
    }

}
