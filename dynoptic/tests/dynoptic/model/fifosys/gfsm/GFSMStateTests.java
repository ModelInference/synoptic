package dynoptic.model.fifosys.gfsm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import dynoptic.DynopticTest;
import dynoptic.model.fifosys.channel.channelstate.ImmutableMultiChState;
import dynoptic.model.fifosys.gfsm.observed.ObsFSMState;
import dynoptic.model.fifosys.gfsm.observed.ObsMultFSMState;
import dynoptic.model.fifosys.gfsm.observed.fifosys.ObsFifoSysState;
import dynoptic.util.Util;

import synoptic.model.channelid.ChannelId;

public class GFSMStateTests extends DynopticTest {

    ChannelId cid1;
    ChannelId cid2;
    List<ChannelId> cids;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        cids = Util.newList(2);
        // Two process system.
        cid1 = new ChannelId(0, 1, 0);
        cid2 = new ChannelId(1, 0, 1);
        cids.add(cid1);
        cids.add(cid2);
    }

    @Test
    public void createGFSMState() {
        GFSMState s = new GFSMState(1);
        assertFalse(s.isAccept());
        assertFalse(s.isAcceptForPid(0));
        assertEquals(s.getNumProcesses(), 1);
        assertEquals(s.getTransitioningEvents().size(), 0);
        logger.info(s.toString());
    }

    @Test
    public void stateWithObs() {
        GFSMState s = new GFSMState(2);

        List<ObsFSMState> obsFsmStates = Util.newList();
        obsFsmStates.add(ObsFSMState.anonObsFSMState(0, true, true));
        obsFsmStates.add(ObsFSMState.anonObsFSMState(1, true, true));
        ObsMultFSMState fsmStates = ObsMultFSMState
                .getMultiFSMState(obsFsmStates);

        ImmutableMultiChState obsChStates = ImmutableMultiChState
                .fromChannelIds(cids);
        ObsFifoSysState o = ObsFifoSysState.getFifoSysState(fsmStates,
                obsChStates);
        s.addObs(o);
        logger.info(s.toString());

        assertTrue(s.isAccept());
        assertTrue(s.isInitial());
        assertTrue(s.isAcceptForPid(0));
        assertTrue(s.isInitForPid(0));
        assertTrue(s.isAcceptForPid(1));
        assertTrue(s.isInitForPid(1));
        assertEquals(s.getTransitioningEvents().size(), 0);
        assertEquals(s.getNumProcesses(), 2);
    }
}
