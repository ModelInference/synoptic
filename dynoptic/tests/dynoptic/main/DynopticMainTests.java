package dynoptic.main;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import dynoptic.DynopticTest;
import dynoptic.invariants.AlwaysFollowedBy;
import dynoptic.invariants.AlwaysPrecedes;
import dynoptic.invariants.BinaryInvariant;
import dynoptic.invariants.EventuallyHappens;
import dynoptic.invariants.NeverFollowedBy;

import synoptic.invariants.AlwaysFollowedInvariant;
import synoptic.invariants.AlwaysPrecedesInvariant;
import synoptic.invariants.NeverFollowedInvariant;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.main.SynopticMain;
import synoptic.main.parser.TraceParser;
import synoptic.model.EventNode;
import synoptic.model.channelid.ChannelId;
import synoptic.model.event.DistEventType;

public class DynopticMainTests extends DynopticTest {

    public DynopticMain dyn;
    public DynopticOptions opts;

    public List<String> getBasicArgsStr() throws Exception {
        List<String> args = new ArrayList<String>();
        args.add("-v");
        args.add(super.getMcPath());
        args.add("-o");
        args.add("test-output" + File.separator + "test");
        return args;
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        // Reset the SynopticMain singleton reference.
        SynopticMain.instance = null;
    }

    // //////////////////// Check error conditions during options processing.

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

    // //////////////////// Test conversion of Synoptic invariants to Dynoptic
    // invariants.

    @Test
    public void convertAFby() {
        DistEventType x = DistEventType.LocalEvent("x", 0);
        DistEventType y = DistEventType.LocalEvent("y", 0);

        TemporalInvariantSet synInvs = new TemporalInvariantSet();
        synInvs.add(new AlwaysFollowedInvariant(x, y, "t"));
        List<dynoptic.invariants.BinaryInvariant> dynInvs = DynopticMain
                .synInvsToDynInvs(synInvs);
        assertTrue(dynInvs.size() == 1);

        BinaryInvariant dInv = dynInvs.iterator().next();
        assertTrue(dInv instanceof AlwaysFollowedBy);
        assertTrue(dInv.getFirst().equals(x));
        assertTrue(dInv.getSecond().equals(y));
    }

    @Test
    public void convertAP() {
        DistEventType x = DistEventType.LocalEvent("x", 0);
        DistEventType y = DistEventType.LocalEvent("y", 0);

        TemporalInvariantSet synInvs = new TemporalInvariantSet();
        synInvs.add(new AlwaysPrecedesInvariant(x, y, "t"));
        List<dynoptic.invariants.BinaryInvariant> dynInvs = DynopticMain
                .synInvsToDynInvs(synInvs);
        assertTrue(dynInvs.size() == 1);

        BinaryInvariant dInv = dynInvs.iterator().next();
        assertTrue(dInv instanceof AlwaysPrecedes);
        assertTrue(dInv.getFirst().equals(x));
        assertTrue(dInv.getSecond().equals(y));
    }

    @Test
    public void convertNFby() {
        DistEventType x = DistEventType.LocalEvent("x", 0);
        DistEventType y = DistEventType.LocalEvent("y", 0);

        TemporalInvariantSet synInvs = new TemporalInvariantSet();
        synInvs.add(new NeverFollowedInvariant(x, y, "t"));
        List<dynoptic.invariants.BinaryInvariant> dynInvs = DynopticMain
                .synInvsToDynInvs(synInvs);
        assertTrue(dynInvs.size() == 1);

        BinaryInvariant dInv = dynInvs.iterator().next();
        assertTrue(dInv instanceof NeverFollowedBy);
        assertTrue(dInv.getFirst().equals(x));
        assertTrue(dInv.getSecond().equals(y));
    }

    @Test
    public void convertEventually() {
        DistEventType x = DistEventType.newInitialDistEventType();
        DistEventType y = DistEventType.LocalEvent("y", 0);

        TemporalInvariantSet synInvs = new TemporalInvariantSet();
        synInvs.add(new AlwaysFollowedInvariant(x, y, "t"));
        List<dynoptic.invariants.BinaryInvariant> dynInvs = DynopticMain
                .synInvsToDynInvs(synInvs);
        assertTrue(dynInvs.size() == 1);

        BinaryInvariant dInv = dynInvs.iterator().next();
        assertTrue(dInv instanceof EventuallyHappens);
        assertTrue(dInv.getFirst().equals(DistEventType.INITIALEventType));
        assertTrue(((EventuallyHappens) dInv).getEvent().equals(y));
        assertTrue(dInv.getSecond().equals(y));
    }

    // //////////////////// Test file parsing.

