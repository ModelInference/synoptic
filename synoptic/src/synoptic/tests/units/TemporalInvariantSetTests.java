package synoptic.tests.units;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import synoptic.invariants.AlwaysFollowedInvariant;
import synoptic.invariants.AlwaysPrecedesInvariant;
import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.NeverFollowedInvariant;
import synoptic.invariants.RelationPath;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.main.Main;
import synoptic.main.ParseException;
import synoptic.main.TraceParser;
import synoptic.model.Graph;
import synoptic.model.LogEvent;
import synoptic.model.PartitionGraph;
import synoptic.util.InternalSynopticException;

/**
 * Tests for synoptic.invariants.TemporalInvariantSet class.
 * 
 * @author ivan
 */
public class TemporalInvariantSetTests {
    /**
     * We test invariants by parsing a string representing a log, and mining
     * invariants from the resulting graph.
     */
    TraceParser parser;

    /**
     * Default relation used in invariant mining.
     */
    static final String defRelation = "t";

    /**
     * Sets up the parser state and Main static variables
     * 
     * @throws ParseException
     * @throws InternalSynopticException
     */
    @Before
    public void setUp() throws ParseException, InternalSynopticException {
        Main.recoverFromParseErrors = false;
        Main.ignoreNonMatchingLines = false;
        Main.debugParse = false;
        parser = new TraceParser();
        parser.addRegex("^(?<TYPE>)$");
        parser.addSeparator("^--$");
    }

