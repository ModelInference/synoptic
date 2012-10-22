package dynoptic.model.fifosys.gfsm.observed.fifosys;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import dynoptic.DynopticTest;
import dynoptic.model.fifosys.channel.channelstate.ImmutableMultiChState;
import dynoptic.model.fifosys.gfsm.observed.ObsFSMState;
import dynoptic.model.fifosys.gfsm.observed.ObsMultFSMState;

import synoptic.model.channelid.ChannelId;

public class ObsFifoSysStateTests extends DynopticTest {

    ChannelId cid1;
    ChannelId cid2;
    List<ChannelId> cids;

    @Test
    public void create() {

        List<ObsFSMState> P = new ArrayList<ObsFSMState>();
        ObsFSMState p0 = ObsFSMState.namedObsFSMState(0, "p", false, true);
        ObsFSMState p1 = ObsFSMState.namedObsFSMState(1, "q", false, true);
        P.add(p0);
        P.add(p1);

        ObsMultFSMState obsFSMState = new ObsMultFSMState(P);

        cids = new ArrayList<ChannelId>(2);
        cid1 = new ChannelId(0, 1, 0);
        cid2 = new ChannelId(1, 0, 1);
        cids.add(cid1);
        cids.add(cid2);
        ImmutableMultiChState Pmc = ImmutableMultiChState.fromChannelIds(cids);

        ObsFifoSysState s = ObsFifoSysState.getFifoSysState(obsFSMState, Pmc,
                false);

        assertTrue(s.isAccept());
        assertFalse(s.isInitial());
        assertEquals(s.getNumProcesses(), 2);

        assertFalse(s.equals(null));
        assertFalse(s.equals(""));
        assertTrue(s.equals(s));

        logger.info(s.toString());

        // Retrieve another instance of fifo sys state, and make sure that the
        // internal cache returns the prior instance.
        ObsFifoSysState s2 = ObsFifoSysState.getFifoSysState(obsFSMState, Pmc,
                false);

        assertTrue(s == s2);
    }

}
