package synoptic.tests.units;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import synoptic.algorithms.graph.IOperation;
import synoptic.algorithms.graph.PartitionSplit;
import synoptic.invariants.BinaryInvariant;
import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.NeverImmediatelyFollowedInvariant;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.invariants.miners.ChainWalkingTOInvMiner;
import synoptic.invariants.miners.TOInvariantMiner;
import synoptic.main.TraceParser;
import synoptic.model.ChainsTraceGraph;
import synoptic.model.EventNode;
import synoptic.model.EventType;
import synoptic.model.Partition;
import synoptic.model.PartitionGraph;
import synoptic.model.StringEventType;
import synoptic.model.interfaces.IGraph;
import synoptic.model.interfaces.INode;
import synoptic.model.interfaces.ITransition;
import synoptic.tests.SynopticTest;
import synoptic.util.Pair;

public class PartitionGraphTests extends SynopticTest {

    @Test
    public void splitThenMergePartitionsTest() throws Exception {
        String[] events = new String[] { "1 a", "--", "1 a" };
        TraceParser parser = new TraceParser();
        parser.addRegex("^(?<TIME>)(?<TYPE>)$");
        parser.addPartitionsSeparator("^--$");

        String traceStr = concatinateWithNewlines(events);
        ArrayList<EventNode> parsedEvents = parser.parseTraceString(traceStr,
                testName.getMethodName(), -1);
        ChainsTraceGraph inputGraph = parser
                .generateDirectTORelation(parsedEvents);

        TOInvariantMiner miner = new ChainWalkingTOInvMiner();
        TemporalInvariantSet invariants = miner.computeInvariants(inputGraph);
        PartitionGraph pGraph = new PartitionGraph(inputGraph, true, invariants);

        // The set of nodes should be: INITIAL, a, TERMINAL
        assertTrue(pGraph.getNodes().size() == 3);

        // Find the 'a' partition and assign it to splitP
        Partition splitP = getNodeByName(pGraph, new StringEventType("a"));
        assertTrue(splitP != null);

        PartitionSplit split = new PartitionSplit(splitP);

        // Add the first two event to the split.
        split.addEventToSplit(parsedEvents.get(0));
        // split.addEventToSplit(parsedEvents.get(1));

        // Perform the split.
        IOperation rewind = pGraph.apply(split);

        exportTestGraph(pGraph, 1);

        // Check the graph after the split.

        // The set of nodes is now: INITIAL, a, a', TERMINAL
        assertTrue(pGraph.getNodes().size() == 4);
        assertTrue(pGraph.getDummyInitialNodes().size() == 1);
        Partition pInitial = pGraph.getDummyInitialNodes().iterator().next();
        assertTrue(pInitial.getTransitions().size() == 2);
        Partition pA1 = pInitial.getTransitions().get(0).getTarget();
        assertTrue(pA1.getEType().equals(new StringEventType("a")));
        assertTrue(pA1.getTransitions().size() == 1);
        assertTrue(pA1.getTransitions().get(0).getTarget().getEType()
                .isTerminalEventType());
        Partition pA2 = pInitial.getTransitions().get(1).getTarget();
        assertTrue(pA2.getEType().equals(new StringEventType("a")));
        assertTrue(pA2.getTransitions().size() == 1);
        assertTrue(pA2.getTransitions().get(0).getTarget().getEType()
                .isTerminalEventType());

        // Undo the split.
        pGraph.apply(rewind);
        // The set of nodes should be back to: INITIAL, a, TERMINAL
        assertTrue(pGraph.getNodes().size() == 3);
    }

    // TODO: Test the multi-split operation.

    // TODO: Test merge operation as a primary operation (not as a rewind).

    private <T extends INode<T>> T getNodeByName(IGraph<T> g, EventType nodeName) {
        for (T node : g.getNodes()) {
            if (node.getEType().equals(nodeName)) {
                return node;
            }
        }
        return null;
    }

