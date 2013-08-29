package synoptic.tests.units;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import synoptic.invariants.CExamplePath;
import synoptic.invariants.fsmcheck.TracingStateSet;
import synoptic.model.Partition;
import synoptic.tests.PynopticTest;
import synoptic.util.time.ITime;
import synoptic.util.time.ITotalTime;

/**
 * Tests for finding counter-example paths using TracingStateSets for
 * constrained temporal invariants
 * 
 * @author Tony Ohmann (ohmann@cs.umass.edu)
 */
public class ConstrainedTracingSetTests extends PynopticTest {

    /**
     * Get partitions corresponding to the 'a' and 'b' predicates of the current
     * constrained invariant and the terminal partition. This method is only
     * guaranteed to give _some_ partition with the label of 'a' and of 'b', so
     * behavior is not guaranteed if there exists more than one 'a' or 'b'
     * partition in the partition graph.
     * 
     * @return 3-element array of partitions: [0] is 'a', [1] is 'b', [2] is
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
     * Check that APUpperTracingSet reaches a failure state when (and only when)
     * the time constraint is violated
     */
    @Test
    public void APUpperFailureStateTest() throws Exception {

        // Get tracing sets and partitions corresponding to 'a' and 'b' events
        Map<Partition, TracingStateSet<Partition>> tracingSets = genConstrTracingSets(
                stdEvents, "a AP c upper", TracingSet.APUpper);
        Partition[] partitions = getPartitions();

        // State machine should be at an accept state at partition 'a'
        assertFalse(tracingSets.get(partitions[0]).isFail());

        // State machine should be at a failure state at partition 'c'
        assertTrue(tracingSets.get(partitions[1]).isFail());

        tearDown();
    }

    /**
     * Check that APUpperTracingSet returns the correct counter-example path
     * when the time constraint is violated
     */
    @Test
    public void APUpperCounterExamplePathTest() throws Exception {

        // Get tracing sets and partitions corresponding to 'a' and 'c' events
        // and terminal
        Map<Partition, TracingStateSet<Partition>> tracingSets = genConstrTracingSets(
                stdEvents, "a AP c upper", TracingSet.APUpper);
        Partition[] partitions = getPartitions();

        // Get counter-example path at the terminal partition
        CExamplePath<Partition> cExPath = tracingSets.get(partitions[2])
                .failpath().toCounterexample(inv);

        // Shorter variable aliases for cleaner assertions below
        List<Partition> path = cExPath.path;
        int vStart = cExPath.violationStart;
        int vEnd = cExPath.violationEnd;

        // Counter-example path should be (INIT -> a -> b -> c -> TERM)
        assertTrue(path.size() == 5);

        // Violation subpath should start at 'a' and end at 'c'
        assertTrue(path.get(vStart).equals(partitions[0]));
        assertTrue(path.get(vEnd).equals(partitions[1]));

        // Violation subpath should be two abstract transitions long
        // (a -> b -> c)
        assertTrue(vEnd - vStart == 2);

        // Counter-example path should store time at the end of violation
        // subpath as 120 after
        // taking all max time transitions ( a --60--> b --60--> c )
        ITime t120 = new ITotalTime(120);
        assertTrue(cExPath.tDeltas.get(vEnd).compareTo(t120) == 0);

        tearDown();
    }
}