    @Test
    public void testParseEventsFromFiles() throws Exception {
        List<String> args = getBasicArgsStr();
        args.add("../traces/EndToEndDynopticTests/simple-po-concurrency/trace.txt");
        args.add("-r");
        args.add("^(?<VTIME>)(?<TYPE>)$");
        args.add("-s");
        args.add("^--$");
        args.add("-q");
        args.add("M:0->1");
        args.add("-i");
        args.add("-d");
        opts = new DynopticOptions(args.toArray(new String[0]));
        dyn = new DynopticMain(opts);
        dyn.initializeSynoptic();

        TraceParser parser = new TraceParser(opts.regExps,
                opts.partitionRegExp, opts.separatorRegExp);
        List<String> logFilenames = opts.logFilenames;

        List<EventNode> parsedEvents = dyn.parseEventsFromFiles(parser,
                logFilenames);

        assertTrue(parsedEvents.size() == 4);
        assertTrue(dyn.getNumProcesses() == 2);

        assertTrue(dyn.getChannelIds().size() == 1);
        ChannelId cid = dyn.getChannelIds().get(0);

        int branchesProduct = 1;
        for (EventNode n : parsedEvents) {
            assertTrue(n.getEType() instanceof DistEventType);

            DistEventType etype = ((DistEventType) n.getEType());

            if (etype.getPid() == 0) {
                if (etype.isLocalEvent()) {
                    assertTrue(etype.getEType().equals("e1"));
                    branchesProduct *= 2;
                }

                if (etype.isCommEvent()) {
                    assertTrue(etype.getEType().equals("m"));
                    assertTrue(etype.isSendEvent());
                    assertTrue(etype.getChannelId() == cid);
                    branchesProduct *= 3;
                }

            } else if (etype.getPid() == 1) {
                if (etype.isLocalEvent()) {
                    assertTrue(etype.getEType().equals("f1"));
                    branchesProduct *= 5;
                }

                if (etype.isCommEvent()) {
                    assertTrue(etype.getEType().equals("m"));
                    assertTrue(etype.isRecvEvent());
                    assertTrue(etype.getChannelId() == cid);
                    branchesProduct *= 7;
                }
            }
        }
        // Make sure we've visited all cases above exactly once.
        assertTrue(branchesProduct == (2 * 3 * 5 * 7));
    }

    // //////////////////// Integration tests.

    private void runDynFromFileArgs(List<String> args) throws Exception {
        opts = new DynopticOptions(args.toArray(new String[0]));
        dyn = new DynopticMain(opts);

        long startTime = System.currentTimeMillis();
        dyn.run();
        long endTime = System.currentTimeMillis();
        long msTime = (endTime - startTime);
        long sTime = msTime / 1000;
        logger.info("Dynoptic run took: " + msTime + "ms ~ " + sTime + "s");
    }

    @Test
    public void runABPSuccess() throws Exception {
        List<String> args = getBasicArgsStr();
        args.add("-r");
        args.add("^(?<VTIME>)(?<TYPE>)$");
        args.add("-s");
        args.add("^--$");
        args.add("-q");
        args.add("M:0->1;A:1->0");
        args.add("-i");
        args.add("-d");
        args.add("../traces/EndToEndDynopticTests/AlternatingBitProtocol/trace_po_sr_simple.txt");
        runDynFromFileArgs(args);
    }

    @Test
    public void runABPLongTraceSuccess() throws Exception {
        List<String> args = getBasicArgsStr();
        args.add("-r");
        args.add("^(?<VTIME>)(?<TYPE>)$");
        args.add("-s");
        args.add("^--$");
        args.add("-q");
        args.add("M:0->1;A:1->0");
        args.add("-i");
        args.add("-d");
        args.add("../traces/AlternatingBitProtocol/src/bam");
        // runDynFromFileArgs(args);
    }

    @Test
    public void runSimpleConcurrencyFileSuccess() throws Exception {
        List<String> args = getBasicArgsStr();
        args.add("-r");
        args.add("^(?<VTIME>)(?<TYPE>)$");
        args.add("-s");
        args.add("^--$");
        args.add("-q");
        args.add("M:0->1");
        args.add("-i");
        args.add("-d");
        args.add("../traces/EndToEndDynopticTests/simple-po-concurrency/trace.txt");
        runDynFromFileArgs(args);
    }

    @Test
    public void runSimpleConcurrencyStringSuccess() throws Exception {
        List<String> args = getBasicArgsStr();
        args.add("-r");
        args.add("^(?<VTIME>)(?<TYPE>)$");
        args.add("-s");
        args.add("^--$");
        args.add("-q");
        args.add("M:0->1");

        opts = new DynopticOptions(args.toArray(new String[0]));
        dyn = new DynopticMain(opts);

        String log = "1,0 e1\n" + "0,1 f1\n" + "2,0 M!m\n" + "2,2 M?m";
        dyn.run(log);
    }
}
