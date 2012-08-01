package dynoptic.model.fifosys.gfsm.trace;

import dynoptic.model.alphabet.EventType;

/**
 * Represents a state that was observed or mined from a log of an execution of a
 * FIFO system.
 */
public class ObservedEvent {
    /** The type of the observed event. */
    final EventType eType;

    public ObservedEvent(EventType eType) {
        this.eType = eType;
    }

    public EventType getType() {
        return eType;
    }

    // //////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        return "obs_" + eType.toString();
    }

}
