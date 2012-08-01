package dynoptic.model.fifosys.channel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import dynoptic.DynopticTest;

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
    public void equality() {
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
