package dynoptic.model;

import java.util.Set;

import dynoptic.model.alphabet.FSMAlphabet;
import dynoptic.util.Util;

import synoptic.model.event.IDistEventType;

/**
 * Describes a basic interface for an FSM.
 */
public abstract class AbsFSM<State extends AbsFSMState<State, TxnEType>, TxnEType extends IDistEventType> {

    // The set of all FSM states. This includes initial and
    // accept states. States manage transitions internally.
    protected final Set<State> states;

    // The FSM's alphabet.
    protected final FSMAlphabet<TxnEType> alphabet;

    // Initial, and accept states.
    protected final Set<State> initStates;
    protected final Set<State> acceptStates;

    public AbsFSM() {
        this.states = Util.newSet();
        this.alphabet = new FSMAlphabet<TxnEType>();
        this.initStates = Util.newSet();
        this.acceptStates = Util.newSet();
    }

    /** Returns the initial states for the FSM. */
    public Set<State> getInitStates() {
        return initStates;
    }

    /** Returns the accept states for the FSM. */
    public Set<State> getAcceptStates() {
        return acceptStates;
    }

    /** An FSM uses a finite alphabet of events. */
    public FSMAlphabet<TxnEType> getAlphabet() {
        return alphabet;
    }

    /** Returns the internal states of this FSM. */
    public Set<State> getStates() {
        return states;
    }

    // //////////////////////////////////////////////////////////////////

    /** Recomputes the alphabet of the FSM based on current states. */
    protected void recomputeAlphabet() {
        this.alphabet.clear();
        for (State s : states) {
            Set<TxnEType> events = s.getTransitioningEvents();
            alphabet.addAll(events);
        }
    }
}
