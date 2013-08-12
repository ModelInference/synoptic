package dynoptic.model.fifosys.gfsm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import dynoptic.DynopticTest;
import dynoptic.model.export.GraphExporter;
import dynoptic.model.fifosys.cfsm.CFSM;
import dynoptic.model.fifosys.channel.channelstate.ImmutableMultiChState;
import dynoptic.model.fifosys.gfsm.observed.ObsDistEventType;
import dynoptic.model.fifosys.gfsm.observed.ObsFSMState;
import dynoptic.model.fifosys.gfsm.observed.ObsMultFSMState;
import dynoptic.model.fifosys.gfsm.observed.fifosys.ObsFifoSys;
import dynoptic.model.fifosys.gfsm.observed.fifosys.ObsFifoSysState;
import dynoptic.util.Util;

import synoptic.model.channelid.ChannelId;
import synoptic.model.event.DistEventType;

public class GFSMTests extends DynopticTest {

    GFSM g;

    ObsFifoSysState Si, St;
    ObsDistEventType e;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        List<ObsFSMState> Pi = Util.newList();
        List<ObsFSMState> Pt = Util.newList();

        ObsFSMState p0i = ObsFSMState.namedObsFSMState(0, "i", true, false);
        Pi.add(p0i);
        ObsMultFSMState obsPi = ObsMultFSMState.getMultiFSMState(Pi);

        ObsFSMState p0t = ObsFSMState.namedObsFSMState(0, "t", false, true);
        Pt.add(p0t);
        ObsMultFSMState obsPt = ObsMultFSMState.getMultiFSMState(Pt);

        // Empty channeldIds list -- no queues.
        List<ChannelId> cids = Util.newList();
        ImmutableMultiChState PiChstate = ImmutableMultiChState
                .fromChannelIds(cids);
        ImmutableMultiChState PtChstate = ImmutableMultiChState
                .fromChannelIds(cids);

        Si = ObsFifoSysState.getFifoSysState(obsPi, PiChstate);
        St = ObsFifoSysState.getFifoSysState(obsPt, PtChstate);

        // Si -> St
        e = new ObsDistEventType(DistEventType.LocalEvent("e", 0), 1);
        Si.addTransition(e, St);

        List<ObsFifoSys> traces = Util.newList(1);

        Set<ObsFifoSysState> states = Util.newSet();
        states.add(Si);
        states.add(St);
        ObsFifoSys trace = new ObsFifoSys(cids, Si, St, states);
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
        // single partition (using default queue-based partitioning).
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
        CFSM c = g.getCFSM(false);
        assertTrue(c.getNumProcesses() == 1);
        assertTrue(c.getInitStates().size() == 1);
        assertTrue(c.getAcceptStates().size() == 1);
        assertTrue(c.getAlphabet().size() == 1);
    }

    @Test
    public void exportGFSM() {
        cleanDotOutputs();
        GraphExporter.exportGFSM(DOT_OUTPUT_FILENAME, g);
        assertTrue(new File(DOT_OUTPUT_FILENAME).exists());

        GraphExporter.generatePngFileFromDotFile(DOT_OUTPUT_FILENAME);
        assertEquals(1, getNumDotPngFiles());
    }
}
