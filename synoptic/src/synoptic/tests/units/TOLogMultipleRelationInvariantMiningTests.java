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
import synoptic.invariants.NeverFollowedInvariant;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.invariants.miners.ChainWalkingTOInvMiner;
import synoptic.invariants.miners.TOInvariantMiner;
import synoptic.main.ParseException;
import synoptic.main.TraceParser;
import synoptic.model.ChainsTraceGraph;
import synoptic.model.EventNode;
import synoptic.model.event.StringEventType;
import synoptic.tests.SynopticTest;

@RunWith(value = Parameterized.class)
public class TOLogMultipleRelationInvariantMiningTests extends SynopticTest {

    // Single trace, non closure, variable relation subgraphs
    public final String[] singleElement = { "1 r w" };
    public final String[] relationSubgraphIsolatedFromInitial = { "1 w",
            "2 r x" };
    public final String[] relationSubgraphIsolatedFromTerminal = { "1 r w",
            "2 x" };
    public final String[] relationSubgraphIsolatedFromSpecialNodes = { "1 w",
            "2 r x", "3 y" };
    public final String[] multipleIsolatedRelationSubgraphs = { "1 v", "2 r w",
            "3 x", "4 r y", "5 z" };
    public final String[] eventPairSubgraphIsolatedFromInitial = { "1 w",
            "2 x", "3 r y" };
    public final String[] multipleIsolatedEventPairSubgraphs = { "1 u", "2 v",
            "3 r w", "4 x", "5 y", "6 r z" };

    // Single trace, closure, variable relation subgraphs
    public final String[] singleClosureElement = { "1 r cl w" };
    public final String[] singleNonTimeClosure = { "1 v", "2 r cl w" };
    public final String[] doubleNonTimeClosure = { "1 v", "2 r cl w", "3 x",
            "4 r cl y" };
    public final String[] closureIntoAndOutOfRegular = { "1 r v", "2 w",
            "3 r cl x", "4 r y" };
    public final String[] disjointClosureAndRegularSubgraphs = { "1 v",
            "2 r cl w", "3 x", "4 r y" };
    public final String[] disjointClosureAndEventPairSubgraphs = { "1 v",
            "2 r cl w", "3 x", "4 y", "5 r z" };

    public final String r = "r";

    private TOInvariantMiner miner = null;
    private TraceParser parser = null;

    /**
     * Generates parameters for this unit test. The only parameter right now is
     * the miner instance to use for mining invariants.
     * 
     * @return The set of parameters to pass to the constructor the unit test.
     */
    @Parameters
    public static Collection<Object[]> data() {
        // Currently, only the chain walking miner supports multiple relations.
        Object[][] data = new Object[][] { { new ChainWalkingTOInvMiner() } };
        return Arrays.asList(data);
    }

    public TOLogMultipleRelationInvariantMiningTests(TOInvariantMiner minerToUse) {
        miner = minerToUse;
    }

    @Override
    public void setUp() throws ParseException {
        super.setUp();
        parser = new TraceParser();
        parser.addRegex("^(?<TIME>)(?<TYPE>)$");
        parser.addRegex("^(?<TIME>)(?<RELATION>)(?<TYPE>)$");
        parser.addRegex("^(?<TIME>)(?<RELATION*>)cl(?<TYPE>)$");
        parser.addPartitionsSeparator("^--$");
    }

    /**
     * Generates a TemporalInvariantSet based on a sequence of log events -- a
     * set of invariants that are mined from the log, and hold true for the
     * initial graph of the log.
     * 
     * @param traceStr
     *            log of events, in single traceStr format
     * @return an invariant set for the input log
     * @throws Exception
     */
    public TemporalInvariantSet genInvariants(String[] trace) throws Exception {

        if (trace.length == 0) {
            throw new IllegalStateException("Trace array is empty");
        }

        String traceStr = trace[0];
        for (int i = 1; i < trace.length; i++) {
            traceStr += "\n" + trace[i];
        }

        List<EventNode> events = parser.parseTraceString(traceStr, "test", -1);
        ChainsTraceGraph graph = parser.generateDirectTORelation(events);
        return miner.computeInvariants(graph, true);
    }

