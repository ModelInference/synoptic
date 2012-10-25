package dynoptic.model.automaton;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import dynoptic.model.AbsFSM;
import dynoptic.model.AbsFSMState;

import synoptic.model.event.DistEventType;

/**
 * Wrapper class for dk.brics.automaton.Automaton which provides character
 * encodings for building Automaton with EventTypes rather than characters.
 */
public class EncodedAutomaton<T extends AbsFSMState<T>> {

    public static Logger logger;
    static {
        logger = Logger.getLogger("EncodedAutomaton");
    }

    // The Automaton wrapped with EventType encodings.
    private Automaton model;

    // The encoding scheme for the Automaton.
    // NOTE: To compare 2 EncodedAutomatons, their EventType encodings
    // must be equivalent.
    private EventTypeEncodings<DistEventType> encodings;

    public EncodedAutomaton(EventTypeEncodings<DistEventType> encodings,
            AbsFSM<T> fsm) {
        this.encodings = encodings;
        model = new Automaton();
        convertFSMToAutomaton(fsm);
    }

    private void convertFSMToAutomaton(AbsFSM<T> fsm) {
        // initial state of this automaton
        State initialState = new State();

        Set<T> visited = new LinkedHashSet<T>();
        Set<T> initStates = fsm.getInitStates();

        for (T initState : initStates) {
            if (!visited.contains(initState)) {
                DFS(initState, initialState, visited);
            }
        }

        model.setInitialState(initialState);
        model.setDeterministic(false);
        model.restoreInvariant();
    }

    /**
     * Traverses the FSM while constructing an equivalent Automaton.
     * 
     * @param state
     *            - FSM state to begin DFS
     * @param autoState
     *            - Automaton state equivalent to the FSM state
     * @param visited
     *            - visited FSM states
     */
    private void DFS(T state, State autoState, Set<T> visited) {
        visited.add(state);
        autoState.setAccept(state.isAccept());
        Set<DistEventType> transitions = state.getTransitioningEvents();

        for (DistEventType transition : transitions) {
            Set<T> nextStates = state.getNextStates(transition);

            for (T nextState : nextStates) {
                if (!visited.contains(nextState)) {
                    char c = encodings.getEncoding(transition);
                    State nextAutoState = new State();
                    Transition autoTransition = new Transition(c, c,
                            nextAutoState);
                    autoState.addTransition(autoTransition);

                    DFS(nextState, nextAutoState, visited);
                }
            }
        }
    }

    public EventTypeEncodings<DistEventType> getEventTypeEncodings() {
        return encodings;
    }

    @Override
    public int hashCode() {
        return model.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (other == this) {
            return true;
        }
        if (!(other instanceof EncodedAutomaton)) {
            return false;
        }
        EncodedAutomaton<T> encodedAutomaton = (EncodedAutomaton<T>) other;
        return model.equals(encodedAutomaton.model);
    }
}
