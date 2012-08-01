package dynoptic.model.fifosys.channel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import dynoptic.DynopticTest;
import dynoptic.model.alphabet.EventType;

public class ChannelStateTests extends DynopticTest {

    ChannelId cid;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        cid = new ChannelId(1, 2);
    }

    @Test
    public void createChannelState() {
        ChannelState s = new ChannelState(cid);
        logger.info(s.toString());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void emptyDequeue() {
        ChannelState s = new ChannelState(cid);
        assertEquals(s.size(), 0);
        s.dequeue();
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void emptyPeek() {
        ChannelState s = new ChannelState(cid);
        assertEquals(s.size(), 0);
        s.peek();
    }

    @Test
    public void enqueue() {
        ChannelState s = new ChannelState(cid);

        EventType e = EventType.SendEvent("m", cid);

        assertEquals(s.size(), 0);
        s.enqueue(e);
        assertEquals(s.size(), 1);
    }

    @Test(expected = AssertionError.class)
    public void enqueueBadEvent1() {
        ChannelState s = new ChannelState(cid);
        EventType e = EventType.LocalEvent("e", 1);
        s.enqueue(e);
    }

    @Test(expected = AssertionError.class)
    public void enqueueBadEvent2() {
        ChannelState s = new ChannelState(cid);
        EventType e = EventType.RecvEvent("e", cid);
        s.enqueue(e);
    }

    @Test(expected = AssertionError.class)
    public void enqueueBadEvent3() {
        ChannelState s = new ChannelState(cid);
        ChannelId cid2 = new ChannelId(2, 3);
        EventType e = EventType.SendEvent("e", cid2);
        s.enqueue(e);
    }

    @Test
    public void enqueuePeekDequeue() {
        ChannelState s = new ChannelState(cid);
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
        ChannelState s = new ChannelState(cid);

        ChannelState s2 = s.clone();
        assertEquals(s, s2);
    }

    @Test
    public void cloneNonEmptyChannelState() {
        ChannelState s = new ChannelState(cid);
        EventType e = EventType.SendEvent("m", cid);
        s.enqueue(e);

        ChannelState s2 = s.clone();
        assertEquals(s, s2);
    }

    @Test
    public void equality() {
        ChannelState s = new ChannelState(cid);
        EventType e = EventType.SendEvent("m", cid);
        s.enqueue(e);

        assertFalse(s.equals(null));
        assertFalse(s.equals(""));
        assertTrue(s.equals(s));

        ChannelState s2 = new ChannelState(cid);
        EventType e2 = EventType.SendEvent("m", cid);
        s2.enqueue(e2);

        assertEquals(s, s2);
    }
}
