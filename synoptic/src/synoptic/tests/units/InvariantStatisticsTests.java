package synoptic.tests.units;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import synoptic.invariants.AlwaysFollowedInvariant;
import synoptic.invariants.AlwaysPrecedesInvariant;
import synoptic.invariants.CExamplePath;
import synoptic.invariants.InterruptedByInvariant;
import synoptic.invariants.NeverFollowedInvariant;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.invariants.miners.ChainWalkingTOInvMiner;
import synoptic.invariants.miners.ITOInvariantMiner;
import synoptic.main.AbstractMain;
import synoptic.main.parser.ParseException;
import synoptic.model.ChainsTraceGraph;
import synoptic.model.EventNode;
import synoptic.model.event.Event;
import synoptic.model.event.StringEventType;
import synoptic.tests.SynopticTest;
import synoptic.util.InternalSynopticException;
import synoptic.util.InvariantStatistics;

/**
 * Tests for mining invariants from totally ordered (TO) logs using
 * ChainWalkingTOInvMiner and outputSupportCount option. Based on the
 * TOLogInvariantTests
 */
@RunWith(value = Parameterized.class)
public class InvariantStatisticsTests extends SynopticTest {
    ITOInvariantMiner miner = null;

    /**
     * Generates parameters for this unit test. The only parameter right now is
     * the miner instance to use for mining invariants.
     * 
     * @return The set of parameters to pass to the constructor the unit test.
     */
    @Parameters
    public static Collection<Object[]> data() {
        Object[][] data = new Object[][] {
        // Currently, only the chain walking miner supports InvariantStatistics.
        { new ChainWalkingTOInvMiner() } };
        return Arrays.asList(data);
    }

