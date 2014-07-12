package csight.mc.spin;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import csight.CSightTest;
import csight.mc.MCSyntaxException;
import csight.util.Util;

import synoptic.model.channelid.ChannelId;
import synoptic.model.event.DistEventType;

public class SpinTests extends CSightTest {

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
        spinPath = CSightTest.getMcPath("spin");
        pmlFilePrefix = "./tests/csight/mc/spin/";

        spin = new Spin(spinPath);

        cids = Util.newList();
        cid0 = new ChannelId(1, 2, 0);
        cid1 = new ChannelId(1, 2, 1);
        cids.add(cid0);
        cids.add(cid1);

        cExEType = DistEventType.SendEvent("i", cid1);
    }

    /**
     * Bad Promela input should result in a syntax error. This happens during
     * verify as the Promela has to be checked before it gets converted to C.
     * 
     * @throws IOException
     */
    @Test(expected = MCSyntaxException.class)
    public void testBadPromelaInput() throws IOException {
        try {
            spin.verify("hello world", 60);
            fail("Verify should have thrown an exception.");
        } catch (InterruptedException e) {
            fail("Verify should not have been interrupted.");
        }
    }
}
