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
 */
public class ConstrainedRefinementTests extends PynopticTest {

    /**
     * Check that one partition with a stitch and another without are detected
     * as such. The stitch detection process differs based on whether the
     * invariant is upper or lower bound, but only one test of each type is
     * necessary.
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
        assertTrue(Bisimulation.makeConstrainedSplitIfStitch(cExPath, bIndex) != null);

        // There is not a stitch at c: second trace is max coming from b and
        // going to d
        assertTrue(Bisimulation.makeConstrainedSplitIfStitch(cExPath, cIndex) == null);
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

        // Create events: one to represent an outgoing min/max target event and
        // another to represent an incoming *and* outgoing min/max source event.
        // Put them in sets so that they can be used as parameters later
        EventNode inAndOutgoingEv = new EventNode(new Event("label"));
        EventNode outgoingEv = new EventNode(new Event("label"));

        Set<EventNode> incomingMinMaxEvents = new HashSet<EventNode>();
        Set<EventNode> outgoingMinMaxEvents = new HashSet<EventNode>();

        incomingMinMaxEvents.add(inAndOutgoingEv);
        outgoingMinMaxEvents.add(inAndOutgoingEv);
        outgoingMinMaxEvents.add(outgoingEv);

        // Create a partition, add above events, and add some events (not part
        // of any min/max transitions) to be randomly assigned to one side
        // of the split or the other
        Partition part = new Partition(outgoingEv);
        part.addOneEventNode(inAndOutgoingEv);
        part.addOneEventNode(new EventNode(new Event("label")));
        part.addOneEventNode(new EventNode(new Event("label")));
        part.addOneEventNode(new EventNode(new Event("label")));

        // Since the splitting process involves randomness, make the split 10
        // times, and verify that it is legal every time
        for (int i = 0; i < 10; ++i) {

            // Create the split
            PartitionSplit split = Bisimulation.makeConstrainedSplit(part,
                    incomingMinMaxEvents, outgoingMinMaxEvents);

            // Must be a valid split: >0 events split out and >0 events not
            // split out
            assertTrue(split.isValid());

            Set<EventNode> splitOutEvents = split.getSplitEvents();
            // Our outgoing min/max event must be split out
            assertTrue(splitOutEvents.contains(outgoingEv));

            // Our incoming *and* outgoing min/max event must not have be split
            // out so that a valid split is always guaranteed
            assertFalse(splitOutEvents.contains(inAndOutgoingEv));
        }
    }

    /**
     * When looking for partitions to split during constrained refinement, check
     * that only the proper partitions are considered. As per the constrained
     * refinement algorithm, such partitions must contain a stitch and have >0
     * legal and >0 illegal concrete paths to some other partition later in the
     * violation subpath.
     */
    private void getSplitsTestCommon(String invString, TracingSet type)
            throws Exception {
        // Same events as in stitchDetectionTest()
        String[] events = { "a 0", "b 3", "c 5", "d 6", "--", "a 10", "b 11",
                "c 14", "d 16" };

        // Get tracing sets
        Map<Partition, TracingStateSet<Partition>> tracingSets = genConstrTracingSets(
                events, invString, type);

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
     * Check that the proper partitions are considered for splitting using an
     * upper-bound invariant
     */
    @Test
    public void upperGetSplitsTest() throws Exception {
        getSplitsTestCommon("a AP d upper", TracingSet.APUpper);
    }

    /**
     * Check that the proper partitions are considered for splitting using a
     * lower-bound invariant
     */
    @Test
    public void lowerGetSplitsTest() throws Exception {
        getSplitsTestCommon("a AP d lower", TracingSet.APLower);
    }

    /**
     * Check that a complete constrained refinement run splits partitions based
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

    /**
     * Common code for testing refinement of graphs aimed at a specific
     * constrained invariant type
     */
    private void refinementTestCommon(String[] events) throws Exception {

        int totalAs = 0;
        int totalBs = 0;
        int totalCs = 0;

        // Count total number of a, b, and c events
        for (String s : events) {
            if (s.startsWith("a")) {
                totalAs++;
            } else if (s.startsWith("b")) {
                totalBs++;
            } else if (s.startsWith("c")) {
                totalCs++;
            }
        }

        // Generate partition graph and run refinement
        graph = genConstrainedPartitionGraph(events);
        exportTestGraph(graph, 0);
        Bisimulation.splitUntilAllInvsSatisfied(graph);
        exportTestGraph(graph, 1);

        for (Partition part : graph.getNodes()) {

            EventType evType = part.getEType();

            // Partition a should not be split: it is sometimes the beginning of
            // a violation subpath but never in the middle
            if (evType.equals(new StringEventType("a"))) {
                assertTrue(part.size() == totalAs);
            }

            // Partitions b should be split
            else if (evType.equals(new StringEventType("b"))) {
                assertTrue(part.size() < totalBs);
            }

            // Partition c should not be split: it is sometimes the end of a
            // violation subpath but never in the middle
            else if (evType.equals(new StringEventType("c"))) {
                assertTrue(part.size() == totalCs);
            }

            // No other partition types should exist except INIT and TERM
            else if (!part.isInitial() && !part.isTerminal()) {
                throw new AssertionError("Unexpected partition type '" + evType
                        + "' in constrained refinement run");
            }
        }
    }

    /**
     * Tests that constrained refinement is correct using a graph that will only
     * be split by an APUpper counter-example
     */
    @Test
    public void APUpperRefinementTest() throws Exception {
        // Traces 1 and 2 cause the split (stitching the length 4 edges). Trace
        // 3 removes the lower-bound violation and forces only the upper-bound
        // counter-example. Trace 4 removes "a AFby c" invariants and forces AP.
        String[] events = { "a 0", "b 4", "c 5", "--", "a 10", "b 11", "c 15",
                "--", "a 20", "b 21", "c 22", "--", "a 30" };

        refinementTestCommon(events);
    }

    /**
     * Tests that constrained refinement is correct using a graph that will only
     * be split by an AFbyUpper counter-example
     */
    @Test
    public void AFbyUpperRefinementTest() throws Exception {
        // The lack of 'a' in Trace 2 removes "a AP c" invariants and forces
        // AFby. The stitch between the two traces only exists for upper-bound
        // invariants, forcing AFbyUpper counter-example exclusively.
        String[] events = { "a 0", "b 4", "c 5", "--", "b 10", "c 14" };

        refinementTestCommon(events);
    }

    /**
     * Tests that constrained refinement is correct using a graph that will only
     * be split by an APLower counter-example
     */
    @Test
    public void APLowerRefinementTest() throws Exception {
        // Traces 1 and 2 cause the split (stitching the length 1 edges). Trace
        // 3 removes the upper-bound violation and forces only the lower-bound
        // counter-example. Trace 4 removes "a AFby c" invariants and forces AP.
        String[] events = { "a 0", "b 4", "c 5", "--", "a 10", "b 11", "c 15",
                "--", "a 20", "b 24", "c 28", "--", "a 30" };

        refinementTestCommon(events);
    }

    /**
     * Tests that constrained refinement is correct using a graph that will only
     * be split by an AFbyLower counter-example
     */
    @Test
    public void AFbyLowerRefinementTest() throws Exception {
        // The lack of 'a' in Trace 2 removes "a AP c" invariants and forces
        // AFby. The stitch between the two traces only exists for lower-bound
        // invariants, forcing AFbyLower counter-example exclusively.
        String[] events = { "a 0", "b 1", "c 5", "--", "b 10", "c 11" };

        refinementTestCommon(events);
    }
}
