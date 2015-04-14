package synoptic.tests.units;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import synoptic.invariants.CExamplePath;
import synoptic.invariants.fsmcheck.AFbyLowerTracingSet;
import synoptic.invariants.fsmcheck.ConstrainedHistoryNode;
import synoptic.invariants.fsmcheck.ConstrainedTracingSet;
import synoptic.invariants.fsmcheck.TracingStateSet;
import synoptic.model.EventNode;
import synoptic.model.Partition;
import synoptic.model.event.Event;
import synoptic.tests.PynopticTest;
import synoptic.util.resource.AbstractResource;
import synoptic.util.resource.ITotalResource;

/**
 * Tests for finding counter-example paths using TracingStateSets for
 * constrained temporal invariants
 */
public class ConstrainedTracingSetTests extends PynopticTest {

    // Events for AP tests. Traces 1 and 2 are common, and trace 3 causes there
    // to be no "x AFby z" invariant to ensure that AP tracing sets are truly
    // modeling AP FSMs
    protected String[] eventsAP = { "x 0", "y 11", "z 71", "w 72", "--",
            "x 100", "y 160", "z 171", "w 172", "--", "x 200", "u 201" };

    // Events for AFby tests. Traces 1 and 2 are common, and trace 3 causes
    // there to be no "x AP z" invariant to ensure that AFby tracing sets are
    // truly modeling AFby FSMs
    protected String[] eventsAFby = { "x 0", "y 11", "z 71", "w 72", "--",
            "x 100", "y 160", "z 171", "w 172", "--", "v 200", "z 201", "w 202" };

    // Events for IntrBy tests. Produces invariants 'z NFby z' and 'x IntrBy z
    // lower=5' and 'x IntrBy z upper=5'
    protected String[] eventsIntrBy = { "x 0", "z 4", "x 5", "--", "x 0",
            "z 1", "x 5", "--", "x 0", "--", "z 0" };

    /**
     * Get partitions corresponding to the A and B predicates of the current
     * constrained invariant and the terminal partition. This method is only
     * guaranteed to give _some_ partition with the label of A and of B, so
     * behavior is not guaranteed if there exists more than one A or B partition
     * in the partition graph.
     * 
     * @return 3-element array of partitions: [0] is A, [1] is B, [2] is
     *         terminal
     */
    private Partition[] getPartitions() {

        Partition[] partitions = new Partition[3];

        // Get partition of first invariant predicate ("A")
        for (Partition part : graph.getNodes()) {
            if (part.getEType().equals(inv.getFirst())) {
                partitions[0] = part;
                break;
            }
        }

        // Get partition of second invariant predicate ("B")
        for (Partition part : graph.getNodes()) {
            if (part.getEType().equals(inv.getSecond())) {
                partitions[1] = part;
                break;
            }
        }

        // Get terminal partition
        for (Partition part : graph.getNodes()) {
            if (part.isTerminal()) {
                partitions[2] = part;
                break;
            }
        }

        return partitions;
    }

    /**
     * Common code for testing failure states of tracing sets for constrained
     * invariants
     */
    private void failureStateTestCommon(String[] events, String invString,
            TracingSet type) throws Exception {

        // Get tracing sets and partitions corresponding to A and B events
        Map<Partition, TracingStateSet<Partition>> tracingSets = genConstrTracingSets(
                events, invString, type);
        Partition[] partitions = getPartitions();

        if (invString.contains("IntrBy")) {
            // State machine should be at a reject state at partition 'x'
            assertTrue(tracingSets.get(partitions[0]).isFail());
        } else {
            // State machine should be at an accept state at partition 'x'
            assertFalse(tracingSets.get(partitions[0]).isFail());
        }

        // State machine should be at a failure state at partition 'z'
        assertTrue(tracingSets.get(partitions[1]).isFail());
    }

    /**
     * Check that APUpperTracingSet reaches a failure state when (and only when)
     * the time constraint is violated
     */
    @Test
    public void APUpperFailureStateTest() throws Exception {
        failureStateTestCommon(eventsAP, "x AP z upper", TracingSet.APUpper);
    }

    /**
     * Check that AFbyUpperTracingSet reaches a failure state when (and only
     * when) the time constraint is violated
     */
    @Test
    public void AFbyUpperFailureStateTest() throws Exception {
        failureStateTestCommon(eventsAFby, "x AFby z upper",
                TracingSet.AFbyUpper);
    }

    /**
     * Check that APLowerTracingSet reaches a failure state when (and only when)
     * the time constraint is violated
     */
    @Test
    public void APLowerFailureStateTest() throws Exception {
        failureStateTestCommon(eventsAP, "x AP z lower", TracingSet.APLower);
    }

    /**
     * Check that AFbyLowerTracingSet reaches a failure state when (and only
     * when) the time constraint is violated
     */
    @Test
    public void AFbyLowerFailureStateTest() throws Exception {
        failureStateTestCommon(eventsAFby, "x AFby z lower",
                TracingSet.AFbyLower);
    }

