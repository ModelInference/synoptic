package synoptic.tests.units;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import synoptic.invariants.AlwaysFollowedInvariant;
import synoptic.invariants.AlwaysPrecedesInvariant;
import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.NeverFollowedInvariant;
import synoptic.invariants.CExamplePath;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.invariants.miners.ChainWalkingTOInvMiner;
import synoptic.invariants.miners.DAGWalkingPOInvMiner;
import synoptic.invariants.miners.InvariantMiner;
import synoptic.invariants.miners.TransitiveClosureInvMiner;
import synoptic.main.Main;
import synoptic.main.ParseException;
import synoptic.model.EventNode;
import synoptic.model.TraceGraph;
import synoptic.model.StringEventType;
import synoptic.tests.SynopticTest;
import synoptic.util.InternalSynopticException;

/**
 * Tests for mining invariants from totally ordered (TO) logs using three
 * different mining algorithms.
 * 
 * @author ivan
 */
@RunWith(value = Parameterized.class)
public class TOLogInvariantMiningTests extends SynopticTest {

    InvariantMiner miner = null;

    /**
     * Generates parameters for this unit test. The only parameter right now is
     * the miner instance to use for mining invariants.
     * 
     * @return The set of parameters to pass to the constructor the unit test.
     */
    @Parameters
    public static Collection<Object[]> data() {
        Object[][] data = new Object[][] {
                { new TransitiveClosureInvMiner(false) },
                { new TransitiveClosureInvMiner(true) },
                { new ChainWalkingTOInvMiner() },
                { new DAGWalkingPOInvMiner() } };
        return Arrays.asList(data);
    }

    public TOLogInvariantMiningTests(InvariantMiner minerToUse) {
        miner = minerToUse;
    }

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
     * @param events
     *            log of events, each one in the format: (?<TYPE>)
     * @return an invariant set for the input log
     * @throws Exception
     */
    public TemporalInvariantSet genInvariants(String[] events) throws Exception {
        TraceGraph inputGraph = genInitialLinearGraph(events);
        return miner.computeInvariants(inputGraph);
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
            invs.add(new AlwaysFollowedInvariant(e, StringEventType
                    .NewTerminalStringEventType(), SynopticTest.defRelation));

            // INITIAL AFby x
            invs.add(new AlwaysFollowedInvariant(StringEventType
                    .NewInitialStringEventType(), e, SynopticTest.defRelation));

            // x NFby INITIAL
            invs.add(new NeverFollowedInvariant(e, StringEventType
                    .NewInitialStringEventType(), SynopticTest.defRelation));
            // TERMINAL NFby x
            invs.add(new NeverFollowedInvariant(StringEventType
                    .NewTerminalStringEventType(), e, SynopticTest.defRelation));
            // INITIAL AP x
            invs.add(new AlwaysPrecedesInvariant(StringEventType
                    .NewInitialStringEventType(), e, SynopticTest.defRelation));

            // x AP TERMINAL
            invs.add(new AlwaysPrecedesInvariant(e, StringEventType
                    .NewTerminalStringEventType(), SynopticTest.defRelation));
        }
        // INITIAL AFby TERMINAL
        invs.add(new AlwaysFollowedInvariant(StringEventType
                .NewInitialStringEventType(), StringEventType
                .NewTerminalStringEventType(), SynopticTest.defRelation));
        // TERMINAL NFby INITIAL
        invs.add(new NeverFollowedInvariant(StringEventType
                .NewTerminalStringEventType(), StringEventType
                .NewInitialStringEventType(), SynopticTest.defRelation));
        // TERMINAL NFby TERMINAL
        invs.add(new NeverFollowedInvariant(StringEventType
                .NewTerminalStringEventType(), StringEventType
                .NewTerminalStringEventType(), SynopticTest.defRelation));
        // INITIAL NFby INITIAL
        invs.add(new NeverFollowedInvariant(StringEventType
                .NewInitialStringEventType(), StringEventType
                .NewInitialStringEventType(), SynopticTest.defRelation));
        // INITIAL AP TERMINAL
        invs.add(new AlwaysPrecedesInvariant(StringEventType
                .NewInitialStringEventType(), StringEventType
                .NewTerminalStringEventType(), SynopticTest.defRelation));
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
        if (!(miner instanceof TransitiveClosureInvMiner)) {
            // Structure-walking invariant miners do not explicitly mine
            // tautological invariants in the first place.
            return;
        }

        String[] eventTypes = new String[] { "--", "a", "b", "c", "d", "e" };
        String[] log = genRandomLog(eventTypes);

        ArrayList<String> allEvents = new ArrayList<String>();
        for (String e : log) {
            if (!e.equals("--")) {
                allEvents.add(e);
            }
        }

        // Generate set including tautological invariants.
        TraceGraph inputGraph = genInitialLinearGraph(log);

        // Generates an invariants set which includes all Tautological
        // invariants. So these are the "raw" invariants which are mined.
        TemporalInvariantSet s1 = ((TransitiveClosureInvMiner) miner)
                .computeInvariants(inputGraph, false, false);

        // Generate set without tautological invariants.
        TemporalInvariantSet s2 = genInvariants(log);

        logger.fine("set including tautological: " + s1);
        logger.fine("set without tautological: " + s2);

        s2.addAll(genTautologicalInvariants(allEvents));
        logger.fine("set without tautological + true tautological: " + s2);

        assertTrue(s1.sameInvariants(s2));
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

        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .NewInitialStringEventType(), "b", SynopticTest.defRelation));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .NewInitialStringEventType(), "a", SynopticTest.defRelation));
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

        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .NewInitialStringEventType(), "b", SynopticTest.defRelation));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .NewInitialStringEventType(), "a", SynopticTest.defRelation));
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
    public void mineAPTest() throws Exception {
        String[] log = new String[] { "a", "a", "b", "--", "a", "--", "a", "b",
                "a", "b", "--" };
        TemporalInvariantSet minedInvs = genInvariants(log);
        TemporalInvariantSet trueInvs = new TemporalInvariantSet();

        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .NewInitialStringEventType(), "a", SynopticTest.defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("a", "b",
                SynopticTest.defRelation));
        logger.info("minedInvs: " + minedInvs.toString());
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
                    && ((AlwaysFollowedInvariant) inv).getFirst()
                            .isInitialEventType()) {
                continue;
            }
            trueInvs3.add(inv);
        }
        for (ITemporalInvariant inv : minedInvs2) {
            if (inv instanceof AlwaysFollowedInvariant
                    && ((AlwaysFollowedInvariant) inv).getFirst()
                            .isInitialEventType()) {
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
     * @throws ParseExceptionent
     */
    @Test
    public void testApproximationExactnessTest() throws ParseException,
            InternalSynopticException {

        // Event types allowed in the log, with partition string at index 0.
        String[] eventTypes = new String[] { "--", "a", "b", "c", "d", "e" };
        String[] log = genRandomLog(eventTypes);

        TraceGraph inputGraph = genInitialLinearGraph(log);
        TemporalInvariantSet minedInvs = miner.computeInvariants(inputGraph);

        // Test with FSM checker.
        Main.useFSMChecker = true;
        List<CExamplePath<EventNode>> cExamples = minedInvs
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
