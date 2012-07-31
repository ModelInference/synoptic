package dynoptic.model.fifosys.gfsm;

import org.junit.Test;

import dynoptic.DynopticTest;
import dynoptic.model.alphabet.EventType;
import dynoptic.model.fifosys.cfsm.fsm.FSMState;
import dynoptic.model.fifosys.channel.ChannelId;

public class GFSMTests extends DynopticTest {

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
        //
    }

    @Test
    @SuppressWarnings("unused")
    public void createGFSM() {
        GFSM g = new GFSM(2, this.getAllToAllChannelIds(2));
    }
}
