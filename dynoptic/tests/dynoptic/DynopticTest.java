package dynoptic;

import static org.junit.Assert.fail;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import mcscm.Os;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import dynoptic.main.DynopticMain;
import dynoptic.main.DynopticOptions;
import dynoptic.model.channel.ChannelId;

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

    /**
     * The DynopticMain instance that is the main entry to running and
     * interfacing with Dynoptic.
     */
    public DynopticMain dyn;

    /**
     * Options that DynopticMain (dyn), defined above, is parameterized with.
     */
    public DynopticOptions opts;

    // //////////////////////////////////////////////////

    /**
     * Sets the dyn instance of DynopticMain based on passed in options. If
     * these are null, then does its best to set options that won't raise an
     * exception.
     * 
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        if (dyn != null) {
            return;
        }
        assert opts == null;
        opts = new DynopticOptions();

        if (opts.mcPath == null) {
            opts.mcPath = getMcPath();
        }
        dyn = new DynopticMain(opts);
    }

    /**
     * Can be used by tests to set custom options and initialize a particular
     * kind of DynopticMain based off of these options. If this method is not
     * used, then setUp (above) will set opts and dyn to default values.
     */
    public void setDynopticOpts(DynopticOptions opts) throws Exception {
        this.opts = opts;
        this.dyn = new DynopticMain(opts);
    }

    // //////////////////////////////////////////////////

    /**
     * Creates an all to all (fully connected graph) topology for numProcesses
     * number of processes.
     * 
     * @param numProcesses
     * @return
     */
    public Set<ChannelId> getAllToAllChannelIds(int numProcesses) {
        Set<ChannelId> channels = new LinkedHashSet<ChannelId>();

        for (int i = 0; i < numProcesses; i++) {
            for (int j = 0; j < numProcesses; j++) {
                if (i == j) {
                    continue;
                }
                channels.add(new ChannelId(i, j));
            }
        }
        return channels;
    }

    // //////////////////////////////////////////////////

    private String getMcPath() {
        // Determine whether to use the Linux or the OSX McScM binary.
        String osStr = null;
        if (Os.isLinux()) {
            osStr = "linux";
        } else if (Os.isMac()) {
            osStr = "osx";
        } else {
            fail("Running on an unsupported OS (not Linux, and not Mac).");
        }

        // NOTE: We assume the tests are run from synoptic/dynoptic/
        return "../bin/mcscm.verify." + osStr;
    }
}
