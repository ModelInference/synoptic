package synoptic.tests.units;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import synoptic.invariants.TemporalInvariantSet;
import synoptic.invariants.miners.KTailInvariantMiner;
import synoptic.main.parser.TraceParser;
import synoptic.model.ChainsTraceGraph;
import synoptic.tests.SynopticTest;

public class KTailInvariantMinerTests extends SynopticTest {

    /**
     * Test KTail invariant mining on a hardcoded mid-branching example from
     * traces/abstract/
     * 
     * @throws Exception
     */
    @Test
    public void kTail3InvariantsMiningTest() throws Exception {

        String[] events = new String[] { "1 0 c", "2 0 b", "3 0 a", "4 0 d",
                "1 1 f", "2 1 b", "3 1 a", "4 1 e", "1 2 f", "2 2 b", "3 2 a",
                "4 2 d" };

        TraceParser parser = new TraceParser();
        parser.addRegex("^(?<DTIME>)(?<nodename>)(?<TYPE>)$");
        parser.setPartitionsMap("\\k<nodename>");

        ChainsTraceGraph inputGraph = (ChainsTraceGraph) genChainsTraceGraph(
                events, parser);

        KTailInvariantMiner miner = new KTailInvariantMiner(4);
        TemporalInvariantSet invars = miner.computeInvariants(inputGraph);

        // The trace graph composed of three traces above contains 25 tails for
        // k == 3.
        int expectedNumInvars = 25;

        assertEquals("Number of kTail invariants", expectedNumInvars,
                invars.numInvariants());
    }

    @Test
    public void kTail2InvariantsMiningTest() throws Exception {

        String[] events = new String[] { "1 0 c", "2 0 b", "3 0 a", "4 0 d",
                "1 1 f", "2 1 b", "3 1 a", "4 1 e", "1 2 f", "2 2 b", "3 2 a",
                "4 2 d" };

        TraceParser parser = new TraceParser();
        parser.addRegex("^(?<DTIME>)(?<nodename>)(?<TYPE>)$");
        parser.setPartitionsMap("\\k<nodename>");

        ChainsTraceGraph inputGraph = (ChainsTraceGraph) genChainsTraceGraph(
                events, parser);

        KTailInvariantMiner miner = new KTailInvariantMiner(3);
        TemporalInvariantSet invars = miner.computeInvariants(inputGraph);

        // The trace graph composed of three traces above contains 20 tails for
        // k == 2.
        int expectedNumInvars = 20;

        assertEquals("Number of kTail invariants", expectedNumInvars,
                invars.numInvariants());

    }
}
