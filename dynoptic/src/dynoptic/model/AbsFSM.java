package dynoptic.model;

import java.util.LinkedHashSet;
import java.util.Set;

import dynoptic.model.alphabet.FSMAlphabet;
import dynoptic.model.automaton.EncodedAutomaton;
import dynoptic.model.automaton.EventTypeEncodings;

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
        this.states = new LinkedHashSet<State>();
        this.alphabet = new FSMAlphabet<TxnEType>();
        this.initStates = new LinkedHashSet<State>();
        this.acceptStates = new LinkedHashSet<State>();
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

    /**
     * Creates EventType encodings for all transitioning events in this FSM.
     * Note that, when comparing any 2 FSMs, only encodings from one of them is
     * used.
     * 
     * @return EventType encodings
     */
    public EventTypeEncodings<TxnEType> getEventTypeEncodings() {
        recomputeAlphabet(); // events of this FSM might have changed
        return new EventTypeEncodings<TxnEType>(alphabet);
    }

    /**
     * Creates an EncodedAutomaton for this FSM using the given EventType
     * encodings.
     * 
     * @return EncodedAutomaton
     */
    public EncodedAutomaton<State, TxnEType> getEncodedAutomaton(
            EventTypeEncodings<TxnEType> eventEncodings) {
        return new EncodedAutomaton<State, TxnEType>(eventEncodings, this);
    }

    /**
     * @return true if the language of this FSM is equal to the language of the
     *         given FSM.
     */
    @Override
    public int hashCode() {
        EventTypeEncodings<TxnEType> eventEncodings = getEventTypeEncodings();
        EncodedAutomaton<State, TxnEType> thisAutomaton = getEncodedAutomaton(eventEncodings);
        int ret = 31;
        ret = ret * 31 + thisAutomaton.hashCode();
        return ret;
    }

    /**
     * @return true if the language of this FSM is equal to the language of the
     *         given FSM.
     */
    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (this == other) {
            return true;
        }
        if (!(other instanceof AbsFSM)) {
            return false;
        }
        AbsFSM<State, TxnEType> aOther = (AbsFSM<State, TxnEType>) other;

        // Use encodings of this.
        EventTypeEncodings<TxnEType> eventEncodings = getEventTypeEncodings();
        EncodedAutomaton<State, TxnEType> thisAutomaton = getEncodedAutomaton(eventEncodings);
        EncodedAutomaton<State, TxnEType> otherAutomaton = aOther
                .getEncodedAutomaton(eventEncodings);
        return thisAutomaton.equals(otherAutomaton);
    }

    // //////////////////////////////////////////////////////////////////

    /** Recomputes the alphabet of the FSM based on current states. */
    protected void recomputeAlphabet() {
        this.alphabet.clear();
        for (State s : states) {
            Set<TxnEType> events = s.getTransitioningEvents();
            if (events.size() != 0) {
                alphabet.addAll(events);
            }
        }
    }
}
