package dynoptic;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileFilter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import mcscm.Os;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import dynoptic.main.DynopticMain;
import dynoptic.main.DynopticOptions;
import dynoptic.model.fifosys.gfsm.observed.ObsFSMState;
import dynoptic.model.fifosys.gfsm.observed.ObsMultFSMState;
import dynoptic.model.fifosys.gfsm.observed.fifosys.ObsFifoSysState;
import dynoptic.util.Util;

import synoptic.model.channelid.ChannelId;

/**
 * Base test class for Dynoptic tests.
 * 
 * <pre>
 * Requires JUnit 4.7 or higher.
 * </pre>
 */
public class DynopticTest {

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
        DynopticOptions opts = new DynopticOptions();
        opts.logLvlQuiet = false;
        opts.logLvlVerbose = true;
        opts.logLvlExtraVerbose = false;
        DynopticMain.setUpLogging(opts);

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

    public static String getMcPath() {
        // Determine whether to use the Linux or the OSX McScM binary.
        String osStr = null;
        if (Os.isLinux()) {
            osStr = "linux.static";
        } else if (Os.isMac()) {
            String version = Os.getMajorOSXVersion();
            String arch = Os.getOsArch();
            osStr = "osx-" + version + "-" + arch + ".dynamic";
        } else {
            fail("Running on an unsupported OS (not Linux, and not Mac).");
        }

        // NOTE: We assume the tests are run from synoptic/dynoptic/
        return "../bin/mcscm/verify.native." + osStr;
    }
}