    public InvariantStatisticsTests(ITOInvariantMiner minerToUse) {
        miner = minerToUse;
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
    public TemporalInvariantSet genInvariants(String[] events,
            boolean multipleRelations, boolean outputSupportCount)
            throws Exception {
        ChainsTraceGraph inputGraph = genInitialLinearGraph(events);
        return miner.computeInvariants(inputGraph, multipleRelations,
                outputSupportCount);
    }

    /**
     * Checks the mined invariants from a log with just two events types.
     * 
     * @throws Exception
     */
    // CAL - note in invariant equals that statistic is not a part of this
    // first create invariant then set statistic
    @Test
    public void mineBasicTest() throws Exception {
        String[] log = new String[] { "a", "b", "--" };
        TemporalInvariantSet minedInvs = genInvariants(log, false, true);
        TemporalInvariantSet trueInvs = new TemporalInvariantSet();

        AlwaysFollowedInvariant invariant1 = new AlwaysFollowedInvariant(
                StringEventType.newInitialStringEventType(), "b",
                Event.defTimeRelationStr);
        invariant1.setStatistics(new InvariantStatistics(1));
        trueInvs.add(invariant1);

        AlwaysFollowedInvariant invariant2 = new AlwaysFollowedInvariant(
                StringEventType.newInitialStringEventType(), "a",
                Event.defTimeRelationStr);
        invariant2.setStatistics(new InvariantStatistics(1));
        trueInvs.add(invariant2);

        AlwaysFollowedInvariant invariant3 = new AlwaysFollowedInvariant("a",
                "b", Event.defTimeRelationStr);
        invariant3.setStatistics(new InvariantStatistics(1));
        trueInvs.add(invariant3);

        AlwaysPrecedesInvariant invariant4 = new AlwaysPrecedesInvariant("a",
                "b", Event.defTimeRelationStr);
        invariant4.setStatistics(new InvariantStatistics(1));
        trueInvs.add(invariant4);

        NeverFollowedInvariant invariant5 = new NeverFollowedInvariant("b",
                "a", Event.defTimeRelationStr);
        invariant5.setStatistics(new InvariantStatistics(1));
        trueInvs.add(invariant5);

        NeverFollowedInvariant invariant6 = new NeverFollowedInvariant("b",
                "b", Event.defTimeRelationStr);
        invariant6.setStatistics(new InvariantStatistics(1));
        trueInvs.add(invariant6);

        NeverFollowedInvariant invariant7 = new NeverFollowedInvariant("a",
                "a", Event.defTimeRelationStr);
        invariant7.setStatistics(new InvariantStatistics(1));
        trueInvs.add(invariant7);

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
        TemporalInvariantSet minedInvs = genInvariants(log, false, true);
        TemporalInvariantSet trueInvs = new TemporalInvariantSet();

        AlwaysFollowedInvariant invariant1 = new AlwaysFollowedInvariant(
                StringEventType.newInitialStringEventType(), "b",
                Event.defTimeRelationStr);
        invariant1.setStatistics(new InvariantStatistics(3));
        trueInvs.add(invariant1);

        AlwaysFollowedInvariant invariant2 = new AlwaysFollowedInvariant(
                StringEventType.newInitialStringEventType(), "a",
                Event.defTimeRelationStr);
        invariant2.setStatistics(new InvariantStatistics(3));
        trueInvs.add(invariant2);

        AlwaysFollowedInvariant invariant3 = new AlwaysFollowedInvariant("a",
                "b", Event.defTimeRelationStr);
        invariant3.setStatistics(new InvariantStatistics(3));
        trueInvs.add(invariant3);

        InterruptedByInvariant invariant4 = new InterruptedByInvariant("b",
                "a", Event.defTimeRelationStr);
        invariant4.setStatistics(new InvariantStatistics(3));
        trueInvs.add(invariant4);

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
        TemporalInvariantSet minedInvs = genInvariants(log, false, true);
        TemporalInvariantSet trueInvs = new TemporalInvariantSet();

        NeverFollowedInvariant invariant = new NeverFollowedInvariant("a", "b",
                Event.defTimeRelationStr);
        invariant.setStatistics(new InvariantStatistics(4));
        trueInvs.add(invariant);
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
        TemporalInvariantSet minedInvs = genInvariants(log, false, true);
        TemporalInvariantSet trueInvs = new TemporalInvariantSet();

        AlwaysFollowedInvariant invariant1 = new AlwaysFollowedInvariant(
                StringEventType.newInitialStringEventType(), "a",
                Event.defTimeRelationStr);
        invariant1.setStatistics(new InvariantStatistics(5));
        trueInvs.add(invariant1);

        AlwaysPrecedesInvariant invariant2 = new AlwaysPrecedesInvariant("a",
                "b", Event.defTimeRelationStr);
        invariant2.setStatistics(new InvariantStatistics(3));
        trueInvs.add(invariant2);

        InterruptedByInvariant invariant3 = new InterruptedByInvariant("b",
                "a", Event.defTimeRelationStr);
        invariant3.setStatistics(new InvariantStatistics(3));
        trueInvs.add(invariant3);

        logger.info("minedInvs: " + minedInvs.toString());
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
     * @throws ParseExceptionent
     */
    @Test
    public void testApproximationExactnessTest() throws ParseException,
            InternalSynopticException {

        // Event types allowed in the log, with partition string at index 0.
        String[] eventTypes = new String[] { "--", "a", "b", "c", "d", "e" };
        String[] log = TOLogInvariantMiningTests.genRandomLog(eventTypes);

        ChainsTraceGraph inputGraph = genInitialLinearGraph(log);
        TemporalInvariantSet minedInvs = miner.computeInvariants(inputGraph,
                false, false);

        // Test with FSM checker.
        AbstractMain main = AbstractMain.getInstance();
        List<CExamplePath<EventNode>> cExamples = minedInvs
                .getAllCounterExamples(inputGraph);
        if (cExamples != null) {
            logger.fine("log: " + Arrays.toString(log));
            logger.fine("minedInvs: " + minedInvs);
            logger.fine("[FSM] cExamples: " + cExamples);
        }
        assertTrue(cExamples == null);
    }
}
