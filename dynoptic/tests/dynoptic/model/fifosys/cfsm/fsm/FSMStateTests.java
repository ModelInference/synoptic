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
    EventType e;

    @Override
    public void setUp() {
        p = new FSMState(false);
        q = new FSMState(true);
        cid = new ChannelId(1, 2);
        e = EventType.SendEvent("m", cid);
    }

    @Test
    public void checkAccept() {
        assertFalse(p.isAccept());
        assertTrue(q.isAccept());
    }

    @Test
    public void simpleTransition() {
        p.addTransition(e, q);
        assertTrue(p.getTransitioningEvents().size() == 1);
        assertTrue(p.getTransitioningEvents().contains(e));
        assertTrue(p.getNextStates(e).size() == 1);
        assertTrue(p.getNextStates(e).contains(q));
    }

    @Test(expected = AssertionError.class)
    public void addIdenticalTransition() {
        p.addTransition(e, q);
        p.addTransition(e, q);
    }

}
