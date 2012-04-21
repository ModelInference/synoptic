package synoptic.tests.units;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import synoptic.algorithms.graphops.IOperation;
import synoptic.algorithms.graphops.PartitionSplit;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.invariants.miners.ChainWalkingTOInvMiner;
import synoptic.invariants.miners.ITOInvariantMiner;
import synoptic.main.parser.TraceParser;
import synoptic.model.ChainsTraceGraph;
import synoptic.model.EventNode;
import synoptic.model.Partition;
import synoptic.model.PartitionGraph;
import synoptic.model.event.EventType;
import synoptic.model.event.StringEventType;
import synoptic.model.interfaces.IGraph;
import synoptic.model.interfaces.INode;
import synoptic.tests.SynopticTest;

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

        ITOInvariantMiner miner = new ChainWalkingTOInvMiner();
        TemporalInvariantSet invariants = miner.computeInvariants(inputGraph,
                false);
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
        assertTrue(pGraph.getDummyInitialNode().size() == 1);
        Partition pInitial = pGraph.getDummyInitialNode();
        assertTrue(pInitial.getAllTransitions().size() == 2);
        Partition pA1 = pInitial.getAllTransitions().get(0).getTarget();
        assertTrue(pA1.getEType().equals(new StringEventType("a")));
        assertTrue(pA1.getAllTransitions().size() == 1);
        assertTrue(pA1.getAllTransitions().get(0).getTarget().getEType()
                .isTerminalEventType());
        Partition pA2 = pInitial.getAllTransitions().get(1).getTarget();
        assertTrue(pA2.getEType().equals(new StringEventType("a")));
        assertTrue(pA2.getAllTransitions().size() == 1);
        assertTrue(pA2.getAllTransitions().get(0).getTarget().getEType()
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
     * are exported properly.
     * 
     * <pre>
     * TODO: Test for cycles (when cycle functionality has been properly implemented).
     * </pre>
     */
    @Test
    public void exportSyntheticTracesTest() throws Exception {
        // This creates two synthetic traces:
        // INITIAL->a->b->TERMINAL
        // INITIAL->q->a->b->c->TERMINAL
        String[] events = new String[] { "1 0 a", "2 0 b", "3 1 q", "4 1 a",
                "5 1 b", "6 1 c" };
        TraceParser parser = new TraceParser();
        parser.addRegex("^(?<TIME>)(?<nodename>)(?<TYPE>)$");
        parser.setPartitionsMap("\\k<nodename>");

        ITOInvariantMiner miner = new ChainWalkingTOInvMiner();
        PartitionGraph pGraph = genInitialPartitionGraph(events, parser, miner,
                false);

        // Get all the synthetic traces from an initial partitioning.
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
     * Make sure an exception is thrown upon passing an invalid parameter.
     * 
     * @throws Exception
     */
    @Test(expected = IllegalArgumentException.class)
    public void exportPathsThroughSelectedNodesNullParam() throws Exception {
        // Some random values to parse.
        String events[] = new String[] { "0 1 a", "0 2 a", "0 5 a" };
        TraceParser parser = new TraceParser();
        parser.addRegex("^(?<DTIME>)(?<nodename>)(?<TYPE>)$");
        parser.setPartitionsMap("\\k<nodename>");
        ITOInvariantMiner miner = new ChainWalkingTOInvMiner();
        PartitionGraph pGraph = genInitialPartitionGraph(events, parser, miner,
                false);

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
        ITOInvariantMiner miner = new ChainWalkingTOInvMiner();
        PartitionGraph pGraph = genInitialPartitionGraph(events, parser, miner,
                false);
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
        Map<Integer, List<Partition>> paths = pGraph
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

        Map<Integer, List<Partition>> paths = pGraph
                .getPathsThroughPartitions(selectedNodes);

        assertEquals("There should be exactly one trace",
                paths.keySet().size(), 1);
    }
}