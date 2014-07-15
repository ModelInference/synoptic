package tests;

import java.io.File;

import main.InvariMintMain;
import main.InvariMintOptions;
import model.EncodedAutomaton;

import org.junit.Before;
import org.junit.rules.TestName;

import synoptic.main.parser.ParseException;
import synoptic.tests.SynopticLibTest;

/**
 * Base class for all InvariMint unit tests. Performs common set-up and
 * tear-down tasks, and defines methods used by multiple tests.
 */
public class InvariMintTest extends SynopticLibTest {

    protected static final String testOutputDir = "." + File.separator
            + "test-output" + File.separator;

    protected static final String tracesBasePath = File.separator + "traces"
            + File.separator;

    static {
        // Set up static SynopticLib state.
        SynopticLibTest.initialize("InvariMintTest");
    }

    /**
     * Sets up state that is necessary for running tests.
     * 
     * @throws ParseException
     */
    @Before
    public void setUp() throws ParseException {
        // Avoid setting up SynopticLib state.
    }

    // //////////////////////////////////////////////
    // Common routines to simplify testing.
    // //////////////////////////////////////////////

    /**
     * Runs invarimint with given command line args and returns the derived
     * EncodedAutomaton model.
     * 
     * @param args
     * @return
     * @throws Exception
     */
    public EncodedAutomaton runInvariMintWithArgs(String[] args)
            throws Exception {
        InvariMintOptions opts = new InvariMintOptions(args);
        InvariMintMain main = new InvariMintMain(opts);
        main.runInvariMint();
        return main.getInvariMintModel();
    }

    /**
     * Exposes SynopticLibTest's testName to derived classes.
     */
    protected TestName getTestName() {
        return testName;
    }
}
