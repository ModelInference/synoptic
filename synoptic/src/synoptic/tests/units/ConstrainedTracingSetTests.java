package synoptic.tests.units;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

import synoptic.invariants.TemporalInvariantSet;
import synoptic.invariants.constraints.TempConstrainedInvariant;
import synoptic.invariants.fsmcheck.APUpperTracingSet;
import synoptic.invariants.fsmcheck.FsmModelChecker;
import synoptic.invariants.fsmcheck.TracingStateSet;
import synoptic.invariants.miners.ChainWalkingTOInvMiner;
import synoptic.invariants.miners.ConstrainedInvMiner;
import synoptic.main.SynopticMain;
import synoptic.main.parser.ParseException;
import synoptic.model.ChainsTraceGraph;
import synoptic.model.Partition;
import synoptic.model.PartitionGraph;
import synoptic.tests.SynopticTest;

/**
 * Tests for finding counter-example paths using TracingStateSets for
 * constrained temporal invariants
 */
public class ConstrainedTracingSetTests extends SynopticTest {

    private String[] stdEvents = { "a 0", "b 11", "c 71", "--", "a 100",
            "b 110", "c 169", "--", "a 200", "b 260", "c 271", "--", "a 300",
            "b 359", "c 369" };
    PartitionGraph graph;
    TempConstrainedInvariant<?> inv;

    @Override
    public void setUp() throws ParseException {
        super.setUp();
        // Enable performance debugging
        SynopticMain.getInstanceWithExistenceCheck().options.enablePerfDebugging = true;
    }
    
    private void tearDown() {
        // Disable performance debugging
        SynopticMain.getInstanceWithExistenceCheck().options.enablePerfDebugging = false;
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
    private PartitionGraph genConstrainedPartitionGraph(String[] events)
            throws Exception {

        // Generate trace graph from passed events
        ChainsTraceGraph inputGraph = (ChainsTraceGraph) genChainsTraceGraph(
                events, genITimeParser());

        // Set up invariant miners
        ChainWalkingTOInvMiner miner = new ChainWalkingTOInvMiner();
        ConstrainedInvMiner constMiner = new ConstrainedInvMiner();

        // Generate constrained invariants
        TemporalInvariantSet invs = constMiner.computeInvariants(miner,
                inputGraph, false);

        // Construct and return partition graph
        return new PartitionGraph(inputGraph, true, invs);
    }

    private enum TracingSet {
        APUpper, APLower, AFbyUpper, AFbyLower
    }

    /**
     * Create partition graph from passed log of events, then generate and
     * return counter-example paths using the tracing state set corresponding to
     * the passed invariant string
     * 
     * @param events
     *            The log of events from which to create the partition graph
     * @param invString
     *            The constrained invariant in the form "a AP b upper"
     * @return Map of counter-example traces by partition
     */
    private Map<Partition, TracingStateSet<Partition>> genCounterExamples(
            String[] events, String invString, TracingSet type)
            throws Exception {

        // Get partition graph
        graph = genConstrainedPartitionGraph(events);

        // Retrieve test invariant
        inv = ConstrainedInvMinerTests.getConstrainedInv(graph.getInvariants(),
                invString);

        // Set up the appropriate ConstrainedTracingSet subtype
        // TODO: Uncomment appropriate lines when other ConstrainedTracingSets
        // are implemented
        TracingStateSet<Partition> tracingSet = null;
        if (type == TracingSet.APUpper) {
            tracingSet = new APUpperTracingSet<Partition>(inv);
        } else if (type == TracingSet.APLower) {
            // tracingSet = new APLowerTracingSet<Partition>(inv);
        } else if (type == TracingSet.AFbyUpper) {
            // tracingSet = new AFbyUpperTracingSet<Partition>(inv);
        } else if (type == TracingSet.AFbyLower) {
            // tracingSet = new AFbyLowerTracingSet<Partition>(inv);
        }

        // Run initial partition graph through the state machine for the
        // retrieved constrained invariant, get counter-examples
        return FsmModelChecker.runChecker(tracingSet, graph, true);
    }

    /**
     * Get partitions corresponding to the 'a' and 'b' predicates of the current
     * constrained invariant
     * 
     * @return 2-element array of partitions: [0] is 'a', [1] is 'b'
     */
    private Partition[] getPartitions() {

        Partition[] partitions = new Partition[2];

        for (Partition part : graph.getNodes()) {

            // Get partition of first invariant predicate ("A")
            if (part.getEType().equals(inv.getFirst())) {
                partitions[0] = part;
            }

            // Get partition of second invariant predicate ("B")
            if (part.getEType().equals(inv.getSecond())) {
                partitions[1] = part;
            }
        }

        return partitions;
    }

    /**
     * Check that APUpperTracingSet reaches a fail state when the time
     * constraint is violated
     */
    @Test
    public void APUpperCounterExamplesTest() throws Exception {

        // Get counter-example paths and partitions corresponding to 'a' and 'b'
        // events
        Map<Partition, TracingStateSet<Partition>> counterEx = genCounterExamples(
                stdEvents, "a AP c upper", TracingSet.APUpper);
        Partition[] partitions = getPartitions();

        // State machine should be at an accept state at partition 'a'
        assertFalse(counterEx.get(partitions[0]).isFail());

        // State machine should be at a failure state at partition 'c'
        assertTrue(counterEx.get(partitions[1]).isFail());

        // Counter-example path should be (c <- b <- a <- INITIAL)
        assertTrue(counterEx.get(partitions[1]).failpath()
                .toCounterexample(inv).path.size() == 4);
        
        tearDown();
    }
}
