package dynoptic.invariants.checkers;

import dynoptic.invariants.BinaryInvariant;

import synoptic.model.event.DistEventType;

public class APChecker extends BinChecker {

    private enum State {
        // INITIAL (accepting):
        // initial state.
        //
        // SAW_X (permanently accepting):
        // state after having observed x.
        //
        // SAW_Y (permanently rejecting):
        // state after having observed y.
        INITIAL, SAW_X, SAW_Y;
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
    public boolean transition(DistEventType e) {
        if (s == State.SAW_X) {
            return true;
        }

        if (s == State.SAW_Y) {
            return false;
        }

        assert s == State.INITIAL;

        if (inv.getFirst().equals(e)) {
            s = State.SAW_X;
            return true;
        }

        if (inv.getSecond().equals(e)) {
            s = State.SAW_Y;
            return false;
        }

        // Remain at INITIAL
        return true;
    }

}
