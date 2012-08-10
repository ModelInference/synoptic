package dynoptic.model.fifosys.gfsm;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import dynoptic.DynopticTest;
import dynoptic.model.fifosys.cfsm.CFSM;
import dynoptic.model.fifosys.channel.ChannelId;
import dynoptic.model.fifosys.channel.ImmutableMultiChannelState;
import dynoptic.model.fifosys.gfsm.trace.ObsMultFSMState;
import dynoptic.model.fifosys.gfsm.trace.ObservedEvent;
import dynoptic.model.fifosys.gfsm.trace.ObservedFSMState;
import dynoptic.model.fifosys.gfsm.trace.ObservedFifoSysState;
import dynoptic.model.fifosys.gfsm.trace.Trace;

public class GFSMTests extends DynopticTest {

    GFSM g;

    ObservedFifoSysState Si, St;
    ObservedEvent e;

    @Override
    public void setUp() {
        List<ObservedFSMState> Pi = new ArrayList<ObservedFSMState>();
        List<ObservedFSMState> Pt = new ArrayList<ObservedFSMState>();

        ObservedFSMState p0i = ObservedFSMState.ObservedInitialFSMState(0, "i");
        Pi.add(p0i);
        ObsMultFSMState obsPi = new ObsMultFSMState(Pi);

        ObservedFSMState p0t = ObservedFSMState
                .ObservedTerminalFSMState(0, "t");
        Pt.add(p0t);
        ObsMultFSMState obsPt = new ObsMultFSMState(Pt);

        // Empty channeldIds list -- no queues.
        List<ChannelId> cids = new ArrayList<ChannelId>();
        ImmutableMultiChannelState PiChstate = ImmutableMultiChannelState
                .fromChannelIds(cids);
        ImmutableMultiChannelState PtChstate = ImmutableMultiChannelState
                .fromChannelIds(cids);

        Si = ObservedFifoSysState.getFifoSysState(obsPi, PiChstate);
        St = ObservedFifoSysState.getFifoSysState(obsPt, PtChstate);

        // Si -> St
        e = ObservedEvent.LocalEvent("e", 0);
        Si.addTransition(e, St);

        List<Trace> traces = new ArrayList<Trace>(1);

        Trace trace = new Trace(Si, St);
        traces.add(trace);

        g = new GFSM(traces);
    }

    @Test
    @SuppressWarnings("unused")
    public void createEmptyGFSM() {
        GFSM g_ = new GFSM(2, this.getAllToAllChannelIds(2));
    }

    @Test
    public void checkGFSMFromOneTrace() {
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

    @Test
    public void gfsmToCFSM() {
        CFSM c = g.getCFSM();
        assertTrue(c.getNumProcesses() == 1);
        assertTrue(c.getInitStates().size() == 1);
        assertTrue(c.getAcceptStates().size() == 1);
        assertTrue(c.getAlphabet().size() == 1);
    }

}