    /**
     * Creates a partition graph and and checks to see if all synthetic traces
     * are exported properly (based on what they "should" be). TODO: Test for
     * cycles (when cycle functionality has been properly implemented).
     */
    @Test
    public void exportSyntheticTracesTest() throws Exception {

        // This should create two synthetic traces: I a b c T, I q a b T.
        String[] events = new String[] { "1 0 a", "2 0 b", "3 1 q", "4 1 a",
                "5 1 b", "6 1 c" };
        TraceParser parser = new TraceParser();
        parser.addRegex("^(?<TIME>)(?<nodename>)(?<TYPE>)$");
        parser.setPartitionsMap("\\k<nodename>");

        TOInvariantMiner miner = new ChainWalkingTOInvMiner();
        PartitionGraph pGraph = genInitialPartitionGraph(events, parser, miner);

        // Prepare the output with what it should be (as mentioned above).
        Set<List<Partition>> pTraces = pGraph.getSyntheticTraces();

        // Should only have 2 synthetic traces.
        assertTrue(pTraces.size() == 2);
        for (List<Partition> p : pTraces) {
            // Synthetic traces will only have either a or q after INITIAL.
            boolean aTrace = p.get(1).getEType()
                    .equals(new StringEventType("a"));
            boolean qTrace = p.get(1).getEType()
                    .equals(new StringEventType("q"));
            assertTrue(aTrace || qTrace);
            assertTrue(p.get(0).isInitial());
            if (aTrace) {
                assertTrue(p.get(2).getEType().equals(new StringEventType("b")));
                assertTrue(p.get(3).getEType().equals(new StringEventType("c")));
            } else {
                assertTrue(p.get(2).getEType().equals(new StringEventType("a")));
                assertTrue(p.get(3).getEType().equals(new StringEventType("b")));
            }
            assertTrue(p.get(4).isTerminal());
        }
    }

    /**
     * Test NIFby invariant mining on a hardcoded mid-branching example from
     * traces/abstract/
     * 
     * @throws Exception
     */
    @Test
    public void neverIFbyInvariantsMiningTest() throws Exception {

        String[] events = new String[] { "1 0 c", "2 0 b", "3 0 a", "4 0 d",
                "1 1 f", "2 1 b", "3 1 a", "4 1 e", "1 2 f", "2 2 b", "3 2 a",
                "4 2 d" };

        TraceParser parser = new TraceParser();
        parser.addRegex("^(?<DTIME>)(?<nodename>)(?<TYPE>)$");
        parser.setPartitionsMap("\\k<nodename>");

        TOInvariantMiner miner = new ChainWalkingTOInvMiner();
        PartitionGraph pGraph = genInitialPartitionGraph(events, parser, miner);

        TemporalInvariantSet NIFbys = pGraph.getNIFbyInvariants();

        int numEventTypes = pGraph.getNodes().size();

        // The trace graph composed of three traces above contains 9 sets of
        // edges, each defined by the connected (src,dst) event type pairs.
        int expectedNumNIFbys = (numEventTypes * numEventTypes) - 9;

        assertEquals("Number of NIFby invariants", expectedNumNIFbys, NIFbys
                .getSet().size());

        // Check that the NIFby are exactly the ones we want.
        StringEventType init = StringEventType.newInitialStringEventType();
        StringEventType a = new StringEventType("a");
        StringEventType b = new StringEventType("b");
        StringEventType c = new StringEventType("c");
        StringEventType d = new StringEventType("d");
        StringEventType e = new StringEventType("e");
        StringEventType f = new StringEventType("f");
        StringEventType term = StringEventType.newTerminalStringEventType();

        // Set of all valid event types.
        Set<EventType> eTypes = new LinkedHashSet<EventType>();
        eTypes.add(init);
        eTypes.add(a);
        eTypes.add(b);
        eTypes.add(c);
        eTypes.add(d);
        eTypes.add(e);
        eTypes.add(f);
        eTypes.add(term);

        // Set of all possible invalid NIFby invariants (pairs of events between
        // which the NIFby relationship does _not_ hold).
        Set<Pair<EventType, EventType>> invalidINFbys = new LinkedHashSet<Pair<EventType, EventType>>();
        invalidINFbys.add(new Pair<EventType, EventType>(init, c));
        invalidINFbys.add(new Pair<EventType, EventType>(init, f));
        invalidINFbys.add(new Pair<EventType, EventType>(c, b));
        invalidINFbys.add(new Pair<EventType, EventType>(f, b));
        invalidINFbys.add(new Pair<EventType, EventType>(b, a));
        invalidINFbys.add(new Pair<EventType, EventType>(a, d));
        invalidINFbys.add(new Pair<EventType, EventType>(a, e));
        invalidINFbys.add(new Pair<EventType, EventType>(d, term));
        invalidINFbys.add(new Pair<EventType, EventType>(e, term));

        for (ITemporalInvariant inv : NIFbys.getSet()) {
            assertTrue(inv instanceof NeverImmediatelyFollowedInvariant);

            // 1. Check that both event types are valid.
            EventType srcT = ((BinaryInvariant) inv).getFirst();
            EventType dstT = ((BinaryInvariant) inv).getSecond();
            assertTrue(eTypes.contains(srcT));
            assertTrue(eTypes.contains(dstT));

            // 2. Check that the mined NIFby invariant is not an invalid one.
            Pair<EventType, EventType> p = new Pair<EventType, EventType>(srcT,
                    dstT);
            assertTrue(!invalidINFbys.contains(p));
        }
    }

