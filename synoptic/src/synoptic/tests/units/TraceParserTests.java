package synoptic.tests.units;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import junit.framework.Assert;

import synoptic.main.Main;
import synoptic.main.ParseException;
import synoptic.main.TraceParser;
import synoptic.model.ChainsTraceGraph;
import synoptic.model.EventNode;
import synoptic.model.EventType;
import synoptic.tests.SynopticTest;
import synoptic.util.InternalSynopticException;
import synoptic.util.Predicate.IBinary;
import synoptic.util.time.DTotalTime;
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
        Main.options.debugParse = true;
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
        parser.addRegex("^\\d+(?<HIDE=>blahblah)$");
    }

    /**
     * Custom TIME/VTIME/FTIME/DTIME fields are not allowed. These tests add a
     * regex with non-default values for these -- expect a ParseException.
     * 
     * @throws ParseException
     */
    @Test(expected = ParseException.class)
    public void addRegexCustomTimeRegExpExceptionTest() throws ParseException {
        parser.addRegex("^(?<TIME=>.+)\\s(?<TYPE>)$");
    }

    @Test(expected = ParseException.class)
    public void addRegexCustomVTimeRegExpExceptionTest() throws ParseException {
        parser.addRegex("^(?<VTIME=>\\d|\\d|\\d)\\s(?<TYPE>)$");
    }

    @Test(expected = ParseException.class)
    public void addRegexCustomFTimeRegExpExceptionTest() throws ParseException {
        parser.addRegex("^(?<FTIME=>\\d.\\d\\d)\\s(?<TYPE>)$");
    }

    @Test(expected = ParseException.class)
    public void addRegexCustomDTimeRegExpExceptionTest() throws ParseException {
        parser.addRegex("^(?<DTIME=>\\d\\d\\d)\\s(?<TYPE>)$");
    }

    /**
     * The LTIME group is built-in and should not be used in any regular
     * expressions. These tests attempt to use LTIME in various ways -- expect a
     * ParseException.
     * 
     * @throws ParseException
     */
    @Test(expected = ParseException.class)
    public void addRegexWithLTimeCustomRegExpExceptionTest()
            throws ParseException {
        parser.addRegex("^(?<LTIME=>\\d\\d)\\s(?<TYPE>)$");
    }

    @Test(expected = ParseException.class)
    public void addRegexWithLTimeHiddenRegExpExceptionTest()
            throws ParseException {
        parser.addRegex("^(?<HIDE=>true)(?<LTIME>)\\s(?<TYPE=>hihi)$");
    }

    @Test(expected = ParseException.class)
    public void addRegexWithLTimeRegExpExceptionTest() throws ParseException {
        parser.addRegex("^(?<LTIME>)\\s(?<TYPE=>hihi)$");
    }

    /**
     * The PID field can only be used in conjunction with VTIME. This test uses
     * it without VTIME -- expect a ParseException.
     * 
     * @throws ParseException
     */
    @Test(expected = ParseException.class)
    public void addRegexPIDWithoutVTIMERegExpExceptionTest()
            throws ParseException {
        parser.addRegex("^(?<PID>)\\s(?<TYPE>)$");
    }

    /**
     * Once a PID field is used with VTIME once, it must be used with every
     * future non-hidden reg-exp that uses VTIME. This test uses PID with VTIME
     * once, but fails to use it in a second reg-exp -- expect a ParseException.
     * 
     * @throws ParseException
     */
    @Test(expected = ParseException.class)
    public void addRegexPIDWithAndWithoutVTIMERegExpExceptionTest()
            throws ParseException {
        try {
            parser.addRegex("^(?<PID>)\\s(?<VTIME>)\\s(?<TYPE>)$");
        } catch (ParseException e) {
            Assert.fail("First addRegex should not have failed.");
        }
        // Use VTIME without PID.
        parser.addRegex("^(?<VTIME>)\\s(?<TYPE>)$");
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
        // Select two time groups at random:
        Random r = new Random();
        int t1Index = r.nextInt(TraceParser.validTimeGroups.size());
        int t2Index;
        do {
            t2Index = r.nextInt(TraceParser.validTimeGroups.size());
        } while (t2Index == t1Index);
        String t1 = TraceParser.validTimeGroups.get(t1Index);
        String t2 = TraceParser.validTimeGroups.get(t2Index);

        try {
            parser.addRegex("^(?<" + t1 + ">)\\s(?<TYPE>)$");
        } catch (Exception e) {
            Assert.fail("First addRegex should not have failed.");
        }
        // Second addRegex should throw the expected exception.
        parser.addRegex("^(?<" + t2 + ">)\\s(?<TYPE>)\\s$");
    }

    // //////////////////////////////////////////////////////////////////////////
    // parseTraceString() tests
    // //////////////////////////////////////////////////////////////////////////

    /**
     * Checks that the type/time of each log event in a list is correct.
     * 
     * @param events
     *            List of occurrences to check
     * @param timeStrs
     *            Array of corresponding occurrence times
     * @param types
     *            Array of corresponding occurrence types
     */
    public void checkLogEventTypesAndTimes(List<EventNode> events,
            String[] timeStrs, List<EventType> types, String timeType) {
        assertSame(events.size(), timeStrs.length);
        assertSame(timeStrs.length, types.size());
        for (int i = 0; i < events.size(); i++) {
            EventNode e = events.get(i);
            ITime eventTime = e.getTime();
            // Check that the type and the time of the occurrence are correct
            assertTrue(e.getEType().equals(types.get(i)));

            if (timeType.equals("VTIME")) {
                assertTrue(new VectorTime(timeStrs[i]).equals(eventTime));
            } else if (timeType.equals("TIME")) {
                int itime = Integer.parseInt(timeStrs[i]);
                assertTrue(new ITotalTime(itime).equals(eventTime));
            } else if (timeType.equals("FTIME")) {
                assertTrue(new FTotalTime(Float.parseFloat(timeStrs[i]))
                        .equals(eventTime));
            } else if (timeType.equals("DTIME")) {
                assertTrue(new DTotalTime(Double.parseDouble(timeStrs[i]))
                        .equals(eventTime));
            }

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
        checkLogEventTypesAndTimes(
                parser.parseTraceString(traceStr, "test", -1),
                new String[] { "1", "2", "3" }, // NOTE: implicit time starts
                // with 1
                stringsToStringEventTypes(new String[] { "a", "b", "c" }),
                "TIME");
        assertTrue(parser.logTimeTypeIsTotallyOrdered());
    }

    /**
     * Parse a log with explicit integer time values.
     */
    @Test
    public void parseExplicitIntegerTimeTest() throws ParseException,
            InternalSynopticException {
        String traceStr = "2 a\n3 b\n4 c\n";
        parser.addRegex("^(?<TIME>)(?<TYPE>)$");
        checkLogEventTypesAndTimes(
                parser.parseTraceString(traceStr, "test", -1), new String[] {
                        "2", "3", "4" },
                stringsToStringEventTypes(new String[] { "a", "b", "c" }),
                "TIME");
        assertTrue(parser.logTimeTypeIsTotallyOrdered());
    }

    /**
     * Parse a log with explicit float time values.
     */
    @Test
    public void parseExplicitFloatTimeTest() throws ParseException,
            InternalSynopticException {
        String traceStr = "2.1 a\n2.2 b\n3.0 c\n";
        parser.addRegex("^(?<FTIME>)(?<TYPE>)$");
        checkLogEventTypesAndTimes(
                parser.parseTraceString(traceStr, "test", -1), new String[] {
                        "2.1", "2.2", "3.0" },
                stringsToStringEventTypes(new String[] { "a", "b", "c" }),
                "FTIME");
        assertTrue(parser.logTimeTypeIsTotallyOrdered());
    }

    /**
     * Parse a log with explicit double time values.
     */
    @Test
    public void parseExplicitDoubleTimeTest() throws ParseException {
        String traceStr = "129892544112.89345 a\n129892544112.89346 b\n129892544112.89347 c\n";
        parser.addRegex("^(?<DTIME>)(?<TYPE>)$");
        checkLogEventTypesAndTimes(
                parser.parseTraceString(traceStr, "test", -1), new String[] {
                        "129892544112.89345", "129892544112.89346",
                        "129892544112.89347" },
                stringsToStringEventTypes(new String[] { "a", "b", "c" }),
                "DTIME");
        assertTrue(parser.logTimeTypeIsTotallyOrdered());
    }

    /**
     * Parse a log with explicit vector time values and a PID group.
     */
    @Test
    public void parseVTimeExplicitPIDTest() throws ParseException {
        String traceStr = "1,0 NODE-0 a\n0,1 NODE-1 b\n2,1 NODE-0 c\n";
        parser.addRegex("^(?<VTIME>)(?<PID>)(?<TYPE>)$");
        // This also checks that the PID is correctly parsed/assigned
        checkLogEventTypesAndTimes(
                parser.parseTraceString(traceStr, "test", -1),
                new String[] { "1,0", "0,1", "2,1" },
                stringsToDistEventTypes(new String[] { "a", "b", "c" },
                        new String[] { "NODE-0", "NODE-1", "NODE-0" }), "VTIME");
        assertFalse(parser.logTimeTypeIsTotallyOrdered());
    }

    /**
     * Parse a log with explicit vector time values and a PID group. But with
     * vector times that are NOT totally ordered at the same PID -- expect a
     * ParseException.
     */
    @Test(expected = ParseException.class)
    public void parseVTimeExplicitPIDNotTotallyOrderedAtPIDExceptionTest()
            throws ParseException {
        String traceStr = "1,0 NODE-0 a\n1,2 NODE-1 b\n0,1 NODE-0 c\n";
        try {
            parser.addRegex("^(?<VTIME>)(?<PID>)(?<TYPE>)$");
        } catch (Exception e) {
            Assert.fail("addRegex should not have failed.");
        }
        parser.parseTraceString(traceStr, "test", -1);
    }

    /**
     * Parse a log with explicit vector time values and implicit PIDs. That is,
     * the PID value for each event instance is _inferred_.
     */
    @Test
    public void parseVTimeImplicitPIDTest() throws ParseException {
        String traceStr = "1,0 a\n0,1 b\n2,1 c\n";
        parser.addRegex("^(?<VTIME>)(?<TYPE>)$");
        checkLogEventTypesAndTimes(
                parser.parseTraceString(traceStr, "test", -1),
                new String[] { "1,0", "0,1", "2,1" },
                stringsToDistEventTypes(new String[] { "a", "b", "c" },
                        new String[] { "0", "1", "0" }), "VTIME");
        assertFalse(parser.logTimeTypeIsTotallyOrdered());
    }

    /**
     * Parse a log with two records with the same INTEGER time in the same
     * partition -- expect a ParseException.
     */
    @Test(expected = ParseException.class)
    public void parseSameTimeExceptionTest() throws ParseException,
            InternalSynopticException {
        String traceStr = "1 a\n2 b\n2 c\n";
        ArrayList<EventNode> events = null;
        try {
            parser.addRegex("^(?<TIME>)(?<TYPE>)$");
            events = parser.parseTraceString(traceStr, "test", -1);
        } catch (Exception e) {
            fail("addRegex and parseTraceString should not have raised an exception");
        }
        // The exception should be thrown by generateDirectTemporalRelation
        parser.generateDirectTORelation(events);
    }

    /**
     * Parse a log with two records with the same FLOAT time in the same
     * partition -- expect a ParseException.
     */
    @Test(expected = ParseException.class)
    public void parseSameFTimeExceptionTest() throws ParseException,
            InternalSynopticException {
        String traceStr = "1.1 a\n2.2 b\n2.2 c\n";
        ArrayList<EventNode> events = null;
        try {
            parser.addRegex("^(?<FTIME>)(?<TYPE>)$");
            events = parser.parseTraceString(traceStr, "test", -1);
        } catch (Exception e) {
            fail("addRegex and parseTraceString should not have raised an exception");
        }
        // The exception should be thrown by generateDirectTemporalRelation
        parser.generateDirectTORelation(events);
    }

    /**
     * Parse a log with two records with the same DOUBLE time in the same
     * partition -- expect a ParseException.
     */
    @Test(expected = ParseException.class)
    public void parseSameDTimeExceptionTest() throws ParseException,
            InternalSynopticException {
        String traceStr = "1.1 a\n2.2 b\n2.2 c\n";
        ArrayList<EventNode> events = null;
        try {
            parser.addRegex("^(?<DTIME>)(?<TYPE>)$");
            events = parser.parseTraceString(traceStr, "test", -1);
        } catch (Exception e) {
            fail("addRegex and parseTraceString should not have raised an exception");
        }
        // The exception should be thrown by generateDirectTemporalRelation
        parser.generateDirectTORelation(events);
    }

    /**
     * Parse a log with two records with the same VECTOR TIME in the same
     * partition -- expect a ParseException.
     */
    @Test(expected = ParseException.class)
    public void parseSameVTimeExceptionTest() throws ParseException,
            InternalSynopticException {
        String traceStr = "1,1,2 a\n1,1,2 b\n2,2,2 c\n";
        ArrayList<EventNode> events = null;
        try {
            parser.addRegex("^(?<VTIME>)(?<TYPE>)$");
            events = parser.parseTraceString(traceStr, "test", -1);
        } catch (Exception e) {
            fail("addRegex and parseTraceString should not have raised an exception");
        }
        // The exception should be thrown by generateDirectTemporalRelation
        parser.generateDirectPORelation(events);
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
        // This should throw a ParseException because FTIME cannot process a
        // VTIME field
        parser.parseTraceString(traceStr, "test", -1);
    }

    /**
     * Parse a log using wrong time named group (should be VTIME) -- expect a
     * ParseException.
     */
    @Test(expected = ParseException.class)
    public void parseNonDTimeExceptionTest() throws ParseException,
            InternalSynopticException {
        String traceStr = "1,1 a\n2,2 b\n3,3 c\n";
        try {
            parser.addRegex("^(?<DTIME>)(?<TYPE>)$");
        } catch (Exception e) {
            fail("addRegex should not have raised an exception");
        }
        // This should throw a ParseException because DTIME cannot process a
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
        parser.addRegex("^(?<VTIME>)(?<TYPE>)$");
        parser.parseTraceString(traceStr, "test", -1);
    }

    /**
     * Parse a log using a non-default TYPE.
     */
    @Test
    public void parseCustomTypeRegExpTest() throws ParseException,
            InternalSynopticException {
        String traceStr = "1 a a\n2 b b\n3 c c\n";
        parser.addRegex("^(?<TIME>)(?<TYPE>.+)$");
        checkLogEventTypesAndTimes(
                parser.parseTraceString(traceStr, "test", -1),
                new String[] { "1", "2", "3" },
                stringsToStringEventTypes(new String[] { "a a", "b b", "c c" }),
                "TIME");
    }

    /**
     * Parses a prefix of lines, instead of all lines.
     */
    @Test
    public void parsePrefixOfLines() throws ParseException,
            InternalSynopticException {
        String traceStr = "1 a\n2 b\n3 c\n";
        parser.addRegex("^(?<TIME>)(?<TYPE>)$");
        checkLogEventTypesAndTimes(
                parser.parseTraceString(traceStr, "test", 2), new String[] {
                        "1", "2" }, stringsToStringEventTypes(new String[] {
                        "a", "b" }), "TIME");
    }

    // ////////////////////////////
    // Multiple partitions parsing

    /**
     * Generates the expected graph for the two cases below.
     */
    private static ChainsTraceGraph genExpectedGraphForTotalOrder(
            List<EventNode> events) {
        // Generate the expected Graph.
        ChainsTraceGraph expectedGraph = new ChainsTraceGraph(events);

        assertTrue(events.size() == 6);

        expectedGraph.tagInitial(events.get(0), defRelation);
        expectedGraph.tagInitial(events.get(3), defRelation);
        expectedGraph.tagTerminal(events.get(2), defRelation);
        expectedGraph.tagTerminal(events.get(5), defRelation);

        EventNode prevEvent = events.get(0);
        for (EventNode event : events.subList(1, 2)) {
            prevEvent.addTransition(event, defRelation);
        }

        prevEvent = events.get(3);
        for (EventNode event : events.subList(4, 5)) {
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
        ArrayList<EventNode> events = parser.parseTraceString(traceStr, "test",
                -1);
        ChainsTraceGraph graph = parser.generateDirectTORelation(events);
        ChainsTraceGraph expectedGraph = genExpectedGraphForTotalOrder(events);
        // Test graph equality.
        assertTrue(expectedGraph.equalsWith(graph,
                new IBinary<EventNode, EventNode>() {
                    @Override
                    public boolean eval(EventNode a, EventNode b) {
                        return (a.getEvent().equals(b.getEvent()));
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
        ArrayList<EventNode> events = parser.parseTraceString(traceStr, "test",
                -1);
        ChainsTraceGraph graph = parser.generateDirectTORelation(events);
        ChainsTraceGraph expectedGraph = genExpectedGraphForTotalOrder(events);
        // Test graph equality.
        assertTrue(expectedGraph.equalsWith(graph,
                new IBinary<EventNode, EventNode>() {
                    @Override
                    public boolean eval(EventNode a, EventNode b) {
                        return (a.getEvent().equals(b.getEvent()));
                    }
                }));
    }
}
