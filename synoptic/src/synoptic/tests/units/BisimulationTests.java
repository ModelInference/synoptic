package synoptic.tests.units;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Test;

import synoptic.algorithms.Bisimulation;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.invariants.miners.ChainWalkingTOInvMiner;
import synoptic.invariants.miners.ITOInvariantMiner;
import synoptic.main.SynopticMain;
import synoptic.main.parser.ParseException;
import synoptic.main.parser.TraceParser;
import synoptic.model.ChainsTraceGraph;
import synoptic.model.EventNode;
import synoptic.model.Partition;
import synoptic.model.PartitionGraph;
import synoptic.tests.SynopticTest;

public class BisimulationTests extends SynopticTest {
    // Simplifies graph generation from string expressions.
    TraceParser parser;

    @Override
    public void setUp() throws ParseException {
        super.setUp();
        parser = new TraceParser();
        parser.addRegex("^(?<VTIME>)(?<TYPE>)$");
        parser.addPartitionsSeparator("^--$");
        // Main.dumpIntermediateStages = true;
        SynopticMain.getInstanceWithExistenceCheck().options.useFSMChecker = true;
    }

    /**
     * Test splitting on a graph whose nodes cannot be split in any way to
     * satisfy the (correctly) mined invariants. <br />
     * <br />
     * NOTE: This test is not relevant because PO logs must be processed using a
     * different model. That is, this test is an example of why the FSM model is
     * insufficient for DAG executions.
     * 
     * @throws ParseException
     * @throws InternalSynopticException
     */
    // @Test
    // public void unsplittablePartitionsTest() throws
    // InternalSynopticException,
    // ParseException {
    // // Simpler trace:
    // // String[] traceStrArray = new String[] { "1,1,1 a", "2,2,2 b",
    // // "1,2,3 c", "--", "1,0,4 a", "1,0,5 b", "2,0,4 c" };
    //
    // // More complex trace:
    // String[] traceStrArray = new String[] { "1,1,1 a", "2,2,2 b",
    // "1,2,3 c", "2,2,4 d", "2,2,5 d", "--", "1,0,4 a", "1,0,5 b",
    // "2,0,4 c", "2,1,5 d", "2,1,6 d" };
    // String traceStr = concatinateWithNewlines(traceStrArray);
    //
    // ArrayList<EventNode> parsedEvents = parser.parseTraceString(traceStr,
    // SynopticTest.testName.getMethodName(), -1);
    // Graph<EventNode> inputGraph = parser
    // .generateDirectTemporalRelation(parsedEvents);
    //
    // InvariantMiner miner = new TransitiveClosureTOInvMiner();
    // TemporalInvariantSet invariants = miner.computeInvariants(inputGraph);
    //
    // PartitionGraph pGraph = Bisimulation.getSplitGraph(inputGraph,
    // invariants);
    // PartitionGraph expectedPGraph = new PartitionGraph(inputGraph, true,
    // invariants);
    //
    // // Check that the resulting pGraph is identical to the initial
    // // partitioning using kTails from INITIAL nodes with k > diameter of
    // // graph.
    // Partition initial1 = pGraph.getInitialNodes().iterator().next();
    // Partition initial2 = expectedPGraph.getInitialNodes().iterator().next();
    // assertTrue(KTails.kEquals(initial1, initial2, 4, false));
    // }

    /**
     * Test splitting on a graph that requires the splitting of all partitions
     * to satisfy the mined invariants. The final graph will therefore look just
     * like the initial graph.
     * 
     * @throws Exception
     */
    @Test
    public void splittablePartitionsTest() throws Exception {
        String[] traceStrArray = new String[] { "a", "x", "y", "z", "b", "--",
                "c", "x", "y", "z", "d" };
        String traceStr = concatinateWithNewlines(traceStrArray);

        TraceParser defParser = genDefParser();
        ArrayList<EventNode> parsedEvents = defParser.parseTraceString(
                traceStr, SynopticTest.getTestName().getMethodName(), -1);
        ChainsTraceGraph inputGraph = defParser
                .generateDirectTORelation(parsedEvents);

        exportTestGraph(inputGraph, 0);

        ITOInvariantMiner miner = new ChainWalkingTOInvMiner();
        TemporalInvariantSet invariants = miner.computeInvariants(inputGraph,
                false);

        PartitionGraph pGraph = new PartitionGraph(inputGraph, true, invariants);
        Bisimulation.splitUntilAllInvsSatisfied(pGraph);

        exportTestGraph(pGraph, 1);

        boolean hasInitial = false;
        boolean hasTerminal = false;
        for (Partition p : pGraph.getNodes()) {
            // Check that each partition contains exactly one LogEvent, and that
            // the set of all LogEvents is exactly the set of the input
            // LogEvents.
            if (p.getEType().isInitialEventType()) {
                hasInitial = true;
                continue;
            }
            if (p.isTerminal()) {
                hasTerminal = true;
                continue;
            }

            assertTrue(p.getEventNodes().size() == 1);
            EventNode e = p.getEventNodes().iterator().next();
            logger.fine("Check partition: " + p.toString() + " and e: "
                    + e.toString());
            assertTrue(parsedEvents.contains(e));
            parsedEvents.remove(e);
        }
        assertTrue(hasInitial);
        assertTrue(hasTerminal);
        assertTrue(parsedEvents.size() == 0);
    }

    // TODO: test the single step splitPartitions version.

    // TODO: change mergePartitionsTest to use total order, since a partially
    // ordered trace cannot be turned into a partition graph.
    // @Test
    // public void mergePartitionsTest() throws Exception {
    //
    // // A trace that cannot be reduced with any k.
    // String[] traceStrArray = new String[] { "1,1,1 a", "2,2,2 b",
    // "1,2,3 c", "2,2,4 d" };
    // String traceStr = concatinateWithNewlines(traceStrArray);
    //
    // ArrayList<EventNode> parsedEvents = parser.parseTraceString(traceStr,
    // SynopticTest.testName.getMethodName(), -1);
    // DAGsTraceGraph inputGraph = parser
    // .generateDirectPORelation(parsedEvents);
    //
    // PartitionGraph pGraph = new PartitionGraph(inputGraph, false, null);
    // Bisimulation.kReduce(pGraph, 0);
    // PartitionGraph expectedPGraph = new PartitionGraph(inputGraph, false,
    // null);
    //
    // Partition initial1 = pGraph.getDummyInitialNodes().iterator().next();
    // Partition initial2 = expectedPGraph.getDummyInitialNodes().iterator()
    // .next();
    // assertTrue(KTails.kEquals(initial1, initial2, 4, false));
    // }

    // TODO: test mergePartitions on graphs where merging is possible.

    // TODO: test mergePartitions with invariant preservation.

}
