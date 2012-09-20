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
import synoptic.invariants.miners.ITOInvariantMiner;
import synoptic.main.parser.ParseException;
import synoptic.main.parser.TraceParser;
import synoptic.model.ChainsTraceGraph;
import synoptic.model.EventNode;
import synoptic.model.event.Event;
import synoptic.model.event.StringEventType;
import synoptic.tests.SynopticTest;

@RunWith(value = Parameterized.class)
public class SingleRelationSubgraphsInvariantMiningTests extends SynopticTest {

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

    private ITOInvariantMiner miner = null;
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

    public SingleRelationSubgraphsInvariantMiningTests(
            ITOInvariantMiner minerToUse) {
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
    public TemporalInvariantSet genSingleRelationInvariants(String[] trace) throws Exception {

        if (trace.length == 0) {
            throw new IllegalStateException("Trace array is empty");
        }

        String traceStr = trace[0];
        for (int i = 1; i < trace.length; i++) {
            traceStr += "\n" + trace[i];
        }

        List<EventNode> events = parser.parseTraceString(traceStr, "test", -1);
        ChainsTraceGraph graph = parser.generateDirectTORelation(events);
        return miner.computeInvariants(graph, false);
    }

    @Test
    public void singleElement() throws Exception {
        // {"1 r w"};

        TemporalInvariantSet minedInvs = genSingleRelationInvariants(singleElement);
        TemporalInvariantSet trueInvs = new TemporalInvariantSet();

        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "w",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("w", "w",
                Event.defTimeRelationStr));

        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "w", r));
        trueInvs.add(new NeverFollowedInvariant("w", "w", r));

        assertTrue(trueInvs.sameInvariants(minedInvs));
    }

    @Test
    public void relationSubgraphIsolatedFromInitial() throws Exception {
        // {"1 w", "2 r x"}
        TemporalInvariantSet minedInvs = genSingleRelationInvariants(relationSubgraphIsolatedFromInitial);
        TemporalInvariantSet trueInvs = new TemporalInvariantSet();

        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "w",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "x",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant("w", "x",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysPrecedesInvariant("w", "x",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("w", "w",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("x", "x",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("x", "w",
                Event.defTimeRelationStr));

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
        TemporalInvariantSet minedInvs = genSingleRelationInvariants(relationSubgraphIsolatedFromTerminal);
        TemporalInvariantSet trueInvs = new TemporalInvariantSet();

        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "w",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "x",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant("w", "x",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysPrecedesInvariant("w", "x",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("w", "w",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("x", "x",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("x", "w",
                Event.defTimeRelationStr));

        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "w", r));
        trueInvs.add(new NeverFollowedInvariant("w", "w", r));

        assertTrue(trueInvs.sameInvariants(minedInvs));
    }

    @Test
    public void relationSubgraphIsolatedFromSpecialNodes() throws Exception {
        // {"1 w", "2 r x", "3 y"}
        TemporalInvariantSet minedInvs = genSingleRelationInvariants(relationSubgraphIsolatedFromSpecialNodes);
        TemporalInvariantSet trueInvs = new TemporalInvariantSet();

        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "w",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "x",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "y",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant("w", "x",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant("w", "y",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant("x", "y",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysPrecedesInvariant("w", "x",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysPrecedesInvariant("w", "y",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysPrecedesInvariant("x", "y",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("w", "w",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("x", "x",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("y", "y",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("x", "w",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("y", "w",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("y", "x",
                Event.defTimeRelationStr));

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
        TemporalInvariantSet minedInvs = genSingleRelationInvariants(multipleIsolatedRelationSubgraphs);
        TemporalInvariantSet trueInvs = new TemporalInvariantSet();

        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "v",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "w",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "x",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "y",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "z",
                Event.defTimeRelationStr));

        trueInvs.add(new AlwaysFollowedInvariant("v", "w",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant("v", "x",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant("v", "y",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant("v", "z",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant("w", "x",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant("w", "y",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant("w", "z",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant("x", "y",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant("x", "z",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant("y", "z",
                Event.defTimeRelationStr));

        trueInvs.add(new AlwaysPrecedesInvariant("v", "w",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysPrecedesInvariant("v", "x",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysPrecedesInvariant("v", "y",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysPrecedesInvariant("v", "z",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysPrecedesInvariant("w", "x",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysPrecedesInvariant("w", "y",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysPrecedesInvariant("w", "z",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysPrecedesInvariant("x", "y",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysPrecedesInvariant("x", "z",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysPrecedesInvariant("y", "z",
                Event.defTimeRelationStr));

        trueInvs.add(new NeverFollowedInvariant("w", "w",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("v", "v",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("y", "y",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("z", "z",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("x", "x",
                Event.defTimeRelationStr));

        trueInvs.add(new NeverFollowedInvariant("w", "v",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("x", "v",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("y", "v",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("z", "v",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("x", "w",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("y", "w",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("z", "w",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("y", "x",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("z", "x",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("z", "y",
                Event.defTimeRelationStr));

        trueInvs.add(new AlwaysFollowedInvariant("v", "w", r));
        trueInvs.add(new AlwaysFollowedInvariant("x", "y", r));

        trueInvs.add(new AlwaysPrecedesInvariant("v", "w", r));
        trueInvs.add(new AlwaysPrecedesInvariant("x", "y", r));

        trueInvs.add(new NeverFollowedInvariant("v", "x", r));
        trueInvs.add(new NeverFollowedInvariant("v", "y", r));
        trueInvs.add(new NeverFollowedInvariant("w", "v", r));
        trueInvs.add(new NeverFollowedInvariant("w", "x", r));
        trueInvs.add(new NeverFollowedInvariant("w", "y", r));
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

        TemporalInvariantSet minedInvs = genSingleRelationInvariants(eventPairSubgraphIsolatedFromInitial);
        TemporalInvariantSet trueInvs = new TemporalInvariantSet();

        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "w",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "x",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "y",
                Event.defTimeRelationStr));

        trueInvs.add(new AlwaysFollowedInvariant("w", "x",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant("w", "y",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant("x", "y",
                Event.defTimeRelationStr));

        trueInvs.add(new AlwaysPrecedesInvariant("w", "x",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysPrecedesInvariant("w", "y",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysPrecedesInvariant("x", "y",
                Event.defTimeRelationStr));

        trueInvs.add(new NeverFollowedInvariant("w", "w",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("x", "x",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("y", "y",
                Event.defTimeRelationStr));

        trueInvs.add(new NeverFollowedInvariant("x", "w",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("y", "w",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("y", "x",
                Event.defTimeRelationStr));

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

        TemporalInvariantSet minedInvs = genSingleRelationInvariants(multipleIsolatedEventPairSubgraphs);
        TemporalInvariantSet trueInvs = new TemporalInvariantSet();

        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "u",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "v",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "w",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "x",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "y",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "z",
                Event.defTimeRelationStr));

        trueInvs.add(new AlwaysFollowedInvariant("u", "v",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant("u", "w",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant("u", "x",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant("u", "y",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant("u", "z",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant("v", "w",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant("v", "x",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant("v", "y",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant("v", "z",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant("w", "x",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant("w", "y",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant("w", "z",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant("x", "y",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant("x", "z",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant("y", "z",
                Event.defTimeRelationStr));

        trueInvs.add(new AlwaysPrecedesInvariant("u", "v",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysPrecedesInvariant("u", "w",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysPrecedesInvariant("u", "x",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysPrecedesInvariant("u", "y",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysPrecedesInvariant("u", "z",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysPrecedesInvariant("v", "w",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysPrecedesInvariant("v", "x",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysPrecedesInvariant("v", "y",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysPrecedesInvariant("v", "z",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysPrecedesInvariant("w", "x",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysPrecedesInvariant("w", "y",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysPrecedesInvariant("w", "z",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysPrecedesInvariant("x", "y",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysPrecedesInvariant("x", "z",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysPrecedesInvariant("y", "z",
                Event.defTimeRelationStr));

        trueInvs.add(new NeverFollowedInvariant("u", "u",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("v", "v",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("w", "w",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("x", "x",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("y", "y",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("z", "z",
                Event.defTimeRelationStr));

        trueInvs.add(new NeverFollowedInvariant("v", "u",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("w", "u",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("x", "u",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("y", "u",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("z", "u",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("w", "v",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("x", "v",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("y", "v",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("z", "v",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("x", "w",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("y", "w",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("z", "w",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("y", "x",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("z", "x",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("z", "y",
                Event.defTimeRelationStr));


        trueInvs.add(new AlwaysFollowedInvariant("v", "w", r));
        trueInvs.add(new AlwaysFollowedInvariant("y", "z", r));

        trueInvs.add(new AlwaysPrecedesInvariant("v", "w", r));
        trueInvs.add(new AlwaysPrecedesInvariant("y", "z", r));

        trueInvs.add(new NeverFollowedInvariant("v", "v", r));
        trueInvs.add(new NeverFollowedInvariant("w", "w", r));
        trueInvs.add(new NeverFollowedInvariant("y", "y", r));
        trueInvs.add(new NeverFollowedInvariant("z", "z", r));

        trueInvs.add(new NeverFollowedInvariant("v", "y", r));
        trueInvs.add(new NeverFollowedInvariant("v", "z", r));
        trueInvs.add(new NeverFollowedInvariant("w", "v", r));
        trueInvs.add(new NeverFollowedInvariant("w", "y", r));
        trueInvs.add(new NeverFollowedInvariant("w", "z", r));
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
        TemporalInvariantSet minedInvs = genSingleRelationInvariants(singleClosureElement);
        TemporalInvariantSet trueInvs = new TemporalInvariantSet();

        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "w",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "w", r));

        trueInvs.add(new NeverFollowedInvariant("w", "w",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("w", "w", r));

        assertTrue(trueInvs.sameInvariants(minedInvs));
    }

    @Test
    public void singleNonTimeClosure() throws Exception {
        // {"1 v", "2 r cl w"}
        TemporalInvariantSet minedInvs = genSingleRelationInvariants(singleNonTimeClosure);
        TemporalInvariantSet trueInvs = new TemporalInvariantSet();

        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "v",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "w",
                Event.defTimeRelationStr));

        trueInvs.add(new AlwaysFollowedInvariant("v", "w",
                Event.defTimeRelationStr));

        trueInvs.add(new AlwaysPrecedesInvariant("v", "w",
                Event.defTimeRelationStr));

        trueInvs.add(new NeverFollowedInvariant("w", "w",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("v", "v",
                Event.defTimeRelationStr));

        trueInvs.add(new NeverFollowedInvariant("w", "v",
                Event.defTimeRelationStr));

        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "w", r));

        trueInvs.add(new NeverFollowedInvariant("w", "w", r));

        assertTrue(trueInvs.sameInvariants(minedInvs));
    }

    @Test
    public void doubleNonTimeClosure() throws Exception {
        // {"1 v", "2 r cl w", "3 x", "4 r cl y"}
        TemporalInvariantSet minedInvs = genSingleRelationInvariants(doubleNonTimeClosure);
        TemporalInvariantSet trueInvs = new TemporalInvariantSet();

        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "v",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "w",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "x",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "y",
                Event.defTimeRelationStr));

        trueInvs.add(new AlwaysFollowedInvariant("v", "w",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant("v", "x",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant("v", "y",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant("w", "x",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant("w", "y",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant("x", "y",
                Event.defTimeRelationStr));

        trueInvs.add(new AlwaysPrecedesInvariant("v", "w",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysPrecedesInvariant("v", "x",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysPrecedesInvariant("v", "y",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysPrecedesInvariant("w", "x",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysPrecedesInvariant("w", "y",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysPrecedesInvariant("x", "y",
                Event.defTimeRelationStr));

        trueInvs.add(new NeverFollowedInvariant("v", "v",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("w", "w",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("x", "x",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("y", "y",
                Event.defTimeRelationStr));

        trueInvs.add(new NeverFollowedInvariant("w", "v",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("x", "v",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("y", "v",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("x", "w",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("y", "w",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("y", "x",
                Event.defTimeRelationStr));

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
        TemporalInvariantSet minedInvs = genSingleRelationInvariants(closureIntoAndOutOfRegular);
        TemporalInvariantSet trueInvs = new TemporalInvariantSet();

        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "v",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "w",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "x",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "y",
                Event.defTimeRelationStr));

        trueInvs.add(new AlwaysFollowedInvariant("v", "w",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant("v", "x",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant("v", "y",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant("w", "x",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant("w", "y",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant("x", "y",
                Event.defTimeRelationStr));

        trueInvs.add(new AlwaysPrecedesInvariant("v", "w",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysPrecedesInvariant("v", "x",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysPrecedesInvariant("v", "y",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysPrecedesInvariant("w", "x",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysPrecedesInvariant("w", "y",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysPrecedesInvariant("x", "y",
                Event.defTimeRelationStr));

        trueInvs.add(new NeverFollowedInvariant("v", "v",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("w", "w",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("x", "x",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("y", "y",
                Event.defTimeRelationStr));

        trueInvs.add(new NeverFollowedInvariant("w", "v",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("x", "v",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("y", "v",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("x", "w",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("y", "w",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("y", "x",
                Event.defTimeRelationStr));

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
        TemporalInvariantSet minedInvs = genSingleRelationInvariants(disjointClosureAndRegularSubgraphs);
        TemporalInvariantSet trueInvs = new TemporalInvariantSet();

        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "v",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "w",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "x",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "y",
                Event.defTimeRelationStr));

        trueInvs.add(new AlwaysFollowedInvariant("v", "w",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant("v", "x",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant("v", "y",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant("w", "x",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant("w", "y",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant("x", "y",
                Event.defTimeRelationStr));

        trueInvs.add(new AlwaysPrecedesInvariant("v", "w",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysPrecedesInvariant("v", "x",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysPrecedesInvariant("v", "y",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysPrecedesInvariant("w", "x",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysPrecedesInvariant("w", "y",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysPrecedesInvariant("x", "y",
                Event.defTimeRelationStr));

        trueInvs.add(new NeverFollowedInvariant("v", "v",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("w", "w",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("x", "x",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("y", "y",
                Event.defTimeRelationStr));

        trueInvs.add(new NeverFollowedInvariant("w", "v",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("x", "v",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("y", "v",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("x", "w",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("y", "w",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("y", "x",
                Event.defTimeRelationStr));

        trueInvs.add(new AlwaysFollowedInvariant("x", "y", r));

        trueInvs.add(new AlwaysPrecedesInvariant("x", "y", r));

        trueInvs.add(new NeverFollowedInvariant("w", "w", r));
        trueInvs.add(new NeverFollowedInvariant("x", "x", r));
        trueInvs.add(new NeverFollowedInvariant("y", "y", r));

        trueInvs.add(new NeverFollowedInvariant("w", "x", r));
        trueInvs.add(new NeverFollowedInvariant("w", "y", r));
        trueInvs.add(new NeverFollowedInvariant("x", "w", r));
        trueInvs.add(new NeverFollowedInvariant("y", "w", r));
        trueInvs.add(new NeverFollowedInvariant("y", "x", r));

        assertTrue(trueInvs.sameInvariants(minedInvs));
    }

    @Test
    public void disjointClosureAndEventPairSubgraphs() throws Exception {
        // "1 v", "2 r cl w", "3 x", "4 y", "5 r z"
        TemporalInvariantSet minedInvs = genSingleRelationInvariants(disjointClosureAndEventPairSubgraphs);
        TemporalInvariantSet trueInvs = new TemporalInvariantSet();

        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "v",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "w",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "x",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "y",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant(StringEventType
                .newInitialStringEventType(), "z",
                Event.defTimeRelationStr));

        trueInvs.add(new AlwaysFollowedInvariant("v", "w",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant("v", "x",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant("v", "y",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant("v", "z",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant("w", "x",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant("w", "y",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant("w", "z",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant("x", "y",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant("x", "z",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysFollowedInvariant("y", "z",
                Event.defTimeRelationStr));

        trueInvs.add(new AlwaysPrecedesInvariant("v", "w",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysPrecedesInvariant("v", "x",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysPrecedesInvariant("v", "y",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysPrecedesInvariant("v", "z",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysPrecedesInvariant("w", "x",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysPrecedesInvariant("w", "y",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysPrecedesInvariant("w", "z",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysPrecedesInvariant("x", "y",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysPrecedesInvariant("x", "z",
                Event.defTimeRelationStr));
        trueInvs.add(new AlwaysPrecedesInvariant("y", "z",
                Event.defTimeRelationStr));

        trueInvs.add(new NeverFollowedInvariant("v", "v",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("w", "w",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("x", "x",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("y", "y",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("z", "z",
                Event.defTimeRelationStr));

        trueInvs.add(new NeverFollowedInvariant("w", "v",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("x", "v",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("y", "v",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("z", "v",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("x", "w",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("y", "w",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("z", "w",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("y", "x",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("z", "x",
                Event.defTimeRelationStr));
        trueInvs.add(new NeverFollowedInvariant("z", "y",
                Event.defTimeRelationStr));

        trueInvs.add(new AlwaysFollowedInvariant("y", "z", r));

        trueInvs.add(new AlwaysPrecedesInvariant("y", "z", r));

        trueInvs.add(new NeverFollowedInvariant("w", "w", r));
        trueInvs.add(new NeverFollowedInvariant("y", "y", r));
        trueInvs.add(new NeverFollowedInvariant("z", "z", r));

        trueInvs.add(new NeverFollowedInvariant("w", "y", r));
        trueInvs.add(new NeverFollowedInvariant("w", "z", r));
        trueInvs.add(new NeverFollowedInvariant("y", "w", r));
        trueInvs.add(new NeverFollowedInvariant("z", "w", r));
        trueInvs.add(new NeverFollowedInvariant("z", "y", r));

        assertTrue(trueInvs.sameInvariants(minedInvs));
    }

}
