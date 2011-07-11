package synoptic.tests.units;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import synoptic.invariants.AlwaysConcurrentInvariant;
import synoptic.invariants.AlwaysFollowedInvariant;
import synoptic.invariants.AlwaysPrecedesInvariant;
import synoptic.invariants.NeverConcurrentInvariant;
import synoptic.invariants.NeverFollowedInvariant;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.invariants.miners.DAGWalkingPOInvMiner;
import synoptic.invariants.miners.InvariantMiner;
import synoptic.invariants.miners.TransitiveClosureInvMiner;
import synoptic.main.ParseException;
import synoptic.main.TraceParser;
import synoptic.model.DistEventType;
import synoptic.model.EventNode;
import synoptic.model.Graph;
import synoptic.model.StringEventType;
import synoptic.tests.SynopticTest;

/**
 * Tests for mining invariants from partially ordered (PO) logs using a couple
 * of different mining algorithms.
 * 
 * @author ivan
 */
@RunWith(value = Parameterized.class)
public class POLogInvariantMiningTests extends SynopticTest {

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
                { new DAGWalkingPOInvMiner() } };
        return Arrays.asList(data);
    }

    public POLogInvariantMiningTests(InvariantMiner minerToUse) {
        miner = minerToUse;
    }

    public TraceParser newTraceParser() throws ParseException {
        TraceParser parser = new TraceParser();
        parser.addRegex("^(?<VTIME>)(?<PID>)(?<TYPE>)$");
        parser.addPartitionsSeparator("^--$");
        return parser;
    }

    /**
     * Tests a trace with just two events at different processes.
     * 
     * @throws Exception
     */
    @Test
    public void mineTwoConcurrentEventsTest() throws Exception {
        TraceParser parser = newTraceParser();

        String[] events = new String[] { "1,0 0 a", "0,1 1 b" };
        Graph<EventNode> inputGraph = genInitialGraph(events, parser);
        TemporalInvariantSet minedInvs = miner.computeInvariants(inputGraph);

        logger.fine("mined: " + minedInvs.toString());

        TemporalInvariantSet trueInvs = new TemporalInvariantSet();

        DistEventType a = new DistEventType("a", "0");
        DistEventType b = new DistEventType("b", "1");
        String R = SynopticTest.defRelation;

        // Add the "eventually x" invariants.
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .NewInitialStringEventType(), a, R));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .NewInitialStringEventType(), b, R));

        trueInvs.add(new NeverFollowedInvariant(a, b, R));
        trueInvs.add(new NeverFollowedInvariant(a, a, R));
        trueInvs.add(new NeverFollowedInvariant(b, b, R));
        trueInvs.add(new NeverFollowedInvariant(b, a, R));
        trueInvs.add(new AlwaysConcurrentInvariant(a, b, R));

        assertTrue(trueInvs.sameInvariants(minedInvs));
    }

    /**
     * Tests a trace with one join.
     * 
     * @throws Exception
     */
    @Test
    public void mineJoinTest() throws Exception {
        TraceParser parser = newTraceParser();

        String[] events = new String[] { "1,0 0 a", "0,1 1 b", "2,1 0 c" };
        Graph<EventNode> inputGraph = genInitialGraph(events, parser);
        TemporalInvariantSet minedInvs = miner.computeInvariants(inputGraph);

        logger.fine("mined: " + minedInvs.toString());

        TemporalInvariantSet trueInvs = new TemporalInvariantSet();

        DistEventType a = new DistEventType("a", "0");
        DistEventType b = new DistEventType("b", "1");
        DistEventType c = new DistEventType("c", "0");
        String R = SynopticTest.defRelation;

        // Add the "eventually x" invariants.
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .NewInitialStringEventType(), a, R));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .NewInitialStringEventType(), b, R));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .NewInitialStringEventType(), c, R));

        trueInvs.add(new AlwaysFollowedInvariant(a, c, R));
        trueInvs.add(new AlwaysFollowedInvariant(b, c, R));

        trueInvs.add(new NeverFollowedInvariant(a, a, R));
        trueInvs.add(new NeverFollowedInvariant(b, b, R));
        trueInvs.add(new NeverFollowedInvariant(a, b, R));
        trueInvs.add(new NeverFollowedInvariant(c, c, R));
        trueInvs.add(new NeverFollowedInvariant(c, b, R));
        trueInvs.add(new NeverFollowedInvariant(b, a, R));
        trueInvs.add(new NeverFollowedInvariant(c, a, R));

        trueInvs.add(new AlwaysPrecedesInvariant(b, c, R));
        trueInvs.add(new AlwaysPrecedesInvariant(a, c, R));

        trueInvs.add(new AlwaysConcurrentInvariant(a, b, R));
        trueInvs.add(new NeverConcurrentInvariant(c, b, R));

        assertTrue(trueInvs.sameInvariants(minedInvs));
    }

    /**
     * Tests a trace with one branch.
     * 
     * @throws Exception
     */
    @Test
    public void mineBranchTest() throws Exception {
        TraceParser parser = newTraceParser();

        String[] events = new String[] { "1,0 0 a", "2,0 0 b", "1,1 1 c" };
        Graph<EventNode> inputGraph = genInitialGraph(events, parser);
        TemporalInvariantSet minedInvs = miner.computeInvariants(inputGraph);

        logger.fine("mined: " + minedInvs.toString());

        TemporalInvariantSet trueInvs = new TemporalInvariantSet();

        DistEventType a = new DistEventType("a", "0");
        DistEventType b = new DistEventType("b", "0");
        DistEventType c = new DistEventType("c", "1");
        String R = SynopticTest.defRelation;

        // Add the "eventually x" invariants.
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .NewInitialStringEventType(), a, R));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .NewInitialStringEventType(), b, R));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .NewInitialStringEventType(), c, R));

        trueInvs.add(new AlwaysFollowedInvariant(a, c, R));
        trueInvs.add(new AlwaysFollowedInvariant(a, b, R));

        trueInvs.add(new NeverFollowedInvariant(a, a, R));
        trueInvs.add(new NeverFollowedInvariant(b, b, R));
        trueInvs.add(new NeverFollowedInvariant(c, c, R));
        trueInvs.add(new NeverFollowedInvariant(c, b, R));
        trueInvs.add(new NeverFollowedInvariant(b, c, R));
        trueInvs.add(new NeverFollowedInvariant(c, a, R));
        trueInvs.add(new NeverFollowedInvariant(b, a, R));

        trueInvs.add(new AlwaysPrecedesInvariant(a, c, R));
        trueInvs.add(new AlwaysPrecedesInvariant(a, b, R));

        // NOTE: there are no concurrency invariants in this trace because all
        // the concurrency invariants are subsumed... ??

        trueInvs.add(new AlwaysConcurrentInvariant(b, c, R));
        trueInvs.add(new NeverConcurrentInvariant(a, c, R));

        assertTrue(trueInvs.sameInvariants(minedInvs));
    }

    /**
     * Tests a trace with a NeverConcurrentInvariant
     * 
     * @throws Exception
     */
    @Test
    public void mineNeverConcurrentTest() throws Exception {
        TraceParser parser = newTraceParser();

        String[] events = new String[] { "1,0 0 a", "0,1 1 b", "1,2 1 b",
                "2,2 0 a", "1,3 1 b" };
        Graph<EventNode> inputGraph = genInitialGraph(events, parser);
        TemporalInvariantSet minedInvs = miner.computeInvariants(inputGraph);

        logger.fine("mined: " + minedInvs.toString());

        TemporalInvariantSet trueInvs = new TemporalInvariantSet();

        DistEventType a = new DistEventType("a", "0");
        DistEventType b = new DistEventType("b", "1");
        String R = SynopticTest.defRelation;

        // Add the "eventually x" invariants.
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .NewInitialStringEventType(), a, R));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .NewInitialStringEventType(), b, R));

        // NOTE: a AFby a and b AFby b are both false because we're dealing with
        // finite traces.
        trueInvs.add(new NeverConcurrentInvariant(a, b, R));

        assertTrue(trueInvs.sameInvariants(minedInvs));
    }

    /**
     * Tests a trace with no NeverConcurrentInvariant
     * 
     * @throws Exception
     */
    @Test
    public void mineNoNeverConcurrentTest() throws Exception {
        TraceParser parser = newTraceParser();
        // Two b's before the two a's
        // One b concurrent with the two a's
        // Two b's after the two a's
        String[] events = new String[] { "0,1 1 b", "0,2 1 b", "1,2 0 a",
                "2,2 0 a", "0,3 1 b", "2,4 1 b", "2,5 1 b" };
        Graph<EventNode> inputGraph = genInitialGraph(events, parser);
        TemporalInvariantSet minedInvs = miner.computeInvariants(inputGraph);

        logger.fine("mined: " + minedInvs.toString());

        TemporalInvariantSet trueInvs = new TemporalInvariantSet();

        DistEventType a = new DistEventType("a", "0");
        DistEventType b = new DistEventType("b", "1");
        String R = SynopticTest.defRelation;

        // Add the "eventually x" invariants.
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .NewInitialStringEventType(), a, R));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .NewInitialStringEventType(), b, R));

        trueInvs.add(new AlwaysPrecedesInvariant(b, a, R));
        trueInvs.add(new AlwaysFollowedInvariant(a, b, R));

        assertTrue(trueInvs.sameInvariants(minedInvs));
    }

    /**
     * Tests a randomly generated trace.
     * 
     * @throws Exception
     */
    @Test
    public void mineRandomLogTest() throws Exception {
        // TODO: generate a random trace over a finite alphabet, of random
        // length. Run all the invariant miners on the trace, and verify that
        // they are unanimous.
    }

}
