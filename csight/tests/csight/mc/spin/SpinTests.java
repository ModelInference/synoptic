package csight.mc.spin;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;

import csight.CSightTest;
import csight.mc.MCSyntaxException;
import csight.mc.mcscm.Os;
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
        } catch (TimeoutException e) {
            fail("Verify should not have timed out.");
        }
    }

    /**
     * A test for the compilation of Spin's pan model checker.
     * 
     * @throws IOException
     * @throws InterruptedException
     * @throws TimeoutException
     */
    @Test
    public void testPrepare() throws IOException, InterruptedException,
            TimeoutException {
        assertTrue("Cannot write to target directory.",
                new File(".").canWrite());
        assertTrue("Cannot read from target directory.",
                new File(".").canWrite());

        File spinMC = new File(super.getMcPath("spin"));
        assertTrue("Cannot execute Spin.", spinMC.canExecute());

        spin.prepare("active proctype p1() { printf(\"Compile Test.\"); }", 20);

        assertTrue("csight.pml was not written.",
                new File("csight.pml").exists());
        assertTrue("Cannot read csight.pml.", new File("csight.pml").canRead());

        // Select the correct name for pan.
        File pan = new File(Os.isWindows() ? "./pan.exe" : "./pan");
        assertTrue("Pan was not compiled.", pan.exists());
        assertTrue("Cannot execute pan.", pan.canExecute());
    }
}
