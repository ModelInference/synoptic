package dynoptic.model.fifosys.gfsm.trace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import dynoptic.DynopticTest;
import dynoptic.model.fifosys.channel.ChannelId;
import dynoptic.model.fifosys.channel.ImmutableMultiChannelState;

public class ObservedFifoSysStateTests extends DynopticTest {

    ChannelId cid1;
    ChannelId cid2;
    List<ChannelId> cids;

    @Test
    public void create() {

        List<ObservedFSMState> P = new ArrayList<ObservedFSMState>();
        ObservedFSMState p0 = ObservedFSMState.ObservedTerminalFSMState(0, "p");
        ObservedFSMState p1 = ObservedFSMState.ObservedTerminalFSMState(1, "q");
        P.add(p0);
        P.add(p1);

        ObsMultFSMState obsFSMState = new ObsMultFSMState(P);

        cids = new ArrayList<ChannelId>(2);
        cid1 = new ChannelId(0, 1, 0);
        cid2 = new ChannelId(1, 0, 1);
        cids.add(cid1);
        cids.add(cid2);
        ImmutableMultiChannelState Pmc = ImmutableMultiChannelState
                .fromChannelIds(cids);

        ObservedFifoSysState s = ObservedFifoSysState.getFifoSysState(
                obsFSMState, Pmc);

        assertTrue(s.isAccept());
        assertFalse(s.isInitial());
        assertEquals(s.getNumProcesses(), 2);

        assertFalse(s.equals(null));
        assertFalse(s.equals(""));
        assertTrue(s.equals(s));

        logger.info(s.toString());

        // Retrieve another instance of fifo sys state, and make sure that the
        // internal cache returns the prior instance.
        ObservedFifoSysState s2 = ObservedFifoSysState.getFifoSysState(
                obsFSMState, Pmc);

        assertTrue(s == s2);
    }

}
