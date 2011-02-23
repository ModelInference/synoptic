package synoptic.tests.units;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

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
import synoptic.tests.SynopticTest;
import synoptic.util.InternalSynopticException;

/**
 * Tests for synoptic.invariants.TemporalInvariantSet class. We test invariants
 * by parsing a string representing a log, and mining invariants from the
 * resulting graph.
 * 
 * @author ivan
 */
public class TemporalInvariantSetTests extends SynopticTest {

    /**
     * Generates a random log composed of three types of events ("a", "b", "c"),
     * and includes random partitions of the form "---".
     * 
     * @return The randomly generated log.
     */
    public static String[] genRandomLog(String[] eventTypes) {
        ArrayList<String> log = new ArrayList<String>();
        // Arbitrary partition limit.
        int numPartitions = 5;

        // Generate a random log.
        while (numPartitions != 0) {
            int rndIndex = Main.random.nextInt(eventTypes.length);
            log.add(eventTypes[rndIndex]);
            if (rndIndex == 0) {
                numPartitions -= 1;
            }
        }
        return log.toArray(new String[log.size()]);
    }

    /**
     * Generates a TemporalInvariantSet based on a sequence of log events -- a
     * set of invariants that are mined from the log, and hold true for the
     * initial graph of the log.
     * 
     * @param a
     *            log of events, each one in the format: (?<TYPE>)
     * @return an invariant set for the input log
     * @throws Exception
     */
    public static TemporalInvariantSet genInvariants(String[] events)
            throws Exception {
        Graph<LogEvent> inputGraph = SynopticTest.genInitialLinearGraph(events);
        exportTestGraph(inputGraph, 0);

        PartitionGraph result = new PartitionGraph(inputGraph, true);
        exportTestGraph(result, 1);

        return result.getInvariants();
    }

    /**
     * Generates a TemporalInvariantSet based on a sequence of log events -- a
     * set of invariants that are mined from the log, and hold true for the
     * initial graph of the log. This set includes all Tautological invariants
     * as well. So these are the "raw" invariants which are mined.
     * 
     * @param a
     *            log of events, each one in the format: (?<TYPE>)
     * @return an invariant set for the input log
     * @throws ParseException
     * @throws InternalSynopticException
     */
    public static TemporalInvariantSet genInvariantsIncludingTautological(
            String[] events) throws ParseException, InternalSynopticException {
        Graph<LogEvent> inputGraph = SynopticTest.genInitialLinearGraph(events);
        return TemporalInvariantSet.computeInvariants(inputGraph);
    }

    /**
     * Generates a list of tautological invariants for a trace. Currently these
     * only involve INITIAL and TERMINAL trace nodes.
     * 
     * @param allEvents
     *            The list of all events in the trace for which to generate the
     *            tautological invariants
     * @return A list of tautological temporal invariants for the input trace
     */
    public static List<ITemporalInvariant> genTautologicalInvariants(
            List<String> allEvents) {
        ArrayList<ITemporalInvariant> invs = new ArrayList<ITemporalInvariant>();
        for (String e : allEvents) {
            // x AFby TERMINAL
            invs.add(new AlwaysFollowedInvariant(e, Main.terminalNodeLabel,
                    SynopticTest.defRelation));

            // INITIAL AFby x
            invs.add(new AlwaysFollowedInvariant(Main.initialNodeLabel, e,
                    SynopticTest.defRelation));

            // x NFby INITIAL
            invs.add(new NeverFollowedInvariant(e, Main.initialNodeLabel,
                    SynopticTest.defRelation));
            // TERMINAL NFby x
            invs.add(new NeverFollowedInvariant(Main.terminalNodeLabel, e,
                    SynopticTest.defRelation));
            // INITIAL AP x
            invs.add(new AlwaysPrecedesInvariant(Main.initialNodeLabel, e,
                    SynopticTest.defRelation));

            // x AP TERMINAL
            invs.add(new AlwaysPrecedesInvariant(e, Main.terminalNodeLabel,
                    SynopticTest.defRelation));
        }
        // INITIAL AFby TERMINAL
        invs.add(new AlwaysFollowedInvariant(Main.initialNodeLabel,
                Main.terminalNodeLabel, SynopticTest.defRelation));
        // TERMINAL NFby INITIAL
        invs.add(new NeverFollowedInvariant(Main.terminalNodeLabel,
                Main.initialNodeLabel, SynopticTest.defRelation));
        // TERMINAL NFby TERMINAL
        invs.add(new NeverFollowedInvariant(Main.terminalNodeLabel,
                Main.terminalNodeLabel, SynopticTest.defRelation));
        // INITIAL NFby INITIAL
        invs.add(new NeverFollowedInvariant(Main.initialNodeLabel,
                Main.initialNodeLabel, SynopticTest.defRelation));
        // INITIAL AP TERMINAL
        invs.add(new AlwaysPrecedesInvariant(Main.initialNodeLabel,
                Main.terminalNodeLabel, SynopticTest.defRelation));
        logger.fine("Returning tautological invariants: " + invs.toString());
        return invs;
    }

