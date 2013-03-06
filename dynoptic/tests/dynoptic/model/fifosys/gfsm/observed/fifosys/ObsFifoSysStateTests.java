package dynoptic.model.fifosys.gfsm.observed.fifosys;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import dynoptic.DynopticTest;
import dynoptic.model.fifosys.channel.channelstate.ImmutableMultiChState;
import dynoptic.model.fifosys.gfsm.observed.ObsDistEventType;
import dynoptic.model.fifosys.gfsm.observed.ObsFSMState;
import dynoptic.model.fifosys.gfsm.observed.ObsMultFSMState;
import dynoptic.util.Util;

import synoptic.model.channelid.ChannelId;
import synoptic.model.event.DistEventType;

public class ObsFifoSysStateTests extends DynopticTest {

    ChannelId cid1;
    ChannelId cid2;
    List<ChannelId> cids;

    ObsMultFSMState obsFSMState1, obsFSMState2;
    ImmutableMultiChState Pmc;

    ObsFifoSysState s1, s2, s3;

    @Before
    public void setUp() throws Exception {
        List<ObsFSMState> P = Util.newList();
        ObsFSMState p0 = ObsFSMState.namedObsFSMState(0, "p", false, true);
        ObsFSMState p1 = ObsFSMState.namedObsFSMState(1, "q", false, true);
        P.add(p0);
        P.add(p1);
        obsFSMState1 = ObsMultFSMState.getMultiFSMState(P);

        cids = Util.newList(2);
        cid1 = new ChannelId(0, 1, 0);
        cid2 = new ChannelId(1, 0, 1);
        cids.add(cid1);
        cids.add(cid2);
        Pmc = ImmutableMultiChState.fromChannelIds(cids);

        s1 = ObsFifoSysState.getFifoSysState(obsFSMState1, Pmc);

        // Retrieve another instance of fifo sys state, and make sure that the
        // internal cache returns the prior instance.
        s2 = ObsFifoSysState.getFifoSysState(obsFSMState1, Pmc);

        List<ObsFSMState> P2 = Util.newList();
        P2.add(p0);
        ObsFSMState p2 = ObsFSMState.namedObsFSMState(1, "r", false, true);
        P2.add(p2);
        obsFSMState2 = ObsMultFSMState.getMultiFSMState(P2);

        // Get a third instance that is different from the first two.
        s3 = ObsFifoSysState.getFifoSysState(obsFSMState2, Pmc);
    }

    @Test
    public void checkCreate() {
        assertTrue(s1.isAccept());
        assertTrue(s1.isAcceptForPid(0));
        assertTrue(s1.isAcceptForPid(1));
        assertFalse(s1.isInitial());
        assertFalse(s1.isInitForPid(0));
        assertFalse(s1.isInitForPid(1));
        assertEquals(s1.getNumProcesses(), 2);
        assertFalse(s1.equals(null));
        assertFalse(s1.equals(""));
        assertTrue(s1.equals(s1));
        assertTrue(s1.getTransitioningEvents().isEmpty());
        logger.info(s1.toString());

        assertTrue(s1 == s2);
        assertTrue(s1.equals(s2));

        assertFalse(s3.equals(s1));
    }

    @Test
    public void checkTxns() {
        DistEventType e = DistEventType.LocalEvent("e", 0);
        ObsDistEventType obsE = new ObsDistEventType(e, 0);
        s1.addTransition(obsE, s3);
        assertTrue(s1.getTransitioningEvents().size() == 1);
        assertTrue(s1.getTransitioningEvents().contains(e));

        assertTrue(s1.getNextState(obsE) == s3);
        assertTrue(s1.getNextState(e) == s3);
        assertTrue(s1.getObsTransitionByEType(e) == obsE);
    }
}
