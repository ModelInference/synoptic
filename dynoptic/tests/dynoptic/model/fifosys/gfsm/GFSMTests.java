package dynoptic.model.fifosys.gfsm;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import dynoptic.DynopticTest;
import dynoptic.model.alphabet.EventType;
import dynoptic.model.fifosys.cfsm.fsm.FSMState;
import dynoptic.model.fifosys.channel.ChannelId;
import dynoptic.model.fifosys.channel.MultiChannelState;
import dynoptic.model.fifosys.gfsm.trace.ObservedEvent;
import dynoptic.model.fifosys.gfsm.trace.ObservedFSMState;
import dynoptic.model.fifosys.gfsm.trace.ObservedFifoSysState;
import dynoptic.model.fifosys.gfsm.trace.Trace;

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
    public void createEmptyGFSM() {
        GFSM g = new GFSM(2, this.getAllToAllChannelIds(2));
    }

    @Test
    @SuppressWarnings("unused")
    public void createGFSMFromTrace() {
        List<ObservedFSMState> Pi = new ArrayList<ObservedFSMState>();
        List<ObservedFSMState> Pt = new ArrayList<ObservedFSMState>();

        ObservedFSMState p0i = ObservedFSMState
                .ObservedInitialFSMState(0, null);
        Pi.add(p0i);

        ObservedFSMState p0t = ObservedFSMState.ObservedTerminalFSMState(0,
                null);
        Pt.add(p0t);

        // Empty channeldIds list -- no queues.
        List<ChannelId> cids = new ArrayList<ChannelId>();
        MultiChannelState PiChstate = MultiChannelState.fromChannelIds(cids);
        MultiChannelState PtChstate = MultiChannelState.fromChannelIds(cids);

        ObservedFifoSysState Si = new ObservedFifoSysState(Pi, PiChstate);
        ObservedFifoSysState St = new ObservedFifoSysState(Pt, PtChstate);

        // Si -> St
        ObservedEvent e = ObservedEvent.LocalEvent("e", 0);
        Si.addTransition(e, St);

        List<Trace> traces = new ArrayList<Trace>(1);

        Trace trace = new Trace(Si, St);
        traces.add(trace);

        GFSM g = new GFSM(traces);

        // Check that the two observations were initially partitioned into a
        // single partition.
        assertTrue(g.getStates().size() == 1);
        GFSMState part = g.getStates().iterator().next();

        // Make sure both observations have the right partition as parent.
        assertTrue(Si.getParent() == part);
        assertTrue(St.getParent() == part);

        // Check basic g properties.
        assertTrue(g.getInitStates().size() == 1);
        assertTrue(g.getInitStates().contains(part));

        assertTrue(g.getInitStatesForPid(0).size() == 1);
        assertTrue(g.getInitStatesForPid(0).contains(part));

        assertTrue(g.getAcceptStates().size() == 1);
        assertTrue(g.getAcceptStates().contains(part));

        assertTrue(g.getAcceptStatesForPid(0).size() == 1);
        assertTrue(g.getAcceptStatesForPid(0).contains(part));

        // Check the one partition in g.
        assertTrue(part.isInitial());
        assertTrue(part.isAccept());

        assertTrue(part.isAcceptForPid(0));
        assertTrue(part.isInitForPid(0));

        assertTrue(part.getNextStates().size() == 1);
        assertTrue(part.getNextStates().contains(part));

        assertTrue(part.getTransitioningEvents().size() == 1);
        assertTrue(part.getNextStates(e).size() == 1);
        assertTrue(part.getNextStates(e).contains(part));
    }
}
