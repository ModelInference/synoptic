package dynoptic.model.fifosys.gfsm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import dynoptic.DynopticTest;
import dynoptic.model.fifosys.channel.ChannelId;
import dynoptic.model.fifosys.channel.MultiChannelState;
import dynoptic.model.fifosys.gfsm.trace.ObservedFSMState;
import dynoptic.model.fifosys.gfsm.trace.ObservedFifoSysState;

public class GFSMStateTests extends DynopticTest {

    ChannelId cid1;
    ChannelId cid2;
    List<ChannelId> cids;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        cids = new ArrayList<ChannelId>(2);
        cid1 = new ChannelId(1, 2, 0);
        cid2 = new ChannelId(2, 1, 1);
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
        GFSMState s = new GFSMState(1);

        List<ObservedFSMState> obsFsmStates = new ArrayList<ObservedFSMState>();
        obsFsmStates.add(ObservedFSMState.ObservedInitialTerminalFSMState(0));

        MultiChannelState obsChStates = new MultiChannelState(cids);
        ObservedFifoSysState o = new ObservedFifoSysState(obsFsmStates,
                obsChStates);
        s.addObs(o);
        logger.info(s.toString());

        assertTrue(s.isAccept());
        assertTrue(s.isAcceptForPid(0));
        assertTrue(s.isInitial());
        assertTrue(s.isInitialForPid(0));
        assertEquals(s.getTransitioningEvents().size(), 0);
    }
}
