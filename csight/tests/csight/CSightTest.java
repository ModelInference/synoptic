package csight;

import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import csight.main.CSightMain;
import csight.main.CSightOptions;
import csight.mc.mcscm.Os;
import csight.model.fifosys.channel.channelstate.ImmutableMultiChState;
import csight.model.fifosys.gfsm.GFSM;
import csight.model.fifosys.gfsm.observed.ObsDistEventType;
import csight.model.fifosys.gfsm.observed.ObsFSMState;
import csight.model.fifosys.gfsm.observed.ObsMultFSMState;
import csight.model.fifosys.gfsm.observed.fifosys.ObsFifoSys;
import csight.model.fifosys.gfsm.observed.fifosys.ObsFifoSysState;
import csight.util.Util;

import synoptic.model.channelid.ChannelId;
import synoptic.model.event.DistEventType;

/**
 * Base test class for CSight tests.
 * 
 * <pre>
 * Requires JUnit 4.7 or higher.
 * </pre>
 */
public class CSightTest {

    protected static final String DOT_OUTPUT_FILENAME = "./test-output/test.dot";

    // Prefix for wildcard strings.
    protected static final String DOT_OUTPUT_PATH = "./test-output";
    protected static final String DOT_OUTPUT_RM_WILDCARD = "test.dot*";
    protected static final String DOT_PNG_OUTPUT_CNTR_WILDCARD = "test.dot*.png";

    /**
     * Can be used to find out the current test name (as of JUnit 4.7) via
     * name.getMethodName().
     **/
    @Rule
    public TestName testName = new TestName();

    /**
     * The logger instance to use across all tests.
     */
    protected static Logger logger;

    static {
        logger = Logger.getLogger("DynopticTest logger");
        logger.setLevel(Level.INFO);
    }

    // //////////////////////////////////////////////////

    @Before
    public void setUp() throws Exception {
        CSightOptions opts = new CSightOptions();
        opts.logLvlQuiet = false;
        opts.logLvlVerbose = true;
        opts.logLvlExtraVerbose = false;
        CSightMain.setUpLogging(opts);

        // Clear all object caches.
        ObsFifoSysState.clearCache();
        ObsFSMState.clearCache();
        ObsMultFSMState.clearCache();
    }

    // //////////////////////////////////////////////////

    /**
     * Creates an all-to-all (fully connected graph) topology for numProcesses
     * number of processes.
     * 
     * @param numProcesses
     * @return
     */
    protected List<ChannelId> getAllToAllChannelIds(int numProcesses) {
        List<ChannelId> channels = Util.newList(numProcesses
                * (numProcesses - 1));

        int scmId = 0;
        for (int i = 0; i < numProcesses; i++) {
            for (int j = 0; j < numProcesses; j++) {
                if (i == j) {
                    continue;
                }
                channels.add(new ChannelId(i, j, scmId));
                scmId++;
            }
        }
        return channels;
    }

    /**
     * Deletes all test.dot and test.dot*.png.
     */
    protected void cleanDotOutputs() {
        File outputDir = new File(DOT_OUTPUT_PATH);
        FileFilter filter = new WildcardFileFilter(DOT_OUTPUT_RM_WILDCARD);
        File[] dotFiles = outputDir.listFiles(filter);

        for (File dotFile : dotFiles) {
            dotFile.delete();
        }
    }

    /**
     * Gets the number of test.dot*.png files.
     */
    protected int getNumDotPngFiles() {
        File outputDir = new File(DOT_OUTPUT_PATH);
        FileFilter filter = new WildcardFileFilter(DOT_PNG_OUTPUT_CNTR_WILDCARD);
        File[] dotPngFiles = outputDir.listFiles(filter);

        return dotPngFiles.length;
    }

    // //////////////////////////////////////////////////

    // Backwards compatibility with old tests.
    public static String getMcPath() {
        return getMcPath("mcscm");
    }

