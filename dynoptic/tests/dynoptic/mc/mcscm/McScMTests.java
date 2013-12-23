package dynoptic.mc.mcscm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import dynoptic.DynopticTest;
import dynoptic.mc.MCResult;
import dynoptic.mc.MCSyntaxException;
import dynoptic.util.Util;

import synoptic.model.channelid.ChannelId;
import synoptic.model.event.DistEventType;

public class McScMTests extends DynopticTest {

    McScM mcscm;
    String verifyPath;
    String scmFilePrefix;

    List<ChannelId> cids;
    ChannelId cid0, cid1;

    DistEventType cExEType;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        // NOTE: hard-coded assumption about where the tests are run
        verifyPath = DynopticTest.getMcPath();
        scmFilePrefix = "./tests/dynoptic/mc/mcscm/";

        mcscm = new McScM(verifyPath);

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
            mcscm.verify("hello world", 60);
        } catch (Exception e) {
            logger.info("Verify threw an exception: " + e.toString());
            fail("Verify should not fail.");
        }

        mcscm.getVerifyResult(cids);
        fail("getVerifyResult should have thrown an exception.");
    }

    /**
     * Test verify on a safe model.
     * 
     * @throws IOException
     */
    @Test
    public void testSafeScmInput() throws IOException {
        String scmStr = fileToString(scmFilePrefix + "ABP_safe.scm");

        try {
            mcscm.verify(scmStr, 60);
        } catch (Exception e) {
            logger.info("Verify threw an exception: " + e.toString());
            fail("Verify should not fail.");
        }

        MCResult result = mcscm.getVerifyResult(cids);
        assertTrue(result.modelIsSafe());
    }

    /**
     * Test verify on an unsafe model with c-example of len 1.
     * 
     * @throws IOException
     */
    @Test
    public void testUnsafeScmInputLen1() throws IOException {
        String scmStr = fileToString(scmFilePrefix
                + "ABP_unsafe_cexample_len1.scm");

        try {
            mcscm.verify(scmStr, 60);
        } catch (Exception e) {
            logger.info("Verify threw an exception: " + e.toString());
            fail("Verify should not fail.");
        }

        MCResult result = mcscm.getVerifyResult(cids);
        assertTrue(!result.modelIsSafe());
        assertEquals(result.getCExample().getEvents().size(), 1);
        assertEquals(result.getCExample().getEvents().get(0), cExEType);
    }

    /**
     * Test verify on an unsafe model with c-example of len 2.
     * 
     * @throws IOException
     */
    @Test
    public void testUnsafeScmInputLen2() throws IOException {
        String scmStr = fileToString(scmFilePrefix
                + "ABP_unsafe_cexample_len2.scm");

        try {
            mcscm.verify(scmStr, 60);
        } catch (Exception e) {
            logger.info("Verify threw an exception: " + e.toString());
            fail("Verify should not fail.");
        }

        MCResult result = mcscm.getVerifyResult(cids);
        assertTrue(!result.modelIsSafe());
        assertTrue(result.getCExample() != null);
        assertTrue(result.getCExample().getEvents() != null);
        assertEquals(result.getCExample().getEvents().size(), 2);
        assertEquals(result.getCExample().getEvents().get(0), cExEType);
        assertEquals(result.getCExample().getEvents().get(1), cExEType);
    }
}
