package synoptic.tests.units;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import junit.framework.Assert;

import synoptic.main.Main;
import synoptic.main.ParseException;
import synoptic.main.TraceParser;
import synoptic.model.Action;
import synoptic.model.Graph;
import synoptic.model.LogEvent;
import synoptic.tests.SynopticTest;
import synoptic.util.InternalSynopticException;
import synoptic.util.Predicate.IBinary;
import synoptic.util.time.FTotalTime;
import synoptic.util.time.ITime;
import synoptic.util.time.ITotalTime;
import synoptic.util.time.VectorTime;

/**
 * Tests for the synoptic.main.TraceParser class.
 * 
 * @author ivan
 */
public class TraceParserTests extends SynopticTest {
    /**
     * The parser instance we use for testing.
     */
    TraceParser parser = null;

    @Override
    public void setUp() throws ParseException {
        super.setUp();
        parser = new TraceParser();
        Main.debugParse = true;
    }

    // //////////////////////////////////////////////////////////////////////////
    // addRegex() tests
    // //////////////////////////////////////////////////////////////////////////

    /**
     * Add a regex without the required TYPE named group -- expect a
     * ParseException
     * 
     * @throws ParseException
     */
    @Test(expected = ParseException.class)
    public void addRegexWithoutTYPERegExpExceptionTest() throws ParseException {
        // This should throw a ParseException because custom TIME fields are not
        // allowed
        parser.addRegex("^(?<TIME>)$");
    }

    /**
     * Add a regex without the required TYPE named group, BUT with a HIDE flag.
     * The HIDE flag should make this a valid regexp (since it won't be
     * generating a LogEvent, we don't need any required fields with it).
     * 
     * @throws ParseException
     */
    @Test
    public void addRegexHiddenWithoutRequiredRegExpExceptionTest()
            throws ParseException {
        // This should throw a ParseException because custom TIME fields are not
        // allowed
        parser.addRegex("^\\d+(?<HIDE=>true)$");
    }

    /**
     * Add a regex with a custom HIDE field value. This throws a ParseException
     * as HIDE can only be assigned to 'true'.
     * 
     * @throws ParseException
     */
    @Test(expected = ParseException.class)
    public void addRegexCustomHIDERegExpExceptionTest() throws ParseException {
        // Non-true HIDE values are not allowed.
        parser.addRegex("^\\d+(?<HIDE=>blahblah)$");
    }

    /**
     * Add a regex with a non-default TIME -- expect a ParseException.
     * 
     * @throws ParseException
     */
    @Test(expected = ParseException.class)
    public void addRegexCustomTimeRegExpExceptionTest() throws ParseException {
        // This should throw a ParseException because custom TIME fields are not
        // allowed
        parser.addRegex("^(?<TIME=>.+)\\s(?<TYPE>)$");
    }

    /**
     * Parse a log using a non-default VTIME -- expect a ParseException.
     * 
     * @throws ParseException
     */
    @Test(expected = ParseException.class)
    public void addRegexCustomVTimeRegExpExceptionTest() throws ParseException {
        // This should throw a ParseException because custom VTIME fields are
        // not allowed
        parser.addRegex("^(?<VTIME=>\\d|\\d|\\d)\\s(?<TYPE>)$");
    }

    /**
     * Add a regex with multiple named groups of the same name that are
     * assigned.
     * 
     * @throws ParseException
     */
    @Test(expected = ParseException.class)
    public void addRegexMultipleIdenticalGroupsAssignedRegExpExceptionTest()
            throws ParseException {
        parser.addRegex("^(?<TIME>)\\s(?<BAM=>.+)\\s(?<BAM=>.+)$");
    }

    /**
     * Add a regex with multiple named groups of the same name that are not
     * assigned (they use the default values).
     * 
     * @throws ParseException
     */
    @Test(expected = ParseException.class)
    public void addRegexMultipleIdenticalGroupsUnassignedRegExpExceptionTest()
            throws ParseException {
        parser.addRegex("^(?<TIME>)\\s(?<TYPE>)\\s(?<TYPE>)\\s(?<TYPE>)$");
    }

    /**
     * Add a regex with an assignment to a required field.
     * 
     * @throws ParseException
     */
    @Test
    public void addRegexAssignRequiredFieldRegExpExceptionTest()
            throws ParseException {
        parser.addRegex("^(?<TIME>)\\s(?<TYPE=>.+)$");
    }

    /**
     * Add a two regexes that mix time types -- will result in a ParseException.
     * 
     * @throws ParseException
     */
    @Test(expected = ParseException.class)
    public void addRegexDiffTimeTypesRegExpExceptionTest()
            throws ParseException {
        try {
            parser.addRegex("^(?<TIME>)\\s(?<TYPE>)$");
        } catch (Exception e) {
            Assert.fail("First addRegex should not have failed.");
        }
        // Second addRegex should throw the expected exception.
        parser.addRegex("^(?<VTIME>)\\s(?<TYPE>)\\s$");
    }