    /**
     * Tests whether the invariants involving INITIAL\TERMINAL nodes are mined
     * correctly.
     * 
     * @throws Exception
     */
    @Test
    public void testTautologicalInvariantMining() throws Exception {

        String[] eventTypes = new String[] { "--", "a", "b", "c", "d", "e" };
        String[] log = genRandomLog(eventTypes);

        ArrayList<String> allEvents = new ArrayList<String>();
        for (String e : log) {
            if (!e.equals("--")) {
                allEvents.add(e);
            }
        }

        // Generate set including tautological invariants.
        TemporalInvariantSet s1 = genInvariantsIncludingTautological(log);

        // Generate set without tautological invariants.
        TemporalInvariantSet s2 = genInvariants(log);

        logger.fine("set including tautological: " + s1);
        logger.fine("set without tautological: " + s2);

        s2.addAll(genTautologicalInvariants(allEvents));
        logger.fine("set without tautological + true tautological: " + s2);

        assertTrue(s1.sameInvariants(s2));
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

        s1.add(new AlwaysFollowedInvariant("a", "b", SynopticTest.defRelation));
        assertFalse(s1.sameInvariants(s2));
        assertFalse(s2.sameInvariants(s1));

        s2.add(new AlwaysFollowedInvariant("a", "b", SynopticTest.defRelation));
        assertTrue(s1.sameInvariants(s2));
        assertTrue(s2.sameInvariants(s1));

        s1.add(new NeverFollowedInvariant("b", "a", SynopticTest.defRelation));
        assertFalse(s1.sameInvariants(s2));
        assertFalse(s2.sameInvariants(s1));

        // Add a similar looking invariant, but different in the B/b label.
        s2.add(new NeverFollowedInvariant("B", "a", SynopticTest.defRelation));
        assertFalse(s1.sameInvariants(s2));
        assertFalse(s2.sameInvariants(s1));
    }

