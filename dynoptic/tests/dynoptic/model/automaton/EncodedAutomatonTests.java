package dynoptic.model.automaton;

import static org.junit.Assert.*;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Test;

import synoptic.model.event.DistEventType;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import dynoptic.DynopticTest;
import dynoptic.model.fifosys.cfsm.fsm.FSM;
import dynoptic.model.fifosys.cfsm.fsm.FSMState;

public class EncodedAutomatonTests extends DynopticTest {

    @Test
    public void fsmIsEquivalentOneState() {
        int pid = 0;
        int scmId = 0;
        
        FSMState s0 = new FSMState(true, true, pid, scmId);
        Set<FSMState> states1 = new LinkedHashSet<FSMState>();
        states1.add(s0);
        
        FSMState t0 = new FSMState(true, true, pid, scmId);
        Set<FSMState> states2 = new LinkedHashSet<FSMState>();
        states2.add(t0);
        
        FSM fsm1 = new FSM(pid, s0, s0, states1, scmId);
        FSM fsm2 = new FSM(pid, t0, t0, states2, scmId);
        
        EventTypeEncodings<DistEventType> encodings = fsm1.getEventTypeEncodings();
        EncodedAutomaton<FSMState> aut1 = fsm1.getEncodedAutomaton(encodings);
        EncodedAutomaton<FSMState> aut2 = fsm2.getEncodedAutomaton(encodings);
        
        assertTrue(aut1.equals(aut2));
        assertTrue(fsm1.isEquivalent(fsm2));
    }
    
    @Test
    public void fsmIsEquivalentTwoStates() {
        
        int pid = 0;
        int scmId = 0;
        
        FSMState s0 = new FSMState(false, true, pid, scmId);
        FSMState s1 = new FSMState(true, false, pid, scmId);
        DistEventType e = DistEventType.LocalEvent("e", pid);
        s0.addTransition(e, s1);
        Set<FSMState> states1 = new LinkedHashSet<FSMState>();
        states1.add(s0);
        states1.add(s1);
        
        FSMState t0 = new FSMState(false, true, pid, scmId);
        FSMState t1 = new FSMState(true, false, pid, scmId);
        DistEventType f = DistEventType.LocalEvent("e", pid);
        t0.addTransition(f, t1);
        Set<FSMState> states2 = new LinkedHashSet<FSMState>();
        states2.add(t0);
        states2.add(t1);
        
        FSM fsm1 = new FSM(pid, s0, s1, states1, scmId);
        FSM fsm2 = new FSM(pid, t0, t1, states2, scmId);

        EventTypeEncodings<DistEventType> encodings = fsm1.getEventTypeEncodings();
        EncodedAutomaton<FSMState> aut1 = fsm1.getEncodedAutomaton(encodings);
        EncodedAutomaton<FSMState> aut2 = fsm2.getEncodedAutomaton(encodings);

        assertTrue(aut1.equals(aut2));
        assertTrue(fsm1.isEquivalent(fsm2));
        
        /*
        Automaton aut1 = new Automaton();
        Automaton aut2 = new Automaton();
        
        // proc1
        State s_0 = new State();
        s_0.setAccept(false);
        State s_1 = new State();
        s_1.setAccept(true);
        
        char c1 = 'c';
        Transition e1 = new Transition(c1, c1, s_1);
        s_0.addTransition(e1);
        
        // proc2
        State t_0 = new State();
        t_0.setAccept(false);
        State t_1 = new State();
        t_1.setAccept(true);
        
        char c2 = 'c';
        Transition f = new Transition(c2, c2, t_1);
        t_0.addTransition(f);
        
        aut1.setInitialState(s_0);
        aut1.setDeterministic(false);
        aut1.restoreInvariant();
        
        aut2.setInitialState(t_0);
        aut2.setDeterministic(false);
        aut2.restoreInvariant();
        
        assertTrue(aut1.equals(aut2));
        */
    }

}
