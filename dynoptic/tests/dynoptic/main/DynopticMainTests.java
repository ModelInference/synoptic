package dynoptic.main;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import dynoptic.DynopticTest;

import synoptic.main.SynopticMain;
import synoptic.model.channelid.ChannelId;

public class DynopticMainTests extends DynopticTest {

    public DynopticMain dyn;
    public DynopticOptions opts;

    public List<String> getBasicArgsStr() throws Exception {
        List<String> args = new ArrayList<String>();
        args.add("-v");
        args.add(super.getMcPath());
        args.add("-o");
        args.add("test-output" + File.separator);
        return args;
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        // Reset the SynopticMain singleton reference.
        SynopticMain.instance = null;
    }

    // //////////////////// Test parsing of channel ids

    @Test
    public void parseChannelIds() {
        List<ChannelId> cids;
        cids = DynopticMain.parseChannelSpec("M:0->1;A:1->0");
        assertTrue(cids.size() == 2);
        assertEquals(cids.get(0), new ChannelId(0, 1, 0, "M"));
        assertEquals(cids.get(1), new ChannelId(1, 0, 1, "A"));

        cids = DynopticMain.parseChannelSpec("M:0->1;A:1->0;");
        assertTrue(cids.size() == 2);
        assertEquals(cids.get(0), new ChannelId(0, 1, 0, "M"));
        assertEquals(cids.get(1), new ChannelId(1, 0, 1, "A"));

        cids = DynopticMain.parseChannelSpec("M:22->100");
        assertTrue(cids.size() == 1);
        assertEquals(cids.get(0), new ChannelId(22, 100, 0, "M"));
    }

    @Test(expected = OptionException.class)
    public void parseChannelIdsErrorDuplicateChNames() {
        List<ChannelId> cids;
        cids = DynopticMain.parseChannelSpec("M:0->1;M:1->0");
    }

    @Test(expected = OptionException.class)
    public void parseChannelIdsErrorUnparseable() {
        List<ChannelId> cids;
        cids = DynopticMain.parseChannelSpec("M:0->1;blah");
    }

    // ////////////////////

    @Test(expected = OptionException.class)
    public void missingChannelSpec() throws Exception {
        List<String> args = getBasicArgsStr();
        opts = new DynopticOptions(args.toArray(new String[0]));
        dyn = new DynopticMain(opts);
    }

    @Test(expected = OptionException.class)
    public void missingLogFiles() throws Exception {
        List<String> args = getBasicArgsStr();
        args.add("-q");
        args.add("M:0->1");
        opts = new DynopticOptions(args.toArray(new String[0]));
        dyn = new DynopticMain(opts);
        dyn.run();
    }

    @Test(expected = OptionException.class)
    public void emptyLogFile() throws Exception {
        List<String> args = getBasicArgsStr();
        args.add("-q");
        args.add("M:0->1");
        args.add("../traces/EndToEndDynopticTests/empty-trace.txt");
        opts = new DynopticOptions(args.toArray(new String[0]));
        dyn = new DynopticMain(opts);
        dyn.run();
    }

    @Test
    public void runABPSuccess() throws Exception {
        List<String> args = getBasicArgsStr();
        args.add("../traces/EndToEndDynopticTests/AlternatingBitProtocol/trace_po_sr_simple.txt");
        args.add("-r");
        args.add("^(?<VTIME>)(?<TYPE>)$");
        args.add("-s");
        args.add("^--$");
        args.add("-q");
        args.add("M:0->1;A:1->0");
        args.add("-i");
        args.add("-d");
        opts = new DynopticOptions(args.toArray(new String[0]));
        dyn = new DynopticMain(opts);
        // TODO: FIX ME
        // dyn.run();
    }
}