    // //////////////////////////////////////////////////////////////////////////
    // parseTraceString() tests
    // //////////////////////////////////////////////////////////////////////////

    /**
     * Checks that the type and the VECTOR time of each log event in a list is
     * correct.
     * 
     * @param events
     *            List of occurrences to check
     * @param vtimeStrs
     *            Array of corresponding occurrence vector times
     * @param types
     *            Array of corresponding occurrence types
     */
    public void checkLogEventTypesVTimes(List<LogEvent> events,
            String[] vtimeStrs, String[] types) {
        assertSame(events.size(), vtimeStrs.length);
        assertSame(vtimeStrs.length, types.length);
        for (int i = 0; i < events.size(); i++) {
            LogEvent e = events.get(i);
            ITime eventTime = e.getTime();
            // Check that the type and the time of the occurrence are correct
            assertTrue(e.getLabel().equals(types[i]));
            assertTrue(new VectorTime(vtimeStrs[i]).equals(eventTime));
        }
    }

    /**
     * Checks that the type and the INTEGER time of each log event in a list is
     * correct.
     * 
     * @param events
     *            List of occurrences to check
     * @param vtimeStrs
     *            Array of corresponding occurrence vector times
     * @param types
     *            Array of corresponding occurrence types
     */
    public void checkLogEventTypesITimes(List<LogEvent> events,
            String[] vtimeStrs, String[] types) {
        assertSame(events.size(), vtimeStrs.length);
        assertSame(vtimeStrs.length, types.length);
        for (int i = 0; i < events.size(); i++) {
            LogEvent e = events.get(i);
            ITime eventTime = e.getTime();
            // Check that the type and the time of the occurrence are correct
            assertTrue(e.getLabel().equals(types[i]));
            int itime = Integer.parseInt(vtimeStrs[i]);
            assertTrue(new ITotalTime(itime).equals(eventTime));
        }
    }

    /**
     * Checks that the type and the FLOAT time of each log event in a list is
     * correct.
     * 
     * @param events
     *            List of occurrences to check
     * @param vtimeStrs
     *            Array of corresponding occurrence vector times
     * @param types
     *            Array of corresponding occurrence types
     */
    public void checkLogEventTypesFTimes(List<LogEvent> events,
            String[] vtimeStrs, String[] types) {
        assertSame(events.size(), vtimeStrs.length);
        assertSame(vtimeStrs.length, types.length);
        for (int i = 0; i < events.size(); i++) {
            LogEvent e = events.get(i);
            ITime eventTime = e.getTime();
            // Check that the type and the time of the occurrence are correct
            assertTrue(e.getLabel().equals(types[i]));
            assertTrue(new FTotalTime(Float.parseFloat(vtimeStrs[i]))
                    .equals(eventTime));
        }
    }

    /**
     * Parse a log with implicit time that increments on each log line.
     * (Purposefully doesn't handle the ParseException and
     * InternalSynopticException as these exceptions imply that the parse has a
     * bug).
     * 
     * @throws ParseException
     * @throws InternalSynopticException
     */
    @Test
    public void parseImplicitTimeTest() throws ParseException,
            InternalSynopticException {
        String traceStr = "a\nb\nc\n";
        parser.addRegex("^(?<TYPE>)$");
        checkLogEventTypesITimes(parser.parseTraceString(traceStr, "test", -1),
                new String[] { "1", "2", "3" }, // NOTE: implicit time starts
                // with 1
                new String[] { "a", "b", "c" });

    }

    /**
     * Parse a log with explicit integer time values.
     */
    @Test
    public void parseExplicitIntegerTimeTest() throws ParseException,
            InternalSynopticException {
        String traceStr = "2 a\n3 b\n4 c\n";
        parser.addRegex("^(?<TIME>)(?<TYPE>)$");
        checkLogEventTypesITimes(parser.parseTraceString(traceStr, "test", -1),
                new String[] { "2", "3", "4" }, new String[] { "a", "b", "c" });
    }

    /**
     * Parse a log with explicit float time values.
     */
    @Test
    public void parseExplicitFloatTimeTest() throws ParseException,
            InternalSynopticException {
        String traceStr = "2.1 a\n2.2 b\n3.0 c\n";
        parser.addRegex("^(?<FTIME>)(?<TYPE>)$");
        checkLogEventTypesFTimes(parser.parseTraceString(traceStr, "test", -1),
                new String[] { "2.1", "2.2", "3.0" }, new String[] { "a", "b",
                        "c" });
    }

