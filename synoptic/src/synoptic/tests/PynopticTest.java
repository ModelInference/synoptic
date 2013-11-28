package synoptic.tests;

import java.util.Map;

import org.junit.After;
import org.junit.Before;

import synoptic.invariants.BinaryInvariant;
import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.invariants.constraints.TempConstrainedInvariant;
import synoptic.invariants.fsmcheck.AFbyLowerTracingSet;
import synoptic.invariants.fsmcheck.AFbyUpperTracingSet;
import synoptic.invariants.fsmcheck.APLowerTracingSet;
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

/**
 * Common helper methods and variables for Pynoptic tests
 * 
 */
public abstract class PynopticTest extends SynopticTest {

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
    protected PartitionGraph genConstrainedPartitionGraph(String[] events)
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
     * Retrieve a specific constrained invariant from a TemporalInvariantSet
     * requested using the form "a AFby b upper" or "c AP d lower".
     * 
     * @param minedInvs
     *            Set of mined invariants
     * @param desiredInv
     *            A string describing the requested invariant
     * @return The requested invariant if it exists in the set, else null
     */
    protected TempConstrainedInvariant<?> getConstrainedInv(
            TemporalInvariantSet minedInvs, String desiredInv) {

        // Iterate through all invariants
        for (ITemporalInvariant genericInv : minedInvs.getSet()) {
            if (!(genericInv instanceof TempConstrainedInvariant)) {
                continue;
            }
            TempConstrainedInvariant<?> invar = (TempConstrainedInvariant<?>) genericInv;

            // Look for invariant matching exactly what was requested
            if ((invar.getFirst() + " " + invar.getShortName() + " "
                    + invar.getSecond() + " " + invar.getConstraint()
                    .toString().substring(0, 5)).equals(desiredInv))
                return invar;
        }

        return null;
    }

    /**
     * Retrieve a specific binary invariant from a TemporalInvariantSet
     * requested using the form "a NFby b".
     * 
     * @param minedInvs
     *            Set of mined invariants
     * @param desiredInv
     *            A string describing the requested invariant
     * @return The requested invariant if it exists in the set, else null
     */
    protected BinaryInvariant getBinaryInv(TemporalInvariantSet minedInvs,
            String desiredInv) {
        // Iterate through all invariants
        for (ITemporalInvariant genericInv : minedInvs.getSet()) {
            BinaryInvariant invar = (BinaryInvariant) genericInv;

            // Look for invariant matching exactly what was requested
            if ((invar.getFirst() + " " + invar.getShortName() + " " + invar
                    .getSecond()).equals(desiredInv))
                return invar;
        }

        return null;
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
     * @param type
     *            The type of the invariant (and tracing set)
     * @return Map of constrained tracing state sets by partition
     */
    protected Map<Partition, TracingStateSet<Partition>> genConstrTracingSets(
            String[] events, String invString, TracingSet type)
            throws Exception {

        // Get partition graph
        graph = genConstrainedPartitionGraph(events);

        // Retrieve test invariant
        inv = getConstrainedInv(graph.getInvariants(), invString);

        // Set up the appropriate ConstrainedTracingSet subtype
        TracingStateSet<Partition> tracingSet = null;
        if (type == TracingSet.APUpper) {
            tracingSet = new APUpperTracingSet<Partition>(inv);
        } else if (type == TracingSet.APLower) {
            tracingSet = new APLowerTracingSet<Partition>(inv);
        } else if (type == TracingSet.AFbyUpper) {
            tracingSet = new AFbyUpperTracingSet<Partition>(inv);
        } else if (type == TracingSet.AFbyLower) {
            tracingSet = new AFbyLowerTracingSet<Partition>(inv);
        }

        // Run initial partition graph through the state machine for the
        // retrieved constrained invariant, get tracing sets
        return FsmModelChecker.runChecker(tracingSet, graph, true);
    }
}