    // Just to make sure an exception is thrown upon passing an invalid
    // parameter.
    @Test(expected = IllegalArgumentException.class)
    public void exportPathsThroughSelectedNodesNullParam() throws Exception {
        // Some random values to parse.
        String events[] = new String[] { "0 1 a", "0 2 a", "0 5 a" };
        TraceParser parser = new TraceParser();
        parser.addRegex("^(?<DTIME>)(?<nodename>)(?<TYPE>)$");
        parser.setPartitionsMap("\\k<nodename>");
        TOInvariantMiner miner = new ChainWalkingTOInvMiner();
        PartitionGraph pGraph = genInitialPartitionGraph(events, parser, miner);

        pGraph.getPathsThroughPartitions(null);
    }

    // Creates a partition graph the format of which will be
    // used for the tests dealing with grabbing paths through selected
    // nodes.
    private PartitionGraph getPGraphTemplate() throws Exception {
        // Abstract log with two traces (branches from Initial to c and f
        // and then merges/separates at the b/a nodes).
        String events[] = new String[] { "1 0 c", "2 0 b", "3 0 a", "4 0 d",
                "1 1 f", "2 1 b", "3 1 a", "4 1 e" };
        TraceParser parser = new TraceParser();
        parser.addRegex("^(?<TIME>)(?<nodename>)(?<TYPE>)$");
        parser.setPartitionsMap("\\k<nodename>");
        TOInvariantMiner miner = new ChainWalkingTOInvMiner();
        PartitionGraph pGraph = genInitialPartitionGraph(events, parser, miner);
        return pGraph;
    }

    @Test
    public void exportPathsThroughSelectedNodesSingleNodeSelected()
            throws Exception {
        PartitionGraph pGraph = getPGraphTemplate();

        // Grab the partition of event type 'c' from the graph.
        // This will be the "selected" node.
        Set<Partition> nodes = pGraph.getNodes();
        Set<INode<Partition>> selectedNodes = new HashSet<INode<Partition>>();
        INode<Partition> cPartition = null;
        for (Partition p : nodes) {
            if (p.getEType().toString().equals("c")) {
                cPartition = p;
                break;
            }
        }

        // Make sure cPartition was actually found.
        assertTrue(cPartition != null);

        selectedNodes.add(cPartition);

        // Grab the set of paths.
        Map<Integer, Set<ITransition<Partition>>> paths = pGraph
                .getPathsThroughPartitions(selectedNodes);

        assertEquals("There should be one trace only.", paths.keySet().size(),
                1);
    }

    @Test
    public void exportPathsThroughSelectedNodesMultipleNodesSelected()
            throws Exception {
        PartitionGraph pGraph = getPGraphTemplate();

        Set<Partition> nodes = pGraph.getNodes();
        Set<INode<Partition>> selectedNodes = new HashSet<INode<Partition>>();

        // There should only be one a node and one C node, as well as one path
        // through the two of them.
        INode<Partition> cPartition = null;
        INode<Partition> aPartition = null;
        for (Partition p : nodes) {
            String eType = p.getEType().toString();
            if (eType.equals("c")) {
                cPartition = p;
            }

            if (eType.equals("a")) {
                aPartition = p;
            }
        }

        assert (aPartition != null && cPartition != null);

        // Add the "selected" partitions.
        selectedNodes.add(aPartition);
        selectedNodes.add(cPartition);

        Map<Integer, Set<ITransition<Partition>>> paths = pGraph
                .getPathsThroughPartitions(selectedNodes);

        assertEquals("There should be exactly one trace",
                paths.keySet().size(), 1);
    }
}