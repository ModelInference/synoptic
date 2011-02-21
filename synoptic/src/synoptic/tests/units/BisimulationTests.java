package synoptic.tests.units;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import synoptic.algorithms.bisim.Bisimulation;
import synoptic.algorithms.bisim.KTails;
import synoptic.main.Main;
import synoptic.main.ParseException;
import synoptic.main.TraceParser;
import synoptic.model.Graph;
import synoptic.model.LogEvent;
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
        Main.useFSMChecker = true;
    }

    /**
     * Test splitting on a graph whose nodes cannot be split in any way to
     * satisfy the (correctly) mined invariants.
     * 
     * @throws Exception
     */
    @Test
    public void splitPartitionsTest() throws Exception {
        // Simpler trace:
        // String traceStr = "1,1,1 a\n" + "2,2,2 b\n" + "1,2,3 c\n" + "--\n"
        // + "1,0,4 a\n" + "1,0,5 b\n" + "2,0,4 c\n";

        // More complex trace:
        String traceStr = "1,1,1 a\n" + "2,2,2 b\n" + "1,2,3 c\n" + "2,2,4 d\n"
                + "2,2,5 d\n" + "--\n" + "1,0,4 a\n" + "1,0,5 b\n"
                + "2,0,4 c\n" + "2,1,5 d\n" + "2,1,6 d\n";

        List<LogEvent> parsedEvents = parser.parseTraceString(traceStr,
                SynopticTest.testName.getMethodName(), -1);
        Graph<LogEvent> inputGraph = parser.generateDirectTemporalRelation(
                parsedEvents, true);

        PartitionGraph pGraph = Bisimulation.getSplitGraph(inputGraph);
        PartitionGraph expectedPGraph = new PartitionGraph(inputGraph, true);

        // Check that the resulting pGraph is identical to the initial
        // partitioning using kTails from INITIAL nodes with k > diameter of
        // graph.
        Partition initial1 = pGraph.getInitialNodes().iterator().next();
        Partition initial2 = expectedPGraph.getInitialNodes().iterator().next();
        assertTrue(KTails.kEquals(initial1, initial2, 4, false));
    }

    // TODO: test getSplitGraph on a graph where splitting is possible.

    // TODO: test the single step splitPartitions version.

    @Test
    public void mergePartitionsTest() throws Exception {

        // A trace that cannot be reduced with any k.
        String traceStr = "1,1,1 a\n" + "2,2,2 b\n" + "1,2,3 c\n" + "2,2,4 d\n";

        List<LogEvent> parsedEvents = parser.parseTraceString(traceStr,
                SynopticTest.testName.getMethodName(), -1);
        Graph<LogEvent> inputGraph = parser.generateDirectTemporalRelation(
                parsedEvents, true);

        PartitionGraph pGraph = new PartitionGraph(inputGraph);
        Bisimulation.kReduce(pGraph, 0);
        PartitionGraph expectedPGraph = new PartitionGraph(inputGraph);

        Partition initial1 = pGraph.getInitialNodes().iterator().next();
        Partition initial2 = expectedPGraph.getInitialNodes().iterator().next();
        assertTrue(KTails.kEquals(initial1, initial2, 4, false));
    }

    // TODO: test mergePartitions on graphs where merging is possible.

    // TODO: test mergePartitions with invariant preservation.

}