    /**
     * Check that IntrByUpperTracingSet reaches a failure state when the time
     * constraint is violated
     */
    @Test
    public void IntrByUpperFailureStateTest() throws Exception {
        failureStateTestCommon(eventsIntrBy, "x IntrBy z upper",
                TracingSet.IntrByLower);
    }

    /**
     * Check that IntrByLowerTracingSet reaches a failure state when the time
     * constraint is violated
     */
    @Test
    public void IntrByLowerFailureStateTest() throws Exception {
        failureStateTestCommon(eventsIntrBy, "x IntrBy z lower",
                TracingSet.IntrByLower);
    }

    /**
     * Common code for testing counter-example paths generated by tracing sets
     * for AFby and AP constrained invariants
     */
    private void cExPathTestAFbyAPCommon(String[] events, String invString,
            TracingSet type) throws Exception {

        // Get tracing sets and partitions corresponding to A and B events and
        // terminal
        Map<Partition, TracingStateSet<Partition>> tracingSets = genConstrTracingSets(
                events, invString, type);
        Partition[] partitions = getPartitions();

        // Get counter-example path at the terminal partition
        CExamplePath<Partition> cExPath = tracingSets.get(partitions[2])
                .failpath().toCounterexample(inv);

        // Shorter variable aliases for cleaner assertions below
        List<Partition> path = cExPath.path;
        int vStart = cExPath.violationStart;
        int vEnd = cExPath.violationEnd;

        // Counter-example path should be (INIT -> x -> y -> z -> w -> TERM)
        assertTrue(path.size() == 6);

        // Violation subpath should start at 'x' and end at 'z'
        assertTrue(path.get(vStart).equals(partitions[0]));
        assertTrue(path.get(vEnd).equals(partitions[1]));

        // Violation subpath should be two abstract transitions long
        // (x -> y -> z)
        assertTrue(vEnd - vStart == 2);

        // Checks specific to upper-bound invariants
        if (type == TracingSet.APUpper || type == TracingSet.AFbyUpper) {
            // Counter-example path should store time at the end of violation
            // subpath as 120 after taking all max time transitions
            // ( x --60--> y --60--> z )
            AbstractResource t120 = new ITotalResource(120);
            assertTrue(cExPath.tDeltas.get(vEnd).compareTo(t120) == 0);
        }

        // Checks specific to lower-bound invariants
        else if (type == TracingSet.APLower || type == TracingSet.AFbyLower) {
            // Counter-example path should store time at the end of violation
            // subpath as 22 after taking all min time transitions
            // ( x --11--> y --11--> z )
            AbstractResource t22 = new ITotalResource(22);
            assertTrue(cExPath.tDeltas.get(vEnd).compareTo(t22) == 0);
        }
    }

    /**
     * Check that APUpperTracingSet returns the correct counter-example path
     * when the time constraint is violated
     */
    @Test
    public void APUpperCounterExamplePathTest() throws Exception {
        cExPathTestAFbyAPCommon(eventsAP, "x AP z upper", TracingSet.APUpper);
    }

    /**
     * Check that AFbyUpperTracingSet returns the correct counter-example path
     * when the time constraint is violated
     */
    @Test
    public void AFbyUpperCounterExamplePathTest() throws Exception {
        cExPathTestAFbyAPCommon(eventsAFby, "x AFby z upper",
                TracingSet.AFbyUpper);
    }

    /**
     * Check that APLowerTracingSet returns the correct counter-example path
     * when the time constraint is violated
     */
    @Test
    public void APLowerCounterExamplePathTest() throws Exception {
        cExPathTestAFbyAPCommon(eventsAP, "x AP z lower", TracingSet.APLower);
    }

    /**
     * Check that AFbyLowerTracingSet returns the correct counter-example path
     * when the time constraint is violated
     */
    @Test
    public void AFbyLowerCounterExamplePathTest() throws Exception {
        cExPathTestAFbyAPCommon(eventsAFby, "x AFby z lower",
                TracingSet.AFbyLower);
    }

