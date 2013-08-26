package synoptic.tests;

import java.util.Map;

import org.junit.After;
import org.junit.Before;

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
import synoptic.tests.units.ConstrainedInvMinerTests;

public abstract class PynopticTest extends SynopticTest {

    protected String[] stdEvents = { "a 0", "b 11", "c 71", "--", "a 100",
            "b 110", "c 169", "--", "a 200", "b 260", "c 271", "--", "a 300",
            "b 359", "c 369" };
    protected PartitionGraph graph;
    protected TempConstrainedInvariant<?> inv;

    protected enum TracingSet {
        APUpper, APLower, AFbyUpper, AFbyLower
    }

    @Before
    @Override
    public void setUp() throws ParseException {
        // Set up SynopticLib state.
        super.setUp();
        // Enable performance debugging
        SynopticMain.getInstanceWithExistenceCheck().options.enablePerfDebugging = true;
    }

    @After
    public void tearDown() {
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
    protected static PartitionGraph genConstrainedPartitionGraph(String[] events)
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

    /**
     * Create partition graph from passed log of events, then generate and
     * return the map of each partition to its final constrained tracing state
     * sets, where the state set is of the type corresponding to the passed
     * invariant string
     * 
     * @param events
     *            The log of events from which to create the partition graph
     * @param invString
     *            The constrained invariant in the form "a AP b upper"
     * @return Map of constrained tracing state sets by partition
     */
    protected Map<Partition, TracingStateSet<Partition>> genConstrTracingSets(
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
        // retrieved constrained invariant, get tracing sets
        return FsmModelChecker.runChecker(tracingSet, graph, true);
    }
}