    /**
     * Parse a log with explicit vector time values.
     */
    @Test
    public void parseExplicitVTimeTest() throws ParseException,
            InternalSynopticException {
        String traceStr = "1,1,1 a\n2,2,2 b\n3,3,4 c\n";
        parser.addRegex("^(?<VTIME>)(?<TYPE>)$");
        checkLogEventTypesVTimes(parser.parseTraceString(traceStr, "test", -1),
                new String[] { "1,1,1", "2,2,2", "3,3,4" }, new String[] { "a",
                        "b", "c" });
    }

    /**
     * Parse a log with two records with the same integer time in the same
     * partition -- expect a ParseException.
     */
    @Test(expected = ParseException.class)
    public void parseSameTimeExceptionTest() throws ParseException,
            InternalSynopticException {
        String traceStr = "1 a\n2 b\n2 c\n";
        ArrayList<LogEvent> events = null;
        try {
            parser.addRegex("^(?<TIME>)(?<TYPE>)$");
            events = parser.parseTraceString(traceStr, "test", -1);
        } catch (Exception e) {
            fail("addRegex and parseTraceString should not have raised an exception");
        }
        // The exception should be thrown by generateDirectTemporalRelation
        parser.generateDirectTemporalRelation(events, true);
    }

    /**
     * Parse a log with two records with the same integer time in the same
     * partition -- expect a ParseException.
     */
    @Test(expected = ParseException.class)
    public void parseSameFTimeExceptionTest() throws ParseException,
            InternalSynopticException {
        String traceStr = "1.1 a\n2.2 b\n2.2 c\n";
        ArrayList<LogEvent> events = null;
        try {
            parser.addRegex("^(?<FTIME>)(?<TYPE>)$");
            events = parser.parseTraceString(traceStr, "test", -1);
        } catch (Exception e) {
            fail("addRegex and parseTraceString should not have raised an exception");
        }
        // The exception should be thrown by generateDirectTemporalRelation
        parser.generateDirectTemporalRelation(events, true);
    }

    /**
     * Parse a log with two records with the same vector time in the same
     * partition -- expect a ParseException.
     */
    @Test(expected = ParseException.class)
    public void parseSameVTimeExceptionTest() throws ParseException,
            InternalSynopticException {
        String traceStr = "1,1,2 a\n1,1,2 b\n2,2,2 c\n";
        ArrayList<LogEvent> events = null;
        try {
            parser.addRegex("^(?<VTIME>)(?<TYPE>)$");
            events = parser.parseTraceString(traceStr, "test", -1);
        } catch (Exception e) {
            fail("addRegex and parseTraceString should not have raised an exception");
        }
        // The exception should be thrown by generateDirectTemporalRelation
        parser.generateDirectTemporalRelation(events, true);
    }

    // TODO: Check setting of constants -- e.g. (?<NODETYPE=>master)

    /**
     * Parse a log using wrong time named group (should be VTIME) -- expect a
     * ParseException.
     */
    @Test(expected = ParseException.class)
    public void parseNonTimeExceptionTest() throws ParseException,
            InternalSynopticException {
        String traceStr = "1,1 a\n2,2 b\n3,3 c\n";
        try {
            parser.addRegex("^(?<TIME>)(?<TYPE>)$");
        } catch (Exception e) {
            fail("addRegex should not have raised an exception");
        }
        // This should throw a ParseException because TIME cannot process a
        // VTIME field
        parser.parseTraceString(traceStr, "test", -1);
    }

    /**
     * Parse a log using wrong time named group (should be VTIME) -- expect a
     * ParseException.
     */
    @Test(expected = ParseException.class)
    public void parseNonFTimeExceptionTest() throws ParseException,
            InternalSynopticException {
        String traceStr = "1,1 a\n2,2 b\n3,3 c\n";
        try {
            parser.addRegex("^(?<FTIME>)(?<TYPE>)$");
        } catch (Exception e) {
            fail("addRegex should not have raised an exception");
        }
        // This should throw a ParseException because TIME cannot process a
        // VTIME field
        parser.parseTraceString(traceStr, "test", -1);
    }

    /**
     * Parse a log with records that have different length vector times --
     * expect a ParseException.
     */
    @Test(expected = ParseException.class)
    public void parseDiffLengthVTimesExceptionTest() throws ParseException,
            InternalSynopticException {
        String traceStr = "1,1,2 a\n1,1,2,3 b\n2,2,2 c\n";
        ArrayList<LogEvent> events = null;
        try {
            parser.addRegex("^(?<VTIME>)(?<TYPE>)$");
            events = parser.parseTraceString(traceStr, "test", -1);
        } catch (Exception e) {
            fail("addRegex and parseTraceString should not have raised an exception");
        }
        // The exception should be thrown by generateDirectTemporalRelation
        parser.generateDirectTemporalRelation(events, true);
    }

