package synoptic.tests.units;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import synoptic.algorithms.Bisimulation;
import synoptic.algorithms.graphops.PartitionSplit;
import synoptic.invariants.CExamplePath;
import synoptic.invariants.fsmcheck.TracingStateSet;
import synoptic.model.EventNode;
import synoptic.model.Partition;
import synoptic.model.event.Event;
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

    /**
     * Check that a partition split is constructed properly
     */
    @Test
    public void splitCreationTest() throws Exception {

        // Create events: one legal, one illegal. Put them in sets so that they
        // can be used as parameters later
        EventNode legalEv = new EventNode(new Event("label"));
        Set<EventNode> startsOfLegalSubpaths = new HashSet<EventNode>();
        startsOfLegalSubpaths.add(legalEv);

        EventNode illegalEv = new EventNode(new Event("label"));
        Set<EventNode> startsOfIllegalSubpaths = new HashSet<EventNode>();
        startsOfIllegalSubpaths.add(illegalEv);

        // Create a partition, add those events, and add some events that are
        // neither legal or illegal to be randomly assigned to one side of the
        // split or the other
        Partition part = new Partition(legalEv);
        part.addOneEventNode(illegalEv);
        part.addOneEventNode(new EventNode(new Event("label")));
        part.addOneEventNode(new EventNode(new Event("label")));
        part.addOneEventNode(new EventNode(new Event("label")));

        // Create the split
        PartitionSplit split = Bisimulation.makeConstrainedSplit(part,
                startsOfLegalSubpaths, startsOfIllegalSubpaths);

        // Must be a valid split: >0 events split out and >0 events not split
        // out
        assertTrue(split.isValid());

        Set<EventNode> splitOutEvents = split.getSplitEvents();
        // Our legal event must be split out
        assertTrue(splitOutEvents.contains(legalEv));

        // Our illegal event must not be split out
        assertFalse(splitOutEvents.contains(illegalEv));
    }
}
