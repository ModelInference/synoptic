package dynoptic.model.automaton;

import static org.junit.Assert.*;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Test;

import synoptic.model.event.DistEventType;

import dynoptic.DynopticTest;
import dynoptic.model.fifosys.cfsm.fsm.FSM;
import dynoptic.model.fifosys.cfsm.fsm.FSMState;

public class EncodedAutomatonTests extends DynopticTest {

    int pid = 0;
    int scmId = 0;
    int nextScmId = 1;
    
    DistEventType e1 = DistEventType.LocalEvent("e1", pid);
    DistEventType e2 = DistEventType.LocalEvent("e2", pid);
    DistEventType f1 = DistEventType.LocalEvent("e1", pid);
    DistEventType f2 = DistEventType.LocalEvent("e2", pid);
    
    @Test
    public void isEquivalentOneStateFSM() {
        FSMState s0 = getInitAndAcceptState();
        Set<FSMState> sStates = addStatesToSet(s0);
        
        FSMState t0 = getInitAndAcceptState();
        Set<FSMState> tStates = addStatesToSet(t0);
        
        FSM fsm1 = new FSM(pid, s0, s0, sStates, nextScmId);
        FSM fsm2 = new FSM(pid, t0, t0, tStates, nextScmId);
        
        assertTrue(fsm1.isEquivalent(fsm2));
    }
    
    @Test
    public void isEquivalentTwoStateFSM() {
        FSMState s0 = getInitState();
        FSMState s1 = getAcceptState();
        s0.addTransition(e1, s1);
        Set<FSMState> sStates = addStatesToSet(s0, s1);
        
        FSMState t0 = getInitState();
        FSMState t1 = getAcceptState();
        t0.addTransition(f1, t1);
        Set<FSMState> tStates = addStatesToSet(t0, t1);
        
        FSM fsm1 = new FSM(pid, s0, s1, sStates, nextScmId);
        FSM fsm2 = new FSM(pid, t0, t1, tStates, nextScmId);

        assertTrue(fsm1.isEquivalent(fsm2));
    }
    
    @Test
    public void isEquivalentTwoAcceptStateFSM() {
        FSMState s0 = getInitState();
        FSMState s1 = getAcceptState();
        FSMState s2 = getAcceptState();
        s0.addTransition(e1, s1);
        s0.addTransition(e2, s2);
        Set<FSMState> sStates = addStatesToSet(s0, s1, s2);
        Set<FSMState> sInitStates = addStatesToSet(s0);
        Set<FSMState> sAcceptStates = addStatesToSet(s1, s2);
        
        FSMState t0 = getInitState();
        FSMState t1 = getAcceptState();
        FSMState t2 = getAcceptState();
        t0.addTransition(f1, t1);
        t0.addTransition(f2, t2);
        Set<FSMState> tStates = addStatesToSet(t0, t1, t2);
        Set<FSMState> tInitStates = addStatesToSet(t0);
        Set<FSMState> tAcceptStates = addStatesToSet(t1, t2);
        
        FSM fsm1 = new FSM(pid, sInitStates, sAcceptStates, sStates, nextScmId);
        FSM fsm2 = new FSM(pid, tInitStates, tAcceptStates, tStates, nextScmId);

        assertTrue(fsm1.isEquivalent(fsm2));
    }
    
    @Test
    public void isEquivalentTwoInitStateFSM() {
        FSMState s0 = getInitState();
        FSMState s1 = getInitState();
        FSMState s2 = getAcceptState();
        s0.addTransition(e1, s2);
        s1.addTransition(e2, s2);
        Set<FSMState> sStates = addStatesToSet(s0, s1, s2);
        Set<FSMState> sInitStates = addStatesToSet(s0, s1);
        Set<FSMState> sAcceptStates = addStatesToSet(s2);
        
        FSMState t0 = getInitState();
        FSMState t1 = getInitState();
        FSMState t2 = getAcceptState();
        t0.addTransition(f1, t2);
        t1.addTransition(f2, t2);
        Set<FSMState> tStates = addStatesToSet(t0, t1, t2);
        Set<FSMState> tInitStates = addStatesToSet(t0, t1);
        Set<FSMState> tAcceptStates = addStatesToSet(t2);
        
        FSM fsm1 = new FSM(pid, sInitStates, sAcceptStates, sStates, nextScmId);
        FSM fsm2 = new FSM(pid, tInitStates, tAcceptStates, tStates, nextScmId);

        assertTrue(fsm1.isEquivalent(fsm2));
    }

    @Test
    public void isEquivalentFalse() {
        FSMState s0 = getInitState();
        FSMState s1 = getIntermediateState();
        FSMState s2 = getAcceptState();
        s0.addTransition(e1, s1);
        s1.addTransition(e2, s2);
        Set<FSMState> sStates = addStatesToSet(s0, s1, s2);
        
        FSMState t0 = getInitState();
        FSMState t1 = getIntermediateState();
        FSMState t2 = getAcceptState();
        t0.addTransition(f2, t1);
        t1.addTransition(f1, t2);
        Set<FSMState> tStates = addStatesToSet(t0, t1, t2);
        
        FSM fsm1 = new FSM(pid, s0, s2, sStates, nextScmId);
        FSM fsm2 = new FSM(pid, t0, t2, tStates, nextScmId);

        assertFalse(fsm1.isEquivalent(fsm2));
    }
    
    private FSMState getInitState() {
        return new FSMState(false, true, pid, scmId);
    }
    
    private FSMState getAcceptState() {
        return new FSMState(true, false, pid, scmId);
    }
    
    private FSMState getInitAndAcceptState() {
        return new FSMState(true, true, pid, scmId);
    }
    
    private FSMState getIntermediateState() {
        return new FSMState(false, false, pid, scmId);
    }
    
    private Set<FSMState> addStatesToSet(FSMState ...fsmStates) {
        Set<FSMState> states = new LinkedHashSet<FSMState>();
        for (FSMState state : fsmStates) {
            states.add(state);
        }
        return states;
    }
}
