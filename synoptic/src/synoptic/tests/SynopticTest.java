package synoptic.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import org.junit.Before;

import synoptic.main.Main;
import synoptic.main.ParseException;
import synoptic.main.TraceParser;
import synoptic.model.Action;
import synoptic.model.Graph;
import synoptic.model.LogEvent;
import synoptic.util.InternalSynopticException;

/**
 * Does not work with JUnit version < 4.8.1 <br />
 * import org.junit.Rule; <br/>
 * import org.junit.rules.TestName;
 */

/**
 * Base class for all Synoptic unit tests. Performs common set-up and tear-down
 * tasks, and defines methods used by multiple tests.
 * 
 * @author ivan
 */
public abstract class SynopticTest {
    /**
     * The default parser used by tests.
     */
    protected static TraceParser defParser;

    /**
     * Can be used to derive the current test name (as of JUnit 4.7) via
     * name.getMethodName(). NOTE: this doesn't work in @Before.
     * 
     * @Rule <br />
     *       public static TestName testName = new TestName();
     */
    public interface JUnitTestName {
        String getMethodName();
    }

    public static JUnitTestName testName = new JUnitTestName() {
        @Override
        public String getMethodName() {
            return "Test";
        }
    };
    // /////////////////////////

    // Set up the parser state.
    static {
        defParser = new TraceParser();
        try {
            defParser.addRegex("^(?<TYPE>)$");
        } catch (ParseException e) {
            throw new InternalSynopticException(e);
        }
        defParser.addSeparator("^--$");
    }

    /**
     * The logger instance to use across all tests.
     */
    protected static Logger logger = Logger.getLogger("SynopticTest Logger");
    /**
     * Default relation used in invariant mining.
     */
    public static final String defRelation = "t";

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
        Main.logLvlExtraVerbose = true;
        Main.SetUpLogging();
        Main.randomSeed = System.currentTimeMillis();
        Main.random = new Random(Main.randomSeed);
    }

    // //////////////////////////////////////////////
    // Common routines to simplify testing.
    // //////////////////////////////////////////////

    /**
     * Generates an initial graph based on a sequence of log events.
     * 
     * @param a
     *            log of events, each one in the format: (?<TYPE>)
     * @return an initial graph corresponding to the log of events
     * @throws ParseException
     * @throws InternalSynopticException
     */
    public static Graph<LogEvent> genInitialLinearGraph(String[] events)
            throws ParseException, InternalSynopticException {

        // Creates a single string out of an array of strings, joined together
        // and delimited using a newline
        StringBuilder sb = new StringBuilder();
        for (String s : events) {
            sb.append(s);
            sb.append('\n');
        }
        sb.deleteCharAt(sb.length() - 1);
        String traceStr = sb.toString();

        List<LogEvent> parsedEvents = defParser.parseTraceString(traceStr,
                testName.getMethodName(), -1);
        return defParser.generateDirectTemporalRelation(parsedEvents, true);
    }

    /**
     * Given an array of strings, create a list of corresponding LogEvent
     * instances.
     * 
     * @param strEvents
     *            A string sequence of events.
     * @return A LogEvent sequence of events.
     */
    public static List<LogEvent> getLogEventPath(String[] strEvents) {
        ArrayList<LogEvent> ret = new ArrayList<LogEvent>();
        LogEvent prevEvent = null;
        for (String strEvent : strEvents) {
            Action act = new Action(strEvent);
            LogEvent logEvent = new LogEvent(act);
            ret.add(logEvent);
            if (prevEvent != null) {
                prevEvent.addTransition(logEvent, defRelation);
            }
        }
        return ret;
    }
}
