package dynoptic.model;

import java.util.Set;

import dynoptic.model.alphabet.EventType;
import dynoptic.model.alphabet.FSMAlphabet;

/**
 * Describes a basic interface for an FSM.
 */
public interface IFSM<State extends IFSMState> {
    /**
     * Returns the current state of the FSM.
     */
    State getState();

    /**
     * An FSM uses a finite alphabet of events.
     */
    FSMAlphabet getAlphabet();

    /**
     * An FSM transitions between states by processing events.
     * 
     * @param event
     * @return
     */
    State transition(EventType event);

    /**
     * Returns the set of events that are currently feasible (that this FSM can
     * transition on).
     */
    Set<EventType> getEnabledEvents();
}
