package dynoptic.model;

import java.util.Set;

import dynoptic.model.alphabet.EventType;

/**
 * Captures complete state of an FSM at some instant.
 * 
 * @param <NextState>
 *            The type of the next state (set) returned by getNextStates.
 */
abstract public class AbsFSMState<NextState extends AbsFSMState<NextState>> {
    /**
     * Whether or not the FSM state is an initial state in the FSM.
     */
    // boolean isInitial();

    /**
     * Whether or not the FSM state is a valid terminal state for the FSM.
     */
    abstract public boolean isAccept();

    /**
     * The set of possible events that can trigger a transition from this state.
     */
    abstract public Set<EventType> getTransitioningEvents();

    /**
     * Returns the unmodifiable (read-only) set of states that follow this state
     * if we transition along an event.
     * 
     * @param event
     * @return
     */
    abstract public Set<NextState> getNextStates(EventType event);
}