    /**
     * Parse a log using a non-default TYPE.
     */
    @Test
    public void parseCustomTypeRegExpTest() throws ParseException,
            InternalSynopticException {
        String traceStr = "1 a a\n2 b b\n3 c c\n";
        parser.addRegex("^(?<TIME>)(?<TYPE>.+)$");
        checkLogEventTypesITimes(parser.parseTraceString(traceStr, "test", -1),
                new String[] { "1", "2", "3" }, new String[] { "a a", "b b",
                        "c c" });
    }

    /**
     * Parses a prefix of lines, instead of all lines.
     */
    @Test
    public void parsePrefixOfLines() throws ParseException,
            InternalSynopticException {
        String traceStr = "1 a\n2 b\n3 c\n";
        parser.addRegex("^(?<TIME>)(?<TYPE>)$");
        checkLogEventTypesITimes(parser.parseTraceString(traceStr, "test", 2),
                new String[] { "1", "2" }, new String[] { "a", "b" });
    }

    // ////////////////////////////
    // Multiple partitions parsing

    /**
     * Generates the expected graph for the two cases below.
     */
    private static Graph<LogEvent> genExpectedGraphForTotalOrder(
            List<LogEvent> events) {
        // Generate the expected Graph.
        Graph<LogEvent> expectedGraph = new Graph<LogEvent>();

        assertTrue(events.size() == 6);
        Action dummyAct = Action.NewInitialAction();
        expectedGraph.setDummyInitial(new LogEvent(dummyAct), defRelation);
        dummyAct = Action.NewTerminalAction();
        expectedGraph.setDummyTerminal(new LogEvent(dummyAct));

        expectedGraph.tagInitial(events.get(0), defRelation);
        expectedGraph.tagInitial(events.get(3), defRelation);
        expectedGraph.tagTerminal(events.get(2), defRelation);
        expectedGraph.tagTerminal(events.get(5), defRelation);

        LogEvent prevEvent = events.get(0);
        expectedGraph.add(prevEvent);
        for (LogEvent event : events.subList(1, 2)) {
            expectedGraph.add(event);
            prevEvent.addTransition(event, defRelation);
        }

        prevEvent = events.get(3);
        expectedGraph.add(prevEvent);
        for (LogEvent event : events.subList(4, 5)) {
            expectedGraph.add(event);
            prevEvent.addTransition(event, defRelation);
        }

        return expectedGraph;
    }

    /**
     * Check that we can parse a log by splitting its lines into partitions.
     * This test also checks that two different partitions may have events that
     * have the same timestamp (this is not allowed if the events are in the
     * same partition -- see test above).
     * 
     * @throws ParseException
     */
    @Test
    public void parseWithSplitPartitionsTotalOrderTest() throws ParseException {
        String traceStr = "1 a\n2 b\n3 c\n--\n1 c\n2 b\n3 a\n";
        parser.addRegex("^(?<TIME>)(?<TYPE>)$");
        parser.addPartitionsSeparator("^--$");
        ArrayList<LogEvent> events = parser.parseTraceString(traceStr, "test",
                -1);
        Graph<LogEvent> graph = parser.generateDirectTemporalRelation(events,
                true);
        Graph<LogEvent> expectedGraph = genExpectedGraphForTotalOrder(events);
        // Test graph equality.
        assertTrue(expectedGraph.equalsWith(graph,
                new IBinary<LogEvent, LogEvent>() {
                    @Override
                    public boolean eval(LogEvent a, LogEvent b) {
                        return (a.getAction().equals(b.getAction()));
                    }
                }));
    }

    /**
     * Check that we can parse a log by mapping log lines to partitions.
     * 
     * @throws ParseException
     */
    @Test
    public void parseWithMappedPartitionsTotalOrderTest() throws ParseException {
        String traceStr = "1 a\n1 b\n1 c\n2 c\n2 b\n2 a\n";
        parser.addRegex("^(?<PARTITION>)(?<TYPE>)$");
        parser.setPartitionsMap("\\k<PARTITION>");
        ArrayList<LogEvent> events = parser.parseTraceString(traceStr, "test",
                -1);
        Graph<LogEvent> graph = parser.generateDirectTemporalRelation(events,
                true);
        Graph<LogEvent> expectedGraph = genExpectedGraphForTotalOrder(events);
        // Test graph equality.
        assertTrue(expectedGraph.equalsWith(graph,
                new IBinary<LogEvent, LogEvent>() {
                    @Override
                    public boolean eval(LogEvent a, LogEvent b) {
                        return (a.getAction().equals(b.getAction()));
                    }
                }));
    }
}
