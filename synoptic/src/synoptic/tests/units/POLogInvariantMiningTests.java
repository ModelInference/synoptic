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
import synoptic.invariants.InvariantMiner;
import synoptic.invariants.NeverFollowedInvariant;
import synoptic.invariants.TCInvariantMiner;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.main.Main;
import synoptic.main.TraceParser;
import synoptic.model.PartitionGraph;
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
        Object[][] data = new Object[][] { { new TCInvariantMiner(false) },
                { new TCInvariantMiner(true) } };
        return Arrays.asList(data);
    }

    public POLogInvariantMiningTests(InvariantMiner minerToUse) {
        miner = minerToUse;
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

        String[] events = new String[] { "1,1,1 a", "2,2,2 b", "1,2,3 c" };
        PartitionGraph result = genInitialPartitionGraph(events, parser, miner);

        // PartitionGraph result = new PartitionGraph(inputGraph, true);
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

}
