package dynoptic.model.fifosys.cfsm.fsm;

import static org.junit.Assert.*;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Test;

import synoptic.model.event.DistEventType;

public class FSMMinimizationTests {

    DistEventType e1 = DistEventType.LocalEvent("a", 0);
    DistEventType e2 = DistEventType.LocalEvent("b", 0);
    
    @Test
    public void isDeterministicOneStateNoTransition() {
        FSMState state = new FSMState(true, true, 0, 0);
        FSM fsm = new FSM(0, state, state, toSet(state), 1);
        
        assertTrue(fsm.isDeterministic());
    }
    
    @Test
    public void isDeterministicOneStateWithTransition() {
        FSMState state = new FSMState(true, true, 0, 0);
        state.addTransition(e1, state);
        FSM fsm = new FSM(0, state, state, toSet(state), 1);
        
        assertTrue(fsm.isDeterministic());
    }
    
    @Test
    public void isDeterministicTwoStatesOneTransition() {
        FSMState initState = new FSMState(false, true, 0, 0);
        FSMState acceptState = new FSMState(true, false, 0, 1);
        initState.addTransition(e1, acceptState);
        FSM fsm = new FSM(0, initState, acceptState, toSet(initState, 
                acceptState), 2);
        
        assertTrue(fsm.isDeterministic());
    }
    
    @Test
    public void isDeterministicTwoStatesTwoTransitions() {
        FSMState initState = new FSMState(false, true, 0, 0);
        FSMState acceptState = new FSMState(true, false, 0, 1);
        initState.addTransition(e1, acceptState);
        initState.addTransition(e2, acceptState);
        FSM fsm = new FSM(0, initState, acceptState, toSet(initState, 
                acceptState), 2);
        
        assertTrue(fsm.isDeterministic());
    }
    
    @Test
    public void nonDeterministicSingleInitState() {
        FSMState s0 = new FSMState(false, true, 0, 0);
        FSMState s1 = new FSMState(true, false, 0, 1);
        FSMState s2 = new FSMState(true, false, 0, 2);
        s0.addTransition(e1, s1);
        s0.addTransition(e1, s2);
        FSM fsm = new FSM(0, toSet(s0), toSet(s1, s2), toSet(s0, s1, s2), 3);
        
        assertFalse(fsm.isDeterministic());
    }
    
    @Test
    public void nonDeterministicMultipleInitStates() {
        FSMState s0 = new FSMState(false, true, 0, 0);
        FSMState s1 = new FSMState(false, true, 0, 1);
        FSMState s2 = new FSMState(true, false, 0, 2);
        s0.addTransition(e1, s2);
        s1.addTransition(e2, s2);
        FSM fsm = new FSM(0, toSet(s0, s1), toSet(s2), toSet(s0, s1, s2), 3);
        
        assertFalse(fsm.isDeterministic());
    }
    
    @Test
    public void minimizeTest() {
        FSMState s0 = new FSMState(false, true, 0, 0);
        FSMState s1 = new FSMState(true, false, 0, 1);
        FSMState s2 = new FSMState(true, false, 0, 2);
        s0.addTransition(e1, s1);
        s0.addTransition(e2, s2);
        FSM fsm = new FSM(0, toSet(s0), toSet(s1, s2), toSet(s0, s1, s2), 3);
        
        assertTrue(fsm.isDeterministic());
        
        fsm.minimize();
        
        FSMState initState = new FSMState(false, true, 0, 0);
        FSMState acceptState = new FSMState(true, false, 0, 1);
        initState.addTransition(e1, acceptState);
        initState.addTransition(e2, acceptState);
        FSM reducedFSM = new FSM(0, initState, acceptState, toSet(initState, 
                acceptState), 2);
        
        // this only checks that fsm and reducedFSM accept same language,
        // they could have different graph forms
        assertEquals(fsm, reducedFSM);
        
        // checks that fsm has same number of states as reducedFSM
        assertTrue(fsm.getStates().size() == reducedFSM.getStates().size());
    }
    
    /** Converts a list of FSMState instances into a set, and returns the set. */
    private Set<FSMState> toSet(FSMState... fsmStates) {
        Set<FSMState> states = new LinkedHashSet<FSMState>();
        for (FSMState state : fsmStates) {
            states.add(state);
        }
        return states;
    }
}
