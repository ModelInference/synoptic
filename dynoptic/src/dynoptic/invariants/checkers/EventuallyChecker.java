package dynoptic.invariants.checkers;

import dynoptic.invariants.BinaryInvariant;

import synoptic.model.event.DistEventType;

enum EventuallyState {
    // INITIAL (rejecting):
    // initial state.
    INITIAL,

    // SAW_X (permanently accepting):
    // state after having observed x.
    SAW_X;
}

public class EventuallyChecker extends BinChecker<EventuallyState> {

    /**
     * @param inv
     *            Eventually x
     */
    public EventuallyChecker(BinaryInvariant inv) {
        super(inv, EventuallyState.INITIAL);
    }

    public EventuallyChecker(EventuallyChecker ch) {
        super(ch.inv, ch.s);
    }

    /**
     * @return whether or not the new state is an accepting state.
     */
    @Override
    public Validity transition(DistEventType e) {
        if (s == EventuallyState.SAW_X) {
            return Validity.PERM_SUCCESS;
        }

        assert s == EventuallyState.INITIAL;

        if (inv.getSecond().equals(e)) {
            s = EventuallyState.SAW_X;
            return Validity.PERM_SUCCESS;
        }
        // Remain at INITIAL.
        return Validity.TEMP_FAIL;
    }

    @Override
    public boolean isFail() {
        return s == EventuallyState.INITIAL;
    }

    @Override
    public EventuallyChecker getClone() {
        return new EventuallyChecker(this);
    }

}
