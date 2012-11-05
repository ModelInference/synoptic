package dynoptic.invariants.checkers;

import dynoptic.invariants.BinaryInvariant;

import synoptic.model.event.DistEventType;

public class AFbyChecker extends BinChecker {

    private enum State {
        // INITIAL (accepting):
        // initial state and the state after having observed y.
        //
        // SAW_X (rejecting):
        // state after having observed x.
        INITIAL, SAW_X;
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
    public boolean transition(DistEventType e) {
        if (inv.getFirst().equals(e)) {
            s = State.SAW_X;
            return false;
        }

        if (inv.getSecond().equals(e)) {
            s = State.INITIAL;
            return true;
        }
        return s == State.INITIAL;
    }
}
