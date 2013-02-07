package synoptic.tests.units;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import synoptic.invariants.AlwaysFollowedInvariant;
import synoptic.invariants.AlwaysPrecedesInvariant;
import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.NeverFollowedInvariant;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.invariants.concurrency.AlwaysConcurrentInvariant;
import synoptic.invariants.concurrency.NeverConcurrentInvariant;
import synoptic.invariants.miners.DAGWalkingPOInvMiner;
import synoptic.invariants.miners.IPOInvariantMiner;
import synoptic.invariants.miners.TransitiveClosureInvMiner;
import synoptic.main.parser.ParseException;
import synoptic.main.parser.TraceParser;
import synoptic.model.DAGsTraceGraph;
import synoptic.model.EventNode;
import synoptic.model.event.DistEventType;
import synoptic.model.event.Event;
import synoptic.model.event.StringEventType;
import synoptic.tests.SynopticTest;

/**
 * Tests for mining invariants from partially ordered (PO) logs using a couple
 * of different mining algorithms.
 * 
 * @author ivan
 */
@RunWith(value = Parameterized.class)
public class POLogInvariantMiningTests extends SynopticTest {

    // This trace is equivalent to the example traces under
    // synoptic/traces/abstract/ticket-reservation-example/
    private static String ticketReservationTrace = "1,0,0 client-0 search\n0,1,0 client-1 search\n1,0,1 server available\n1,1,2 server available\n2,0,1 client-0 buy\n2,1,3 server sold\n1,2,2 client-1 buy\n2,2,4 server sold-out\n--\n0,1,0 client-1 search\n1,0,0 client-0 search\n0,1,1 server available\n1,1,2 server available\n0,2,1 client-1 buy\n1,2,3 server sold\n2,1,2 client-0 buy\n2,2,4 server sold-out\n--\n1,0,0 client-0 search\n1,0,1 server available\n0,1,0 client-1 search\n1,1,2 server available\n1,2,2 client-1 buy\n1,2,3 server sold\n2,0,1 client-0 buy\n2,2,4 server sold-out\n--\n1,0,0 client-0 search\n1,0,1 server available\n0,1,0 client-1 search\n1,1,2 server available\n--\n1,0,0 client-0 search\n1,0,1 server available\n2,0,1 client-0 buy\n2,0,2 server sold\n0,1,0 client-1 search\n2,1,3 server sold-out";

