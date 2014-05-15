package csight;

import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import csight.main.CSightMain;
import csight.main.CSightOptions;
import csight.mc.mcscm.Os;
import csight.model.fifosys.gfsm.observed.ObsFSMState;
import csight.model.fifosys.gfsm.observed.ObsMultFSMState;
import csight.model.fifosys.gfsm.observed.fifosys.ObsFifoSysState;
import csight.util.Util;

import synoptic.model.channelid.ChannelId;

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

        // NOTE: We assume the tests are run from synoptic/csight/
        return "../bin/mcscm/verify.native." + osStr;
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
}
