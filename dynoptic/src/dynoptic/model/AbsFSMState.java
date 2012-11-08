package dynoptic.model;

import java.util.Collection;
import java.util.Set;

import dynoptic.util.Util;

import synoptic.model.event.DistEventType;
import synoptic.model.event.IDistEventType;

/**
 * An abstraction for a state of an FSM. This state can be initial/terminal, and
 * has some number of transitions to other state instances.
 * 
 * @param <NextState>
 *            The type of the next state (set) returned by getNextStates.
 * @param <TxnEType>
 *            The type of transition event type; implements the IDistEventType
 *            interface
 */
abstract public class AbsFSMState<NextState extends AbsFSMState<NextState, TxnEType>, TxnEType extends IDistEventType> {

    /** Used for functional calls below. */
    protected interface IStateToBooleanFn<T> {
        boolean eval(T s);
    }

    // Fn: (AbsFSMState s) -> "s an accept state"
    static protected IStateToBooleanFn<AbsFSMState<?, ?>> fnIsInitialState = new IStateToBooleanFn<AbsFSMState<?, ?>>() {
        @Override
        public boolean eval(AbsFSMState<?, ?> s) {
            return s.isAccept();
        }
    };

    // Fn: (AbsFSMState s) -> "s an init state"
    static protected IStateToBooleanFn<AbsFSMState<?, ?>> fnIsAcceptState = new IStateToBooleanFn<AbsFSMState<?, ?>>() {
        @Override
        public boolean eval(AbsFSMState<?, ?> s) {
            return s.isInitial();
        }
    };

    /** Returns true iff each state in states evaluates to true through fn. */
    static protected <S extends AbsFSMState<?, ?>> boolean statesEvalToTrue(
            Collection<S> states, IStateToBooleanFn<AbsFSMState<?, ?>> fn) {
        for (S s : states) {
            if (!fn.eval(s)) {
                return false;
            }
        }
        return true;
    }

    /** Returns a set of states that evaluate to true through fn. */
    static protected <S extends AbsFSMState<?, ?>> Set<S> getStatesThatEvalToTrue(
            Set<S> states, IStateToBooleanFn<AbsFSMState<?, ?>> fn) {
        Set<S> ret = Util.newSet();
        for (S s : states) {
            if (fn.eval(s)) {
                ret.add(s);
            }
        }
        return ret;
    }

    // //////////////////////////////////////////////////////////////////
    // Generic transitivity methods.

    /**
     * Returns a set of GFSMState nodes that are reachable from s though all
     * event transitions. This set does not include s.
     * 
     * @param visited
     */
    public static <State extends AbsFSMState<State, EType>, EType extends DistEventType> void findTransitiveClosure(
            State s, Set<State> visited, Set<State> txClosure) {
        findNonPidTransitiveClosure(-1, s, visited, txClosure);
    }

    /**
     * Returns a set of GFSMState nodes that are reachable from s though event
     * transitions that are non-pid transitions. This set does NOT include s. If
     * pid is -1 then it ignores the pid constraint and returns all reachable
     * states from s.
     * 
     * @param visited
     */
    public static <State extends AbsFSMState<State, EType>, EType extends DistEventType> void findNonPidTransitiveClosure(
            int pid, State s, Set<State> visited,
            Set<State> nonPidTxClosureStates) {
        // Record that we have visited s.
        visited.add(s);

        for (EType e : s.getTransitioningEvents()) {
            if (pid != -1 && e.getPid() == pid) {
                continue;
            }
            Set<State> children = s.getNextStates(e);
            nonPidTxClosureStates.addAll(children);

            // Recursively build up the transitive set of
            // non-pid-event-reachable states from s.
            for (State child : children) {
                if (!visited.contains(child)) {
                    findNonPidTransitiveClosure(pid, child, visited,
                            nonPidTxClosureStates);
                }
            }
        }
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
     * The set of possible (abstract, non-observed) event types that can trigger
     * a transition from this state.
     */
    abstract public Set<TxnEType> getTransitioningEvents();

    /**
     * Returns the unmodifiable (read-only) set of states that follow this state
     * if we transition along an event.
     * 
     * @param event
     * @return
     */
    abstract public Set<NextState> getNextStates(TxnEType event);

    /**
     * Returns the set of all states that follows this state.
     * 
     * @param event
     * @return
     */
    public Set<NextState> getNextStates() {
        Set<NextState> ret = Util.newSet();
        for (TxnEType e : getTransitioningEvents()) {
            ret.addAll(getNextStates(e));
        }
        return ret;
    }
}
