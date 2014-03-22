package synoptic.tests;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.rules.TestName;

import junit.framework.Assert;

import synoptic.invariants.miners.ITOInvariantMiner;
import synoptic.main.SynopticMain;
import synoptic.main.parser.ParseException;
import synoptic.main.parser.TraceParser;
import synoptic.model.ChainsTraceGraph;
import synoptic.model.DAGsTraceGraph;
import synoptic.model.EventNode;
import synoptic.model.PartitionGraph;
import synoptic.model.TraceGraph;
import synoptic.model.event.DistEventType;
import synoptic.model.event.Event;
import synoptic.model.event.EventType;
import synoptic.model.event.StringEventType;
import synoptic.util.InternalSynopticException;

/**
 * Base class for all Synoptic project tests. Performs common set-up and
 * tear-down tasks, and defines methods used by multiple tests.
 * 
 * <pre>
 * Requires JUnit 4.7 or higher.
 * </pre>
 */
public abstract class SynopticTest extends SynopticLibTest {

    protected static final String testOutputDir = "." + File.separator
            + "test-output" + File.separator;

    static {
        // Set up static SynopticLib state.
        SynopticLibTest.initialize("SynopticTest Logger");
    }

    /**
     * Sets up the Synoptic state that is necessary for running tests associated
     * with the Synoptic project.
     * 
     * @throws ParseException
     */
    @Before
    public void setUp() throws ParseException {
        // Set up SynopticLib state.
        super.setUp();
        SynopticMain.getInstanceWithExistenceCheck().options.enablePerfDebugging = false;
    }

    // //////////////////////////////////////////////
    // Common routines to simplify testing.
    // //////////////////////////////////////////////

    /**
     * Exposes SynopticLibTest's testName to derived classes.
     */
    protected TestName getTestName() {
        return testName;
    }

    /**
     * Converts an array of strings into a list of EventType objects. Does not
     * handle INITIAL or TERMINAL events types.
     */
    public List<EventType> stringsToStringEventTypes(String[] types) {
        ArrayList<EventType> ret = new ArrayList<EventType>(types.length);
        for (int i = 0; i < types.length; i++) {
            ret.add(new StringEventType(types[i]));
        }
        return ret;
    }

    /**
     * Converts an array of strings containing event types and an corresponding
     * array of host ids into a list of DistEventType objects. Does not handle
     * INITIAL or TERMINAL events types.
     */
    public List<EventType> stringsToDistEventTypes(String[] types, String[] pids) {
        Assert.assertTrue(types.length == pids.length);
        ArrayList<EventType> ret = new ArrayList<EventType>(types.length);
        for (int i = 0; i < types.length; i++) {
            ret.add(new DistEventType(types[i], pids[i]));
        }
        return ret;
    }

    /**
     * Constructs the default parser used by tests. Note: the parser may not be
     * re-used for parsing different traces (it is stateful).
     */
    public static TraceParser genDefParser() {
        TraceParser parser = new TraceParser();
        try {
            parser.addRegex("^(?<TYPE>)$");
        } catch (ParseException e) {
            throw new InternalSynopticException(e);
        }
        parser.addPartitionsSeparator("^--$");
        return parser;
    }

    /**
     * Constructs a parser used by tests concerning an integer time. Note: the
     * parser may not be re-used for parsing different traces (it is stateful).
     */
    public static TraceParser genITimeParser() {
        TraceParser parser = new TraceParser();
        try {
            parser.addRegex("^(?<TYPE>)(?<TIME>)$");
        } catch (ParseException e) {
            throw new InternalSynopticException(e);
        }
        parser.addPartitionsSeparator("^--$");
        return parser;
    }

    /**
     * Constructs a parser used by tests concerning a float time. Note: the
     * parser may not be re-used for parsing different traces (it is stateful).
     */
    public static TraceParser genFTimeParser() {
        TraceParser parser = new TraceParser();
        try {
            parser.addRegex("^(?<TYPE>)(?<FTIME>)$");
        } catch (ParseException e) {
            throw new InternalSynopticException(e);
        }
        parser.addPartitionsSeparator("^--$");
        return parser;
    }

