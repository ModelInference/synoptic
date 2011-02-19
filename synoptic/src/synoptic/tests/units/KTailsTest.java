package synoptic.tests.units;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
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

/**
 * Tests the KTails algorithm in synoptic.algorithms.bisim.KTails <br />
 * <br />
 * TODO: A good way of testing subsumption (once its implemented) is to create a
 * random graph g, and then to create a new graph g' that is identical to g
 * except some of the nodes\edges are missing. The test is then g subsumes g' at
 * all corresponding vertices that were not removed, for all k > diameter of g.
 * 
 * @author ivan
 */
public class KTailsTest extends SynopticTest {

    /**
     * Simplifies graph generation from string expressions.
     */
    TraceParser parser;

    private static void testTrueBothSubsumingAndNotSubsuming(LogEvent e1,
            LogEvent e2, int k) {
        // TODO: implement subsumption
        // assertTrue(KTails.kEquals(e1, e2, k, true));

        // Without subsumption e1 =k= e2 should imply e2 =k= e1
        assertTrue(KTails.kEquals(e1, e2, k, false));
        assertTrue(KTails.kEquals(e2, e1, k, false));
    }

    private static void testFalseBothSubsumingAndNotSubsuming(LogEvent e1,
            LogEvent e2, int k) {
        // TODO: implement subsumption
        // assertFalse(KTails.kEquals(e1, e2, k, true));

        // Without subsumption e1 !=k= e2 should imply e2 !=k= e1
        assertFalse(KTails.kEquals(e1, e2, k, false));
        assertFalse(KTails.kEquals(e2, e1, k, false));
    }

