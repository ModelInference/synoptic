package dynoptic.model.fifosys.cfsm.fsm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Test;

import dynoptic.DynopticTest;
import dynoptic.model.alphabet.EventType;
import dynoptic.model.fifosys.channel.ChannelId;

public class FSMTests extends DynopticTest {

    // Non-accepting state at pid 1.
    FSMState init_1;
    // Accepting state at pid 1.
    FSMState accepting_1;

    // Set containing just init_1.
    Set<FSMState> listInit_1;
    // Set containing just accepting_1.
    Set<FSMState> listAccepting_1;

    // Random state at pid 2.
    FSMState state_2;

    Set<FSMState> states;

    // cid: 1->2
    ChannelId cid;
    // cid!m
    EventType e_pid1;
    // e_2
    EventType e2_pid1;

    // e_3
    @Override
    public void setUp() {
        init_1 = new FSMState(false, true, 1, 0);
        listInit_1 = new LinkedHashSet<FSMState>();
        listInit_1.add(init_1);

        accepting_1 = new FSMState(true, false, 1, 1);
        listAccepting_1 = new LinkedHashSet<FSMState>();
        listAccepting_1.add(accepting_1);

        state_2 = new FSMState(false, false, 2, 2);
        cid = new ChannelId(1, 2, 0);
        e_pid1 = EventType.SendEvent("m", cid);
        e2_pid1 = EventType.LocalEvent("e", 1);
        states = new LinkedHashSet<FSMState>();
    }

    @Test
    public void createFSM() {
        states.add(init_1);
        states.add(accepting_1);
        FSM f = new FSM(1, init_1, accepting_1, states);
        assertEquals(f.getAlphabet().size(), 0);
        assertEquals(f.getPid(), 1);
        assertEquals(f.getInitStates(), listInit_1);
        assertEquals(f.getAcceptStates(), listAccepting_1);
    }

    @Test
    public void createFSMWithTxns() {
        init_1.addTransition(e_pid1, accepting_1);
        accepting_1.addTransition(e2_pid1, init_1);
        states.add(init_1);
        states.add(accepting_1);

        FSM f = new FSM(1, init_1, accepting_1, states);
        assertEquals(f.getAlphabet().size(), 2);
        assertTrue(f.getAlphabet().contains(e_pid1));
        assertTrue(f.getAlphabet().contains(e2_pid1));

        assertEquals(f.getPid(), 1);
        assertEquals(f.getInitStates(), listInit_1);
        assertEquals(f.getAcceptStates(), listAccepting_1);
    }

    @Test
    public void scmString() {
        init_1.addTransition(e_pid1, accepting_1);
        accepting_1.addTransition(e2_pid1, init_1);
        states.add(init_1);
        states.add(accepting_1);

        FSM f = new FSM(1, init_1, accepting_1, states);

        logger.info(f.toScmString());
    }

    @Test(expected = AssertionError.class)
    @SuppressWarnings("unused")
    public void createBadFSM1() {
        states.add(init_1);
        // error: accepting_1 \not\in states
        FSM f = new FSM(1, init_1, accepting_1, states);
    }

    @Test(expected = AssertionError.class)
    @SuppressWarnings("unused")
    public void createBadFSM2() {
        states.add(init_1);
        states.add(accepting_1);
        states.add(state_2);
        // error: state_2.pid != 2
        FSM f = new FSM(1, init_1, accepting_1, states);
    }

}
