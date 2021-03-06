package csight.model.fifosys.channel.channelstate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import csight.CSightTest;
import csight.util.Util;

import synoptic.model.channelid.ChannelId;
import synoptic.model.event.DistEventType;

public class MutibleMultiChStateTests extends CSightTest {

    ChannelId cid1;
    ChannelId cid2;
    List<ChannelId> cids;
    MutableMultiChState mc;
    MutableMultiChState mc2;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        cids = Util.newList(2);
        cid1 = new ChannelId(1, 2, 0);
        cid2 = new ChannelId(2, 1, 1);
        cids.add(cid1);
        cids.add(cid2);

        mc = MutableMultiChState.fromChannelIds(cids);
        mc2 = MutableMultiChState.fromChannelIds(cids);
    }

    @Test
    public void toStringCheck() {
        logger.info(mc.toString());
        mc.hashCode();
    }

    @Test
    public void isEmpty() {
        assertTrue(mc.isEmpty());
        assertTrue(mc.isEmptyForPid(1));
        assertTrue(mc.isEmptyForPid(2));
        assertTrue(mc.isEmptyForPid(42));
    }

    @Test
    public void enqueueDequeueSeq() {
        DistEventType e = DistEventType.SendEvent("e", cid1);

        int h1 = mc.hashCode();

        // Enqueue e
        mc.enqueue(e);
        int h2 = mc.hashCode();
        assertTrue(h1 != h2);
        assertFalse(mc.isEmpty());
        assertFalse(mc.isEmptyForPid(2));
        assertTrue(mc.isEmptyForPid(1));

        // Peek at e.
        DistEventType e2 = mc.peek(cid1);
        assertEquals(e, e2);
        assertFalse(mc.isEmpty());
        assertFalse(mc.isEmptyForPid(2));
        assertTrue(mc.isEmptyForPid(1));

        // Dequeue e (using cid)
        e2 = mc.dequeue(cid1);
        assertEquals(e, e2);
        assertTrue(mc.isEmpty());
        assertTrue(mc.isEmptyForPid(1));
        assertTrue(mc.isEmptyForPid(2));

        // Enqueue e, again, but dequeue this time using e (not the cid).
        mc.enqueue(e);

        DistEventType recvE = DistEventType.RecvEvent("e", cid1);
        mc.dequeue(recvE);
    }

    @Test
    public void cloneMCState() {
        DistEventType e = DistEventType.SendEvent("e", cid1);
        mc.enqueue(e);

        mc2 = mc.clone();
        assertEquals(mc, mc2);
    }

    @Test
    public void equals() {
        DistEventType e = DistEventType.SendEvent("e", cid1);
        mc.enqueue(e);

        assertFalse(mc.equals(null));
        assertFalse(mc.equals(""));
        assertTrue(mc.equals(mc));

        DistEventType e2 = DistEventType.SendEvent("e", cid1);
        mc2.enqueue(e2);
        assertEquals(mc, mc2);
        assertEquals(mc2, mc);
    }

    @Test
    public void topKHashCode() {
        DistEventType e = DistEventType.SendEvent("e", cid1);
        mc.enqueue(e);
        assertFalse(mc.topKOfQueuesHash(1) == mc2.topKOfQueuesHash(1));
        assertFalse(mc.topKOfQueuesHash(2) == mc2.topKOfQueuesHash(2));

        DistEventType e2 = DistEventType.SendEvent("e", cid1);
        mc2.enqueue(e2);

        assertTrue(mc.topKOfQueuesHash(1) == mc2.topKOfQueuesHash(1));
        assertTrue(mc.topKOfQueuesHash(2) == mc2.topKOfQueuesHash(2));
        assertTrue(mc.topKOfQueuesHash(3) == mc2.topKOfQueuesHash(3));
        assertTrue(mc.topKOfQueuesHash(4) == mc2.topKOfQueuesHash(4));

        DistEventType f = DistEventType.SendEvent("f", cid1);
        mc.enqueue(f);
        DistEventType f2 = DistEventType.SendEvent("f", cid1);
        mc2.enqueue(f2);

        assertTrue(mc.topKOfQueuesHash(1) == mc2.topKOfQueuesHash(1));
        assertTrue(mc.topKOfQueuesHash(2) == mc2.topKOfQueuesHash(2));
        assertTrue(mc.topKOfQueuesHash(3) == mc2.topKOfQueuesHash(3));
        assertTrue(mc.topKOfQueuesHash(4) == mc2.topKOfQueuesHash(4));

        DistEventType f3 = DistEventType.SendEvent("f", cid1);
        mc.enqueue(f3);
        // mc contents : e,f,f
        // mc2 contents: e,f

        assertTrue(mc.topKOfQueuesHash(1) == mc2.topKOfQueuesHash(1));
        assertTrue(mc.topKOfQueuesHash(2) == mc2.topKOfQueuesHash(2));
        assertFalse(mc.topKOfQueuesHash(3) == mc2.topKOfQueuesHash(3));
        assertFalse(mc.topKOfQueuesHash(4) == mc2.topKOfQueuesHash(4));
    }

}