    /**
     * Common code for testing counter-example paths generated by tracing sets
     * for IntrBy constrained invariants
     */
    private void cExPathTestIntrByCommon(String[] events, String invString,
            TracingSet type) throws Exception {

        // Get tracing sets and partitions corresponding to A and B events and
        // terminal
        Map<Partition, TracingStateSet<Partition>> tracingSets = genConstrTracingSets(
                events, invString, type);
        Partition[] partitions = getPartitions();

        // Get counter-example paths at x, z, and terminal partitions
        CExamplePath<Partition> cExPathX = tracingSets.get(partitions[0])
                .failpath().toCounterexample(inv);
        CExamplePath<Partition> cExPathZ = tracingSets.get(partitions[1])
                .failpath().toCounterexample(inv);
        CExamplePath<Partition> cExPathTerm = tracingSets.get(partitions[2])
                .failpath().toCounterexample(inv);

        // Shorter variable aliases for cleaner assertions below
        List<Partition> pathX = cExPathX.path;
        List<Partition> pathZ = cExPathZ.path;
        List<Partition> pathTerm = cExPathTerm.path;
        int vStartX = cExPathX.violationStart;
        int vStartZ = cExPathZ.violationStart;
        int vStartTerm = cExPathTerm.violationStart;
        int vEndX = cExPathX.violationEnd;
        int vEndZ = cExPathZ.violationEnd;
        int vEndTerm = cExPathTerm.violationEnd;

        // Counter-ex path at x should be (INIT -> x -> z -> x)
        assertTrue(pathX.size() == 4);

        // Counter-ex path at z should be (INIT -> x -> z -> x -> z)
        assertTrue(pathZ.size() == 5);

        // Counter-ex path at TERM should be (INIT -> x -> z -> x -> TERM)
        assertTrue(pathTerm.size() == 5);

        // All violation subpaths should start and end at 'x'
        assertTrue(vStartX == vStartZ && vStartZ == vStartTerm);
        assertTrue(vEndX == vEndZ && vEndZ == vEndTerm);
        assertTrue(pathX.get(vStartX).equals(partitions[0]));
        assertTrue(pathX.get(vEndX).equals(partitions[0]));

        // All violation subpaths should be two abstract transitions long
        // (x -> z -> x)
        assertTrue(vEndX - vStartX == 2);
        assertTrue(vEndZ - vStartZ == 2);
        assertTrue(vEndTerm - vStartTerm == 2);

        // Checks specific to upper-bound invariant
        if (type == TracingSet.IntrByUpper) {
            // After taking all max time transitions, counter-example path
            // should store time at the end of violation subpath as 8.
            // ( x --4--> z --4--> x )
            AbstractResource t8 = new ITotalResource(8);
            assertTrue(cExPathX.tDeltas.get(vEndX).compareTo(t8) == 0);
            assertTrue(cExPathZ.tDeltas.get(vEndZ).compareTo(t8) == 0);
            assertTrue(cExPathTerm.tDeltas.get(vEndTerm).compareTo(t8) == 0);
        }

        // Checks specific to lower-bound invariant
        else if (type == TracingSet.IntrByLower) {
            // After taking all min time transitions, counter-example path
            // should store time at the end of violation subpath as 2.
            // ( x --1--> z --1--> x )
            AbstractResource t2 = new ITotalResource(2);
            assertTrue(cExPathX.tDeltas.get(vEndX).compareTo(t2) == 0);
            assertTrue(cExPathZ.tDeltas.get(vEndZ).compareTo(t2) == 0);
            assertTrue(cExPathTerm.tDeltas.get(vEndTerm).compareTo(t2) == 0);
        }
    }

    /**
     * Check that IntrByUpperTracingSet returns the correct counter-example path
     * when the time constraint is violated
     */
    @Test
    public void IntrByUpperCounterExamplePathTest() throws Exception {
        cExPathTestIntrByCommon(eventsIntrBy, "x IntrBy z upper",
                TracingSet.IntrByUpper);
    }

    /**
     * Check that IntrByLowerTracingSet returns the correct counter-example path
     * when the time constraint is violated
     */
    @Test
    public void IntrByLowerCounterExamplePathTest() throws Exception {
        cExPathTestIntrByCommon(eventsIntrBy, "x IntrBy z lower",
                TracingSet.IntrByLower);
    }

    /**
     * Check that it can be correctly detected whether or not a
     * ConstraintedTracingSet inhabits a subset of the states of another, which
     * is used for loop-detection and termination
     */
    @Test
    public void stateSubsetTest() throws Exception {
        // Create a legal node to be the "inhabited states"
        ConstrainedHistoryNode<Partition> node = new ConstrainedHistoryNode<Partition>(
                new Partition(new EventNode(new Event(""))), new ITotalResource(0));

        // Create the lists of inhabited states: (0,2,3) and (0,2)
        List<ConstrainedHistoryNode<Partition>> cTSetStates = new ArrayList<ConstrainedHistoryNode<Partition>>();
        cTSetStates.add(node);
        cTSetStates.add(null);
        cTSetStates.add(node);
        cTSetStates.add(node);
        cTSetStates.add(null);
        List<ConstrainedHistoryNode<Partition>> cTSubsetStates = new ArrayList<ConstrainedHistoryNode<Partition>>();
        cTSubsetStates.add(node);
        cTSubsetStates.add(null);
        cTSubsetStates.add(node);
        cTSubsetStates.add(null);
        cTSubsetStates.add(null);

        // Create the tracing sets, and set the created states
        ConstrainedTracingSet<Partition> cTSet = new AFbyLowerTracingSet<Partition>();
        cTSet.setStates(cTSetStates);
        ConstrainedTracingSet<Partition> cTSubset = new AFbyLowerTracingSet<Partition>();
        cTSubset.setStates(cTSubsetStates);

        // cTSubset inhabits a subset of states that cTSet does but not vice
        // versa
        assertTrue(cTSubset.isSubset(cTSet));
        assertFalse(cTSet.isSubset(cTSubset));
    }
}
