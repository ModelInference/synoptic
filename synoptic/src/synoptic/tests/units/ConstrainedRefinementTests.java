package synoptic.tests.units;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
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
import synoptic.model.event.EventType;
import synoptic.model.event.StringEventType;
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
     * as such. The stitch detection process uses some code from
     * ConstrainedTracingSets but only differs based on whether the invariant is
     * upper or lower bound, so only one test of each type is necessary.
     */
    private void stitchDetectionTestCommon(String invString, TracingSet type)
            throws Exception {
        String[] events = { "a 0", "b 3", "c 5", "d 6", "--", "a 10", "b 11",
                "c 14", "d 16" };

        // Get tracing sets
        Map<Partition, TracingStateSet<Partition>> tracingSets = genConstrTracingSets(
                events, invString, type);

        CExamplePath<Partition> cExPath = null;

        // Get the counter-example path at partition d
        for (Partition part : graph.getNodes()) {
            if (part.getEType().equals(new StringEventType("d"))) {
                cExPath = tracingSets.get(part).failpath()
                        .toCounterexample(inv);
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
     * Check that a stitch is detected for an upper-bound invariant.
     */
    @Test
    public void upperStitchDetectionTest() throws Exception {
        stitchDetectionTestCommon("a AP d upper", TracingSet.APUpper);
    }

    /**
     * Check that a stitch is detected for an lower-bound invariant.
     */
    @Test
    public void lowerStitchDetectionTest() throws Exception {
        stitchDetectionTestCommon("a AP d lower", TracingSet.APLower);
    }

    /**
     * Check that a partition split is constructed properly
     */
    @Test
    public void singleSplitCreationTest() throws Exception {

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

    /**
     * When looking for partitions to split during constrained refinement, check
     * that only the proper partitions are considered. As per the constrained
     * refinement algorithm, such partitions must contain a stitch and have >0
     * legal and >0 illegal concrete paths to some other partition later in the
     * violation subpath.
     */
    @Test
    public void getSplitsTest() throws Exception {
        // Same events as in stitchDetectionTest()
        String[] events = { "a 0", "b 3", "c 5", "d 6", "--", "a 10", "b 11",
                "c 14", "d 16" };

        // Get tracing sets
        Map<Partition, TracingStateSet<Partition>> tracingSets = genConstrTracingSets(
                events, "a AP d upper", TracingSet.APUpper);

        CExamplePath<Partition> cExPath = null;

        Partition bPart = null;
        Partition cPart = null;

        // Get the b and c partitions and the counter-example path at partition
        // d
        for (Partition part : graph.getNodes()) {

            EventType evType = part.getEType();

            if (evType.equals(new StringEventType("b"))) {
                bPart = part;
            } else if (evType.equals(new StringEventType("c"))) {
                cPart = part;
            } else if (evType.equals(new StringEventType("d"))) {
                cExPath = tracingSets.get(part).failpath()
                        .toCounterexample(inv);
            }
        }

        List<PartitionSplit> splits = Bisimulation.getSplits(cExPath, graph);

        boolean bIsCandidate = false;
        boolean cIsCandidate = false;

        // Find if any candidate split is on partition b or partition c
        for (PartitionSplit split : splits) {
            // Look for partition b in split
            if (split.getPartition().equals(bPart)) {
                bIsCandidate = true;
            }

            // Look for partition c in split
            else if (split.getPartition().equals(cPart)) {
                cIsCandidate = true;
            }
        }

        // Partition b should be a candidate split, as it contains a stitch and
        // has >0 legal and >0 illegal concrete paths to d (also to c)
        assertTrue(bIsCandidate);

        // Partition c should not be a candidate split, as it contains a stitch
        // but no legal concrete paths to d
        assertFalse(cIsCandidate);
    }

    /**
     * Check that a complete constrained refinment run splits partitions based
     * on violations of various invariants and that it does not split a
     * partition which is only an endpoint of violation subpaths
     */
    @Test
    public void completeRefinementTest() throws Exception {
        // Same events as in stitchDetectionTest() plus one e
        String[] events = { "a 0", "b 3", "c 5", "d 6", "e 9", "--", "a 10",
                "b 11", "c 14", "d 16" };

        // Generate partition graph and run refinement
        graph = genConstrainedPartitionGraph(events);
        exportTestGraph(graph, 0);
        Bisimulation.splitUntilAllInvsSatisfied(graph);
        exportTestGraph(graph, 1);

        boolean hasInitial = false;
        boolean hasTerminal = false;

        for (Partition part : graph.getNodes()) {
            // Check for initial and terminal partitions
            if (part.isInitial()) {
                hasInitial = true;
                continue;
            }
            if (part.isTerminal()) {
                hasTerminal = true;
                continue;
            }

            EventType evType = part.getEType();

            // Partition a should not be split: it is sometimes the beginning of
            // a violation subpath but never in the middle
            if (evType.equals(new StringEventType("a"))) {
                assertTrue(part.size() == 2);
            }

            // Partitions b, c, and d should all be split, and e should begin
            // and end with only 1 event
            else if (evType.equals(new StringEventType("b"))
                    || evType.equals(new StringEventType("c"))
                    || evType.equals(new StringEventType("d"))
                    || evType.equals(new StringEventType("e"))) {
                assertTrue(part.size() == 1);
            }

            // No other partition types should exist
            else {
                throw new AssertionError("Unexpected partition type '" + evType
                        + "' in constrained refinement run");
            }
        }

        // Refined partition graph must have terminal and initial partitions
        assertTrue(hasInitial);
        assertTrue(hasTerminal);
    }
}
