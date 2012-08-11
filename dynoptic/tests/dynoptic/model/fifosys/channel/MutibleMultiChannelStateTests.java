package dynoptic.model.fifosys.channel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import dynoptic.DynopticTest;
import dynoptic.model.alphabet.EventType;

public class MutibleMultiChannelStateTests extends DynopticTest {

    ChannelId cid1;
    ChannelId cid2;
    List<ChannelId> cids;
    MutableMultiChannelState mc;
    MutableMultiChannelState mc2;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        cids = new ArrayList<ChannelId>(2);
        cid1 = new ChannelId(1, 2, 0);
        cid2 = new ChannelId(2, 1, 1);
        cids.add(cid1);
        cids.add(cid2);

        mc = MutableMultiChannelState.fromChannelIds(cids);
        mc2 = MutableMultiChannelState.fromChannelIds(cids);
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
        EventType e = EventType.SendEvent("e", cid1);

        int h1 = mc.hashCode();

        // Enqueue e
        mc.enqueue(e);
        int h2 = mc.hashCode();
        assertTrue(h1 != h2);
        assertFalse(mc.isEmpty());
        assertFalse(mc.isEmptyForPid(2));
        assertTrue(mc.isEmptyForPid(1));

        // Peek at e.
        EventType e2 = mc.peek(cid1);
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

        EventType recvE = EventType.RecvEvent("e", cid1);
        mc.dequeue(recvE);
    }

    @Test
    public void cloneMCState() {
        EventType e = EventType.SendEvent("e", cid1);
        mc.enqueue(e);

        mc2 = mc.clone();
        assertEquals(mc, mc2);
    }

    @Test
    public void equals() {
        EventType e = EventType.SendEvent("e", cid1);
        mc.enqueue(e);

        assertFalse(mc.equals(null));
        assertFalse(mc.equals(""));
        assertTrue(mc.equals(mc));

        assertFalse(mc.topOfQueuesHash() == mc2.topOfQueuesHash());

        EventType e2 = EventType.SendEvent("e", cid1);
        mc2.enqueue(e2);

        assertEquals(mc, mc2);
        assertTrue(mc.topOfQueuesHash() == mc2.topOfQueuesHash());
    }

}
