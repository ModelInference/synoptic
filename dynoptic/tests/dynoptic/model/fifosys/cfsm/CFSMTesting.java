package dynoptic.model.fifosys.cfsm;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import dynoptic.DynopticTest;
import dynoptic.model.fifosys.cfsm.fsm.FSM;
import dynoptic.model.fifosys.cfsm.fsm.FSMState;
import dynoptic.util.Util;

import synoptic.model.channelid.ChannelId;
import synoptic.model.event.DistEventType;

public class CFSMTesting extends DynopticTest {

    // Non-accepting state at pid 0/1.
    FSMState p0Init, p1Init;
    // Accepting state at pid 0/1.
    FSMState p0Accept, p1Accept;

    // cid: 0->1
    ChannelId cid;
    // [cid]
    List<ChannelId> channels;
    // cid!m, cid?m
    DistEventType p0Sm, p1Rm;
    // e_0, e_1
    DistEventType p0Le, p1Lf;

    // FSM for pids 0,1
    FSM f0, f1;

    // CFSM composed of f1 and f2.
    CFSM cfsm;

    Set<FSMState> states;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        p0Init = new FSMState(false, true, 0, 0);
        p0Accept = new FSMState(true, false, 0, 1);

        states = Util.newSet();
        states.add(p0Init);
        states.add(p0Accept);

        cid = new ChannelId(0, 1, 0);
        channels = Util.newList(1);
        channels.add(cid);

        p0Sm = DistEventType.SendEvent("m", cid);
        p0Le = DistEventType.LocalEvent("e", 0);

        p0Init.addTransition(p0Sm, p0Accept);
        p0Accept.addTransition(p0Le, p0Init);

        f0 = new FSM(0, p0Init, p0Accept, states, 2);

        // ///////////

        p1Init = new FSMState(false, true, 1, 0);
        p1Accept = new FSMState(true, false, 1, 1);

        states.clear();
        states.add(p1Init);
        states.add(p1Accept);

        p1Rm = DistEventType.RecvEvent("m", cid);
        p1Lf = DistEventType.LocalEvent("f", 1);

        p1Init.addTransition(p1Rm, p1Accept);
        p1Accept.addTransition(p1Lf, p1Init);

        f1 = new FSM(1, p1Init, p1Accept, states, 2);

        cfsm = new CFSM(2, channels);
        cfsm.addFSM(f0);
        cfsm.addFSM(f1);
    }

}
