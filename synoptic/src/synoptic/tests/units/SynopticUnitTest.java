package synoptic.tests.units;

import java.util.Random;
import java.util.logging.Logger;

import org.junit.Before;

import synoptic.main.Main;
import synoptic.main.ParseException;

/**
 * Base class for all Synoptic unit tests. Performs common set-up and tear-down
 * tasks.
 * 
 * @author ivan
 */
abstract class SynopticUnitTest {
    protected static Logger logger = Logger
            .getLogger("SynopticUnitTest Logger");

    /**
     * Sets up the Synoptic state that is necessary for running unit tests in
     * general.
     * 
     * @throws ParseException
     */
    @Before
    public void setUp() throws ParseException {
        Main.recoverFromParseErrors = false;
        Main.ignoreNonMatchingLines = false;
        Main.debugParse = false;
        // Main.logLvlExtraVerbose = true;
        Main.SetUpLogging();
        Main.randomSeed = new Long(0);
        Main.random = new Random(Main.randomSeed);
    }
}
