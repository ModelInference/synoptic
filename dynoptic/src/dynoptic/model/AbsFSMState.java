package dynoptic.model;

import java.util.Collection;
import java.util.Set;

import dynoptic.model.alphabet.EventType;

/**
 * Captures complete state of an FSM at some instant.
 * 
 * @param <NextState>
 *            The type of the next state (set) returned by getNextStates.
 */
abstract public class AbsFSMState<NextState extends AbsFSMState<NextState>> {

    /** Used for functional calls below. */
    protected interface IStateToBooleanFn<T> {
        boolean eval(T s);
    }

    // Fn: (AbsFSMState s) -> "s an accept state"
    static protected IStateToBooleanFn<AbsFSMState<?>> fnInitialState = new IStateToBooleanFn<AbsFSMState<?>>() {
        @Override
        public boolean eval(AbsFSMState<?> s) {
            return s.isAccept();
        }
    };

    // Fn: (AbsFSMState s) -> "s an init state"
    static protected IStateToBooleanFn<AbsFSMState<?>> fnAcceptState = new IStateToBooleanFn<AbsFSMState<?>>() {
        @Override
        public boolean eval(AbsFSMState<?> s) {
            return s.isInitial();
        }
    };

    /**
     * Returns true if all the AbsFSMState states in the collection evaluate to
     * true through fn.
     * 
     * @param states
     * @return
     * @return
     */
    static protected boolean statesEvalToTrue(
            Collection<? extends AbsFSMState<?>> states,
            IStateToBooleanFn<AbsFSMState<?>> fn) {
        for (AbsFSMState<?> s : states) {
            if (!fn.eval(s)) {
                return false;
            }
        }
        return true;
    }

    // //////////////////////////////////////////////////////////////////

    /**
     * Whether or not the FSM state is an initial state in the FSM.
     */
    abstract public boolean isInitial();

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
