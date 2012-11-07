package dynoptic.model.fifosys.channel.channelstate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import dynoptic.DynopticTest;

import synoptic.model.channelid.ChannelId;
import synoptic.model.event.DistEventType;

public class ChStateTests extends DynopticTest {

    ChannelId cid;
    ChState<DistEventType> s;
    ChState<DistEventType> s2;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        cid = new ChannelId(1, 2, 0);
        s = new ChState<DistEventType>(cid);
    }

    @Test
    public void createChannelState() {
        logger.info(s.toString());
        s.hashCode();
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void emptyDequeue() {
        assertEquals(s.size(), 0);
        s.dequeue();
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void emptyPeek() {
        assertEquals(s.size(), 0);
        s.peek();
    }

    @Test
    public void enqueue() {
        DistEventType e = DistEventType.SendEvent("m", cid);

        assertEquals(s.size(), 0);
        int h = s.hashCode();
        s.enqueue(e);
        assertEquals(s.size(), 1);
        logger.info(s.toString());
        int h2 = s.hashCode();
        assertTrue(h != h2);
    }

    @Test(expected = AssertionError.class)
    public void enqueueBadEvent1() {
        DistEventType e = DistEventType.LocalEvent("e", 1);
        s.enqueue(e);
    }

    @Test(expected = AssertionError.class)
    public void enqueueBadEvent2() {
        DistEventType e = DistEventType.RecvEvent("e", cid);
        s.enqueue(e);
    }

    @Test(expected = AssertionError.class)
    public void enqueueBadEvent3() {
        ChannelId cid2 = new ChannelId(2, 3, 1);
        DistEventType e = DistEventType.SendEvent("e", cid2);
        s.enqueue(e);
    }

    @Test
    public void enqueuePeekDequeue() {
        DistEventType e = DistEventType.SendEvent("m", cid);

        assertEquals(s.size(), 0);
        s.enqueue(e);
        assertEquals(s.size(), 1);
        assertEquals(e, s.peek());
        assertEquals(s.size(), 1);
        assertEquals(e, s.dequeue());
        assertEquals(s.size(), 0);
    }

    @Test
    public void cloneEmptyChannelState() {
        s2 = s.clone();
        assertEquals(s, s2);
    }

    @Test
    public void cloneNonEmptyChannelState() {
        DistEventType e = DistEventType.SendEvent("m", cid);
        s.enqueue(e);

        s2 = s.clone();
        assertEquals(s, s2);
    }

    @Test
    public void equality() {
        DistEventType e = DistEventType.SendEvent("m", cid);
        s.enqueue(e);

        assertFalse(s.equals(null));
        assertFalse(s.equals(""));
        assertTrue(s.equals(s));

        s2 = new ChState<DistEventType>(cid);
        DistEventType e2 = DistEventType.SendEvent("m", cid);
        s2.enqueue(e2);

        assertEquals(s, s2);
    }
}
