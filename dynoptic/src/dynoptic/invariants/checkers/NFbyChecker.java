package dynoptic.invariants.checkers;

import dynoptic.invariants.BinaryInvariant;

import synoptic.model.event.DistEventType;

public class NFbyChecker extends BinChecker {

    private enum State {
        // INITIAL (accepting):
        // initial state.
        //
        // SAW_X (accepting):
        // state after having observed x.
        //
        // SAW_XY (permanently rejecting):
        // state after observing x, and then y.
        INITIAL, SAW_X, SAW_XY;
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
    public boolean transition(DistEventType e) {
        if (s == State.SAW_XY) {
            // Permanently rejecting.
            return false;
        }

        if (s == State.SAW_X) {
            if (inv.getSecond().equals(e)) {
                s = State.SAW_XY;
                return false;
            }
            return true;
        }

        assert s == State.INITIAL;

        if (inv.getFirst().equals(e)) {
            s = State.SAW_X;
            return true;
        }

        return true;
    }

}
