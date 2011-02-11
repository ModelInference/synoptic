package synoptic.tests.units;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import synoptic.algorithms.bisim.KTails;
import synoptic.main.ParseException;
import synoptic.main.TraceParser;
import synoptic.model.Action;
import synoptic.model.Graph;
import synoptic.model.LogEvent;
import synoptic.model.Relation;
import synoptic.tests.SynopticTest;
import synoptic.util.InternalSynopticException;

public class KTailsTest extends SynopticTest {

    private void testTrueBothSubsumingAndNotSubsuming(LogEvent e1, LogEvent e2,
            int k) {
        // TODO: implement subsumption
        // assertTrue(KTails.kEquals(e1, e2, k, true));
        assertTrue(KTails.kEquals(e1, e2, k, false));
    }

    private void testFalseBothSubsumingAndNotSubsuming(LogEvent e1,
            LogEvent e2, int k) {
        // TODO: implement subsumption
        // assertFalse(KTails.kEquals(e1, e2, k, true));
        assertFalse(KTails.kEquals(e1, e2, k, false));
    }

    /**
     * Tests the k=0 case.
     */
    @Test
    public void baseCaseTest() {
        Action a1 = new Action("label1");
        Action a2 = new Action("label1");

        LogEvent e1 = new LogEvent(a1);
        LogEvent e2 = new LogEvent(a2);

        // Subsumption or not should not matter for k = 0.
        testTrueBothSubsumingAndNotSubsuming(e1, e2, 0);

        a2 = new Action("label2");
        e2 = new LogEvent(a2);
        // Subsumption or not should not matter for k = 0.
        testFalseBothSubsumingAndNotSubsuming(e1, e2, 0);
    }

    /**
     * Tests k-equivalence of nodes in two linear graphs (chains).
     * 
     * @throws ParseException
     * @throws InternalSynopticException
     */
    @Test
    public void linearGraphsTest() throws InternalSynopticException,
            ParseException {
        Action a1 = new Action("label1");
        Action a2 = new Action("label1");

        LogEvent e1 = new LogEvent(a1);
        LogEvent e2 = new LogEvent(a2);
        // If k exceeds the depth of the graph, if they are equivalent to max
        // existing depth then they are equal. Regardless of subsumption.
        testTrueBothSubsumingAndNotSubsuming(e1, e2, 100);
        // A node should always be k-equivalent to itself.
        testTrueBothSubsumingAndNotSubsuming(e1, e1, 100);

        String[] events = new String[] { "a", "b", "c", "d" };
        Graph<LogEvent> g1 = SynopticTest.genInitialLinearGraph(events);
        Graph<LogEvent> g2 = SynopticTest.genInitialLinearGraph(events);
        LogEvent[] g1Nodes = new LogEvent[g1.getNodes().size()];
        g1.getNodes().toArray(g1Nodes);
        LogEvent[] g2Nodes = new LogEvent[g2.getNodes().size()];
        g2.getNodes().toArray(g2Nodes);
        // g1 and g2 should be equivalent for all k at every corresponding node,
        // regardless of subsumption.

        // NOTE: both graphs have an additional INITIAL and TERMINAL nodes, thus
        // the + 2 in the loop condition.
        for (int i = 0; i < events.length + 2; i++) {
            e1 = g1Nodes[i];
            e2 = g2Nodes[i];
            for (int k = 0; k < 5; k++) {
                testTrueBothSubsumingAndNotSubsuming(e1, e2, k);
                testTrueBothSubsumingAndNotSubsuming(e1, e1, k);
            }
        }

        events = new String[] { "a", "b", "c", "e" };
        g2 = SynopticTest.genInitialLinearGraph(events);
        g2.getNodes().toArray(g2Nodes);
        // g1 and g2 are k-equivalent at first three nodes for k=3,2,1
        // respectively, but no further. Subsumption follows the same pattern.
        testTrueBothSubsumingAndNotSubsuming(g1Nodes[0], g2Nodes[0], 2);
        testFalseBothSubsumingAndNotSubsuming(g1Nodes[0], g2Nodes[0], 3);

        testTrueBothSubsumingAndNotSubsuming(g1Nodes[1], g2Nodes[1], 1);
        testFalseBothSubsumingAndNotSubsuming(g1Nodes[1], g2Nodes[1], 2);

        testTrueBothSubsumingAndNotSubsuming(g1Nodes[2], g2Nodes[2], 0);
        testFalseBothSubsumingAndNotSubsuming(g1Nodes[2], g2Nodes[2], 1);

        events = new String[] { "a", "a", "a" };
        g2 = SynopticTest.genInitialLinearGraph(events);
        g2.getNodes().toArray(g2Nodes);
        // The last node in g2 should not be 1-equivalent to first node.
        testFalseBothSubsumingAndNotSubsuming(g2Nodes[0], g2Nodes[2], 1);
    }

    /**
     * Tests k-equivalence of nodes in two DAG graphs.
     * 
     * @throws Exception
     */
    @Test
    public void dagGraphsTest() throws Exception {
        TraceParser parser = new TraceParser();
        // TODO: change this to VTIME once this feature is implemented in the
        // parser.
        parser.addRegex("^(?<TIME>)(?<TYPE>)$");
        parser.addSeparator("^--$");
        String traceStr = "1,1,1 a\n" + "2,2,2 b\n" + "1,2,3 c\n" + "--\n"
                + "2,2,2 a\n" + "3,3,3 b\n" + "2,3,4 c\n";
        List<LogEvent> parsedEvents = parser.parseTraceString(traceStr,
                testName.getMethodName(), -1);
        Graph<LogEvent> inputGraph = parser.generateDirectTemporalRelation(
                parsedEvents, true);

        // This returns a set with one node -- INITIAL. It will have two
        // children -- the two "a" nodes, which should be k-equivalent for all
        // k.
        assertFalse(inputGraph.getInitialNodes().isEmpty());
        List<Relation<LogEvent>> initNodeTransitions = inputGraph
                .getInitialNodes().iterator().next().getTransitions();
        LogEvent firstA = initNodeTransitions.get(0).getTarget();
        LogEvent secondA = initNodeTransitions.get(1).getTarget();
        for (int k = 0; k < 3; k++) {
            testTrueBothSubsumingAndNotSubsuming(firstA, secondA, k);
        }
        // GraphVizExporter exporter = new GraphVizExporter();
        // logger.fine(exporter.export(inputGraph));
        // exporter.exportAsDotAndPngFast("../" + testName.getMethodName()
        // + ".dot", inputGraph, true);

    }

    /**
     * Tests k-equivalence of nodes in graphs that contain cycles.
     * 
     * @throws ParseException
     * @throws InternalSynopticException
     */
    @Test
    public void cyclicalGraphsTest() throws InternalSynopticException {

    }
}
