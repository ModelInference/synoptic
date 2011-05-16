package synoptic.tests.units;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import synoptic.invariants.AlwaysFollowedInvariant;
import synoptic.invariants.AlwaysPrecedesInvariant;
import synoptic.invariants.NeverFollowedInvariant;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.invariants.miners.DAGWalkingPOInvMiner;
import synoptic.invariants.miners.InvariantMiner;
import synoptic.invariants.miners.TransitiveClosureTOInvMiner;
import synoptic.main.TraceParser;
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
                { new TransitiveClosureTOInvMiner(false) },
                { new TransitiveClosureTOInvMiner(true) },
                { new DAGWalkingPOInvMiner() } };
        return Arrays.asList(data);
    }

    public POLogInvariantMiningTests(InvariantMiner minerToUse) {
        miner = minerToUse;
    }

    /**
     * Tests a trace with one branch.
     * 
     * @throws Exception
     */
    @Test
    public void mineBranchTest() throws Exception {
        TraceParser parser = new TraceParser();
        parser.addRegex("^(?<VTIME>)(?<TYPE>)$");
        parser.addPartitionsSeparator("^--$");

        String[] events = new String[] { "1,1,1 a", "2,2,2 b", "1,2,3 c" };
        Graph<EventNode> inputGraph = genInitialGraph(events, parser);
        TemporalInvariantSet minedInvs = miner.computeInvariants(inputGraph);

        logger.fine("mined: " + minedInvs.toString());

        TemporalInvariantSet trueInvs = new TemporalInvariantSet();

        // Add the "eventually x" invariants.
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .NewInitialStringEventType(), "a", SynopticTest.defRelation));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .NewInitialStringEventType(), "b", SynopticTest.defRelation));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .NewInitialStringEventType(), "c", SynopticTest.defRelation));

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
     * Tests a trace with a join.
     * 
     * @throws Exception
     */
    @Test
    public void mineJoinTest() throws Exception {
        TraceParser parser = new TraceParser();
        parser.addRegex("^(?<VTIME>)(?<TYPE>)$");
        parser.addPartitionsSeparator("^--$");

        String[] events = new String[] { "1,2 a", "2,1 b", "2,2 c" };
        Graph<EventNode> inputGraph = genInitialGraph(events, parser);
        TemporalInvariantSet minedInvs = miner.computeInvariants(inputGraph);

        logger.fine("mined: " + minedInvs.toString());

        TemporalInvariantSet trueInvs = new TemporalInvariantSet();

        // Add the "eventually x" invariants.
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .NewInitialStringEventType(), "a", SynopticTest.defRelation));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .NewInitialStringEventType(), "b", SynopticTest.defRelation));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .NewInitialStringEventType(), "c", SynopticTest.defRelation));

        trueInvs.add(new AlwaysFollowedInvariant("a", "c",
                SynopticTest.defRelation));
        trueInvs.add(new AlwaysFollowedInvariant("b", "c",
                SynopticTest.defRelation));

        trueInvs.add(new NeverFollowedInvariant("a", "a",
                SynopticTest.defRelation));
        trueInvs.add(new NeverFollowedInvariant("b", "b",
                SynopticTest.defRelation));
        trueInvs.add(new NeverFollowedInvariant("c", "c",
                SynopticTest.defRelation));
        trueInvs.add(new NeverFollowedInvariant("c", "b",
                SynopticTest.defRelation));
        trueInvs.add(new NeverFollowedInvariant("c", "a",
                SynopticTest.defRelation));
        trueInvs.add(new NeverFollowedInvariant("b", "a",
                SynopticTest.defRelation));
        trueInvs.add(new NeverFollowedInvariant("a", "b",
                SynopticTest.defRelation));

        trueInvs.add(new AlwaysPrecedesInvariant("a", "c",
                SynopticTest.defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("b", "c",
                SynopticTest.defRelation));

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
