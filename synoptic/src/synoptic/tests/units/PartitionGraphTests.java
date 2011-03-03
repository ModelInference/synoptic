package synoptic.tests.units;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Test;

import synoptic.algorithms.graph.IOperation;
import synoptic.algorithms.graph.PartitionSplit;
import synoptic.invariants.InvariantMiner;
import synoptic.invariants.SpecializedInvariantMiner;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.main.Main;
import synoptic.main.TraceParser;
import synoptic.model.Graph;
import synoptic.model.LogEvent;
import synoptic.model.Partition;
import synoptic.model.PartitionGraph;
import synoptic.model.interfaces.IGraph;
import synoptic.model.interfaces.INode;
import synoptic.tests.SynopticTest;

public class PartitionGraphTests extends SynopticTest {

    @Test
    public void splitThenMergePartitionsTest() throws Exception {
        String[] events = new String[] { "1,1 a", "0,2 a" };
        TraceParser parser = new TraceParser();
        parser.addRegex("^(?<VTIME>)(?<TYPE>)$");
        parser.addPartitionsSeparator("^--$");

        String traceStr = concatinateWithNewlines(events);
        ArrayList<LogEvent> parsedEvents = parser.parseTraceString(traceStr,
                testName.getMethodName(), -1);
        Graph<LogEvent> inputGraph = parser.generateDirectTemporalRelation(
                parsedEvents, true);

        InvariantMiner miner = new SpecializedInvariantMiner();
        TemporalInvariantSet invariants = miner.computeInvariants(inputGraph);
        PartitionGraph pGraph = new PartitionGraph(inputGraph, true, invariants);

        // The set of nodes should be: INITIAL, a, TERMINAL
        assertTrue(pGraph.getNodes().size() == 3);

        // Find the 'a' partition and assign it to splitP
        Partition splitP = getNodeByName(pGraph, "a");
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
        assertTrue(pGraph.getInitialNodes().size() == 1);
        Partition pInitial = pGraph.getInitialNodes().iterator().next();
        assertTrue(pInitial.getTransitions().size() == 2);
        Partition pA1 = pInitial.getTransitions().get(0).getTarget();
        assertTrue(pA1.getLabel().equals("a"));
        assertTrue(pA1.getTransitions().size() == 1);
        assertTrue(pA1.getTransitions().get(0).getTarget().getLabel()
                .equals(Main.terminalNodeLabel));
        Partition pA2 = pInitial.getTransitions().get(1).getTarget();
        assertTrue(pA2.getLabel().equals("a"));
        assertTrue(pA2.getTransitions().size() == 1);
        assertTrue(pA2.getTransitions().get(0).getTarget().getLabel()
                .equals(Main.terminalNodeLabel));

        // Undo the split.
        pGraph.apply(rewind);
        // The set of nodes should be back to: INITIAL, a, TERMINAL
        assertTrue(pGraph.getNodes().size() == 3);
    }

    // TODO: Test the multi-split operation.

    // TODO: Test merge operation as a primary operation (not as a rewind).

    private <T extends INode<T>> T getNodeByName(IGraph<T> g, String nodeName) {
        for (T node : g.getNodes()) {
            if (node.getLabel().equals(nodeName)) {
                return node;
            }
        }
        return null;
    }
}
