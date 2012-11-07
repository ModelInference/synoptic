package dynoptic.model.fifosys.gfsm;

import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import dynoptic.DynopticTest;
import dynoptic.model.fifosys.channel.channelstate.ImmutableMultiChState;
import dynoptic.model.fifosys.gfsm.observed.ObsDistEventType;
import dynoptic.model.fifosys.gfsm.observed.ObsFSMState;
import dynoptic.model.fifosys.gfsm.observed.ObsMultFSMState;
import dynoptic.model.fifosys.gfsm.observed.fifosys.ObsFifoSysState;
import dynoptic.util.Util;

import synoptic.model.channelid.ChannelId;
import synoptic.model.event.DistEventType;

public class GFSMPathTests extends DynopticTest {

    ChannelId cid1, cid2;
    List<ChannelId> cids;
    GFSMState g1, g2, g3;
    ObsMultFSMState obsFSMState1, obsFSMState2;
    ImmutableMultiChState Pmc;
    ObsFifoSysState s1, s2;
    DistEventType e1;

    public GFSMState fromOneObs(int numProcesses, ObsFifoSysState obs) {
        Set<ObsFifoSysState> set = Util.newSet();
        set.add(obs);
        return new GFSMState(numProcesses, set);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        cids = Util.newList(2);
        // Two process system.
        cid1 = new ChannelId(0, 1, 0);
        cid2 = new ChannelId(1, 0, 1);
        cids.add(cid1);
        cids.add(cid2);

        Pmc = ImmutableMultiChState.fromChannelIds(cids);

        List<ObsFSMState> P = Util.newList();
        ObsFSMState p0 = ObsFSMState.namedObsFSMState(0, "p", false, true);
        ObsFSMState p1 = ObsFSMState.namedObsFSMState(1, "q", false, true);
        P.add(p0);
        P.add(p1);
        obsFSMState1 = ObsMultFSMState.getMultiFSMState(P);

        s1 = ObsFifoSysState.getFifoSysState(obsFSMState1, Pmc);

        List<ObsFSMState> P2 = Util.newList();
        P2.add(p0);
        ObsFSMState p2 = ObsFSMState.namedObsFSMState(1, "r", false, true);
        P2.add(p2);
        obsFSMState2 = ObsMultFSMState.getMultiFSMState(P2);

        // Get a third instance that is different from the first two.
        s2 = ObsFifoSysState.getFifoSysState(obsFSMState2, Pmc);

        e1 = DistEventType.LocalEvent("e", 0);
        ObsDistEventType obsE = new ObsDistEventType(e1, 0);
        s1.addTransition(obsE, s2);

        g1 = fromOneObs(2, s1);
        g2 = fromOneObs(2, s2);
    }

    @Test
    public void createPath() {
        GFSMPath p1 = new GFSMPath(g1, 1);
        GFSMPath p2 = new GFSMPath(1);
        p2.prefixEventAndState(e1, g2);

        GFSMPath p1p2 = new GFSMPath(p1, p2);
        p1p2.toString();
    }

}