    boolean mineNCWith = true;
    IPOInvariantMiner miner = null;

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
                { new DAGWalkingPOInvMiner(true) },
                { new DAGWalkingPOInvMiner(false) } };
        return Arrays.asList(data);
    }

    public POLogInvariantMiningTests(IPOInvariantMiner minerToUse) {
        miner = minerToUse;
        if (miner instanceof DAGWalkingPOInvMiner) {
            mineNCWith = ((DAGWalkingPOInvMiner) miner)
                    .getMineNeverConcurrentWith();
        }
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
        DAGsTraceGraph inputGraph = genDAGsTraceGraph(events, parser);
        TemporalInvariantSet minedInvs = miner.computeInvariants(inputGraph);

        logger.fine("mined: " + minedInvs.toString());

        TemporalInvariantSet trueInvs = new TemporalInvariantSet();

        DistEventType a = new DistEventType("a", "0");
        DistEventType b = new DistEventType("b", "1");
        String R = Event.defTimeRelationStr;

        // Add the "eventually x" invariants.
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), a, R));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), b, R));

        trueInvs.add(new NeverFollowedInvariant(a, a, R));
        trueInvs.add(new NeverFollowedInvariant(b, b, R));
        trueInvs.add(new AlwaysConcurrentInvariant(a, b, R));

        // NOTE: "a NFby b" and "b NFby b" are subsumed by "a ACwith b"
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
        DAGsTraceGraph inputGraph = genDAGsTraceGraph(events, parser);
        TemporalInvariantSet minedInvs = miner.computeInvariants(inputGraph);

        logger.fine("mined: " + minedInvs.toString());

        TemporalInvariantSet trueInvs = new TemporalInvariantSet();

        DistEventType a = new DistEventType("a", "0");
        DistEventType b = new DistEventType("b", "1");
        DistEventType c = new DistEventType("c", "0");
        String R = Event.defTimeRelationStr;

        // Add the "eventually x" invariants.
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), a, R));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), b, R));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), c, R));

        trueInvs.add(new AlwaysFollowedInvariant(a, c, R));
        trueInvs.add(new AlwaysFollowedInvariant(b, c, R));

        trueInvs.add(new NeverFollowedInvariant(a, a, R));
        trueInvs.add(new NeverFollowedInvariant(b, b, R));
        trueInvs.add(new NeverFollowedInvariant(c, c, R));
        trueInvs.add(new NeverFollowedInvariant(c, b, R));
        trueInvs.add(new NeverFollowedInvariant(c, a, R));

        trueInvs.add(new AlwaysPrecedesInvariant(b, c, R));
        trueInvs.add(new AlwaysPrecedesInvariant(a, c, R));

        trueInvs.add(new AlwaysConcurrentInvariant(a, b, R));

        // NOTE: a NFby b and b NFby b are redundant because a ACwith b
        // and c NCwith b is redundant because b AP c

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
        DAGsTraceGraph inputGraph = genDAGsTraceGraph(events, parser);
        TemporalInvariantSet minedInvs = miner.computeInvariants(inputGraph);

        logger.fine("mined: " + minedInvs.toString());

        TemporalInvariantSet trueInvs = new TemporalInvariantSet();

        DistEventType a = new DistEventType("a", "0");
        DistEventType b = new DistEventType("b", "0");
        DistEventType c = new DistEventType("c", "1");
        String R = Event.defTimeRelationStr;

        // Add the "eventually x" invariants.
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), a, R));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), b, R));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), c, R));

        trueInvs.add(new AlwaysFollowedInvariant(a, c, R));
        trueInvs.add(new AlwaysFollowedInvariant(a, b, R));

        trueInvs.add(new NeverFollowedInvariant(a, a, R));
        trueInvs.add(new NeverFollowedInvariant(b, b, R));
        trueInvs.add(new NeverFollowedInvariant(c, c, R));
        trueInvs.add(new NeverFollowedInvariant(c, a, R));
        trueInvs.add(new NeverFollowedInvariant(b, a, R));

        trueInvs.add(new AlwaysPrecedesInvariant(a, c, R));
        trueInvs.add(new AlwaysPrecedesInvariant(a, b, R));

        trueInvs.add(new AlwaysConcurrentInvariant(b, c, R));

        // NOTE: b NFby c and c NFby b are redundant because b ACwith c
        // and a NCwith c is redundant because a AP c

        assertTrue(trueInvs.sameInvariants(minedInvs));
    }

    /**
     * Tests a trace with no invariants.
     * 
     * @throws Exception
     */
    @Test
    public void mineNoInvariantsTest() throws Exception {
        TraceParser parser = newTraceParser();

        String[] events = new String[] { "1,0 0 a", "--", "0,1 1 b", "--",
                "1,0 0 a", "0,1 1 b", "1,2 1 b", "2,2 0 a", "1,3 1 b" };
        DAGsTraceGraph inputGraph = genDAGsTraceGraph(events, parser);
        TemporalInvariantSet minedInvs = miner.computeInvariants(inputGraph);

        logger.fine("mined: " + minedInvs.toString());

        TemporalInvariantSet trueInvs = new TemporalInvariantSet();

        // NOTE: a NCwith b is false because the a is concurrent
        // with the first b.

        assertTrue(trueInvs.sameInvariants(minedInvs));
    }

    /**
     * Tests a trace with no concurrency invariants.
     * 
     * @throws Exception
     */
    @Test
    public void mineNoConcurrentInvsTest() throws Exception {
        TraceParser parser = newTraceParser();
        // Two b's before the two a's
        // One b concurrent with the two a's
        // Two b's after the two a's
        String[] events = new String[] { "0,1 1 b", "0,2 1 b", "1,2 0 a",
                "2,2 0 a", "0,3 1 b", "2,4 1 b", "2,5 1 b" };
        DAGsTraceGraph inputGraph = genDAGsTraceGraph(events, parser);
        TemporalInvariantSet minedInvs = miner.computeInvariants(inputGraph);

        logger.fine("mined: " + minedInvs.toString());

        TemporalInvariantSet trueInvs = new TemporalInvariantSet();

        DistEventType a = new DistEventType("a", "0");
        DistEventType b = new DistEventType("b", "1");
        String R = Event.defTimeRelationStr;

        // Add the "eventually x" invariants.
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), a, R));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), b, R));

        trueInvs.add(new AlwaysPrecedesInvariant(b, a, R));
        trueInvs.add(new AlwaysFollowedInvariant(a, b, R));

        assertTrue(trueInvs.sameInvariants(minedInvs));
    }

    /**
     * Tests a trace with a single AlwaysConcurrent invariant.
     * 
     * @throws Exception
     */
    @Test
    public void mineAlwaysConcurrentTest() throws Exception {
        TraceParser parser = newTraceParser();

        String[] events = new String[] { "1,0 0 a", "2,0 0 a", "--", "0,1 1 b",
                "0,2 1 b", "--", "1,0 0 a", "0,1 1 b" };

        DAGsTraceGraph inputGraph = genDAGsTraceGraph(events, parser);
        TemporalInvariantSet minedInvs = miner.computeInvariants(inputGraph);

        logger.fine("mined: " + minedInvs.toString());

        TemporalInvariantSet trueInvs = new TemporalInvariantSet();

        DistEventType a = new DistEventType("a", "0");
        DistEventType b = new DistEventType("b", "1");
        String R = Event.defTimeRelationStr;

        trueInvs.add(new AlwaysConcurrentInvariant(b, a, R));

        assertTrue(trueInvs.sameInvariants(minedInvs));
    }

    /**
     * Tests a trace that satisfies the "a never followed by b" invariant, but
     * which is subsumed by "b always concurrent with a" invariant.
     * 
     * @throws Exception
     */
    @Test
    public void mineNFbySubsumedByAlwaysConcurrentTest() throws Exception {
        TraceParser parser = newTraceParser();

        String[] events = new String[] { "1,0 0 x", "2,0 0 a", "1,1 1 b",
                "3,1 0 y" };

        DAGsTraceGraph inputGraph = genDAGsTraceGraph(events, parser);
        TemporalInvariantSet minedInvs = miner.computeInvariants(inputGraph);

        logger.fine("mined: " + minedInvs.toString());

        DistEventType a = new DistEventType("a", "0");
        DistEventType b = new DistEventType("b", "1");
        String R = Event.defTimeRelationStr;

        assertTrue(minedInvs.getSet().contains(
                new AlwaysConcurrentInvariant(b, a, R)));
        assertTrue(!minedInvs.getSet().contains(
                new NeverFollowedInvariant(a, b, R)));
        assertTrue(!minedInvs.getSet().contains(
                new NeverFollowedInvariant(b, a, R)));
    }

    /**
     * Tests a trace with a single NeverConcurrent invariant.
     * 
     * @throws Exception
     */
    @Test
    public void mineNeverConcurrentTest() throws Exception {
        TraceParser parser = newTraceParser();

        String[] events = new String[] { "1,0 0 a", "2,0 0 a", "--", "0,1 1 b",
                "0,2 1 b", "--", "1,0 0 a", "1,1 1 b", "--", "0,1 1 b",
                "1,1 0 a", "--" };

        DAGsTraceGraph inputGraph = genDAGsTraceGraph(events, parser);
        TemporalInvariantSet minedInvs = miner.computeInvariants(inputGraph);

        logger.fine("mined: " + minedInvs.toString());

        TemporalInvariantSet trueInvs = new TemporalInvariantSet();

        DistEventType a = new DistEventType("a", "0");
        DistEventType b = new DistEventType("b", "1");
        String R = Event.defTimeRelationStr;

        if (mineNCWith) {
            trueInvs.add(new NeverConcurrentInvariant(b, a, R));
        }

        assertTrue(trueInvs.sameInvariants(minedInvs));
    }

    /**
     * Tests a trace that satisfies the "a never concurrent b" invariant, but
     * which is subsumed by "b always followed by a" invariant.
     * 
     * @throws Exception
     */
    @Test
    public void mineNeverConcurrentSubsumedByAFbyTest() throws Exception {
        TraceParser parser = newTraceParser();

        String[] events = new String[] { "1 1 b", "2 0 a", "--", "1 0 x",
                "2 0 a", "--" };

        DAGsTraceGraph inputGraph = genDAGsTraceGraph(events, parser);
        TemporalInvariantSet minedInvs = miner.computeInvariants(inputGraph);

        logger.fine("mined: " + minedInvs.toString());

        DistEventType a = new DistEventType("a", "0");
        DistEventType b = new DistEventType("b", "1");
        String R = Event.defTimeRelationStr;

        assertTrue(minedInvs.getSet().contains(
                new AlwaysFollowedInvariant(b, a, R)));
        assertTrue(!minedInvs.getSet().contains(
                new NeverConcurrentInvariant(b, a, R)));
    }

    /**
     * Tests a trace that satisfies the "a never concurrent b" invariant, but
     * which is subsumed by "b always precedes a" invariant.
     * 
     * @throws Exception
     */
    @Test
    public void mineNeverConcurrentSubsumedByAPTest() throws Exception {
        TraceParser parser = newTraceParser();

        String[] events = new String[] { "1 1 b", "2 0 a", "--", "1 1 b",
                "2 0 x", "--" };

        DAGsTraceGraph inputGraph = genDAGsTraceGraph(events, parser);
        TemporalInvariantSet minedInvs = miner.computeInvariants(inputGraph);

        logger.fine("mined: " + minedInvs.toString());

        DistEventType a = new DistEventType("a", "0");
        DistEventType b = new DistEventType("b", "1");
        String R = Event.defTimeRelationStr;

        assertTrue(minedInvs.getSet().contains(
                new AlwaysPrecedesInvariant(b, a, R)));
        assertTrue(!minedInvs.getSet().contains(
                new NeverConcurrentInvariant(b, a, R)));
    }

    /**
     * A more complex test to mine the NeverConcurrent invariant from a log with
     * a true partial order instead of a linear traces.
     * 
     * @throws Exception
     */
    @Test
    public void mineComplexNeverConcurrentTest() throws Exception {
        TraceParser parser = newTraceParser();

        String[] events = new String[] { "1,0 0 a", "2,0 0 a", "--", "0,1 1 c",
                "0,2 1 c", "--", "1,0 0 a", "2,0 0 b", "1,1 1 b", "2,2 1 c",
                "--", "0,1 1 c", "1,1 0 a" };

        DAGsTraceGraph inputGraph = genDAGsTraceGraph(events, parser);
        TemporalInvariantSet minedInvs = miner.computeInvariants(inputGraph);

        logger.fine("mined: " + minedInvs.toString());

        DistEventType a = new DistEventType("a", "0");
        DistEventType c = new DistEventType("c", "1");
        String R = Event.defTimeRelationStr;

        if (mineNCWith) {
            assertTrue(minedInvs.getSet().contains(
                    new NeverConcurrentInvariant(c, a, R)));
        }
    }

    /**
     * Tests the ticket-reservation partially ordered trace used in SLAML'11
     * submission. Checks that all the example invariants reported in the paper
     * are indeed mined from the log.
     * 
     * <pre>
     * NOTE: This test depends on the trace file located here:
     *       traces/abstract/ticket-reservation-example/trace.txt
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void mineTicketReservationExampleTest() throws Exception {
        TraceParser parser = newTraceParser();

        ArrayList<EventNode> parsedEvents = parser.parseTraceString(
                ticketReservationTrace, "ticket-reservation-example", -1);
        DAGsTraceGraph inputGraph = parser
                .generateDirectPORelation(parsedEvents);
        TemporalInvariantSet minedInvs = miner.computeInvariants(inputGraph);
        logger.info("Mined invariants from TicketReservationExample: "
                + minedInvs.toString());

        TemporalInvariantSet trueInvs = new TemporalInvariantSet();

        DistEventType available = new DistEventType("available", "server");
        DistEventType sold = new DistEventType("sold", "server");
        DistEventType sold_out = new DistEventType("sold-out", "server");
        DistEventType search_c0 = new DistEventType("search", "client-0");
        DistEventType search_c1 = new DistEventType("search", "client-1");
        DistEventType buy_c0 = new DistEventType("buy", "client-0");
        DistEventType buy_c1 = new DistEventType("buy", "client-1");

        String R = Event.defTimeRelationStr;

        trueInvs.add(new NeverFollowedInvariant(sold_out, sold, R));
        trueInvs.add(new AlwaysPrecedesInvariant(sold, sold_out, R));
        trueInvs.add(new AlwaysConcurrentInvariant(buy_c0, buy_c1, R));
        trueInvs.add(new AlwaysConcurrentInvariant(search_c0, search_c1, R));
        trueInvs.add(new AlwaysPrecedesInvariant(available, buy_c0, R));
        trueInvs.add(new AlwaysPrecedesInvariant(available, buy_c1, R));
        trueInvs.add(new NeverFollowedInvariant(sold_out, buy_c0, R));
        trueInvs.add(new NeverFollowedInvariant(sold_out, buy_c1, R));
        trueInvs.add(new AlwaysFollowedInvariant(buy_c0, sold_out, R));
        trueInvs.add(new AlwaysFollowedInvariant(buy_c1, sold_out, R));
        trueInvs.add(new AlwaysPrecedesInvariant(buy_c0, sold_out, R));

        Set<ITemporalInvariant> minedInvsSet = minedInvs.getSet();
        for (ITemporalInvariant trueInv : trueInvs.getSet()) {
            assertTrue(minedInvsSet.contains(trueInv));
        }
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