    public static String getMcPath(String mcType) {
        String mcStr = null;
        String osStr = null;
        mcType = mcType.toLowerCase();
        // NOTE: We assume the tests are run from synoptic/csight/
        if (mcType.equals("mcscm")) {
            mcStr = "../bin/mcscm/verify.native.";
        } else if (mcType.equals("spin")) {
            mcStr = "../bin/spin/spin.";
        } else {
            fail("Unsupported model checker (not McScM or SPIN).");
        }

        // Determine whether to use the Linux, OSX or Windows binary for Spin or
        // McScm.
        if (Os.isLinux()) {
            // Determine if Linux is 64-bit
            if (Os.getOsArch().contains("64")) {
                osStr = "linux.static";
            } else {
                // NOTE: We assume the 32-bit binary file exists
                osStr = "linux-x86";
            }
        } else if (Os.isMac()) {
            String version = Os.getMajorOSXVersion();
            String arch = Os.getOsArch();
            osStr = "osx-" + version + "-" + arch + ".dynamic";
        } else if (Os.isWindows()) {
            // Windows can't run McScM.
            if (mcType.equals("mcscm")) {
                fail("McScM is not supported on Windows.");
            }
            osStr = "exe";
        } else {
            fail("Running on an unsupported OS (not Linux, Mac or Windows).");
        }

        // This ensures that the binary location is encoded correctly on the
        // current operating systems.
        File mcLoc = new File(mcStr + osStr);

        return mcLoc.toString();
    }

    /**
     * Reads a text file from the current directory and returns it as a single
     * string.
     * 
     * @throws IOException
     */
    static public String fileToString(String filePath) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(filePath));

        StringBuilder everything = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            everything.append(line);
            everything.append("\n");
        }
        in.close();
        return everything.toString();
    }

    /**
     * Create a GFSM with all singleton partitions, and a send and receive event
     * "e"
     * 
     * @return
     */
    protected GFSM createSingletonGFSM() {
        List<ObsFSMState> Pi = Util.newList();
        List<ObsFSMState> Pm = Util.newList();

        ObsFSMState p0i = ObsFSMState.namedObsFSMState(0, "M", true, true);
        ObsFSMState p1i = ObsFSMState.namedObsFSMState(1, "A", true, true);
        Pi.add(p0i);
        Pi.add(p1i);
        ObsMultFSMState obsPi = ObsMultFSMState.getMultiFSMState(Pi);

        ObsFSMState p0m = ObsFSMState.namedObsFSMState(0, "M", false, false);
        ObsFSMState p1m = ObsFSMState.namedObsFSMState(1, "A", false, false);
        Pm.add(p0m);
        Pm.add(p1m);
        ObsMultFSMState obsPm = ObsMultFSMState.getMultiFSMState(Pm);

        ChannelId cid0 = new ChannelId(0, 1, 0);
        ChannelId cid1 = new ChannelId(1, 0, 1);
        DistEventType eSend = DistEventType.SendEvent("e", cid0);
        DistEventType eRecv = DistEventType.RecvEvent("e", cid0);

        List<ChannelId> cids = Util.newList();
        cids.add(cid0);
        cids.add(cid1);

        ImmutableMultiChState PiChstate = ImmutableMultiChState
                .fromChannelIds(cids);
        ImmutableMultiChState PmChstate = PiChstate.getNextChState(eSend);

        ObsFifoSysState Si = ObsFifoSysState.getFifoSysState(obsPi, PiChstate);
        ObsFifoSysState Sm = ObsFifoSysState.getFifoSysState(obsPm, PmChstate);

        ObsDistEventType obsESend = new ObsDistEventType(eSend, 0);
        ObsDistEventType obsERecv = new ObsDistEventType(eRecv, 0);

        // Si -> Sm -> Sf
        Si.addTransition(obsESend, Sm);
        Sm.addTransition(obsERecv, Si);

        List<ObsFifoSys> traces = Util.newList(1);

        Set<ObsFifoSysState> states = Util.newSet();
        states.add(Si);
        states.add(Sm);

        ObsFifoSys trace = new ObsFifoSys(cids, Si, Si, states);
        traces.add(trace);

        return new GFSM(traces, 1);
    }
}
