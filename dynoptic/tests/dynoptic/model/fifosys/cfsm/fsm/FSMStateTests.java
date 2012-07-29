package dynoptic.model.fifosys.cfsm.fsm;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import dynoptic.DynopticTest;
import dynoptic.model.alphabet.EventType;
import dynoptic.model.fifosys.channel.ChannelId;

public class FSMStateTests extends DynopticTest {

    // Non-accepting state
    FSMState p;
    // Accepting state.
    FSMState q;

    // cid: 1->2
    ChannelId cid;
    // cid!m
    EventType e_pid1;
    // e_2
    EventType e2_pid1;
    // e_3
    EventType e3_pid2;

    @Override
    public void setUp() {
        p = new FSMState(false, 1);
        q = new FSMState(true, 1);
        cid = new ChannelId(1, 2);
        e_pid1 = EventType.SendEvent("m", cid);
        e2_pid1 = EventType.LocalEvent("e", 1);
        e3_pid2 = EventType.LocalEvent("e", 2);
    }

    @Test
    public void checkAccept() {
        assertFalse(p.isAccept());
        assertTrue(q.isAccept());
    }

    @Test
    public void oneTransition() {
        p.addTransition(e_pid1, q);
        assertTrue(p.getTransitioningEvents().size() == 1);
        assertTrue(p.getTransitioningEvents().contains(e_pid1));
        assertTrue(p.getNextStates(e_pid1).size() == 1);
        assertTrue(p.getNextStates(e_pid1).contains(q));
    }

    @Test
    public void twoTransitions() {
        p.addTransition(e_pid1, q);
        p.addTransition(e2_pid1, q);

        assertTrue(p.getTransitioningEvents().size() == 2);
        assertTrue(p.getTransitioningEvents().contains(e_pid1));
        assertTrue(p.getTransitioningEvents().contains(e2_pid1));
        assertTrue(p.getNextStates(e_pid1).size() == 1);
        assertTrue(p.getNextStates(e_pid1).contains(q));

        assertTrue(p.getNextStates(e2_pid1).size() == 1);
        assertTrue(p.getNextStates(e2_pid1).contains(q));
    }

    @Test(expected = AssertionError.class)
    public void addIdenticalTransition() {
        p.addTransition(e_pid1, q);
        p.addTransition(e_pid1, q);
    }

    @Test(expected = AssertionError.class)
    public void wrongEventPid() {
        p.addTransition(e3_pid2, q);
    }

}
