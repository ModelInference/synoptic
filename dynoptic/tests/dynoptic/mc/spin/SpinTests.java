package dynoptic.mc.spin;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import dynoptic.DynopticTest;
import dynoptic.mc.MCSyntaxException;
import dynoptic.util.Util;

import synoptic.model.channelid.ChannelId;
import synoptic.model.event.DistEventType;

public class SpinTests extends DynopticTest {

    Spin spin;
    String spinPath;
    String pmlFilePrefix;

    List<ChannelId> cids;
    ChannelId cid0, cid1;

    DistEventType cExEType;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        // NOTE: hard-coded assumption about where the tests are run
        spinPath = DynopticTest.getMcPath();
        pmlFilePrefix = "./tests/dynoptic/mc/spin/";

        spin = new Spin(spinPath);

        cids = Util.newList();
        cid0 = new ChannelId(1, 2, 0);
        cid1 = new ChannelId(1, 2, 1);
        cids.add(cid0);
        cids.add(cid1);

        cExEType = DistEventType.SendEvent("i", cid1);
    }

    /**
     * Bad scm input should result in a syntax error.
     * 
     * @throws IOException
     */
    @Test(expected = MCSyntaxException.class)
    public void testBadScmInput() throws IOException {
        try {
            spin.verify("hello world", 60);
        } catch (Exception e) {
            logger.info("Verify threw an exception: " + e.toString());
            fail("Verify should not fail.");
        }

        spin.getVerifyResult(cids);
        fail("getVerifyResult should have thrown an exception.");
    }

}
