package dynoptic.model.fifosys.channel.channelstate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import dynoptic.DynopticTest;
import dynoptic.model.alphabet.EventType;
import dynoptic.model.fifosys.channel.channelid.ChannelId;
import dynoptic.model.fifosys.channel.channelstate.ChState;

public class ChStateTests extends DynopticTest {

    ChannelId cid;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        cid = new ChannelId(1, 2, 0);
    }

    @Test
    public void createChannelState() {
        ChState s = new ChState(cid);
        logger.info(s.toString());
        s.hashCode();
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void emptyDequeue() {
        ChState s = new ChState(cid);
        assertEquals(s.size(), 0);
        s.dequeue();
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void emptyPeek() {
        ChState s = new ChState(cid);
        assertEquals(s.size(), 0);
        s.peek();
    }

    @Test
    public void enqueue() {
        ChState s = new ChState(cid);

        EventType e = EventType.SendEvent("m", cid);

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
        ChState s = new ChState(cid);
        EventType e = EventType.LocalEvent("e", 1);
        s.enqueue(e);
    }

    @Test(expected = AssertionError.class)
    public void enqueueBadEvent2() {
        ChState s = new ChState(cid);
        EventType e = EventType.RecvEvent("e", cid);
        s.enqueue(e);
    }

    @Test(expected = AssertionError.class)
    public void enqueueBadEvent3() {
        ChState s = new ChState(cid);
        ChannelId cid2 = new ChannelId(2, 3, 1);
        EventType e = EventType.SendEvent("e", cid2);
        s.enqueue(e);
    }

    @Test
    public void enqueuePeekDequeue() {
        ChState s = new ChState(cid);
        EventType e = EventType.SendEvent("m", cid);

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
        ChState s = new ChState(cid);

        ChState s2 = s.clone();
        assertEquals(s, s2);
    }

    @Test
    public void cloneNonEmptyChannelState() {
        ChState s = new ChState(cid);
        EventType e = EventType.SendEvent("m", cid);
        s.enqueue(e);

        ChState s2 = s.clone();
        assertEquals(s, s2);
    }

    @Test
    public void equality() {
        ChState s = new ChState(cid);
        EventType e = EventType.SendEvent("m", cid);
        s.enqueue(e);

        assertFalse(s.equals(null));
        assertFalse(s.equals(""));
        assertTrue(s.equals(s));

        ChState s2 = new ChState(cid);
        EventType e2 = EventType.SendEvent("m", cid);
        s2.enqueue(e2);

        assertEquals(s, s2);
    }
}
