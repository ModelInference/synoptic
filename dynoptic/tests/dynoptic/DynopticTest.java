package dynoptic;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import mcscm.Os;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import synoptic.model.channelid.ChannelId;

import dynoptic.main.DynopticMain;
import dynoptic.main.DynopticOptions;

/**
 * Base test class for Dynoptic tests.
 * 
 * <pre>
 * Requires JUnit 4.7 or higher.
 * </pre>
 */
public class DynopticTest {
    /**
     * Can be used to find out the current test name (as of JUnit 4.7) via
     * name.getMethodName().
     **/
    @Rule
    public static TestName testName;

    /**
     * The logger instance to use across all tests.
     */
    protected static Logger logger;

    static {
        // Set up static DynopticTest state.
        testName = new TestName();
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
        List<ChannelId> channels = new ArrayList<ChannelId>(numProcesses
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

    // //////////////////////////////////////////////////

    public static String getMcPath() {
        // Determine whether to use the Linux or the OSX McScM binary.
        String osStr = null;
        if (Os.isLinux()) {
            osStr = "linux.static";
        } else if (Os.isMac()) {
        	String version = Os.getOsVersion();
        	String arch = Os.getOsArch();
            osStr = "osx-" + version + "-" + arch + ".dynamic";
        } else {
            fail("Running on an unsupported OS (not Linux, and not Mac).");
        }

        // NOTE: We assume the tests are run from synoptic/dynoptic/
        return "../bin/mcscm-1.2/verify.native." + osStr;
    }
}
