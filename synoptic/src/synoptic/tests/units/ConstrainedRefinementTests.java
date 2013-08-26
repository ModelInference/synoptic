package synoptic.tests.units;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

import synoptic.algorithms.Bisimulation;
import synoptic.invariants.CExamplePath;
import synoptic.invariants.fsmcheck.TracingStateSet;
import synoptic.model.Partition;
import synoptic.tests.PynopticTest;

/**
 * Tests for refinement in Bisimulation based on invariants with time
 * constraints.
 * 
 * @author Tony Ohmann (ohmann@cs.umass.edu)
 */
public class ConstrainedRefinementTests extends PynopticTest {

    /**
     * Check that one partition with a stitch and another without are detected
     * as such
     */
    @Test
    public void stitchDetectionTest() throws Exception {
        String[] events = { "a 0", "b 3", "c 5", "d 6", "--", "a 10", "b 11",
                "c 14", "d 16" };

        // Get tracing sets
        Map<Partition, TracingStateSet<Partition>> counterEx = genConstrTracingSets(
                events, "a AP d upper", TracingSet.APUpper);

        CExamplePath<Partition> cExPath = null;

        // Get the counter-example path at partition d (which is the event type
        // of the invariant's second predicate)
        for (Partition part : graph.getNodes()) {
            if (part.getEType().equals(inv.getSecond())) {
                cExPath = counterEx.get(part).failpath().toCounterexample(inv);
            }
        }

        // Partition graph looks like (INIT -> a -> b -> c -> d -> TERM)
        int bIndex = 2;
        int cIndex = 3;

        // There is a sitch at b: first trace is max coming from a, but second
        // trace is max going to c
        assertTrue(Bisimulation.stitchExists(cExPath, bIndex));

        // There is not a stitch at c: second trace is max coming from b and
        // going to d
        assertFalse(Bisimulation.stitchExists(cExPath, cIndex));
    }
}
