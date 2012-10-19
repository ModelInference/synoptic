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
    
    protected EventTypeEncodings<DistEventType> encodings;
    protected EncodedAutomaton<State> encodedAutomaton;
    
    public AbsFSM() {
        this.states = new LinkedHashSet<State>();
        this.alphabet = new FSMAlphabet();
        this.initStates = new LinkedHashSet<State>();
        this.acceptStates = new LinkedHashSet<State>();
        this.encodings = null; 
        this.encodedAutomaton = null;
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
     * @return true if the language of this FSM is equal to the language of
     * the given FSM.
     */
    public boolean isEquivalent(AbsFSM<State> other) {
        // use encodings of this
        EventTypeEncodings<DistEventType> eventEncodings = getEventTypeEncodings();
        EncodedAutomaton<State> thisAutomaton = getEncodedAutomaton(
                eventEncodings);
        EncodedAutomaton<State> otherAutomaton = other.getEncodedAutomaton(
                eventEncodings);
        return thisAutomaton.equals(otherAutomaton);
    }

    // //////////////////////////////////////////////////////////////////

    protected EventTypeEncodings<DistEventType> getEventTypeEncodings() {
        if (encodings == null) {
            encodings = new EventTypeEncodings<DistEventType>(alphabet);
        }
        return encodings;
    }
    
    protected EncodedAutomaton<State> getEncodedAutomaton(
            EventTypeEncodings<DistEventType> eventEncodings) {
        if (encodedAutomaton == null || 
                !encodedAutomaton.getEventTypeEncodings().equals(eventEncodings)) {
            encodedAutomaton = new EncodedAutomaton<State>(eventEncodings, this);
        }
        return encodedAutomaton;
    }

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
