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
import synoptic.model.LogEvent;
import synoptic.util.InternalSynopticException;

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
