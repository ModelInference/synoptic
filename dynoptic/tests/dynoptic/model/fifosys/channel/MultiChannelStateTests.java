package dynoptic.model.fifosys.channel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import dynoptic.DynopticTest;
import dynoptic.model.alphabet.EventType;

public class MultiChannelStateTests extends DynopticTest {

    ChannelId cid1;
    ChannelId cid2;
    Set<ChannelId> cids;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        cids = new LinkedHashSet<ChannelId>();
        cid1 = new ChannelId(1, 2);
        cid2 = new ChannelId(2, 1);
        cids.add(cid1);
        cids.add(cid2);
    }

    @Test
    public void createMChannelState() {
        MultiChannelState mc = new MultiChannelState(cids);
        logger.info(mc.toString());
    }

    @Test
    public void isEmpty() {
        MultiChannelState mc = new MultiChannelState(cids);
        assertTrue(mc.isEmpty());
        assertTrue(mc.isEmptyForPid(1));
        assertTrue(mc.isEmptyForPid(2));
        assertTrue(mc.isEmptyForPid(42));
    }

    @Test
    public void enqueueIsEmptyPeekDequeue() {
        MultiChannelState mc = new MultiChannelState(cids);
        EventType e = EventType.SendEvent("e", cid1);
        // Enqueue e
        mc.enqueue(e);
        assertFalse(mc.isEmpty());
        assertFalse(mc.isEmptyForPid(2));
        assertTrue(mc.isEmptyForPid(1));

        // Peek at e.
        EventType e2 = mc.peek(cid1);
        assertEquals(e, e2);
        assertFalse(mc.isEmpty());
        assertFalse(mc.isEmptyForPid(2));
        assertTrue(mc.isEmptyForPid(1));

        // Dequeue e.
        e2 = mc.dequeue(cid1);
        assertEquals(e, e2);
        assertTrue(mc.isEmpty());
        assertTrue(mc.isEmptyForPid(1));
        assertTrue(mc.isEmptyForPid(2));
    }

    @Test
    public void cloneMCState() {
        MultiChannelState mc = new MultiChannelState(cids);
        EventType e = EventType.SendEvent("e", cid1);
        mc.enqueue(e);

        MultiChannelState mc2 = mc.clone();
        assertEquals(mc, mc2);
    }

    @Test
    public void equals() {
        MultiChannelState mc = new MultiChannelState(cids);
        EventType e = EventType.SendEvent("e", cid1);
        mc.enqueue(e);

        assertFalse(mc.equals(null));
        assertFalse(mc.equals(""));
        assertTrue(mc.equals(mc));

        MultiChannelState mc2 = new MultiChannelState(cids);
        EventType e2 = EventType.SendEvent("e", cid1);
        mc2.enqueue(e2);

        assertEquals(mc, mc2);
    }

}
