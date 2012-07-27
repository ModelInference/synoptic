package dynoptic.model;

import java.util.Set;

import dynoptic.model.alphabet.EventType;

/**
 * Captures complete state of an FSM at some instant.
 * 
 * @param <NextState>
 *            The type of the next state (set) returned by getNextStates.
 */
public interface IFSMState<NextState extends IFSMState<NextState>> {
    /**
     * Whether or not the FSM state is an initial state in the FSM.
     */
    // boolean isInitial();

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
    Set<NextState> getNextStates(EventType event);
}
