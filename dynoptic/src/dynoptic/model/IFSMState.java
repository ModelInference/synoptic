package dynoptic.model;

import java.util.Set;

import dynoptic.model.alphabet.EventType;

/**
 * Captures complete state of an FSM at some instant.
 */
public interface IFSMState<State extends IFSMState<State>> {
    /**
     * Whether or not the FSM state is a valid terminal state for the FSM.
     */
    boolean isAccept();

    /**
     * The set of possible events that can trigger a transition from this state.
     */
    Set<EventType> getTransitioningEvents();

    /**
     * Returns the unmodifiable (read-only) set of states that follow this state
     * if we transition along an event.
     * 
     * @param event
     * @return
     */
    Set<State> getNextStates(EventType event);
}
