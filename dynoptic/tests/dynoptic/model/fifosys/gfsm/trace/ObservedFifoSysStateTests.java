package dynoptic.model.fifosys.gfsm.trace;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import dynoptic.DynopticTest;
import dynoptic.model.fifosys.channel.ChannelId;
import dynoptic.model.fifosys.channel.MultiChannelState;

public class ObservedFifoSysStateTests extends DynopticTest {

    ChannelId cid1;
    ChannelId cid2;
    Set<ChannelId> cids;

    @Test
    public void create() {
        ObservedFSMState p = ObservedFSMState.ObservedTerminalFSMState(0, null);
        List<ObservedFSMState> P = new ArrayList<ObservedFSMState>();
        P.add(p);

        cids = new LinkedHashSet<ChannelId>();
        cid1 = new ChannelId(1, 2);
        cid2 = new ChannelId(2, 1);
        cids.add(cid1);
        cids.add(cid2);
        MultiChannelState Pmc = new MultiChannelState(cids);

        ObservedFifoSysState s = new ObservedFifoSysState(P, Pmc);
    }

}
