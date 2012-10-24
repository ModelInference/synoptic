package dynoptic.model.automaton;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Test;

import synoptic.model.channelid.ChannelId;
import synoptic.model.event.DistEventType;

import dynoptic.DynopticTest;
import dynoptic.model.fifosys.cfsm.CFSM;
import dynoptic.model.fifosys.cfsm.fsm.FSM;
import dynoptic.model.fifosys.cfsm.fsm.FSMState;

public class EncodedAutomatonTests extends DynopticTest {

    int pid0 = 0;
    int pid1 = 1;
    int scmId = 0;
    int nextScmId = 1;
    
    DistEventType e1 = DistEventType.LocalEvent("e1", pid0);
    DistEventType e2 = DistEventType.LocalEvent("e2", pid0);
    DistEventType f1 = DistEventType.LocalEvent("e1", pid0);
    DistEventType f2 = DistEventType.LocalEvent("e2", pid0);
    
    @Test
    public void isEquivalentOneStateFSM() {
        FSMState s0 = getInitAndAcceptState(pid0);
        Set<FSMState> sStates = addStatesToSet(s0);
        
        FSMState t0 = getInitAndAcceptState(pid0);
        Set<FSMState> tStates = addStatesToSet(t0);
        
        FSM fsm1 = new FSM(pid0, s0, s0, sStates, nextScmId);
        FSM fsm2 = new FSM(pid0, t0, t0, tStates, nextScmId);
        
        assertTrue(fsm1.isEquivalent(fsm2));
    }
    
    @Test
    public void isEquivalentTwoStateFSM() {
        FSMState s0 = getInitState(pid0);
        FSMState s1 = getAcceptState(pid0);
        s0.addTransition(e1, s1);
        Set<FSMState> sStates = addStatesToSet(s0, s1);
        
        FSMState t0 = getInitState(pid0);
        FSMState t1 = getAcceptState(pid0);
        t0.addTransition(f1, t1);
        Set<FSMState> tStates = addStatesToSet(t0, t1);
        
        FSM fsm1 = new FSM(pid0, s0, s1, sStates, nextScmId);
        FSM fsm2 = new FSM(pid0, t0, t1, tStates, nextScmId);

        assertTrue(fsm1.isEquivalent(fsm2));
    }
    
    @Test
    public void isEquivalentTwoAcceptStateFSM() {
        FSMState s0 = getInitState(pid0);
        FSMState s1 = getAcceptState(pid0);
        FSMState s2 = getAcceptState(pid0);
        s0.addTransition(e1, s1);
        s0.addTransition(e2, s2);
        Set<FSMState> sStates = addStatesToSet(s0, s1, s2);
        Set<FSMState> sInitStates = addStatesToSet(s0);
        Set<FSMState> sAcceptStates = addStatesToSet(s1, s2);
        
        FSMState t0 = getInitState(pid0);
        FSMState t1 = getAcceptState(pid0);
        FSMState t2 = getAcceptState(pid0);
        t0.addTransition(f1, t1);
        t0.addTransition(f2, t2);
        Set<FSMState> tStates = addStatesToSet(t0, t1, t2);
        Set<FSMState> tInitStates = addStatesToSet(t0);
        Set<FSMState> tAcceptStates = addStatesToSet(t1, t2);
        
        FSM fsm1 = new FSM(pid0, sInitStates, sAcceptStates, sStates, nextScmId);
        FSM fsm2 = new FSM(pid0, tInitStates, tAcceptStates, tStates, nextScmId);

        assertTrue(fsm1.isEquivalent(fsm2));
    }
    
    @Test
    public void isEquivalentTwoInitStateFSM() {
        FSMState s0 = getInitState(pid0);
        FSMState s1 = getInitState(pid0);
        FSMState s2 = getAcceptState(pid0);
        s0.addTransition(e1, s2);
        s1.addTransition(e2, s2);
        Set<FSMState> sStates = addStatesToSet(s0, s1, s2);
        Set<FSMState> sInitStates = addStatesToSet(s0, s1);
        Set<FSMState> sAcceptStates = addStatesToSet(s2);
        
        FSMState t0 = getInitState(pid0);
        FSMState t1 = getInitState(pid0);
        FSMState t2 = getAcceptState(pid0);
        t0.addTransition(f1, t2);
        t1.addTransition(f2, t2);
        Set<FSMState> tStates = addStatesToSet(t0, t1, t2);
        Set<FSMState> tInitStates = addStatesToSet(t0, t1);
        Set<FSMState> tAcceptStates = addStatesToSet(t2);
        
        FSM fsm1 = new FSM(pid0, sInitStates, sAcceptStates, sStates, nextScmId);
        FSM fsm2 = new FSM(pid0, tInitStates, tAcceptStates, tStates, nextScmId);

        assertTrue(fsm1.isEquivalent(fsm2));
    }