    @Override
    public void setUp() throws ParseException {
        super.setUp();
        parser = new TraceParser();
        // TODO: change this to VTIME once this feature is implemented in the
        // parser.
        parser.addRegex("^(?<VTIME>)(?<TYPE>)$");
        parser.addPartitionsSeparator("^--$");
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
     * @throws Exception
     */
    @Test
    public void linearGraphsTest() throws Exception {
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
        exportTestGraph(g1, 0);
        exportTestGraph(g2, 1);
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
        exportTestGraph(g2, 2);
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
        exportTestGraph(g2, 3);
        g2.getNodes().toArray(g2Nodes);
        // The last node in g2 should not be 1-equivalent to first node.
        testFalseBothSubsumingAndNotSubsuming(g2Nodes[0], g2Nodes[2], 1);
    }

    /**
     * Tests k-equivalence of nodes in two tree graphs.
     * 
     * @throws Exception
     */
    @Test
    public void treeGraphsTest() throws Exception {
        // Construct a tree, rooted at INITIAL, with two a children, both of
        // which have different b and c children.
        String traceStr = "1,1,1 a\n" + "2,2,2 b\n" + "1,2,3 c\n" + "--\n"
                + "1,0,4 a\n" + "1,0,5 b\n" + "2,0,4 c\n";
        List<LogEvent> parsedEvents = parser.parseTraceString(traceStr,
                SynopticTest.testName.getMethodName(), -1);
        Graph<LogEvent> inputGraph = parser.generateDirectTemporalRelation(
                parsedEvents, true);
        exportTestGraph(inputGraph, 0);

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

        // In this tree the firstA and secondA should not be 1-equivalent, but
        // they are still 0-equivalent.
        traceStr = "1,1,1 a\n" + "2,2,2 b\n" + "1,2,3 c\n" + "--\n"
                + "1,0,4 a\n" + "1,0,5 b\n" + "2,0,4 d\n";
        parsedEvents = parser.parseTraceString(traceStr,
                SynopticTest.testName.getMethodName(), -1);
        inputGraph = parser.generateDirectTemporalRelation(parsedEvents, true);
        exportTestGraph(inputGraph, 1);

        initNodeTransitions = inputGraph.getInitialNodes().iterator().next()
                .getTransitions();
        firstA = initNodeTransitions.get(0).getTarget();
        secondA = initNodeTransitions.get(1).getTarget();
        testTrueBothSubsumingAndNotSubsuming(firstA, secondA, 0);
        testFalseBothSubsumingAndNotSubsuming(firstA, secondA, 1);

    }

    /**
     * Tests k-equivalence of nodes in two DAG graphs.
     * 
     * @throws Exception
     */
    @Test
    public void dagGraphsTest() throws Exception {
        String traceStr = "1,1,1 a\n" + "2,2,2 b\n" + "1,2,3 c\n" + "0,1,2 a\n";

        List<LogEvent> parsedEvents = parser.parseTraceString(traceStr,
                testName.getMethodName(), -1);
        Graph<LogEvent> g1 = parser.generateDirectTemporalRelation(
                parsedEvents, true);
        exportTestGraph(g1, 0);

        List<Relation<LogEvent>> initNodeTransitions = g1.getInitialNodes()
                .iterator().next().getTransitions();
        LogEvent firstA, secondA;
        firstA = initNodeTransitions.get(0).getTarget();
        secondA = initNodeTransitions.get(1).getTarget();
        for (int k = 0; k < 3; k++) {
            testTrueBothSubsumingAndNotSubsuming(firstA, secondA, k);
        }

        // Adds a 'd' node to end of graph (topology change), and switches
        // temporal order of b and c nodes (non-topological change).
        traceStr = "1,1,1 a\n" + "1,2,3 b\n" + "2,2,2 c\n" + "0,1,2 a\n"
                + "3,3,3 d\n";
        parsedEvents = parser.parseTraceString(traceStr,
                testName.getMethodName(), -1);
        Graph<LogEvent> g2 = parser.generateDirectTemporalRelation(
                parsedEvents, true);
        exportTestGraph(g2, 1);

        LogEvent initG1 = g1.getInitialNodes().iterator().next();
        LogEvent initG2 = g2.getInitialNodes().iterator().next();
        for (int k = 0; k < 3; k++) {
            testTrueBothSubsumingAndNotSubsuming(initG1, initG2, k);
        }
        // The 'd' in g2 makes it different from g1 at k=3.
        testFalseBothSubsumingAndNotSubsuming(initG1, initG2, 3);
    }

    /**
     * Creates a set of nodes based on the labels array. Adds these nodes to the
     * graph, but does not create any transitions between them. Returns the set
     * of created nodes in the order in which they were ordered in labels.
     * Assumes that the first label belongs to an initial node, which will be
     * tagged as initial.
     * 
     * @param g
     *            Graph to add the nodes to.
     * @param labels
     *            Array of labels for new nodes to add to the graph
     * @return The list of generated nodes
     */
    private static List<LogEvent> addNodesToGraph(Graph<LogEvent> g,
            String[] labels) {
        LinkedList<LogEvent> list = new LinkedList<LogEvent>();
        for (String label : labels) {
            Action act = new Action(label);
            LogEvent e = new LogEvent(act);
            g.add(e);
            list.add(e);
        }

        Action dummyAct = Action.NewInitialAction();
        dummyAct = dummyAct.intern();
        g.setDummyInitial(new LogEvent(dummyAct), defRelation);
        g.tagInitial(list.get(0), defRelation);
        return list;
    }

    /**
     * Tests k-equivalence of nodes in graphs that contain cycles.
     * 
     * @throws Exception
     */
    @Test
    public void cyclicalGraphs1Test() throws Exception {
        // NOTE: we can't use the parser to create a circular graph because
        // vector clocks are partially ordered and do not admit cycles. So we
        // have to create circular graphs manually.
        Graph<LogEvent> g1 = new Graph<LogEvent>();
        List<LogEvent> g1Nodes = addNodesToGraph(g1, new String[] { "a", "a",
                "a" });
        // Create a loop in g1, with 3 nodes
        g1Nodes.get(0).addTransition(g1Nodes.get(1), defRelation);
        g1Nodes.get(1).addTransition(g1Nodes.get(2), defRelation);
        g1Nodes.get(2).addTransition(g1Nodes.get(0), defRelation);
        exportTestGraph(g1, 0);

        Graph<LogEvent> g2 = new Graph<LogEvent>();
        List<LogEvent> g2Nodes = addNodesToGraph(g2, new String[] { "a", "a" });
        // Create a loop in g2, with 2 nodes
        g2Nodes.get(0).addTransition(g2Nodes.get(1), defRelation);
        g2Nodes.get(1).addTransition(g2Nodes.get(0), defRelation);
        exportTestGraph(g2, 1);

        testTrueBothSubsumingAndNotSubsuming(g1Nodes.get(0), g2Nodes.get(0), 0);
        testTrueBothSubsumingAndNotSubsuming(g1Nodes.get(0), g2Nodes.get(0), 1);
        testFalseBothSubsumingAndNotSubsuming(g1Nodes.get(0), g2Nodes.get(0), 2);
        testFalseBothSubsumingAndNotSubsuming(g1Nodes.get(0), g2Nodes.get(0), 3);

        Graph<LogEvent> g3 = new Graph<LogEvent>();
        List<LogEvent> g3Nodes = addNodesToGraph(g2, new String[] { "a" });
        // Create a loop in g3, from a to itself
        g3Nodes.get(0).addTransition(g3Nodes.get(0), defRelation);
        exportTestGraph(g3, 2);

        testTrueBothSubsumingAndNotSubsuming(g3Nodes.get(0), g2Nodes.get(0), 0);
        testFalseBothSubsumingAndNotSubsuming(g3Nodes.get(0), g2Nodes.get(0), 1);
        testFalseBothSubsumingAndNotSubsuming(g3Nodes.get(0), g2Nodes.get(0), 2);

    }

    /**
     * More complex looping graphs tests.
     * 
     * @throws Exception
     */
    @Test
    public void cyclicalGraphs2Test() throws Exception {
        // Test history tracking -- the "last a" in g1 and g2 below and
        // different kinds of nodes topologically. At k=4 this becomes apparent
        // with kTails, if we start at the first 'a'.

        Graph<LogEvent> g1 = new Graph<LogEvent>();
        List<LogEvent> g1Nodes = addNodesToGraph(g1, new String[] { "a", "b",
                "c", "d" });
        // Create a loop in g1, with 4 nodes
        g1Nodes.get(0).addTransition(g1Nodes.get(1), defRelation);
        g1Nodes.get(1).addTransition(g1Nodes.get(2), defRelation);
        g1Nodes.get(2).addTransition(g1Nodes.get(3), defRelation);
        g1Nodes.get(3).addTransition(g1Nodes.get(0), defRelation);
        exportTestGraph(g1, 0);

        // g1.a is k-equivalent to g1.a for all k
        for (int k = 0; k < 5; k++) {
            testTrueBothSubsumingAndNotSubsuming(g1Nodes.get(0),
                    g1Nodes.get(0), k);
        }

        Graph<LogEvent> g2 = new Graph<LogEvent>();
        List<LogEvent> g2Nodes = addNodesToGraph(g2, new String[] { "a", "b",
                "c", "d", "a" });
        // Create a chain from a to a'.
        g2Nodes.get(0).addTransition(g2Nodes.get(1), defRelation);
        g2Nodes.get(1).addTransition(g2Nodes.get(2), defRelation);
        g2Nodes.get(2).addTransition(g2Nodes.get(3), defRelation);
        g2Nodes.get(3).addTransition(g2Nodes.get(4), defRelation);
        exportTestGraph(g2, 1);

        testTrueBothSubsumingAndNotSubsuming(g1Nodes.get(0), g2Nodes.get(0), 0);
        testTrueBothSubsumingAndNotSubsuming(g1Nodes.get(0), g2Nodes.get(0), 1);
        testTrueBothSubsumingAndNotSubsuming(g1Nodes.get(0), g2Nodes.get(0), 2);
        testTrueBothSubsumingAndNotSubsuming(g1Nodes.get(0), g2Nodes.get(0), 3);
        testFalseBothSubsumingAndNotSubsuming(g1Nodes.get(0), g2Nodes.get(0), 4);
    }

    /**
     * More complex looping graphs tests.
     * 
     * @throws Exception
     */
    @Test
    public void cyclicalGraphs3Test() throws Exception {
        // Test graphs with multiple loops. g1 has two different loops, which
        // have to be correctly matched to g2 -- which is build in a different
        // order but is topologically identical to g1.

        Graph<LogEvent> g1 = new Graph<LogEvent>();
        List<LogEvent> g1Nodes = addNodesToGraph(g1, new String[] { "a", "b",
                "c", "d", "b", "c" });

        // Create loop1 in g1, with the first 4 nodes.
        g1Nodes.get(0).addTransition(g1Nodes.get(1), defRelation);
        g1Nodes.get(1).addTransition(g1Nodes.get(2), defRelation);
        g1Nodes.get(2).addTransition(g1Nodes.get(3), defRelation);
        g1Nodes.get(3).addTransition(g1Nodes.get(0), defRelation);

        // Create loop2 in g1, with the last 2 nodes, plus the initial node.
        g1Nodes.get(0).addTransition(g1Nodes.get(4), defRelation);
        g1Nodes.get(4).addTransition(g1Nodes.get(5), defRelation);
        g1Nodes.get(5).addTransition(g1Nodes.get(0), defRelation);

        exportTestGraph(g1, 0);

        // //////////////////
        // Now create g2, by generating the two identical loops in the reverse
        // order.

        Graph<LogEvent> g2 = new Graph<LogEvent>();
        List<LogEvent> g2Nodes = addNodesToGraph(g2, new String[] { "a", "b",
                "c", "d", "b", "c" });

        // Create loop2 in g2, with the last 2 nodes, plus the initial node.
        g2Nodes.get(0).addTransition(g2Nodes.get(4), defRelation);
        g2Nodes.get(4).addTransition(g2Nodes.get(5), defRelation);
        g2Nodes.get(5).addTransition(g2Nodes.get(0), defRelation);

        // Create loop1 in g2, with the first 4 nodes.
        g2Nodes.get(0).addTransition(g2Nodes.get(1), defRelation);
        g2Nodes.get(1).addTransition(g2Nodes.get(2), defRelation);
        g2Nodes.get(2).addTransition(g2Nodes.get(3), defRelation);
        g2Nodes.get(3).addTransition(g2Nodes.get(0), defRelation);

        exportTestGraph(g2, 1);

        // //////////////////
        // Now test that the two graphs are identical for all k starting at the
        // initial node.

        for (int k = 0; k < 6; k++) {
            testTrueBothSubsumingAndNotSubsuming(g1Nodes.get(0),
                    g2Nodes.get(0), k);
        }
    }

}
