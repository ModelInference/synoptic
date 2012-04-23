package synoptic.tests.units;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Test;

import synoptic.invariants.BinaryInvariant;
import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.NeverImmediatelyFollowedInvariant;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.invariants.miners.ImmediateInvariantMiner;
import synoptic.main.parser.TraceParser;
import synoptic.model.ChainsTraceGraph;
import synoptic.model.event.EventType;
import synoptic.model.event.StringEventType;
import synoptic.tests.SynopticTest;
import synoptic.util.Pair;

public class ImmediateInvariantMinerTests extends SynopticTest {

    /**
     * Test NIFby invariant mining on a hardcoded mid-branching example from
     * traces/abstract/
     * 
     * @throws Exception
     */
    @Test
    public void neverIFbyInvariantsMiningTest() throws Exception {

        String[] events = new String[] { "1 0 c", "2 0 b", "3 0 a", "4 0 d",
                "1 1 f", "2 1 b", "3 1 a", "4 1 e", "1 2 f", "2 2 b", "3 2 a",
                "4 2 d" };

        TraceParser parser = new TraceParser();
        parser.addRegex("^(?<DTIME>)(?<nodename>)(?<TYPE>)$");
        parser.setPartitionsMap("\\k<nodename>");

        ChainsTraceGraph inputGraph = (ChainsTraceGraph) genChainsTraceGraph(
                events, parser);

        ImmediateInvariantMiner miner = new ImmediateInvariantMiner(inputGraph);

        TemporalInvariantSet NIFbys = miner.getNIFbyInvariants();

        int numEventTypes = miner.getEventTypes().size();

        // The trace graph composed of three traces above contains 9 sets of
        // edges, each defined by the connected (src,dst) event type pairs.
        int expectedNumNIFbys = (numEventTypes * numEventTypes) - 9;

        assertEquals("Number of NIFby invariants", expectedNumNIFbys, NIFbys
                .getSet().size());

        // Check that the NIFby are exactly the ones we want.
        StringEventType init = StringEventType.newInitialStringEventType();
        StringEventType a = new StringEventType("a");
        StringEventType b = new StringEventType("b");
        StringEventType c = new StringEventType("c");
        StringEventType d = new StringEventType("d");
        StringEventType e = new StringEventType("e");
        StringEventType f = new StringEventType("f");
        StringEventType term = StringEventType.newTerminalStringEventType();

        // Set of all valid event types.
        Set<EventType> eTypes = new LinkedHashSet<EventType>();
        eTypes.add(init);
        eTypes.add(a);
        eTypes.add(b);
        eTypes.add(c);
        eTypes.add(d);
        eTypes.add(e);
        eTypes.add(f);
        eTypes.add(term);

        assertEquals(miner.getEventTypes(), eTypes);

        // Set of all possible invalid NIFby invariants (pairs of events between
        // which the NIFby relationship does _not_ hold).
        Set<Pair<EventType, EventType>> invalidINFbys = new LinkedHashSet<Pair<EventType, EventType>>();
        invalidINFbys.add(new Pair<EventType, EventType>(init, c));
        invalidINFbys.add(new Pair<EventType, EventType>(init, f));
        invalidINFbys.add(new Pair<EventType, EventType>(c, b));
        invalidINFbys.add(new Pair<EventType, EventType>(f, b));
        invalidINFbys.add(new Pair<EventType, EventType>(b, a));
        invalidINFbys.add(new Pair<EventType, EventType>(a, d));
        invalidINFbys.add(new Pair<EventType, EventType>(a, e));
        invalidINFbys.add(new Pair<EventType, EventType>(d, term));
        invalidINFbys.add(new Pair<EventType, EventType>(e, term));

        for (ITemporalInvariant inv : NIFbys.getSet()) {
            assertTrue(inv instanceof NeverImmediatelyFollowedInvariant);

            // 1. Check that both event types are valid.
            EventType srcT = ((BinaryInvariant) inv).getFirst();
            EventType dstT = ((BinaryInvariant) inv).getSecond();
            assertTrue(eTypes.contains(srcT));
            assertTrue(eTypes.contains(dstT));

            // 2. Check that the mined NIFby invariant is not an invalid one.
            Pair<EventType, EventType> p = new Pair<EventType, EventType>(srcT,
                    dstT);
            assertTrue(!invalidINFbys.contains(p));
        }
    }
}