    /**
     * Creates a single string out of an array of strings, joined together and
     * delimited using a newline
     * 
     * @param strAr
     *            array of strings to join
     * @return the joined string
     */
    private String joinString(String[] strAr) {
        StringBuilder sb = new StringBuilder();
        for (String s : strAr) {
            sb.append(s);
            sb.append('\n');
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    /**
     * Generates a TemporalInvariantSet based on a sequence of log events -- a
     * set of invariants that are mined from the log, and hold true for the
     * initial graph of the log.
     * 
     * @param a
     *            log of events, each one in the format: (?<TYPE>)
     * @return an invariant set for the input log
     * @throws ParseException
     * @throws InternalSynopticException
     */
    private TemporalInvariantSet genInvariants(String[] events)
            throws ParseException, InternalSynopticException {
        Graph<LogEvent> inputGraph = genInitialGraph(events);
        PartitionGraph result = new PartitionGraph(inputGraph, true);
        return result.getInvariants();
    }

    /**
     * Generates an initial graph based on a sequence of log events.
     * 
     * @param a
     *            log of events, each one in the format: (?<TYPE>)
     * @return an initial graph corresponding to the log of events
     * @throws ParseException
     * @throws InternalSynopticException
     */
    private Graph<LogEvent> genInitialGraph(String[] events)
            throws ParseException, InternalSynopticException {
        String traceStr = joinString(events);
        List<LogEvent> parsedEvents = parser.parseTraceString(traceStr, "test",
                -1);
        return parser.generateDirectTemporalRelation(parsedEvents, true);
    }

    /**
     * Tests the sameInvariants() method.
     */
    @Test
    public void testSameInvariants() {
        TemporalInvariantSet s1 = new TemporalInvariantSet();
        TemporalInvariantSet s2 = new TemporalInvariantSet();

        assertTrue(s1.sameInvariants(s2));
        assertTrue(s2.sameInvariants(s1));

        s1.add(new AlwaysFollowedInvariant("a", "b", defRelation));
        assertFalse(s1.sameInvariants(s2));
        assertFalse(s2.sameInvariants(s1));

        s2.add(new AlwaysFollowedInvariant("a", "b", defRelation));
        assertTrue(s1.sameInvariants(s2));
        assertTrue(s2.sameInvariants(s1));

        s1.add(new NeverFollowedInvariant("b", "a", defRelation));
        assertFalse(s1.sameInvariants(s2));
        assertFalse(s2.sameInvariants(s1));

        // Add a similar looking invariant, but different in the B/b label.
        s2.add(new NeverFollowedInvariant("B", "a", defRelation));
        assertFalse(s1.sameInvariants(s2));
        assertFalse(s2.sameInvariants(s1));
    }

    /**
     * Checks the mined invariants from a log with just two events types.
     * 
     * @throws ParseException
     * @throws InternalSynopticException
     */
    @Test
    public void mineBasicTest() throws ParseException,
            InternalSynopticException {
        String[] log = new String[] { "a", "b", "--" };
        TemporalInvariantSet minedInvs = genInvariants(log);
        TemporalInvariantSet trueInvs = new TemporalInvariantSet();

        trueInvs.add(new AlwaysFollowedInvariant("a", "b", defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("a", "b", defRelation));
        trueInvs.add(new NeverFollowedInvariant("b", "a", defRelation));
        trueInvs.add(new NeverFollowedInvariant("b", "b", defRelation));
        trueInvs.add(new NeverFollowedInvariant("a", "a", defRelation));

        assertTrue(trueInvs.sameInvariants(minedInvs));
    }

    /**
     * Compose a log in which "a AFby b" is the only true invariant.
     * 
     * @throws ParseException
     * @throws InternalSynopticException
     */
    @Test
    public void mineAFbyTest() throws ParseException, InternalSynopticException {
        String[] log = new String[] { "a", "a", "b", "--", "b", "a", "b", "--" };
        TemporalInvariantSet minedInvs = genInvariants(log);
        TemporalInvariantSet trueInvs = new TemporalInvariantSet();

        trueInvs.add(new AlwaysFollowedInvariant("a", "b", defRelation));
        assertTrue(trueInvs.sameInvariants(minedInvs));
    }

    /**
     * Compose a log in which "a NFby b" is the only true invariant.
     * 
     * @throws InternalSynopticException
     * @throws ParseException
     */
    @Test
    public void mineNFbyTest() throws ParseException, InternalSynopticException {
        String[] log = new String[] { "a", "a", "--", "a", "--", "b", "a",
                "--", "b", "b", "--", "b", "--" };
        TemporalInvariantSet minedInvs = genInvariants(log);
        TemporalInvariantSet trueInvs = new TemporalInvariantSet();

        trueInvs.add(new NeverFollowedInvariant("a", "b", defRelation));
        assertTrue(trueInvs.sameInvariants(minedInvs));
    }

    /**
     * Compose a log in which "a AP b" is the only true invariant.
     * 
     * @throws InternalSynopticException
     * @throws ParseException
     */
    @Test
    public void mineAPbyTest() throws ParseException, InternalSynopticException {
        String[] log = new String[] { "a", "a", "b", "--", "a", "--", "a", "b",
                "a", "b", "--" };
        TemporalInvariantSet minedInvs = genInvariants(log);
        TemporalInvariantSet trueInvs = new TemporalInvariantSet();

        trueInvs.add(new AlwaysPrecedesInvariant("a", "b", defRelation));
        assertTrue(trueInvs.sameInvariants(minedInvs));
    }

    /**
     * Tests the correctness of the invariants mined between two partitions,
     * which have no event types in common.
     * 
     * @throws ParseException
     * @throws InternalSynopticException
     */
    @Test
    public void mineAcrossMultiplePartitionsTest() throws ParseException,
            InternalSynopticException {
        String[] log1 = new String[] { "a", "b", "--" };
        String[] log2 = new String[] { "x", "y", "--" };
        String[] log3 = new String[] { "a", "b", "--", "x", "y", "--" };

        TemporalInvariantSet minedInvs1 = genInvariants(log1);
        TemporalInvariantSet minedInvs2 = genInvariants(log2);
        TemporalInvariantSet minedInvs3 = genInvariants(log3);

        // Mined log3 invariants should be a union of invariants mined form log1
        // and log2, as well as a few invariants that relate events between the
        // two partitions.
        TemporalInvariantSet trueInvs3 = new TemporalInvariantSet();
        for (ITemporalInvariant inv : minedInvs1) {
            trueInvs3.add(inv);
        }
        for (ITemporalInvariant inv : minedInvs2) {
            trueInvs3.add(inv);
        }

        trueInvs3.add(new NeverFollowedInvariant("a", "x", defRelation));
        trueInvs3.add(new NeverFollowedInvariant("a", "y", defRelation));
        trueInvs3.add(new NeverFollowedInvariant("b", "x", defRelation));
        trueInvs3.add(new NeverFollowedInvariant("b", "y", defRelation));

        trueInvs3.add(new NeverFollowedInvariant("x", "a", defRelation));
        trueInvs3.add(new NeverFollowedInvariant("y", "b", defRelation));
        trueInvs3.add(new NeverFollowedInvariant("x", "a", defRelation));
        trueInvs3.add(new NeverFollowedInvariant("y", "b", defRelation));

        System.out.println("mined: " + minedInvs3);
        assertTrue(trueInvs3.sameInvariants(minedInvs3));
    }

    /**
     * Mines invariants from a randomly generated log and then uses the model
     * checker to check that every mined invariant actually holds.
     * 
     * @throws InternalSynopticException
     * @throws ParseException
     */
    @Test
    public void testApproximationExactnessTest() throws ParseException,
            InternalSynopticException {
        ArrayList<String> log = new ArrayList<String>();
        Random generator = new Random();

        // Arbitrary partition limit.
        int numPartitions = 5;

        // Event types allowed in the log, with partition string at index 0.
        String[] eventTypes = new String[] { "--", "a", "b", "c" };

        // Generate a random log.
        while (numPartitions != 0) {
            int rndIndex = generator.nextInt(eventTypes.length);
            log.add(eventTypes[rndIndex]);
            if (rndIndex == 0) {
                numPartitions -= 1;
            }
        }

        Graph<LogEvent> inputGraph = genInitialGraph(log.toArray(new String[log
                .size()]));
        PartitionGraph graph = new PartitionGraph(inputGraph, true);
        TemporalInvariantSet minedInvs = graph.getInvariants();

        // Test with FSM checker.
        Main.useFSMChecker = true;
        List<RelationPath<LogEvent>> cExamples = minedInvs
                .getAllCounterExamples(inputGraph);
        if (cExamples != null) {
            System.out.println("log: " + log);
            System.out.println("minedInvs: " + minedInvs);
            System.out.println("[FSM] cExamples: " + cExamples);
        }
        assertTrue(cExamples == null);

        // Test with LTL checker.
        Main.useFSMChecker = false;
        cExamples = minedInvs.getAllCounterExamples(inputGraph);
        if (cExamples != null) {
            System.out.println("log: " + log);
            System.out.println("minedInvs: " + minedInvs);
            System.out.println("[LTL] cExamples: " + cExamples);
        }
        assertTrue(cExamples == null);
    }
}
