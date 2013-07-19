package synoptic.tests.units;

import org.junit.Test;

import synoptic.invariants.TemporalInvariantSet;
import synoptic.invariants.fsmcheck.ConstrainedTracingSet;
import synoptic.invariants.miners.ChainWalkingTOInvMiner;
import synoptic.invariants.miners.ConstrainedInvMiner;
import synoptic.main.parser.ParseException;
import synoptic.model.ChainsTraceGraph;
import synoptic.model.EventNode;
import synoptic.model.Partition;
import synoptic.model.PartitionGraph;
import synoptic.tests.SynopticTest;

/**
 * Tests for finding counter-example paths using TracingStateSets for
 * constrained temporal invariants
 */
public class ConstrainedTracingSetTests extends SynopticTest {

    @Override
    public void setUp() throws ParseException {
        super.setUp();
    }
    
    /**
     * Generate a partition graph with constrained invariants using the passed
     * log of events with integer timestamps, e.g., {"a 1", "b 4"}
     * 
     * @param events
     *            Log of events with timings
     * @return PartitionGraph with constrained invariants
     * @throws Exception
     */
    private PartitionGraph genConstrainedPartitionGraph(String[] events) throws Exception {
        
        // Generate trace graph from passed events
        ChainsTraceGraph inputGraph = (ChainsTraceGraph) genChainsTraceGraph(
                events, genITimeParser());
        
        // Set up invariant miners
        ChainWalkingTOInvMiner miner = new ChainWalkingTOInvMiner();
        ConstrainedInvMiner constMiner = new ConstrainedInvMiner();
        
        // Generate constrained invariants
        TemporalInvariantSet invs = constMiner.computeInvariants(miner, inputGraph, false);
        
        // Construct and return partition graph
        return new PartitionGraph(inputGraph, true, invs);
    }

    @Test
    public void testUpper() {
        //
    }
}