    @Test
    public void isEquivalentFSMFalse() {
        FSMState s0 = getInitState(pid0);
        FSMState s1 = getIntermediateState(pid0);
        FSMState s2 = getAcceptState(pid0);
        s0.addTransition(e1, s1);
        s1.addTransition(e2, s2);
        Set<FSMState> sStates = addStatesToSet(s0, s1, s2);
        
        FSMState t0 = getInitState(pid0);
        FSMState t1 = getIntermediateState(pid0);
        FSMState t2 = getAcceptState(pid0);
        t0.addTransition(f2, t1);
        t1.addTransition(f1, t2);
        Set<FSMState> tStates = addStatesToSet(t0, t1, t2);
        
        FSM fsm1 = new FSM(pid0, s0, s2, sStates, nextScmId);
        FSM fsm2 = new FSM(pid0, t0, t2, tStates, nextScmId);

        assertFalse(fsm1.isEquivalent(fsm2));
    }
    
    @Test
    public void isEquivalentCFSM() {        
        FSM fsm1 = getOneStateFSM(pid0);
        FSM fsm2 = getOneStateFSM(pid1);
        
        CFSM cfsm1 = new CFSM(2, Collections.<ChannelId> emptyList());
        cfsm1.addFSM(fsm1);
        cfsm1.addFSM(fsm2);
        
        FSM fsm3 = getOneStateFSM(pid0);
        FSM fsm4 = getOneStateFSM(pid1);
        
        CFSM cfsm2 = new CFSM(2, Collections.<ChannelId> emptyList());
        cfsm2.addFSM(fsm3);
        cfsm2.addFSM(fsm4);
        
        assertTrue(cfsm1.isEquivalent(cfsm2));
    }
    
    @Test
    public void isEquivalentCFSMFalseSameSize() {
        FSM fsm1 = getTwoStateFSM(pid0, e1);
        FSM fsm2 = getOneStateFSM(pid1);
        
        CFSM cfsm1 = new CFSM(2, Collections.<ChannelId> emptyList());
        cfsm1.addFSM(fsm1);
        cfsm1.addFSM(fsm2);
        
        FSM fsm3 = getOneStateFSM(pid0);
        FSM fsm4 = getOneStateFSM(pid1);
        
        CFSM cfsm2 = new CFSM(2, Collections.<ChannelId> emptyList());
        cfsm2.addFSM(fsm3);
        cfsm2.addFSM(fsm4);
        
        assertFalse(cfsm1.isEquivalent(cfsm2));
    }
    
    @Test
    public void isEquivalentCFSMFalseDiffSize() {
        FSM fsm1 = getTwoStateFSM(pid0, e1);
        FSM fsm2 = getOneStateFSM(pid1);
        
        CFSM cfsm1 = new CFSM(2, Collections.<ChannelId> emptyList());
        cfsm1.addFSM(fsm1);
        cfsm1.addFSM(fsm2);
        
        FSM fsm3 = getOneStateFSM(pid0);
        
        CFSM cfsm2 = new CFSM(1, Collections.<ChannelId> emptyList());
        cfsm2.addFSM(fsm3);
        
        assertFalse(cfsm1.isEquivalent(cfsm2));
    }
    
    private FSM getOneStateFSM(int pid) {
        FSMState s = getInitAndAcceptState(pid);
        FSM fsm = new FSM(pid, s, s, addStatesToSet(s), nextScmId);
        return fsm;
    }
    
    private FSM getTwoStateFSM(int pid, DistEventType e) {
        FSMState s0 = getInitState(pid);
        FSMState s1 = getAcceptState(pid);
        s0.addTransition(e, s1);
        Set<FSMState> sStates = addStatesToSet(s0, s1);
        FSM fsm = new FSM(pid, s0, s1, sStates, nextScmId);
        return fsm;
    }
    
    private FSMState getInitState(int pid) {
        return new FSMState(false, true, pid, scmId);
    }
    
    private FSMState getAcceptState(int pid) {
        return new FSMState(true, false, pid, scmId);
    }
    
    private FSMState getInitAndAcceptState(int pid) {
        return new FSMState(true, true, pid, scmId);
    }
    
    private FSMState getIntermediateState(int pid) {
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
