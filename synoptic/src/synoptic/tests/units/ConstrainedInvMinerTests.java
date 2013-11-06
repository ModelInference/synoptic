package synoptic.tests.units;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import synoptic.invariants.AlwaysFollowedInvariant;
import synoptic.invariants.AlwaysPrecedesInvariant;
import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.NeverFollowedInvariant;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.invariants.constraints.TempConstrainedInvariant;
import synoptic.invariants.miners.ChainWalkingTOInvMiner;
import synoptic.invariants.miners.ConstrainedInvMiner;
import synoptic.invariants.miners.ITOInvariantMiner;
import synoptic.main.parser.ParseException;
import synoptic.main.parser.TraceParser;
import synoptic.model.ChainsTraceGraph;
import synoptic.model.event.Event;
import synoptic.model.event.StringEventType;
import synoptic.tests.PynopticTest;
import synoptic.util.time.DTotalTime;
import synoptic.util.time.ITime;
import synoptic.util.time.ITotalTime;

/**
 * Tests for mining constrained invariants.
 */
public class ConstrainedInvMinerTests extends PynopticTest {
    ITOInvariantMiner miner;

    @Override
    @Before
    public void setUp() throws ParseException {
        super.setUp();
        miner = new ChainWalkingTOInvMiner();
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
            boolean multipleRelations) throws Exception {
        ChainsTraceGraph inputGraph = genInitialLinearGraph(events);
        TemporalInvariantSet invs = miner.computeInvariants(inputGraph, false);

        ConstrainedInvMiner constrMiner = new ConstrainedInvMiner();
        return constrMiner.computeInvariants(inputGraph, multipleRelations,
                invs);
    }

    /**
     * Generates a TemporalInvariantSet based on a sequence of log events -- a
     * set of invariants that are mined from the log, and hold true for the
     * initial graph of the log.
     * 
     * @param events
     *            log of events
     * @param parser
     *            a parser that contains regex for log of events
     * @return an invariant set for the input log
     * @throws Exception
     */
    public TemporalInvariantSet genTimeInvariants(String[] events,
            boolean multipleRelations, TraceParser parser) throws Exception {
        ChainsTraceGraph inputGraph = (ChainsTraceGraph) genChainsTraceGraph(
                events, parser);
        TemporalInvariantSet invs = miner.computeInvariants(inputGraph, false);

        ConstrainedInvMiner constrMiner = new ConstrainedInvMiner();
        return constrMiner.computeInvariants(inputGraph, multipleRelations,
                invs);
    }

