package dynoptic.model.fifosys.channel.channelid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import dynoptic.DynopticTest;
import dynoptic.invariants.AlwaysFollowedBy;
import dynoptic.invariants.BinaryInvariant;

import synoptic.model.channelid.ChannelId;
import synoptic.model.event.DistEventType;

public class ChannelIdTests extends DynopticTest {

    @Test
    public void createChannelId() {
        ChannelId cid = new ChannelId(1, 2, 0);
        assertEquals(cid.getSrcPid(), 1);
        assertEquals(cid.getDstPid(), 2);
        assertEquals(cid.getScmId(), 0);
        assertEquals(cid.toString(), "0-1->2:1->2");
        logger.info(cid.toString());

        ChannelId cid2 = new ChannelId(1, 2, 0, "ChName");
        assertEquals(cid2.getName(), "ChName");
        assertFalse(cid2.equals(cid));
        assertFalse(cid.equals(cid2));
        logger.info(cid2.toString());
    }

    @Test(expected = AssertionError.class)
    @SuppressWarnings("unused")
    public void createBadChannelId() {
        ChannelId cid = new ChannelId(-1, 2, 0);
    }

    @Test
    public void invChannelId() {
        DistEventType e = DistEventType.LocalEvent("e", 0);
        DistEventType f = DistEventType.LocalEvent("f", 0);

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
        BinaryInvariant inv2 = new AlwaysFollowedBy(f, e);
        assertFalse(cid.equals(new InvChannelId(inv2, 0)));

        DistEventType e2 = DistEventType.LocalEvent("e", 0);
        DistEventType f2 = DistEventType.LocalEvent("f", 0);

        inv2 = new AlwaysFollowedBy(e2, f2);
        InvChannelId cid2 = new InvChannelId(inv2, 0);
        assertTrue(cid.equals(cid2));
    }

    @Test
    public void localEventsChannelId() {
        LocalEventsChannelId cid = new LocalEventsChannelId(42);

        assertEquals(cid.getSrcPid(), 0);
        assertEquals(cid.getDstPid(), 0);
        assertEquals(cid.getScmId(), 42);
        logger.info(cid.toString());
        cid.hashCode();

        // Equality.
        assertFalse(cid.equals(null));
        assertFalse(cid.equals(""));
        assertTrue(cid.equals(cid));

        LocalEventsChannelId cid2 = new LocalEventsChannelId(42);
        assertTrue(cid.equals(cid2));

        DistEventType e = DistEventType.LocalEvent("e", 0);
        DistEventType f = DistEventType.LocalEvent("f", 0);
        BinaryInvariant inv = new AlwaysFollowedBy(e, f);
        assertFalse(cid.equals(new InvChannelId(inv, 42)));
    }

    @Test
    public void localEventsChannelIdMapping() {
        LocalEventsChannelId cid = new LocalEventsChannelId(42);
        DistEventType e = DistEventType.LocalEvent("e", 0);
        cid.addLocalEventString(e, "e");
        assertEquals(e, cid.getEventType("e"));

        LocalEventsChannelId cid2 = new LocalEventsChannelId(42);
        assertFalse(cid.equals(cid2));

        assertFalse(cid.equals(new ChannelId(Integer.MAX_VALUE, 0, 42)));
    }

    @Test
    public void channelIdEquals() {
        ChannelId cid = new ChannelId(1, 2, 0);

        assertFalse(cid.equals(null));
        assertFalse(cid.equals(""));
        assertTrue(cid.equals(cid));

        ChannelId cid2 = new ChannelId(1, 2, 0);
        assertEquals(cid, cid2);

        cid2 = new ChannelId(3, 2, 0);
        assertFalse(cid.equals(cid2));
        assertFalse(cid2.equals(cid));

        cid2 = new ChannelId(1, 3, 0);
        assertFalse(cid.equals(cid2));
        assertFalse(cid2.equals(cid));

        cid2 = new ChannelId(1, 2, 1);
        assertFalse(cid.equals(cid2));
        assertFalse(cid2.equals(cid));
    }

    // //////////////////// Test parsing of channel ids specification.

    @Test
    public void parseChannelIds() throws Exception {
        List<ChannelId> cids;
        cids = ChannelId.parseChannelSpec("M:0->1;A:1->0");
        assertTrue(cids.size() == 2);
        assertEquals(cids.get(0), new ChannelId(0, 1, 0, "M"));
        assertEquals(cids.get(1), new ChannelId(1, 0, 1, "A"));

        cids = ChannelId.parseChannelSpec("M:0->1;A:1->0;");
        assertTrue(cids.size() == 2);
        assertEquals(cids.get(0), new ChannelId(0, 1, 0, "M"));
        assertEquals(cids.get(1), new ChannelId(1, 0, 1, "A"));

        cids = ChannelId.parseChannelSpec("M:22->100");
        assertTrue(cids.size() == 1);
        assertEquals(cids.get(0), new ChannelId(22, 100, 0, "M"));
    }

    @Test(expected = Exception.class)
    public void parseChannelIdsErrorDuplicateChNames() throws Exception {
        ChannelId.parseChannelSpec("M:0->1;M:1->0");
    }

    @Test(expected = Exception.class)
    public void parseChannelIdsErrorUnparseable() throws Exception {
        ChannelId.parseChannelSpec("M:0->1;blah");
    }

}
