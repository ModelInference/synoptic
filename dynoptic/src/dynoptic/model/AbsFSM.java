package dynoptic.model;

import java.util.LinkedHashSet;
import java.util.Set;

import dynoptic.model.alphabet.FSMAlphabet;
import dynoptic.model.automaton.EncodedAutomaton;
import dynoptic.model.automaton.EventTypeEncodings;

import synoptic.model.event.DistEventType;

/**
 * Describes a basic interface for an FSM.
 */
public abstract class AbsFSM<State extends AbsFSMState<State>> {

    // The set of all FSM states. This includes initial and
    // accept states. States manage transitions internally.
    protected final Set<State> states;

    // The FSM's alphabet.
    protected final FSMAlphabet alphabet;

    // Initial, and accept states.
    protected final Set<State> initStates;
    protected final Set<State> acceptStates;

    public AbsFSM() {
        this.states = new LinkedHashSet<State>();
        this.alphabet = new FSMAlphabet();
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
    public FSMAlphabet getAlphabet() {
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
    public EventTypeEncodings<DistEventType> getEventTypeEncodings() {
        recomputeAlphabet(); // events of this FSM might have changed
        return new EventTypeEncodings<DistEventType>(alphabet);
    }

    /**
     * Creates an EncodedAutomaton for this FSM using the given EventType
     * encodings.
     * 
     * @return EncodedAutomaton
     */
    public EncodedAutomaton<State> getEncodedAutomaton(
            EventTypeEncodings<DistEventType> eventEncodings) {
        return new EncodedAutomaton<State>(eventEncodings, this);
    }

    /**
     * @return true if this FSM is deterministic.
     */
    public boolean isDeterministic() {
        for (State state : states) {
            Set<DistEventType> events = state.getTransitioningEvents();

            for (DistEventType event : events) {
                if (state.getNextStates(event).size() > 1) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * @return true if the language of this FSM is equal to the language of the
     *         given FSM.
     */
    @Override
    public int hashCode() {
        EventTypeEncodings<DistEventType> eventEncodings = getEventTypeEncodings();
        EncodedAutomaton<State> thisAutomaton = getEncodedAutomaton(eventEncodings);
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
        AbsFSM<State> aOther = (AbsFSM<State>) other;

        // Use encodings of this.
        EventTypeEncodings<DistEventType> eventEncodings = getEventTypeEncodings();
        EncodedAutomaton<State> thisAutomaton = getEncodedAutomaton(eventEncodings);
        EncodedAutomaton<State> otherAutomaton = aOther
                .getEncodedAutomaton(eventEncodings);
        return thisAutomaton.equals(otherAutomaton);
    }

    // //////////////////////////////////////////////////////////////////

    /** Recomputes the alphabet of the FSM based on current states. */
    protected void recomputeAlphabet() {
        this.alphabet.clear();
        for (State s : states) {
            Set<? extends DistEventType> events = s.getTransitioningEvents();
            if (events.size() != 0) {
                alphabet.addAll(events);
            }
        }
    }
}
