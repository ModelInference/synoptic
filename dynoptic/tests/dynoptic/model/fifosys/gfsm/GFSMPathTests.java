package dynoptic.model.fifosys.gfsm;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import dynoptic.DynopticTest;
import dynoptic.util.Util;

import synoptic.model.channelid.ChannelId;
import synoptic.model.event.DistEventType;

public class GFSMPathTests extends DynopticTest {

    ChannelId cid1, cid2;
    List<ChannelId> cids;
    GFSMState s1, s2;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        cids = Util.newList(2);
        // Two process system.
        cid1 = new ChannelId(0, 1, 0);
        cid2 = new ChannelId(1, 0, 1);
        cids.add(cid1);
        cids.add(cid2);

        s1 = new GFSMState(2);
        s2 = new GFSMState(2);
    }

    @Test
    public void createPath() {
        GFSMPath p1 = new GFSMPath(s1, 1);
        GFSMPath p2 = new GFSMPath(1);
        DistEventType e = DistEventType.LocalEvent("e", 1);
        p2.prefixEventAndState(e, s2);

        GFSMPath p1p2 = new GFSMPath(p1, p2);
        p1p2.toString();
    }

}