    /**
     * Checks the mined invariants from a log with just two events types.
     * 
     * @throws Exception
     */
    @Test
    public void mineBasicTest() throws Exception {
        String[] log = new String[] { "a", "b", "--" };
        TemporalInvariantSet minedInvs = genInvariants(log);
        TemporalInvariantSet trueInvs = new TemporalInvariantSet();

        trueInvs.add(new AlwaysFollowedInvariant(Main.initialNodeLabel, "b",
                SynopticTest.defRelation));
        trueInvs.add(new AlwaysFollowedInvariant(Main.initialNodeLabel, "a",
                SynopticTest.defRelation));
        trueInvs.add(new AlwaysFollowedInvariant("a", "b",
                SynopticTest.defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("a", "b",
                SynopticTest.defRelation));
        trueInvs.add(new NeverFollowedInvariant("b", "a",
                SynopticTest.defRelation));
        trueInvs.add(new NeverFollowedInvariant("b", "b",
                SynopticTest.defRelation));
        trueInvs.add(new NeverFollowedInvariant("a", "a",
                SynopticTest.defRelation));

        assertTrue(trueInvs.sameInvariants(minedInvs));
    }

    /**
     * Compose a log in which "a AFby b" is the only true invariant.
     * 
     * @throws Exception
     */
    @Test
    public void mineAFbyTest() throws Exception {
        String[] log = new String[] { "a", "a", "b", "--", "b", "a", "b", "--" };
        TemporalInvariantSet minedInvs = genInvariants(log);
        TemporalInvariantSet trueInvs = new TemporalInvariantSet();

        trueInvs.add(new AlwaysFollowedInvariant(Main.initialNodeLabel, "b",
                SynopticTest.defRelation));
        trueInvs.add(new AlwaysFollowedInvariant(Main.initialNodeLabel, "a",
                SynopticTest.defRelation));
        trueInvs.add(new AlwaysFollowedInvariant("a", "b",
                SynopticTest.defRelation));
        assertTrue(trueInvs.sameInvariants(minedInvs));
    }

    /**
     * Compose a log in which "a NFby b" is the only true invariant.
     * 
     * @throws Exception
     */
    @Test
    public void mineNFbyTest() throws Exception {
        String[] log = new String[] { "a", "a", "--", "a", "--", "b", "a",
                "--", "b", "b", "--", "b", "--" };
        TemporalInvariantSet minedInvs = genInvariants(log);
        TemporalInvariantSet trueInvs = new TemporalInvariantSet();

        trueInvs.add(new NeverFollowedInvariant("a", "b",
                SynopticTest.defRelation));
        assertTrue(trueInvs.sameInvariants(minedInvs));
    }

    /**
     * Compose a log in which "a AP b" is the only true invariant. Except that
     * we making it the only invariant complicates the trace too much so we
     * settle for also including the INITIAL AFby a invariant.
     * 
     * @throws Exception
     */
    @Test
    public void mineAPbyTest() throws Exception {
        String[] log = new String[] { "a", "a", "b", "--", "a", "--", "a", "b",
                "a", "b", "--" };
        TemporalInvariantSet minedInvs = genInvariants(log);
        TemporalInvariantSet trueInvs = new TemporalInvariantSet();

        trueInvs.add(new AlwaysFollowedInvariant(Main.initialNodeLabel, "a",
                SynopticTest.defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("a", "b",
                SynopticTest.defRelation));
        assertTrue(trueInvs.sameInvariants(minedInvs));
    }

    /**
     * Tests the correctness of the invariants mined between two partitions,
     * which have no event types in common.
     * 
     * @throws Exception
     */
    @Test
    public void mineAcrossMultiplePartitionsTest() throws Exception {
        String[] log1 = new String[] { "a", "b", "--" };
        String[] log2 = new String[] { "x", "y", "--" };
        String[] log3 = new String[] { "a", "b", "--", "x", "y", "--" };

        TemporalInvariantSet minedInvs1 = genInvariants(log1);
        TemporalInvariantSet minedInvs2 = genInvariants(log2);
        TemporalInvariantSet minedInvs3 = genInvariants(log3);

        // Mined log3 invariants should be the UNION of invariants mined form
        // log1 and log2, as well as a few invariants that relate events between
        // the two partitions, MINUS the "eventually x" invariants mined for
        // log1 and log2 which no longer hold in log3.

        TemporalInvariantSet trueInvs3 = new TemporalInvariantSet();
        for (ITemporalInvariant inv : minedInvs1) {
            if (inv instanceof AlwaysFollowedInvariant
                    && ((AlwaysFollowedInvariant) inv).getFirst().equals(
                            Main.initialNodeLabel)) {
                continue;
            }
            trueInvs3.add(inv);
        }
        for (ITemporalInvariant inv : minedInvs2) {
            if (inv instanceof AlwaysFollowedInvariant
                    && ((AlwaysFollowedInvariant) inv).getFirst().equals(
                            Main.initialNodeLabel)) {
                continue;
            }
            trueInvs3.add(inv);
        }

        trueInvs3.add(new NeverFollowedInvariant("a", "x",
                SynopticTest.defRelation));
        trueInvs3.add(new NeverFollowedInvariant("a", "y",
                SynopticTest.defRelation));
        trueInvs3.add(new NeverFollowedInvariant("b", "x",
                SynopticTest.defRelation));
        trueInvs3.add(new NeverFollowedInvariant("b", "y",
                SynopticTest.defRelation));

        trueInvs3.add(new NeverFollowedInvariant("x", "a",
                SynopticTest.defRelation));
        trueInvs3.add(new NeverFollowedInvariant("x", "b",
                SynopticTest.defRelation));
        trueInvs3.add(new NeverFollowedInvariant("y", "a",
                SynopticTest.defRelation));
        trueInvs3.add(new NeverFollowedInvariant("y", "b",
                SynopticTest.defRelation));

        logger.fine("mined: " + minedInvs3);
        assertTrue(trueInvs3.sameInvariants(minedInvs3));
    }

    /**
     * Tests the correctness of the invariants mined from a partially ordered
     * log.
     * 
     * @throws Exception
     */
    @Test
    public void minePartiallyOrderedTraceTest() throws Exception {
        TraceParser parser = new TraceParser();
        parser.addRegex("^(?<VTIME>)(?<TYPE>)$");
        parser.addPartitionsSeparator("^--$");

        String traceStr = "1,1,1 a\n" + "2,2,2 b\n" + "1,2,3 c\n";
        List<LogEvent> parsedEvents = parser.parseTraceString(traceStr,
                testName.getMethodName(), -1);

        Graph<LogEvent> inputGraph = parser.generateDirectTemporalRelation(
                parsedEvents, true);

        PartitionGraph result = new PartitionGraph(inputGraph, true);
        TemporalInvariantSet minedInvs = result.getInvariants();
        logger.fine("mined: " + minedInvs.toString());

        TemporalInvariantSet trueInvs = new TemporalInvariantSet();

        // Add the "eventually x" invariants.
        trueInvs.add(new AlwaysFollowedInvariant(Main.initialNodeLabel, "a",
                SynopticTest.defRelation));
        trueInvs.add(new AlwaysFollowedInvariant(Main.initialNodeLabel, "b",
                SynopticTest.defRelation));
        trueInvs.add(new AlwaysFollowedInvariant(Main.initialNodeLabel, "c",
                SynopticTest.defRelation));

        trueInvs.add(new AlwaysFollowedInvariant("a", "b",
                SynopticTest.defRelation));
        trueInvs.add(new AlwaysFollowedInvariant("a", "c",
                SynopticTest.defRelation));

        trueInvs.add(new NeverFollowedInvariant("a", "a",
                SynopticTest.defRelation));
        trueInvs.add(new NeverFollowedInvariant("b", "b",
                SynopticTest.defRelation));
        trueInvs.add(new NeverFollowedInvariant("c", "c",
                SynopticTest.defRelation));
        trueInvs.add(new NeverFollowedInvariant("b", "c",
                SynopticTest.defRelation));
        trueInvs.add(new NeverFollowedInvariant("c", "b",
                SynopticTest.defRelation));
        trueInvs.add(new NeverFollowedInvariant("b", "a",
                SynopticTest.defRelation));
        trueInvs.add(new NeverFollowedInvariant("c", "a",
                SynopticTest.defRelation));

        trueInvs.add(new AlwaysPrecedesInvariant("a", "b",
                SynopticTest.defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("a", "c",
                SynopticTest.defRelation));

        assertTrue(trueInvs.sameInvariants(minedInvs));
    }

    /**
     * Mines invariants from a randomly generated log and then uses both model
     * checkers to check that every mined invariant actually holds.
     * 
     * <pre>
     * TODO: this checks only one side of the approximation. We need a test to
     * check the other side -- that the mined set is the complete set of satisfied
     * invariants. This could be done by doing a check on the complete set of
     * invariants expected in the log (i.e. 3 x number-of-events-types^2).
     * </pre>
     * 
     * @throws InternalSynopticException
     * @throws ParseException
     */
    @Test
    public void testApproximationExactnessTest() throws ParseException,
            InternalSynopticException {

        // Event types allowed in the log, with partition string at index 0.
        String[] eventTypes = new String[] { "--", "a", "b", "c", "d", "e" };
        String[] log = genRandomLog(eventTypes);

        Graph<LogEvent> inputGraph = SynopticTest.genInitialLinearGraph(log);
        PartitionGraph graph = new PartitionGraph(inputGraph, true);
        TemporalInvariantSet minedInvs = graph.getInvariants();

        // Test with FSM checker.
        Main.useFSMChecker = true;
        List<RelationPath<LogEvent>> cExamples = minedInvs
                .getAllCounterExamples(inputGraph);
        if (cExamples != null) {
            logger.fine("log: " + log);
            logger.fine("minedInvs: " + minedInvs);
            logger.fine("[FSM] cExamples: " + cExamples);
        }
        assertTrue(cExamples == null);

        // Test with LTL checker.
        Main.useFSMChecker = false;
        cExamples = minedInvs.getAllCounterExamples(inputGraph);
        if (cExamples != null) {
            logger.fine("log: " + log);
            logger.fine("minedInvs: " + minedInvs);
            logger.fine("[LTL] cExamples: " + cExamples);
        }
        assertTrue(cExamples == null);
    }
}