    @Test
    public void singleElement() throws Exception {
        // {"1 r w"};

        TemporalInvariantSet minedInvs = genInvariants(singleElement);
        TemporalInvariantSet trueInvs = new TemporalInvariantSet();

        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "w", defRelation));
        trueInvs.add(new NeverFollowedInvariant("w", "w", defRelation));

        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "w", r));
        trueInvs.add(new NeverFollowedInvariant("w", "w", r));

        assertTrue(trueInvs.sameInvariants(minedInvs));
    }

    @Test
    public void relationSubgraphIsolatedFromInitial() throws Exception {
        // {"1 w", "2 r x"}
        TemporalInvariantSet minedInvs = genInvariants(relationSubgraphIsolatedFromInitial);
        TemporalInvariantSet trueInvs = new TemporalInvariantSet();

        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "w", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "x", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant("w", "x", defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("w", "x", defRelation));
        trueInvs.add(new NeverFollowedInvariant("w", "w", defRelation));
        trueInvs.add(new NeverFollowedInvariant("x", "x", defRelation));
        trueInvs.add(new NeverFollowedInvariant("x", "w", defRelation));

        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "w", r));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "x", r));
        trueInvs.add(new AlwaysFollowedInvariant("w", "x", r));
        trueInvs.add(new AlwaysPrecedesInvariant("w", "x", r));
        trueInvs.add(new NeverFollowedInvariant("w", "w", r));
        trueInvs.add(new NeverFollowedInvariant("x", "x", r));
        trueInvs.add(new NeverFollowedInvariant("x", "w", r));

        assertTrue(trueInvs.sameInvariants(minedInvs));
    }

    @Test
    public void relationSubgraphIsolatedFromTerminal() throws Exception {
        // {"1 r w", "2 x"}
        TemporalInvariantSet minedInvs = genInvariants(relationSubgraphIsolatedFromTerminal);
        TemporalInvariantSet trueInvs = new TemporalInvariantSet();

        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "w", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "x", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant("w", "x", defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("w", "x", defRelation));
        trueInvs.add(new NeverFollowedInvariant("w", "w", defRelation));
        trueInvs.add(new NeverFollowedInvariant("x", "x", defRelation));
        trueInvs.add(new NeverFollowedInvariant("x", "w", defRelation));

        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "w", r));
        trueInvs.add(new NeverFollowedInvariant("w", "w", r));

        assertTrue(trueInvs.sameInvariants(minedInvs));
    }

    @Test
    public void relationSubgraphIsolatedFromSpecialNodes() throws Exception {
        // {"1 w", "2 r x", "3 y"}
        TemporalInvariantSet minedInvs = genInvariants(relationSubgraphIsolatedFromSpecialNodes);
        TemporalInvariantSet trueInvs = new TemporalInvariantSet();

        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "w", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "x", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "y", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant("w", "x", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant("w", "y", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant("x", "y", defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("w", "x", defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("w", "y", defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("x", "y", defRelation));
        trueInvs.add(new NeverFollowedInvariant("w", "w", defRelation));
        trueInvs.add(new NeverFollowedInvariant("x", "x", defRelation));
        trueInvs.add(new NeverFollowedInvariant("y", "y", defRelation));
        trueInvs.add(new NeverFollowedInvariant("x", "w", defRelation));
        trueInvs.add(new NeverFollowedInvariant("y", "w", defRelation));
        trueInvs.add(new NeverFollowedInvariant("y", "x", defRelation));

        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "w", r));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "x", r));
        trueInvs.add(new AlwaysFollowedInvariant("w", "x", r));
        trueInvs.add(new AlwaysPrecedesInvariant("w", "x", r));
        trueInvs.add(new NeverFollowedInvariant("w", "w", r));
        trueInvs.add(new NeverFollowedInvariant("x", "x", r));
        trueInvs.add(new NeverFollowedInvariant("x", "w", r));

        assertTrue(trueInvs.sameInvariants(minedInvs));
    }

    @Test
    public void multipleIsolatedRelationSubgraphs() throws Exception {
        // {"1 v", "2 r w", "3 x", "4 r y", "5 z"}
        TemporalInvariantSet minedInvs = genInvariants(multipleIsolatedRelationSubgraphs);
        TemporalInvariantSet trueInvs = new TemporalInvariantSet();

        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "v", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "w", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "x", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "y", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "z", defRelation));

        trueInvs.add(new AlwaysFollowedInvariant("v", "w", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant("v", "x", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant("v", "y", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant("v", "z", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant("w", "x", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant("w", "y", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant("w", "z", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant("x", "y", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant("x", "z", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant("y", "z", defRelation));

        trueInvs.add(new AlwaysPrecedesInvariant("v", "w", defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("v", "x", defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("v", "y", defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("v", "z", defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("w", "x", defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("w", "y", defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("w", "z", defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("x", "y", defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("x", "z", defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("y", "z", defRelation));

        trueInvs.add(new NeverFollowedInvariant("w", "w", defRelation));
        trueInvs.add(new NeverFollowedInvariant("v", "v", defRelation));
        trueInvs.add(new NeverFollowedInvariant("y", "y", defRelation));
        trueInvs.add(new NeverFollowedInvariant("z", "z", defRelation));
        trueInvs.add(new NeverFollowedInvariant("x", "x", defRelation));

        trueInvs.add(new NeverFollowedInvariant("w", "v", defRelation));
        trueInvs.add(new NeverFollowedInvariant("x", "v", defRelation));
        trueInvs.add(new NeverFollowedInvariant("y", "v", defRelation));
        trueInvs.add(new NeverFollowedInvariant("z", "v", defRelation));
        trueInvs.add(new NeverFollowedInvariant("x", "w", defRelation));
        trueInvs.add(new NeverFollowedInvariant("y", "w", defRelation));
        trueInvs.add(new NeverFollowedInvariant("z", "w", defRelation));
        trueInvs.add(new NeverFollowedInvariant("y", "x", defRelation));
        trueInvs.add(new NeverFollowedInvariant("z", "x", defRelation));
        trueInvs.add(new NeverFollowedInvariant("z", "y", defRelation));

        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "v", r));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "w", r));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "x", r));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "y", r));

        trueInvs.add(new AlwaysFollowedInvariant("v", "w", r));
        trueInvs.add(new AlwaysFollowedInvariant("v", "x", r));
        trueInvs.add(new AlwaysFollowedInvariant("v", "y", r));
        trueInvs.add(new AlwaysFollowedInvariant("w", "x", r));
        trueInvs.add(new AlwaysFollowedInvariant("w", "y", r));
        trueInvs.add(new AlwaysFollowedInvariant("x", "y", r));

        trueInvs.add(new AlwaysPrecedesInvariant("v", "w", r));
        trueInvs.add(new AlwaysPrecedesInvariant("v", "x", r));
        trueInvs.add(new AlwaysPrecedesInvariant("v", "y", r));
        trueInvs.add(new AlwaysPrecedesInvariant("w", "x", r));
        trueInvs.add(new AlwaysPrecedesInvariant("w", "y", r));
        trueInvs.add(new AlwaysPrecedesInvariant("x", "y", r));

        trueInvs.add(new NeverFollowedInvariant("w", "v", r));
        trueInvs.add(new NeverFollowedInvariant("x", "v", r));
        trueInvs.add(new NeverFollowedInvariant("y", "v", r));
        trueInvs.add(new NeverFollowedInvariant("x", "w", r));
        trueInvs.add(new NeverFollowedInvariant("y", "w", r));
        trueInvs.add(new NeverFollowedInvariant("y", "x", r));

        trueInvs.add(new NeverFollowedInvariant("v", "v", r));
        trueInvs.add(new NeverFollowedInvariant("w", "w", r));
        trueInvs.add(new NeverFollowedInvariant("x", "x", r));
        trueInvs.add(new NeverFollowedInvariant("y", "y", r));

        assertTrue(trueInvs.sameInvariants(minedInvs));
    }

    @Test
    public void eventPairSubgraphIsolatedFromInitial() throws Exception {
        // "1 w", "2 x", "3 r y"

        TemporalInvariantSet minedInvs = genInvariants(eventPairSubgraphIsolatedFromInitial);
        TemporalInvariantSet trueInvs = new TemporalInvariantSet();

        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "w", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "x", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "y", defRelation));

        trueInvs.add(new AlwaysFollowedInvariant("w", "x", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant("w", "y", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant("x", "y", defRelation));

        trueInvs.add(new AlwaysPrecedesInvariant("w", "x", defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("w", "y", defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("x", "y", defRelation));

        trueInvs.add(new NeverFollowedInvariant("w", "w", defRelation));
        trueInvs.add(new NeverFollowedInvariant("x", "x", defRelation));
        trueInvs.add(new NeverFollowedInvariant("y", "y", defRelation));

        trueInvs.add(new NeverFollowedInvariant("x", "w", defRelation));
        trueInvs.add(new NeverFollowedInvariant("y", "w", defRelation));
        trueInvs.add(new NeverFollowedInvariant("y", "x", defRelation));

        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "x", r));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "y", r));
        trueInvs.add(new AlwaysFollowedInvariant("x", "y", r));
        trueInvs.add(new AlwaysPrecedesInvariant("x", "y", r));
        trueInvs.add(new NeverFollowedInvariant("x", "x", r));
        trueInvs.add(new NeverFollowedInvariant("y", "y", r));
        trueInvs.add(new NeverFollowedInvariant("y", "x", r));

        assertTrue(trueInvs.sameInvariants(minedInvs));
    }

    @Test
    public void multipleIsolatedEventPairSubgraphs() throws Exception {
        // "1 u", "2 v", "3 r w", "4 x", "5 y", "6 r z"

        TemporalInvariantSet minedInvs = genInvariants(multipleIsolatedEventPairSubgraphs);
        TemporalInvariantSet trueInvs = new TemporalInvariantSet();

        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "u", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "v", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "w", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "x", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "y", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "z", defRelation));

        trueInvs.add(new AlwaysFollowedInvariant("u", "v", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant("u", "w", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant("u", "x", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant("u", "y", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant("u", "z", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant("v", "w", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant("v", "x", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant("v", "y", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant("v", "z", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant("w", "x", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant("w", "y", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant("w", "z", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant("x", "y", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant("x", "z", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant("y", "z", defRelation));

        trueInvs.add(new AlwaysPrecedesInvariant("u", "v", defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("u", "w", defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("u", "x", defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("u", "y", defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("u", "z", defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("v", "w", defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("v", "x", defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("v", "y", defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("v", "z", defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("w", "x", defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("w", "y", defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("w", "z", defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("x", "y", defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("x", "z", defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("y", "z", defRelation));

        trueInvs.add(new NeverFollowedInvariant("u", "u", defRelation));
        trueInvs.add(new NeverFollowedInvariant("v", "v", defRelation));
        trueInvs.add(new NeverFollowedInvariant("w", "w", defRelation));
        trueInvs.add(new NeverFollowedInvariant("x", "x", defRelation));
        trueInvs.add(new NeverFollowedInvariant("y", "y", defRelation));
        trueInvs.add(new NeverFollowedInvariant("z", "z", defRelation));

        trueInvs.add(new NeverFollowedInvariant("v", "u", defRelation));
        trueInvs.add(new NeverFollowedInvariant("w", "u", defRelation));
        trueInvs.add(new NeverFollowedInvariant("x", "u", defRelation));
        trueInvs.add(new NeverFollowedInvariant("y", "u", defRelation));
        trueInvs.add(new NeverFollowedInvariant("z", "u", defRelation));
        trueInvs.add(new NeverFollowedInvariant("w", "v", defRelation));
        trueInvs.add(new NeverFollowedInvariant("x", "v", defRelation));
        trueInvs.add(new NeverFollowedInvariant("y", "v", defRelation));
        trueInvs.add(new NeverFollowedInvariant("z", "v", defRelation));
        trueInvs.add(new NeverFollowedInvariant("x", "w", defRelation));
        trueInvs.add(new NeverFollowedInvariant("y", "w", defRelation));
        trueInvs.add(new NeverFollowedInvariant("z", "w", defRelation));
        trueInvs.add(new NeverFollowedInvariant("y", "x", defRelation));
        trueInvs.add(new NeverFollowedInvariant("z", "x", defRelation));
        trueInvs.add(new NeverFollowedInvariant("z", "y", defRelation));

        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "v", r));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "w", r));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "y", r));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "z", r));

        trueInvs.add(new AlwaysFollowedInvariant("v", "w", r));
        trueInvs.add(new AlwaysFollowedInvariant("v", "y", r));
        trueInvs.add(new AlwaysFollowedInvariant("v", "z", r));
        trueInvs.add(new AlwaysFollowedInvariant("w", "y", r));
        trueInvs.add(new AlwaysFollowedInvariant("w", "z", r));
        trueInvs.add(new AlwaysFollowedInvariant("y", "z", r));

        trueInvs.add(new AlwaysPrecedesInvariant("v", "w", r));
        trueInvs.add(new AlwaysPrecedesInvariant("v", "y", r));
        trueInvs.add(new AlwaysPrecedesInvariant("v", "z", r));
        trueInvs.add(new AlwaysPrecedesInvariant("w", "y", r));
        trueInvs.add(new AlwaysPrecedesInvariant("w", "z", r));
        trueInvs.add(new AlwaysPrecedesInvariant("y", "z", r));

        trueInvs.add(new NeverFollowedInvariant("v", "v", r));
        trueInvs.add(new NeverFollowedInvariant("w", "w", r));
        trueInvs.add(new NeverFollowedInvariant("y", "y", r));
        trueInvs.add(new NeverFollowedInvariant("z", "z", r));

        trueInvs.add(new NeverFollowedInvariant("w", "v", r));
        trueInvs.add(new NeverFollowedInvariant("y", "v", r));
        trueInvs.add(new NeverFollowedInvariant("z", "v", r));
        trueInvs.add(new NeverFollowedInvariant("y", "w", r));
        trueInvs.add(new NeverFollowedInvariant("z", "w", r));
        trueInvs.add(new NeverFollowedInvariant("z", "y", r));

        assertTrue(trueInvs.sameInvariants(minedInvs));
    }

    @Test
    public void singleClosureElement() throws Exception {
        // {"1 r cl w"}
        TemporalInvariantSet minedInvs = genInvariants(singleClosureElement);
        TemporalInvariantSet trueInvs = new TemporalInvariantSet();

        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "w", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "w", r));

        trueInvs.add(new NeverFollowedInvariant("w", "w", defRelation));
        trueInvs.add(new NeverFollowedInvariant("w", "w", r));

        assertTrue(trueInvs.sameInvariants(minedInvs));
    }

    @Test
    public void singleNonTimeClosure() throws Exception {
        // {"1 v", "2 r cl w"}
        TemporalInvariantSet minedInvs = genInvariants(singleNonTimeClosure);
        TemporalInvariantSet trueInvs = new TemporalInvariantSet();

        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "v", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "w", defRelation));

        trueInvs.add(new AlwaysFollowedInvariant("v", "w", defRelation));

        trueInvs.add(new AlwaysPrecedesInvariant("v", "w", defRelation));

        trueInvs.add(new NeverFollowedInvariant("w", "w", defRelation));
        trueInvs.add(new NeverFollowedInvariant("v", "v", defRelation));

        trueInvs.add(new NeverFollowedInvariant("w", "v", defRelation));

        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "w", r));

        trueInvs.add(new NeverFollowedInvariant("w", "w", r));

        assertTrue(trueInvs.sameInvariants(minedInvs));
    }

    @Test
    public void doubleNonTimeClosure() throws Exception {
        // {"1 v", "2 r cl w", "3 x", "4 r cl y"}
        TemporalInvariantSet minedInvs = genInvariants(doubleNonTimeClosure);
        TemporalInvariantSet trueInvs = new TemporalInvariantSet();

        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "v", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "w", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "x", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "y", defRelation));

        trueInvs.add(new AlwaysFollowedInvariant("v", "w", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant("v", "x", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant("v", "y", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant("w", "x", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant("w", "y", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant("x", "y", defRelation));

        trueInvs.add(new AlwaysPrecedesInvariant("v", "w", defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("v", "x", defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("v", "y", defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("w", "x", defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("w", "y", defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("x", "y", defRelation));

        trueInvs.add(new NeverFollowedInvariant("v", "v", defRelation));
        trueInvs.add(new NeverFollowedInvariant("w", "w", defRelation));
        trueInvs.add(new NeverFollowedInvariant("x", "x", defRelation));
        trueInvs.add(new NeverFollowedInvariant("y", "y", defRelation));

        trueInvs.add(new NeverFollowedInvariant("w", "v", defRelation));
        trueInvs.add(new NeverFollowedInvariant("x", "v", defRelation));
        trueInvs.add(new NeverFollowedInvariant("y", "v", defRelation));
        trueInvs.add(new NeverFollowedInvariant("x", "w", defRelation));
        trueInvs.add(new NeverFollowedInvariant("y", "w", defRelation));
        trueInvs.add(new NeverFollowedInvariant("y", "x", defRelation));

        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "w", r));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "y", r));

        trueInvs.add(new AlwaysFollowedInvariant("w", "y", r));
        trueInvs.add(new AlwaysPrecedesInvariant("w", "y", r));

        trueInvs.add(new NeverFollowedInvariant("w", "w", r));
        trueInvs.add(new NeverFollowedInvariant("y", "y", r));

        trueInvs.add(new NeverFollowedInvariant("y", "w", r));

        assertTrue(trueInvs.sameInvariants(minedInvs));
    }

    @Test
    public void closureIntoAndOutOfRegular() throws Exception {
        // {"1 r v", "2 w", "3 r cl x", "4 r y"}
        TemporalInvariantSet minedInvs = genInvariants(closureIntoAndOutOfRegular);
        TemporalInvariantSet trueInvs = new TemporalInvariantSet();

        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "v", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "w", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "x", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "y", defRelation));

        trueInvs.add(new AlwaysFollowedInvariant("v", "w", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant("v", "x", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant("v", "y", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant("w", "x", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant("w", "y", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant("x", "y", defRelation));

        trueInvs.add(new AlwaysPrecedesInvariant("v", "w", defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("v", "x", defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("v", "y", defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("w", "x", defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("w", "y", defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("x", "y", defRelation));

        trueInvs.add(new NeverFollowedInvariant("v", "v", defRelation));
        trueInvs.add(new NeverFollowedInvariant("w", "w", defRelation));
        trueInvs.add(new NeverFollowedInvariant("x", "x", defRelation));
        trueInvs.add(new NeverFollowedInvariant("y", "y", defRelation));

        trueInvs.add(new NeverFollowedInvariant("w", "v", defRelation));
        trueInvs.add(new NeverFollowedInvariant("x", "v", defRelation));
        trueInvs.add(new NeverFollowedInvariant("y", "v", defRelation));
        trueInvs.add(new NeverFollowedInvariant("x", "w", defRelation));
        trueInvs.add(new NeverFollowedInvariant("y", "w", defRelation));
        trueInvs.add(new NeverFollowedInvariant("y", "x", defRelation));

        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "v", r));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "x", r));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "y", r));

        trueInvs.add(new AlwaysFollowedInvariant("v", "x", r));
        trueInvs.add(new AlwaysFollowedInvariant("v", "y", r));
        trueInvs.add(new AlwaysFollowedInvariant("x", "y", r));

        trueInvs.add(new AlwaysPrecedesInvariant("v", "x", r));
        trueInvs.add(new AlwaysPrecedesInvariant("v", "y", r));
        trueInvs.add(new AlwaysPrecedesInvariant("x", "y", r));

        trueInvs.add(new NeverFollowedInvariant("v", "v", r));
        trueInvs.add(new NeverFollowedInvariant("x", "x", r));
        trueInvs.add(new NeverFollowedInvariant("y", "y", r));

        trueInvs.add(new NeverFollowedInvariant("x", "v", r));
        trueInvs.add(new NeverFollowedInvariant("y", "v", r));
        trueInvs.add(new NeverFollowedInvariant("y", "x", r));

        assertTrue(trueInvs.sameInvariants(minedInvs));
    }

    @Test
    public void disjointClosureAndRegularSubgraphs() throws Exception {
        // {"1 v", "2 r cl w", "3 x", "4 r y"}
        TemporalInvariantSet minedInvs = genInvariants(disjointClosureAndRegularSubgraphs);
        TemporalInvariantSet trueInvs = new TemporalInvariantSet();

        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "v", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "w", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "x", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "y", defRelation));

        trueInvs.add(new AlwaysFollowedInvariant("v", "w", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant("v", "x", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant("v", "y", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant("w", "x", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant("w", "y", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant("x", "y", defRelation));

        trueInvs.add(new AlwaysPrecedesInvariant("v", "w", defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("v", "x", defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("v", "y", defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("w", "x", defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("w", "y", defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("x", "y", defRelation));

        trueInvs.add(new NeverFollowedInvariant("v", "v", defRelation));
        trueInvs.add(new NeverFollowedInvariant("w", "w", defRelation));
        trueInvs.add(new NeverFollowedInvariant("x", "x", defRelation));
        trueInvs.add(new NeverFollowedInvariant("y", "y", defRelation));

        trueInvs.add(new NeverFollowedInvariant("w", "v", defRelation));
        trueInvs.add(new NeverFollowedInvariant("x", "v", defRelation));
        trueInvs.add(new NeverFollowedInvariant("y", "v", defRelation));
        trueInvs.add(new NeverFollowedInvariant("x", "w", defRelation));
        trueInvs.add(new NeverFollowedInvariant("y", "w", defRelation));
        trueInvs.add(new NeverFollowedInvariant("y", "x", defRelation));

        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "w", r));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "x", r));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "y", r));

        trueInvs.add(new AlwaysFollowedInvariant("w", "x", r));
        trueInvs.add(new AlwaysFollowedInvariant("w", "y", r));
        trueInvs.add(new AlwaysFollowedInvariant("x", "y", r));

        trueInvs.add(new AlwaysPrecedesInvariant("w", "x", r));
        trueInvs.add(new AlwaysPrecedesInvariant("w", "y", r));
        trueInvs.add(new AlwaysPrecedesInvariant("x", "y", r));

        trueInvs.add(new NeverFollowedInvariant("w", "w", r));
        trueInvs.add(new NeverFollowedInvariant("x", "x", r));
        trueInvs.add(new NeverFollowedInvariant("y", "y", r));

        trueInvs.add(new NeverFollowedInvariant("x", "w", r));
        trueInvs.add(new NeverFollowedInvariant("y", "w", r));
        trueInvs.add(new NeverFollowedInvariant("y", "x", r));

        assertTrue(trueInvs.sameInvariants(minedInvs));
    }

    @Test
    public void disjointClosureAndEventPairSubgraphs() throws Exception {
        // "1 v", "2 r cl w", "3 x", "4 y", "5 r z"
        TemporalInvariantSet minedInvs = genInvariants(disjointClosureAndEventPairSubgraphs);
        TemporalInvariantSet trueInvs = new TemporalInvariantSet();

        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "v", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "w", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "x", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "y", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "z", defRelation));

        trueInvs.add(new AlwaysFollowedInvariant("v", "w", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant("v", "x", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant("v", "y", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant("v", "z", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant("w", "x", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant("w", "y", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant("w", "z", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant("x", "y", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant("x", "z", defRelation));
        trueInvs.add(new AlwaysFollowedInvariant("y", "z", defRelation));

        trueInvs.add(new AlwaysPrecedesInvariant("v", "w", defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("v", "x", defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("v", "y", defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("v", "z", defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("w", "x", defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("w", "y", defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("w", "z", defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("x", "y", defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("x", "z", defRelation));
        trueInvs.add(new AlwaysPrecedesInvariant("y", "z", defRelation));

        trueInvs.add(new NeverFollowedInvariant("v", "v", defRelation));
        trueInvs.add(new NeverFollowedInvariant("w", "w", defRelation));
        trueInvs.add(new NeverFollowedInvariant("x", "x", defRelation));
        trueInvs.add(new NeverFollowedInvariant("y", "y", defRelation));
        trueInvs.add(new NeverFollowedInvariant("z", "z", defRelation));

        trueInvs.add(new NeverFollowedInvariant("w", "v", defRelation));
        trueInvs.add(new NeverFollowedInvariant("x", "v", defRelation));
        trueInvs.add(new NeverFollowedInvariant("y", "v", defRelation));
        trueInvs.add(new NeverFollowedInvariant("z", "v", defRelation));
        trueInvs.add(new NeverFollowedInvariant("x", "w", defRelation));
        trueInvs.add(new NeverFollowedInvariant("y", "w", defRelation));
        trueInvs.add(new NeverFollowedInvariant("z", "w", defRelation));
        trueInvs.add(new NeverFollowedInvariant("y", "x", defRelation));
        trueInvs.add(new NeverFollowedInvariant("z", "x", defRelation));
        trueInvs.add(new NeverFollowedInvariant("z", "y", defRelation));

        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "w", r));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "y", r));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "z", r));

        trueInvs.add(new AlwaysFollowedInvariant("w", "y", r));
        trueInvs.add(new AlwaysFollowedInvariant("w", "z", r));
        trueInvs.add(new AlwaysFollowedInvariant("y", "z", r));

        trueInvs.add(new AlwaysPrecedesInvariant("w", "y", r));
        trueInvs.add(new AlwaysPrecedesInvariant("w", "z", r));
        trueInvs.add(new AlwaysPrecedesInvariant("y", "z", r));

        trueInvs.add(new NeverFollowedInvariant("w", "w", r));
        trueInvs.add(new NeverFollowedInvariant("y", "y", r));
        trueInvs.add(new NeverFollowedInvariant("z", "z", r));

        trueInvs.add(new NeverFollowedInvariant("y", "w", r));
        trueInvs.add(new NeverFollowedInvariant("z", "w", r));
        trueInvs.add(new NeverFollowedInvariant("z", "y", r));

        assertTrue(trueInvs.sameInvariants(minedInvs));
    }

}
