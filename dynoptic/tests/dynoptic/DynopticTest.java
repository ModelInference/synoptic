package dynoptic;

import java.util.logging.Level;
import java.util.logging.Logger;

import mcscm.McScM;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

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

    static {
        // Set up static SynopticLib state.
        testName = new TestName();
        logger = Logger.getLogger("DynopticTest logger");
    }

    /**
     * Can be used to derive the current test name (as of JUnit 4.7) via
     * name.getMethodName().
     **/
    @Rule
    public static TestName testName;

    /**
     * The logger instance to use across all tests.
     */
    protected static Logger logger;

    /**
     * The DynopticMain instance that is the main entry to running and
     * interfacing with Dynoptic.
     */
    public DynopticMain dyn;

    /**
     * Sets up the Synoptic state that is necessary for running tests that
     * depend on the Synoptic library.
     * 
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {

        // Determine whether to use the Linux or the OSX McScM binary.
        String osStr = null;
        if (Os.isLinux()) {
            osStr = "linux";
        } else if (Os.isMac()) {
            osStr = "osx";
        } else {
            fail("Running on an unsupported OS (not Linux, and not Mac).");
        }

        // NOTE: We assume the tests are run from synoptic/mcscm-bridge/
        verifyPath = "../bin/mcscm.verify." + osStr;
        scmFilePrefix = "./tests/mcscm/";

        logger = Logger.getLogger("TestMcScM");
        logger.setLevel(Level.INFO);
        bridge = new McScM(verifyPath);

        dyn = new DynopticMain(new DynopticOptions());
    }
}
