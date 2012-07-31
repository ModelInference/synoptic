package dynoptic.model.fifosys.channel;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import dynoptic.DynopticTest;

public class ChannelIdTests extends DynopticTest {

    @Test
    public void createChannelId() {
        ChannelId cid = new ChannelId(1, 2);
        assertEquals(cid.getSrcPid(), 1);
        assertEquals(cid.getDstPid(), 2);
        assertEquals(cid.toString(), "1->2");
        logger.info(cid.toString());
    }

    @Test(expected = AssertionError.class)
    @SuppressWarnings("unused")
    public void createBadChannelId() {
        ChannelId cid = new ChannelId(-1, 2);
    }

}
