package dynoptic.model.fifosys.gfsm.trace;

import dynoptic.model.alphabet.EventType;

public class ObservedEvent {
    /** The type of the observed event. */
    final EventType eType;

    public ObservedEvent(EventType eType) {
        this.eType = eType;
    }

    public EventType getType() {
        return eType;
    }

}
