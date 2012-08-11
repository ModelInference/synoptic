package dynoptic.model.fifosys.channel.channelid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import dynoptic.DynopticTest;
import dynoptic.invariants.AlwaysFollowedBy;
import dynoptic.invariants.BinaryInvariant;
import dynoptic.model.alphabet.EventType;
import dynoptic.model.fifosys.channel.channelid.ChannelId;
import dynoptic.model.fifosys.channel.channelid.InvChannelId;
import dynoptic.model.fifosys.channel.channelid.LocalEventsChannelId;

public class ChannelIdTests extends DynopticTest {

    @Test
    public void createChannelId() {
        ChannelId cid = new ChannelId(1, 2, 0);
        assertEquals(cid.getSrcPid(), 1);
        assertEquals(cid.getDstPid(), 2);
        assertEquals(cid.getScmId(), 0);
        assertEquals(cid.toString(), "1->2");
        logger.info(cid.toString());
    }

    @Test(expected = AssertionError.class)
    @SuppressWarnings("unused")
    public void createBadChannelId() {
        ChannelId cid = new ChannelId(-1, 2, 0);
    }

    @Test
    public void invChannelId() {
        EventType e = EventType.LocalEvent("e", 0);
        EventType f = EventType.LocalEvent("f", 0);

        BinaryInvariant inv = new AlwaysFollowedBy(e, f);
        InvChannelId cid = new InvChannelId(inv, 0);

        assertEquals(cid.getSrcPid(), 0);
        assertEquals(cid.getDstPid(), 0);
        assertEquals(cid.getScmId(), 0);
        logger.info(cid.toString());
        cid.hashCode();

        // Equality.
        assertFalse(cid.equals(null));
        assertFalse(cid.equals(""));
        assertTrue(cid.equals(cid));
        assertFalse(cid.equals(new LocalEventsChannelId(42)));
        BinaryInvariant inv2 = new AlwaysFollowedBy(e, e);
        assertFalse(cid.equals(new InvChannelId(inv2, 0)));

        EventType e2 = EventType.LocalEvent("e", 0);
        EventType f2 = EventType.LocalEvent("f", 0);

        inv2 = new AlwaysFollowedBy(e2, f2);
        InvChannelId cid2 = new InvChannelId(inv2, 0);
        assertTrue(cid.equals(cid2));
    }

    @Test
    public void localEventsChannelId() {
        LocalEventsChannelId cid = new LocalEventsChannelId(42);

        assertEquals(cid.getSrcPid(), Integer.MAX_VALUE);
        assertEquals(cid.getDstPid(), Integer.MAX_VALUE);
        assertEquals(cid.getScmId(), 42);
        logger.info(cid.toString());
        cid.hashCode();

        // Equality.
        assertFalse(cid.equals(null));
        assertFalse(cid.equals(""));
        assertTrue(cid.equals(cid));

        LocalEventsChannelId cid2 = new LocalEventsChannelId(42);
        assertTrue(cid.equals(cid2));

        EventType e = EventType.LocalEvent("e", 0);
        BinaryInvariant inv = new AlwaysFollowedBy(e, e);
        assertFalse(cid.equals(new InvChannelId(inv, 42)));
    }

    @Test
    public void localEventsChannelIdMapping() {
        LocalEventsChannelId cid = new LocalEventsChannelId(42);
        EventType e = EventType.LocalEvent("e", 0);
        cid.addLocalEventString(e, "e");
        assertEquals(e, cid.getEventType("e"));

        LocalEventsChannelId cid2 = new LocalEventsChannelId(42);
        assertFalse(cid.equals(cid2));

        assertFalse(cid.equals(new ChannelId(Integer.MAX_VALUE, 0, 42)));
    }

    @Test
    public void channelId() {
        ChannelId cid = new ChannelId(1, 2, 0);

        assertFalse(cid.equals(null));
        assertFalse(cid.equals(""));
        assertTrue(cid.equals(cid));

        ChannelId cid2 = new ChannelId(1, 2, 0);
        assertEquals(cid, cid2);

        cid2 = new ChannelId(3, 2, 0);
        assertFalse(cid.equals(cid2));

        cid2 = new ChannelId(1, 3, 0);
        assertFalse(cid.equals(cid2));

        cid2 = new ChannelId(1, 2, 1);
        assertFalse(cid.equals(cid2));
    }

}