    /**
     * Constructs a parser used by tests concerning a double time. Note: the
     * parser may not be re-used for parsing different traces (it is stateful).
     */
    public static TraceParser genDTimeParser() {
        TraceParser parser = new TraceParser();
        try {
            parser.addRegex("^(?<TYPE>)(?<DTIME>)$");
        } catch (ParseException e) {
            throw new InternalSynopticException(e);
        }
        parser.addPartitionsSeparator("^--$");
        return parser;
    }

    /**
     * Creates a single string out of an array of strings, joined together and
     * delimited using a newline.
     */
    public static String concatinateWithNewlines(String[] events) {
        StringBuilder sb = new StringBuilder();
        for (String s : events) {
            sb.append(s);
            sb.append('\n');
        }
        // Delete the trailing \n.
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    public ArrayList<EventNode> parseLogEvents(String[] events,
            TraceParser parser) throws InternalSynopticException,
            ParseException {
        String traceStr = concatinateWithNewlines(events);
        ArrayList<EventNode> parsedEvents = parser.parseTraceString(traceStr,
                testName.getMethodName(), -1);
        return parsedEvents;
    }

    /**
     * Generates an initial graph using the supplied parser.
     * 
     * @param events
     *            log of events
     * @return an initial graph corresponding to the log of events
     * @throws ParseException
     * @throws InternalSynopticException
     */
    public TraceGraph<?> genChainsTraceGraph(String[] events, TraceParser parser)
            throws ParseException, InternalSynopticException {
        ArrayList<EventNode> parsedEvents = parseLogEvents(events, parser);
        return parser.generateDefaultOrderRelation(parsedEvents);
    }

    /**
     * Generates an initial graph using the supplied parser.
     * 
     * @param events
     *            log of events
     * @return an initial graph corresponding to the log of events
     * @throws ParseException
     * @throws InternalSynopticException
     */
    public DAGsTraceGraph genDAGsTraceGraph(String[] events, TraceParser parser)
            throws ParseException, InternalSynopticException {
        ArrayList<EventNode> parsedEvents = parseLogEvents(events, parser);
        return parser.generateDirectPORelation(parsedEvents);
    }

    /**
     * Generates an initial linear graph based on a sequence of log events. Uses
     * the defParser parser for parsing the log of events.
     * 
     * @param events
     *            log of events, each one in the format: (?<TYPE>)
     * @return an initial graph corresponding to the log of events
     * @throws ParseException
     * @throws InternalSynopticException
     */
    public ChainsTraceGraph genInitialLinearGraph(String[] events)
            throws ParseException, InternalSynopticException {
        return (ChainsTraceGraph) genChainsTraceGraph(events, genDefParser());
    }

    /**
     * Parsers events using the supplied parser, generates the initial
     * _partitioning_ graph and returns it to the caller.
     * 
     * @param miner
     * @throws Exception
     */
    public PartitionGraph genInitialPartitionGraph(String[] events,
            TraceParser parser, ITOInvariantMiner miner,
            boolean multipleRelations) throws Exception {
        ChainsTraceGraph inputGraph = (ChainsTraceGraph) genChainsTraceGraph(
                events, parser);
        return new PartitionGraph(inputGraph, true, miner.computeInvariants(
                inputGraph, multipleRelations));
    }

    /**
     * Given an array of strings, create a list of corresponding LogEvent
     * instances.
     * 
     * @param strEvents
     *            A string sequence of events.
     * @return A LogEvent sequence of events.
     */
    public static List<EventNode> getLogEventPath(String[] strEvents) {
        ArrayList<EventNode> ret = new ArrayList<EventNode>();
        EventNode prevEvent = null;
        for (String strEvent : strEvents) {
            Event act = new Event(strEvent);
            EventNode logEvent = new EventNode(act);
            ret.add(logEvent);
            if (prevEvent != null) {
                prevEvent.addTransition(logEvent, Event.defTimeRelationStr);
            }
            prevEvent = logEvent;
        }
        return ret;
    }

}