    /**
     * Compose a log in which "a AP b" is the only true invariant. But, instead
     * of comparing the mined constrained invariants against the true
     * constrained AP invariant, we compare against the standard AP invariant,
     * using assertFalse. This checks that equality of constrained invariants
     * works correctly.
     * 
     * @throws Exception
     */
    @Test
    public void mineAPTest() throws Exception {
        String[] log = new String[] { "a", "a", "b", "--", "a", "--", "a", "b",
                "a", "b", "--" };
        TemporalInvariantSet minedInvs = genInvariants(log, false);
        TemporalInvariantSet trueInvs = new TemporalInvariantSet();

        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "a", Event.defTimeRelationStr));
        trueInvs.add(new AlwaysPrecedesInvariant("a", "b",
                Event.defTimeRelationStr));
        logger.info("minedInvs: " + minedInvs.toString());
        assertFalse(trueInvs.sameInvariants(minedInvs));
    }

    /**
     * Tests correct number of constrained invariants mined.
     * 
     * @throws Exception
     */
    @Test
    public void mineConstraintSize() throws Exception {
        String[] log = new String[] { "a 1", "b 4" };
        TemporalInvariantSet minedInvs = genTimeInvariants(log, false,
                genITimeParser());
        logger.info("minedInvs: " + minedInvs.toString());

        // Generates a pair of constraints for both AFby and AP invariants, as
        // well as three NFbys
        assertEquals(7, minedInvs.getSet().size());
        // Make sure there are 4 AF/AP invariants
        int count = 0;
        for (ITemporalInvariant invar : minedInvs) {
            if (invar instanceof NeverFollowedInvariant)
                continue;
            count++;
        }
        assertEquals(4, count);
    }

    /**
     * Simple test that checks if NFby invariants are mined.
     * 
     * @throws Exception
     */
    @Test
    public void mineNFbyConstrained() throws Exception {
        logger.info("NFby test");
        String[] log = new String[] { "a 1", "b 4" };
        TemporalInvariantSet minedInvs = genTimeInvariants(log, false,
                genITimeParser());
        logger.info("minedInvs: " + minedInvs.toString());
        NeverFollowedInvariant inv1 = new NeverFollowedInvariant("b", "a", "t");
        NeverFollowedInvariant inv2 = new NeverFollowedInvariant("b", "b", "t");
        NeverFollowedInvariant inv3 = new NeverFollowedInvariant("a", "a", "t");

        // make sure the NFbys are included
        assertTrue(minedInvs.getSet().contains(inv1));
        assertTrue(minedInvs.getSet().contains(inv2));
        assertTrue(minedInvs.getSet().contains(inv3));
    }

    /**
     * Tests that upper and lower bound constraints are as expected for a simple
     * invariant.
     * 
     * @throws Exception
     */
    @Test
    public void mineSingleConstrainedInv() throws Exception {
        String[] log = new String[] { "a 1", "b 4" };
        TemporalInvariantSet minedInvs = genTimeInvariants(log, false,
                genITimeParser());
        logger.info("minedInvs: " + minedInvs.toString());

        // a AFby b (lower bound)
        TempConstrainedInvariant<?> lowerInv = getConstrainedInv(minedInvs,
                "a AFby b lower");
        // a AFby b (upper bound)
        TempConstrainedInvariant<?> upperInv = getConstrainedInv(minedInvs,
                "a AFby b upper");

        ITime actualTime = new ITotalTime(3);

        assertEquals(actualTime, lowerInv.getConstraint().getThreshold());
        assertEquals(actualTime, upperInv.getConstraint().getThreshold());
    }

    /**
     * Tests that computed lower and upper bounds are correct for a log with
     * multiple time deltas for a AFby b and a AP c.
     * 
     * @throws Exception
     */
    @Test
    public void testCorrectLowerUpperBounds() throws Exception {
        String[] log = new String[] { "a 1.0", "c 2.0", "b 10.0", "--",
                "a 11.0", "b 13.5", "--", "a 20.0", "b 25.0", "c 26.0" };
        TemporalInvariantSet minedInvs = genTimeInvariants(log, false,
                genDTimeParser());

        // a AFby b (lower bound)
        TempConstrainedInvariant<?> lowerInvAFby = getConstrainedInv(minedInvs,
                "a AFby b lower");
        // a AFby b (upper bound)
        TempConstrainedInvariant<?> upperInvAFby = getConstrainedInv(minedInvs,
                "a AFby b upper");

        // a AP c (lower bound)
        TempConstrainedInvariant<?> lowerInvAP = getConstrainedInv(minedInvs,
                "a AP c lower");
        // a AP c (upper bound)
        TempConstrainedInvariant<?> upperInvAP = getConstrainedInv(minedInvs,
                "a AP c upper");

        ITime actualLowerBoundAFby = new DTotalTime(2.5);
        ITime actualUpperBoundAFby = new DTotalTime(9.0);

        ITime actualLowerBoundAP = new DTotalTime(1.0);
        ITime actualUpperBoundAP = new DTotalTime(6.0);

        assertEquals(actualLowerBoundAFby, lowerInvAFby.getConstraint()
                .getThreshold());
        assertEquals(actualUpperBoundAFby, upperInvAFby.getConstraint()
                .getThreshold());

        assertEquals(actualLowerBoundAP, lowerInvAP.getConstraint()
                .getThreshold());
        assertEquals(actualUpperBoundAP, upperInvAP.getConstraint()
                .getThreshold());
    }

    /**
     * Mine two a AFby b invariants with very close time deltas values. Test
     * that these constrained invariants are not equal.
     * 
     * @throws Exception
     */
    @Test
    public void testCloseConstraintEquality() throws Exception {
        String[] log1 = new String[] { "a 1.0", "b 3.9" };
        String[] log2 = new String[] { "a 1.0", "b 4.0" };

        TemporalInvariantSet minedInvs1 = genTimeInvariants(log1, false,
                genDTimeParser());
        TemporalInvariantSet minedInvs2 = genTimeInvariants(log2, false,
                genDTimeParser());

        // a AFby b w/ lowerbound = 2.9
        TempConstrainedInvariant<?> inv1 = getConstrainedInv(minedInvs1,
                "a AFby b lower");
        // a AFby b w/ lowerbound = 3.0
        TempConstrainedInvariant<?> inv2 = getConstrainedInv(minedInvs2,
                "a AFby b lower");

        assertFalse(inv1.equals(inv2));
    }

    /**
     * Compose a log where a -> b -> c. Test for: a AFby b (bound) + b AFby c
     * (bound) = a AFby c (bound)
     * 
     * @throws Exception
     */
    @Test
    public void testInvariantsEquals() throws Exception {
        String[] log = new String[] { "a 1.0", "b 2.5", "c 5.0" };

        TemporalInvariantSet minedInvs = genTimeInvariants(log, false,
                genDTimeParser());

        // All of these constraints are lower bounds.
        TempConstrainedInvariant<?> aAFbyb = getConstrainedInv(minedInvs,
                "a AFby b lower");
        TempConstrainedInvariant<?> aAFbyc = getConstrainedInv(minedInvs,
                "a AFby c lower");
        TempConstrainedInvariant<?> bAFbyc = getConstrainedInv(minedInvs,
                "b AFby c lower");

        ITime aAFbyb_time = aAFbyb.getConstraint().getThreshold();
        ITime aAFbyc_time = aAFbyc.getConstraint().getThreshold();
        ITime bAFbyc_time = bAFbyc.getConstraint().getThreshold();

        assertEquals(aAFbyc_time, aAFbyb_time.incrBy(bAFbyc_time));
    }
}
