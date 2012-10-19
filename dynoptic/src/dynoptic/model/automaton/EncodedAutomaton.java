package dynoptic.model.automaton;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import dynoptic.model.fifosys.cfsm.fsm.FSM;
import dynoptic.model.fifosys.cfsm.fsm.FSMState;

import synoptic.model.event.DistEventType;

/**
 * Wrapper class for dk.brics.automaton.Automaton which provides character
 * encodings for building Automaton with EventTypes rather than characters.
 */
public class EncodedAutomaton {

    public static Logger logger;
    static {
        logger = Logger.getLogger("EncodedAutomaton");
    }

    // The Automaton wrapped with EventType encodings.
    private Automaton model;
    
    // The encoding scheme for the Automaton.
    private EventTypeEncodings<DistEventType> encodings;

    public EncodedAutomaton(EventTypeEncodings<DistEventType> encodings,
            FSM fsm) {
        this.encodings = encodings;
        model = new Automaton();
        convertFSMToAutomaton(fsm);
    }
    
    private void convertFSMToAutomaton(FSM fsm) {
        // initial state of this automaton
        State initialState = new State();
        
        Set<FSMState> visited = new LinkedHashSet<FSMState>();
        Set<FSMState> initStates = fsm.getInitStates();
        
        for (FSMState initState : initStates) {
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
     * @param state - FSM state to begin DFS
     * @param autoState - Automaton state equivalent to the FSM state
     * @param visited - visited FSM states
     */
    private void DFS(FSMState state, State autoState, Set<FSMState> visited) {
        visited.add(state);
        Set<DistEventType> transitions = state.getTransitioningEvents();
        
        for (DistEventType transition : transitions) {
            Set<FSMState> nextStates = state.getNextStates(transition);
            
            for (FSMState nextState : nextStates) {
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
    
    @Override
    public int hashCode() {
        return model.hashCode();
    }
    
    @Override
    public boolean equals(Object other) {
        return model.equals(other);
    }
}
