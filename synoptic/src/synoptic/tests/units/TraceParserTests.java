package synoptic.tests.units;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import junit.framework.Assert;

import synoptic.main.SynopticMain;
import synoptic.main.parser.ParseException;
import synoptic.main.parser.TraceParser;
import synoptic.model.ChainsTraceGraph;
import synoptic.model.EventNode;
import synoptic.model.event.Event;
import synoptic.model.event.EventType;
import synoptic.model.event.StringEventType;
import synoptic.model.state.State;
import synoptic.tests.SynopticTest;
import synoptic.util.InternalSynopticException;
import synoptic.util.Predicate.IBoolBinary;
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

    /**
     * generic relations for invariant mining
     */
    public static final String callRelation = "call";

    public static final String returnRelation = "return";

    public static final String staticRelation = "static";

    public static final String instanceRelation = "instance";

    public static final String nativeRelation = "native";

    @Override
    public void setUp() throws ParseException {
        super.setUp();
        parser = new TraceParser();
        SynopticMain.getInstanceWithExistenceCheck().options.debugParse = true;
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

    @Test(expected = ParseException.class)
    public void addRegexNullRegExpExceptionTest() throws ParseException {
        parser.addRegex(null);
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
    public void addRegexAssignRequiredFieldRegTest()
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
    
    /**
     * Add a regex with the STATE named group.
     * 
     * @throws ParseException 
     */
    @Test
    public void addRegexStateTest() throws ParseException {
        parser.addRegex("^(?<STATE>)$");
    }
    
    /**
     * Add a regex with an assignment to the STATE field.
     * 
     * @throws ParseException
     */
    @Test
    public void addRegexAssignStateTest() throws ParseException {
        parser.addRegex("^(?<STATE=>a=1,b=2)$");
    }
    
    /**
     * Add a regex with both TYPE and STATE named groups --
     * expect a ParseException.
     * 
     * @throws ParseException
     */
    @Test(expected = ParseException.class)
    public void addRegexWithTypeAndStateGroupsExpExceptionTest() throws ParseException {
        parser.addRegex("^(?<TYPE>\\S+)\\s(?<STATE>\\S+)$");
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

    @Test(expected = ParseException.class)
    public void parseNullStringExceptionTest() throws ParseException {
        parser.addRegex("^(?<VTIME>)(?<TYPE>)$");
        parser.parseTraceString(null, "test", -1);
    }

    @Test(expected = ParseException.class)
    public void parseNullTraceNameExceptionTest() throws ParseException {
        String traceStr = "1,1,2 a\n1,1,2,3 b\n2,2,2 c\n";
        parser.addRegex("^(?<VTIME>)(?<TYPE>)$");
        parser.parseTraceString(traceStr, null, -1);
    }

    @Test(expected = ParseException.class)
    public void parseEmptyRegExExceptionTest() throws ParseException {
        String traceStr = "1,1,2 a\n1,1,2,3 b\n2,2,2 c\n";
        parser.addRegex("");
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

        expectedGraph.tagInitial(events.get(0), Event.defTimeRelationStr);
        expectedGraph.tagInitial(events.get(3), Event.defTimeRelationStr);
        expectedGraph.tagTerminal(events.get(2), Event.defTimeRelationStr);
        expectedGraph.tagTerminal(events.get(5), Event.defTimeRelationStr);

        EventNode prevEvent = events.get(0);
        for (EventNode event : events.subList(1, 2)) {
            prevEvent.addTransition(event, Event.defTimeRelationStr);
        }

        prevEvent = events.get(3);
        for (EventNode event : events.subList(4, 5)) {
            prevEvent.addTransition(event, Event.defTimeRelationStr);
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
                new IBoolBinary<EventNode, EventNode>() {
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
                new IBoolBinary<EventNode, EventNode>() {
                    @Override
                    public boolean eval(EventNode a, EventNode b) {
                        return (a.getEvent().equals(b.getEvent()));
                    }
                }));
    }

    // ////////////////////////////
    // Multiple relations parsing

    /**
     * Generates the expected graph for the call and return multiple relations
     * test
     */
    private ChainsTraceGraph genExpectedGraphForBasicMultipleRelation(
            ArrayList<EventNode> events) {
        /*
         * Generate the expected Graph for
         * 
         * String traceStr = "0 call main\n" + "1 call foo\n" + "2 return main";
         * 
         * INITIAL -t,call-> main -t,call-> food -t,return-> main -t-> TERMINAL
         */

        ChainsTraceGraph expectedGraph = new ChainsTraceGraph(events);
        assertTrue(events.size() == 3);

        EventNode cMain = events.get(0);
        EventNode cFoo = events.get(1);
        EventNode rMain = events.get(2);

        expectedGraph.tagInitial(
                cMain,
                new LinkedHashSet<String>(Arrays.asList(
                        Event.defTimeRelationStr, callRelation)));

        cMain.addTransition(
                cFoo,
                new LinkedHashSet<String>(Arrays.asList(
                        Event.defTimeRelationStr, callRelation)));

        cFoo.addTransition(
                rMain,
                new LinkedHashSet<String>(Arrays.asList(
                        Event.defTimeRelationStr, returnRelation)));

        expectedGraph.tagTerminal(rMain, Event.defTimeRelationStr);

        return expectedGraph;
    }

    private ChainsTraceGraph genExpectedGraphForSeparatorAndPartitionMultipleRelation(
            ArrayList<EventNode> events) {
        /*
         * Generate the expected Graph for
         * 
         * String traceStr = "0 call main\n" + "1 call foo\n" +
         * "2 return main\n" + "--\n" + "0 call main\n" + "1 call foo\n" +
         * "2 return main";
         * 
         * INITIAL -t,call-> main -t,call-> food -t,return-> main -t-> TERMINAL
         */

        ChainsTraceGraph expectedGraph = new ChainsTraceGraph(events);
        assertTrue(events.size() == 6);

        EventNode cMain = events.get(0);
        EventNode cFoo = events.get(1);
        EventNode rMain = events.get(2);

        expectedGraph.tagInitial(
                cMain,
                new LinkedHashSet<String>(Arrays.asList(
                        Event.defTimeRelationStr, callRelation)));

        cMain.addTransition(
                cFoo,
                new LinkedHashSet<String>(Arrays.asList(
                        Event.defTimeRelationStr, callRelation)));

        cFoo.addTransition(
                rMain,
                new LinkedHashSet<String>(Arrays.asList(
                        Event.defTimeRelationStr, returnRelation)));

        expectedGraph.tagTerminal(rMain, Event.defTimeRelationStr);

        cMain = events.get(3);
        cFoo = events.get(4);
        rMain = events.get(5);

        expectedGraph.tagInitial(
                cMain,
                new LinkedHashSet<String>(Arrays.asList(
                        Event.defTimeRelationStr, callRelation)));

        cMain.addTransition(
                cFoo,
                new LinkedHashSet<String>(Arrays.asList(
                        Event.defTimeRelationStr, callRelation)));

        cFoo.addTransition(
                rMain,
                new LinkedHashSet<String>(Arrays.asList(
                        Event.defTimeRelationStr, returnRelation)));

        expectedGraph.tagTerminal(rMain, Event.defTimeRelationStr);

        return expectedGraph;
    }

    private ChainsTraceGraph genExpectedGraphForMultipleRelationsPerLine(
            ArrayList<EventNode> events) {
        /*
         * Generate expected graph for String traceStr =
         * "0 call static Main.main\n" + "1 call instance Main.instance\n" +
         * "2 return static Main.main\n" + "3 call native Foo.foo\n" +
         * "4 return static Main.main";
         * 
         * INITIAL -t,call,static-> Main.main -t,call,instance-> Main.instance
         * -t,return,static-> Main.main -t,call,native-> Foo.foo
         * -t,return,static-> Main.main -t-> TERMINAL
         */

        ChainsTraceGraph expectedGraph = new ChainsTraceGraph(events);
        assertTrue(events.size() == 5);

        EventNode mainMain = events.get(0);
        expectedGraph
                .tagInitial(
                        mainMain,
                        new LinkedHashSet<String>(Arrays.asList(
                                Event.defTimeRelationStr, callRelation,
                                staticRelation)));

        EventNode mainInstance = events.get(1);
        mainMain.addTransition(
                mainInstance,
                new LinkedHashSet<String>(Arrays.asList(
                        Event.defTimeRelationStr, callRelation,
                        instanceRelation)));

        EventNode mainMain2 = events.get(2);
        mainInstance.addTransition(
                mainMain2,
                new LinkedHashSet<String>(Arrays.asList(
                        Event.defTimeRelationStr, returnRelation,
                        staticRelation)));

        EventNode fooFoo = events.get(3);
        mainMain2
                .addTransition(
                        fooFoo,
                        new LinkedHashSet<String>(Arrays.asList(
                                Event.defTimeRelationStr, callRelation,
                                nativeRelation)));

        EventNode mainMain3 = events.get(4);
        fooFoo.addTransition(
                mainMain3,
                new LinkedHashSet<String>(Arrays.asList(
                        Event.defTimeRelationStr, returnRelation,
                        staticRelation)));

        expectedGraph.tagTerminal(mainMain3, Event.defTimeRelationStr);

        return expectedGraph;
    }

    private ChainsTraceGraph genExpectedGraphForClosureAndGenericMultipleRelations(
            ArrayList<EventNode> events) {
        /*
         * Generate expected graph for String traceStr =
         * "0 call static Main.main\n" + "1 call instance Main.instance\n" +
         * "2 return static Main.main\n" + "3 call native Foo.foo\n" +
         * "4 return static Main.main";
         * 
         * 
         * Time/Call/Return INITIAL -t,call-> Main.main -t,call-> Main.instance
         * -t,return-> Main.main -t,call-> Foo.foo -t,return-> Main.main -t->
         * TERMINAL
         * 
         * Static INITIAL -static-> Main.main -static-> Main.main -static->
         * Main.main
         * 
         * Instance INITIAL -instance-> Main.instance
         * 
         * Native INITIAL -native-> Foo.foo
         */

        ChainsTraceGraph expectedGraph = new ChainsTraceGraph(events);
        assertTrue(events.size() == 5);

        EventNode mainMain = events.get(0);
        expectedGraph.tagInitial(
                mainMain,
                new LinkedHashSet<String>(Arrays.asList(
                        Event.defTimeRelationStr, callRelation)));

        EventNode mainInstance = events.get(1);
        mainMain.addTransition(
                mainInstance,
                new LinkedHashSet<String>(Arrays.asList(
                        Event.defTimeRelationStr, callRelation)));

        EventNode mainMain2 = events.get(2);
        mainInstance.addTransition(
                mainMain2,
                new LinkedHashSet<String>(Arrays.asList(
                        Event.defTimeRelationStr, returnRelation)));

        EventNode fooFoo = events.get(3);
        mainMain2.addTransition(
                fooFoo,
                new LinkedHashSet<String>(Arrays.asList(
                        Event.defTimeRelationStr, callRelation)));

        EventNode mainMain3 = events.get(4);
        fooFoo.addTransition(
                mainMain3,
                new LinkedHashSet<String>(Arrays.asList(
                        Event.defTimeRelationStr, returnRelation)));

        expectedGraph.tagTerminal(mainMain3, Event.defTimeRelationStr);

        expectedGraph.tagInitial(mainMain, staticRelation);
        mainMain.addTransition(mainMain2, staticRelation);
        mainMain2.addTransition(mainMain3, staticRelation);

        expectedGraph.tagInitial(mainInstance, instanceRelation);
        expectedGraph.tagInitial(fooFoo, nativeRelation);

        return expectedGraph;
    }

    private ChainsTraceGraph genExpectedGraphForClosureRelations(
            ArrayList<EventNode> events) {
        /*
         * Generate expected graph for String traceStr =
         * "0 call static Main.main\n" + "1 call instance Main.instance\n" +
         * "2 return static Main.main\n" + "3 call native Foo.foo\n" +
         * "4 return static Main.main";
         * 
         * Time INITIAL -t-> Main.main -t-> Main.instance -t-> Main.main -t->
         * Foo.foo -t-> Main.main -t-> TERMINAL
         * 
         * Call INITIAL -call-> Main.main -call-> Main.instance -call-> Foo.foo
         * 
         * Return INITIAL -return-> Main.main -return-> Main.main
         * 
         * Static INITIAL -static-> Main.main -static-> Main.main -static->
         * Main.main
         * 
         * Instance INITIAL -instance-> Main.instance
         * 
         * Native INITIAL -native-> Foo.foo
         */

        ChainsTraceGraph expectedGraph = new ChainsTraceGraph(events);
        assertTrue(events.size() == 5);

        EventNode mainMain = events.get(0);
        expectedGraph.tagInitial(mainMain, Event.defTimeRelationStr);

        EventNode mainInstance = events.get(1);
        mainMain.addTransition(mainInstance, Event.defTimeRelationStr);

        EventNode mainMain2 = events.get(2);
        mainInstance.addTransition(mainMain2, Event.defTimeRelationStr);

        EventNode fooFoo = events.get(3);
        mainMain2.addTransition(fooFoo, Event.defTimeRelationStr);

        EventNode mainMain3 = events.get(4);
        fooFoo.addTransition(mainMain3, Event.defTimeRelationStr);

        expectedGraph.tagTerminal(mainMain3, Event.defTimeRelationStr);

        expectedGraph.tagInitial(mainMain, callRelation);
        mainMain.addTransition(mainInstance, callRelation);
        mainInstance.addTransition(fooFoo, callRelation);

        expectedGraph.tagInitial(mainMain2, returnRelation);
        mainMain2.addTransition(mainMain3, returnRelation);

        expectedGraph.tagInitial(mainMain, staticRelation);
        mainMain.addTransition(mainMain2, staticRelation);
        mainMain2.addTransition(mainMain3, staticRelation);

        expectedGraph.tagInitial(mainInstance, instanceRelation);

        expectedGraph.tagInitial(fooFoo, nativeRelation);

        return expectedGraph;
    }

    /**
     * Check that we can parse a multiple relations log with each line in total
     * order
     * 
     * @throws ParseException
     */
    @Test
    public void parseBasicMultipleRelation() throws ParseException {
        String traceStr = "0 call main\n" + "1 call foo\n" + "2 return main";
        parser.addRegex("^(?<TIME>)(?<RELATION>)(?<TYPE>)$");
        ArrayList<EventNode> events = parser.parseTraceString(traceStr, "test",
                -1);
        ChainsTraceGraph graph = parser.generateDirectTORelation(events);
        ChainsTraceGraph expectedGraph = genExpectedGraphForBasicMultipleRelation(events);
        assertTrue(expectedGraph.equalsWith(graph,
                new IBoolBinary<EventNode, EventNode>() {
                    @Override
                    public boolean eval(EventNode a, EventNode b) {
                        return (a.getEvent().equals(b.getEvent()));
                    }
                }));
    }

    /**
     * Check that we can parse a multiple relations log where lines are not in
     * the total order
     * 
     * @throws ParseException
     */
    @Test
    public void parseOutOfOrderMultipleRelation() throws ParseException {
        String traceStr = "1 call foo\n" + "0 call main\n" + "2 return main";
        parser.addRegex("^(?<TIME>)(?<RELATION>)(?<TYPE>)$");
        ArrayList<EventNode> events = parser.parseTraceString(traceStr, "test",
                -1);
        ChainsTraceGraph graph = parser.generateDirectTORelation(events);
        Collections.sort(events, new Comparator<EventNode>() {
            @Override
            public int compare(EventNode e1, EventNode e2) {
                return e1.getTime().compareTo(e2.getTime());
            }
        });
        ChainsTraceGraph expectedGraph = genExpectedGraphForBasicMultipleRelation(events);
        assertTrue(expectedGraph.equalsWith(graph,
                new IBoolBinary<EventNode, EventNode>() {
                    @Override
                    public boolean eval(EventNode a, EventNode b) {
                        return (a.getEvent().equals(b.getEvent()));
                    }
                }));
    }

    /**
     * Tests that we can parse basic multiple relations with partition
     * separators
     * 
     * @throws ParseException
     */
    @Test
    public void parseMultipleRelationPartitionsSeparator()
            throws ParseException {
        String traceStr = "0 call main\n" + "1 call foo\n" + "2 return main\n"
                + "--\n" + "0 call main\n" + "1 call foo\n" + "2 return main";
        parser.addRegex("^(?<TIME>)(?<RELATION>)(?<TYPE>)$");
        parser.addPartitionsSeparator("^--$");
        ArrayList<EventNode> events = parser.parseTraceString(traceStr, "test",
                -1);
        ChainsTraceGraph graph = parser.generateDirectTORelation(events);
        ChainsTraceGraph expectedGraph = genExpectedGraphForSeparatorAndPartitionMultipleRelation(events);
        assertTrue(expectedGraph.equalsWith(graph,
                new IBoolBinary<EventNode, EventNode>() {
                    @Override
                    public boolean eval(EventNode a, EventNode b) {
                        return (a.getEvent().equals(b.getEvent()));
                    }
                }));
    }

    /**
     * Tests that we can parse basic multiple relations with partition maps
     * 
     * @throws ParseException
     */
    @Test
    public void parseMultipleRelationPartitionsMap() throws ParseException {
        String traceStr = "1 0 call main\n" + "1 1 call foo\n"
                + "1 2 return main\n" + "2 0 call main\n" + "2 1 call foo\n"
                + "2 2 return main";
        parser.addRegex("^(?<PARTITION>)(?<TIME>)(?<RELATION>)(?<TYPE>)$");
        parser.setPartitionsMap("\\k<PARTITION>");
        ArrayList<EventNode> events = parser.parseTraceString(traceStr, "test",
                -1);
        ChainsTraceGraph graph = parser.generateDirectTORelation(events);
        ChainsTraceGraph expectedGraph = genExpectedGraphForSeparatorAndPartitionMultipleRelation(events);
        assertTrue(expectedGraph.equalsWith(graph,
                new IBoolBinary<EventNode, EventNode>() {
                    @Override
                    public boolean eval(EventNode a, EventNode b) {
                        return (a.getEvent().equals(b.getEvent()));
                    }
                }));
    }

    /**
     * Multiple relations is dependant on TO logs so throw an exception if log
     * is Partially ordered
     * 
     * @throws ParseException
     */
    @Test(expected = ParseException.class)
    public void parseVTimeWithMultipleRelations() throws ParseException {
        String traceStr = "1,0,0 call main\n" + "0,1,0 call foo\n"
                + "2,1,0 return main";
        parser.addRegex("^(?<VTIME>)(?<RELATION>)(?<TYPE>)$");
        parser.parseTraceString(traceStr, "test", -1);
    }

    /**
     * Multiple relations is dependant on TO logs so throw an exception if log
     * is Partially ordered
     * 
     * @throws ParseException
     */
    @Test(expected = ParseException.class)
    public void parseVTimeWithNamedMultipleRelations() throws ParseException {
        String traceStr = "1,0,0 call main\n" + "0,1,0 call foo\n"
                + "2,1,0 return main";
        parser.addRegex("^(?<VTIME>)(?<RELATION-CallReturn>)(?<TYPE>)$");
        parser.parseTraceString(traceStr, "test", -1);
    }

    /**
     * Multiple relations is dependant on TO logs so throw an exception if log
     * is Partially ordered
     * 
     * @throws ParseException
     */
    @Test(expected = ParseException.class)
    public void parseVTimeWithClosureRelations() throws ParseException {
        String traceStr = "1,0,0 call main\n" + "0,1,0 call foo\n"
                + "2,1,0 return main";
        parser.addRegex("^(?<VTIME>)(?<RELATION*>)(?<TYPE>)$");
        parser.parseTraceString(traceStr, "test", -1);
    }

    /**
     * Multiple relations is dependant on TO logs so throw an exception if log
     * is Partially ordered
     * 
     * @throws ParseException
     */
    @Test(expected = ParseException.class)
    public void parseVTimeWithNamedClosureRelations() throws ParseException {
        String traceStr = "1,0,0 call main\n" + "0,1,0 call foo\n"
                + "2,1,0 return main";
        parser.addRegex("^(?<VTIME>)(?<RELATION*-CallReturn>)(?<TYPE>)$");
        parser.parseTraceString(traceStr, "test", -1);
    }

    /**
     * Test that parser can parse multiple relations per log line using multiple
     * regex fields
     * 
     * @throws ParseException
     */
    @Test
    public void parseMultipleRelationsMultiRegex() throws ParseException {
        String traceStr = "0 call static Main.main\n"
                + "1 call instance Main.instance\n"
                + "2 return static Main.main\n" + "3 call native Foo.foo\n"
                + "4 return static Main.main";
        parser.addRegex("^(?<TIME>)(?<RELATION-CallReturn>)(?<RELATION-MethodType>)(?<TYPE>)$");
        ArrayList<EventNode> events = parser.parseTraceString(traceStr, "test",
                -1);
        ChainsTraceGraph graph = parser.generateDirectTORelation(events);
        ChainsTraceGraph expectedGraph = genExpectedGraphForMultipleRelationsPerLine(events);
        assertTrue(expectedGraph.equalsWith(graph,
                new IBoolBinary<EventNode, EventNode>() {
                    @Override
                    public boolean eval(EventNode a, EventNode b) {
                        return (a.getEvent().equals(b.getEvent()));
                    }
                }));
    }

    /**
     * Test that parser can parse multiple closure and generic relations per log
     * line using multiple regex fields
     * 
     * @throws ParseException
     */
    @Test
    public void parseClosureAndGenericRelations() throws ParseException {
        String traceStr = "0 call static Main.main\n"
                + "1 call instance Main.instance\n"
                + "2 return static Main.main\n" + "3 call native Foo.foo\n"
                + "4 return static Main.main";
        parser.addRegex("^(?<TIME>)(?<RELATION-CallReturn>)(?<RELATION*-MethodType>)(?<TYPE>)$");
        ArrayList<EventNode> events = parser.parseTraceString(traceStr, "test",
                -1);
        ChainsTraceGraph graph = parser.generateDirectTORelation(events);
        ChainsTraceGraph expectedGraph = genExpectedGraphForClosureAndGenericMultipleRelations(events);
        assertTrue(expectedGraph.equalsWith(graph,
                new IBoolBinary<EventNode, EventNode>() {
                    @Override
                    public boolean eval(EventNode a, EventNode b) {
                        return (a.getEvent().equals(b.getEvent()));
                    }
                }));
    }

    /**
     * Test that parser can parse only closure relations in a log line using
     * multiple regex fields
     * 
     * @throws ParseException
     */
    @Test
    public void parseMultipleClosureRelations() throws ParseException {
        String traceStr = "0 call static Main.main\n"
                + "1 call instance Main.instance\n"
                + "2 return static Main.main\n" + "3 call native Foo.foo\n"
                + "4 return static Main.main";
        parser.addRegex("^(?<TIME>)(?<RELATION*-CallReturn>)(?<RELATION*-MethodType>)(?<TYPE>)$");
        ArrayList<EventNode> events = parser.parseTraceString(traceStr, "test",
                -1);
        ChainsTraceGraph graph = parser.generateDirectTORelation(events);
        ChainsTraceGraph expectedGraph = genExpectedGraphForClosureRelations(events);
        assertTrue(expectedGraph.equalsWith(graph,
                new IBoolBinary<EventNode, EventNode>() {
                    @Override
                    public boolean eval(EventNode a, EventNode b) {
                        return (a.getEvent().equals(b.getEvent()));
                    }
                }));
    }

    /**
     * Test that parser throws an error for multiple anonymous captures
     * 
     * @throws ParseException
     */
    @Test(expected = ParseException.class)
    public void parseMultipleRelationsMultipleAnonymousCaptures()
            throws ParseException {
        String traceStr = "0 call static Main.main\n"
                + "1 call instance Main.instance\n"
                + "2 return static Main.main\n" + "3 call native Foo.foo\n"
                + "4 return static Main.main";
        parser.addRegex("^(?<TIME>)(?<RELATION>)(?<RELATION>)(?<TYPE>)$");
        parser.parseTraceString(traceStr, "test", -1);
    }

    /**
     * Test that parser throws an error for multiple anonymous closure captures
     * 
     * @throws ParseException
     */
    @Test(expected = ParseException.class)
    public void parseMultipleRelationsMultipleAnonymousClosureCaptures()
            throws ParseException {
        String traceStr = "0 call static Main.main\n"
                + "1 call instance Main.instance\n"
                + "2 return static Main.main\n" + "3 call native Foo.foo\n"
                + "4 return static Main.main";
        parser.addRegex("^(?<TIME>)(?<RELATION*>)(?<RELATION*>)(?<TYPE>)$");
        parser.parseTraceString(traceStr, "test", -1);
    }

    /**
     * Test that parser throws an error for duplicate relation capture values
     * 
     * @throws ParseException
     */
    @Test(expected = ParseException.class)
    public void parseMultipleRelationsDuplicateRelationCapture()
            throws ParseException {
        String traceStr = "0 static static Main.main";
        parser.addRegex("^(?<TIME>)(?<RELATION-a>)(?<RELATION-b>)(?<TYPE>)$");
        parser.parseTraceString(traceStr, "test", -1);
    }

    /**
     * Test that parser throws an error for duplicate closure relation capture
     * values
     * 
     * @throws ParseException
     */
    @Test(expected = ParseException.class)
    public void parseMultipleRelationsDuplicateClosureRelationCapture()
            throws ParseException {
        String traceStr = "0 static static Main.main";
        parser.addRegex("^(?<TIME>)(?<RELATION*-a>)(?<RELATION*-b>)(?<TYPE>)$");
        parser.parseTraceString(traceStr, "test", -1);
    }

    /**
     * Test that parser throws an error for duplicate relation capture values
     * 
     * @throws ParseException
     */
    @Test(expected = ParseException.class)
    public void parseMultipleRelationsDuplicateMixedRelationCapture()
            throws ParseException {
        String traceStr = "0 static static Main.main";
        parser.addRegex("^(?<TIME>)(?<RELATION>)(?<RELATION*>)(?<TYPE>)$");
        parser.parseTraceString(traceStr, "test", -1);
    }

    /**
     * Check that the type/pre- and post-event states of each log event
     * in the list is correct.
     * 
     * @throws ParseException
     */
    private void checkEventTypesAndStates(List<EventNode> eventNodes,
            EventType[] types, State[] preEvent, State[] postEvent) throws ParseException {
        assertTrue(eventNodes.size() == types.length);
        for (int i = 0; i < eventNodes.size(); i++) {
            EventNode eventNode = eventNodes.get(i);
            assertEquals(types[i], eventNode.getEType());
            assertTrue((preEvent[i] == null && eventNode.getPreEventState() == null)
                    || preEvent[i].equals(eventNode.getPreEventState()));
            assertTrue((postEvent[i] == null && eventNode.getPostEventState() == null)
                    || postEvent[i].equals(eventNode.getPostEventState()));
        }
    }
    
    /**
     * Parse a trace that contains a single state -- expect parse exception.
     * 
     * @throws ParseException
     */
    @Test(expected = ParseException.class)
    public void parseTraceWithSingleStateTest() throws ParseException {
        String traceStr = "STATE:s=0\n";
        parser.addRegex("^STATE:(?<STATE>)$");
        parser.parseTraceString(traceStr, "test", -1);
    }
    
    /**
     * Parse an event with a pre-event state.
     * 
     * @throws ParseException 
     */
    @Test
    public void parseEventWithPreEventStateTest() throws ParseException {
        String traceStr = "STATE:x=0,y=1\na\n";
        parser.addRegex("^(?<TYPE>[abc])$");
        parser.addRegex("^STATE:(?<STATE>)$");
        List<EventNode> eventNodes = parser.parseTraceString(traceStr, "test", -1);
        EventType type = new StringEventType("a");
        State state = new State("x=0,y=1");
        checkEventTypesAndStates(eventNodes, new EventType[] { type },
                new State[] { state }, new State[] { null });
    }
    
    /**
     * Parse an event with a post-event state.
     * 
     * @throws ParseException 
     */
    @Test
    public void parseEventWithPostEventStateTest() throws ParseException {
        String traceStr = "b\nSTATE:i=1,j=2\n";
        parser.addRegex("^(?<TYPE>[abc])$");
        parser.addRegex("^STATE:(?<STATE>)$");
        List<EventNode> eventNodes = parser.parseTraceString(traceStr, "test", -1);
        EventType type = new StringEventType("b");
        State state = new State("i=1,j=2");
        checkEventTypesAndStates(eventNodes, new EventType[] { type },
                new State[] { null }, new State[] { state });
    }
    
    /**
     * Parse an event with pre- and post-event states.
     * 
     * @throws ParseException 
     */
    @Test
    public void parseEventWithPrePostEventStatesTest() throws ParseException {
        String traceStr = "STATE:n=3,m=4\nc\nSTATE:p=0,q=1\n";
        parser.addRegex("^(?<TYPE>[abc])$");
        parser.addRegex("^STATE:(?<STATE>)$");
        List<EventNode> eventNodes = parser.parseTraceString(traceStr, "test", -1);
        EventType type = new StringEventType("c");
        State preEvent = new State("n=3,m=4");
        State postEvent = new State("p=0,q=1");
        checkEventTypesAndStates(eventNodes, new EventType[] { type },
                new State[] { preEvent }, new State[] { postEvent });
    }
    
    /**
     * Parse 2 events interleaved by a state -- the first event should
     * have a post-event and the second should have a pre-event.
     * 
     * @throws ParseException 
     */
    @Test
    public void parseTwoEventsInterleavedByStateTest() throws ParseException {
        String traceStr = "a\nSTATE:x=0,y=1\nb\n";
        parser.addRegex("^(?<TYPE>[abc])$");
        parser.addRegex("^STATE:(?<STATE>)$");
        List<EventNode> eventNodes = parser.parseTraceString(traceStr, "test", -1);
        EventType a = new StringEventType("a");
        EventType b = new StringEventType("b");
        State preEvent = new State("x=0,y=1");
        State postEvent = new State("x=0,y=1");
        checkEventTypesAndStates(eventNodes, new EventType[] { a, b },
                new State[] { null, preEvent }, new State[] { postEvent, null });
    }

    /**
     * Parse 2 events that follow a single state -- the first event
     * should have a pre-event but the second event should not.
     * 
     * @throws ParseException 
     */
    @Test
    public void parseTwoEventsFollowSingleStateTest() throws ParseException {
        String traceStr = "STATE:u=0,t=1\na\nb\n";
        parser.addRegex("^(?<TYPE>[abc])$");
        parser.addRegex("^STATE:(?<STATE>)$");
        List<EventNode> eventNodes = parser.parseTraceString(traceStr, "test", -1);
        EventType a = new StringEventType("a");
        EventType b = new StringEventType("b");
        State state = new State("u=0,t=1");
        checkEventTypesAndStates(eventNodes, new EventType[] { a, b },
                new State[] { state, null }, new State[] { null, null });
    }
    
    /**
     * Parse a string containing 2 traces. A state should not be merged to an event
     * of different trace.
     * 
     * @throws ParseException
     */
    @Test
    public void parseStateWithSplitPartitionsTest() throws ParseException {
        String traceStr = "a\nSTATE:connected=true\n--\nb\n";
        parser.addRegex("^(?<TYPE>[abc])$");
        parser.addRegex("^STATE:(?<STATE>)$");
        parser.addPartitionsSeparator("^--$");
        List<EventNode> eventNodes = parser.parseTraceString(traceStr, "test", -1);
        EventType a = new StringEventType("a");
        EventType b = new StringEventType("b");
        State state = new State("connected=true");
        checkEventTypesAndStates(eventNodes, new EventType[] { a, b },
                new State[] { null, null }, new State[] { state, null });
    }
    
    /**
     * Parse 2 consecutive states of same the trace -- expect parse exception.
     * 
     * @throws ParseException
     */
    @Test(expected = ParseException.class)
    public void parseTwoConsecutiveStatesExpExceptionTest() throws ParseException {
        String traceStr = "a\nSTATE:x=0,y=1\nSTATE:connected=true\n";
        parser.addRegex("^(?<TYPE>[abc])$");
        parser.addRegex("^STATE:(?<STATE>)$");
        parser.parseTraceString(traceStr, "test", -1);
    }

    /**
     * Construct a state with invalid format of state string.
     * 
     * @throws ParseException
     */
    @Test(expected = ParseException.class)
    public void stateConstructorInvalidStateStrExpExceptionTest() throws ParseException {
        String stateStr = "q == 1";
        State state = new State(stateStr);
    }
}
