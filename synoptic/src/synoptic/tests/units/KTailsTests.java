package synoptic.tests.units;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import synoptic.algorithms.KTails;
import synoptic.main.parser.ParseException;
import synoptic.main.parser.TraceParser;
import synoptic.model.ChainsTraceGraph;
import synoptic.model.DAGsTraceGraph;
import synoptic.model.EventNode;
import synoptic.model.PartitionGraph;
import synoptic.model.Transition;
import synoptic.model.event.Event;
import synoptic.tests.SynopticTest;
import synoptic.util.InternalSynopticException;

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
public class KTailsTests extends SynopticTest {

    private static void testTrueBothSubsumingAndNotSubsuming(EventNode e1,
            EventNode e2, int k) {
        // TODO: implement subsumption
        // assertTrue(KTails.kEquals(e1, e2, k, true));

        // Without subsumption e1 =k= e2 should imply e2 =k= e1
        assertTrue(KTails.kEquals(e1, e2, k, false));
        assertTrue(KTails.kEquals(e2, e1, k, false));
    }

    private static void testFalseBothSubsumingAndNotSubsuming(EventNode e1,
            EventNode e2, int k) {
        // TODO: implement subsumption
        // assertFalse(KTails.kEquals(e1, e2, k, true));

        // Without subsumption e1 !=k= e2 should imply e2 !=k= e1
        assertFalse(KTails.kEquals(e1, e2, k, false));
        assertFalse(KTails.kEquals(e2, e1, k, false));
    }

    // Returns a parser to simplify graph generation from string expressions.
    private TraceParser genParser() throws ParseException {
        TraceParser parser = new TraceParser();
        parser.addRegex("^(?<VTIME>)(?<TYPE>)$");
        parser.addPartitionsSeparator("^--$");
        return parser;
    }

    /**
     * Tests the k=0 case, and the case with two graphs with one node each.
     * Tests performKTails with k = 0
     * 
     * @throws ParseException
     * @throws InternalSynopticException
     */
    @Test
    public void performKTails0Test() throws InternalSynopticException,
            ParseException {
        PartitionGraph pGraph = KTails.performKTails(makeSimpleGraph(), 0);
        // All a's and b's should be merged + initial + terminal.
        assertTrue(pGraph.getNodes().size() == 4);
    }

    /**
     * Tests performKTails with k = 1
     * 
     * @throws ParseException
     * @throws InternalSynopticException
     */
    @Test
    public void performKTails1Test() throws InternalSynopticException,
            ParseException {
        PartitionGraph pGraph = KTails.performKTails(makeSimpleGraph(), 1);
        // Only the two b nodes should be merged.
        assertTrue(pGraph.getNodes().size() == 6);
    }

    /**
     * Tests performKTails with k = 2
     * 
     * @throws ParseException
     * @throws InternalSynopticException
     */
    @Test
    public void performKTails2Test() throws InternalSynopticException,
            ParseException {
        PartitionGraph pGraph = KTails.performKTails(makeSimpleGraph(), 2);
        // Only the b nodes should be merged.
        assertTrue(pGraph.getNodes().size() == 6);
    }

    /**
     * Returns a simple trace graph with three short chains.
     * 
     * @throws ParseException
     * @throws InternalSynopticException
     */
    private static ChainsTraceGraph makeSimpleGraph()
            throws InternalSynopticException, ParseException {

        String[] logArr = new String[] { "a", "a", "--", "b", "--", "a", "b" };
        TraceParser defParser = SynopticTest.genDefParser();

        ChainsTraceGraph ret = (ChainsTraceGraph) genChainsTraceGraph(logArr,
                defParser);
        return ret;
        /*
         * Set<EventNode> nodes = new HashSet<EventNode>();
         * 
         * EventNode e1 = new EventNode(new Event("a")); EventNode e2 = new
         * EventNode(new Event("b")); e1.addTransition(e1,
         * Event.defaultTimeRelationString);
         * 
         * EventNode e3 = new EventNode(new Event("a")); EventNode e4 = new
         * EventNode(new Event("b")); e3.addTransition(e4,
         * Event.defaultTimeRelationString);
         * 
         * nodes.add(e1); nodes.add(e2); nodes.add(e3); nodes.add(e4);
         * 
         * return new ChainsTraceGraph(nodes);
         */
    }

    /**
     * Tests the k=0 case.
     */
    @Test
    public void baseCaseTest() {
        Event a1 = new Event("label1");
        Event a2 = new Event("label1");

        EventNode e1 = new EventNode(a1);
        EventNode e2 = new EventNode(a2);

        // Subsumption or not should not matter for k = 0.
        testTrueBothSubsumingAndNotSubsuming(e1, e2, 0);

        a2 = new Event("label2");
        e2 = new EventNode(a2);
        // Subsumption or not should not matter for k = 0.
        testFalseBothSubsumingAndNotSubsuming(e1, e2, 0);
    }

    /**
     * Tests two identical graphs of one node.
     */
    @Test
    public void baseCaseTriviallyIdenticalGraphsTest() {
        Event a1 = new Event("label1");
        Event a2 = new Event("label1");

        EventNode e1 = new EventNode(a1);
        EventNode e2 = new EventNode(a2);
        // If k exceeds the depth of the graph, if they are equivalent to max
        // existing depth then they are equal. Regardless of subsumption.
        testTrueBothSubsumingAndNotSubsuming(e1, e2, 100);
        // A node should always be k-equivalent to itself.
        testTrueBothSubsumingAndNotSubsuming(e1, e1, 100);
    }

    /**
     * Tests k-equivalence of nodes in two identical linear (chains) graphs.
     * 
     * @throws Exception
     */
    @Test
    public void identicalLinearGraphsTest() throws Exception {
        // Create two a->b->c->d graphs
        String events[] = new String[] { "a", "b", "c", "d" };
        EventNode[] g1Nodes = getChainTraceGraphNodesInOrder(events);
        EventNode[] g2Nodes = getChainTraceGraphNodesInOrder(events);

        // Check that g1 and g2 are equivalent for all k at every corresponding
        // node, regardless of subsumption.

        // NOTE: both graphs have an additional INITIAL and TERMINAL nodes, thus
        // the +2 in the loop condition.
        EventNode e1, e2;
        for (int i = 0; i < (events.length + 2); i++) {
            e1 = g1Nodes[i];
            e2 = g2Nodes[i];
            for (int k = 0; k < 5; k++) {
                testTrueBothSubsumingAndNotSubsuming(e1, e2, k);
                testTrueBothSubsumingAndNotSubsuming(e1, e1, k);
            }
        }
    }

    public EventNode[] getChainTraceGraphNodesInOrder(String[] events)
            throws Exception {
        ChainsTraceGraph g = genInitialLinearGraph(events);
        EventNode[] gNodes = new EventNode[g.getNodes().size()];

        EventNode node = g.getDummyInitialNode();
        // gNodes[0] = initNode;
        Set<EventNode> successors;// = initNode.getAllSuccessors();
        // assertTrue(successors.size() == 1);

        // EventNode node = successors.iterator().next();
        // int index = 1;

        int index = 0;
        while (true) {
            gNodes[index] = node;
            index += 1;
            successors = node.getAllSuccessors();
            if (successors.size() == 0) {
                break;
            }
            assertTrue(successors.size() == 1);
            node = successors.iterator().next();
        }

        return gNodes;
    }

    /**
     * Tests for lack of k-equivalence of nodes in two linear (chains) graphs
     * that differ in just one node:
     * 
     * <pre>
     * graph 1: a->b->c->d
     * graph 2: a->b->c->e
     * </pre>
     * 
     * @throws Exception
     */
    @Test
    public void differentLinearGraphsTest() throws Exception {
        EventNode[] g1Nodes = getChainTraceGraphNodesInOrder(new String[] {
                "a", "b", "c", "d" });

        EventNode[] g2Nodes = getChainTraceGraphNodesInOrder(new String[] {
                "a", "b", "c", "e" });

        // ///////////////////
        // g1 and g2 are k-equivalent at first three nodes for k=1,2,3
        // respectively, but no further. Subsumption follows the same pattern.

        // "INITIAL" not at root:
        testTrueBothSubsumingAndNotSubsuming(g1Nodes[0], g2Nodes[0], 0);
        testTrueBothSubsumingAndNotSubsuming(g1Nodes[0], g2Nodes[0], 1);
        testTrueBothSubsumingAndNotSubsuming(g1Nodes[0], g2Nodes[0], 2);
        testTrueBothSubsumingAndNotSubsuming(g1Nodes[0], g2Nodes[0], 3);
        testFalseBothSubsumingAndNotSubsuming(g1Nodes[0], g2Nodes[0], 4);
        testFalseBothSubsumingAndNotSubsuming(g1Nodes[0], g2Nodes[0], 5);

        // "a" node at root:
        testTrueBothSubsumingAndNotSubsuming(g1Nodes[1], g2Nodes[1], 0);
        testTrueBothSubsumingAndNotSubsuming(g1Nodes[1], g2Nodes[1], 1);
        testTrueBothSubsumingAndNotSubsuming(g1Nodes[1], g2Nodes[1], 2);
        testFalseBothSubsumingAndNotSubsuming(g1Nodes[1], g2Nodes[1], 3);
        testFalseBothSubsumingAndNotSubsuming(g1Nodes[1], g2Nodes[1], 4);

        // "b" node at root:
        testTrueBothSubsumingAndNotSubsuming(g1Nodes[2], g2Nodes[2], 0);
        testTrueBothSubsumingAndNotSubsuming(g1Nodes[2], g2Nodes[2], 1);
        testFalseBothSubsumingAndNotSubsuming(g1Nodes[2], g2Nodes[2], 2);

        // "c" node at root:
        testTrueBothSubsumingAndNotSubsuming(g1Nodes[3], g2Nodes[3], 0);
        testFalseBothSubsumingAndNotSubsuming(g1Nodes[3], g2Nodes[3], 1);

        // "d" and "e" nodes at root:
        testFalseBothSubsumingAndNotSubsuming(g1Nodes[4], g2Nodes[4], 0);
    }

    /**
     * Tests k-equivalence of nodes in two tree graphs.
     * 
     * @throws Exception
     */
    @Test
    public void treeGraphsTest() throws Exception {
        // Construct a tree, rooted at INITIAL, with two "a" children, both of
        // which have different "b" and "c" children.
        String traceStr = "1,0 a\n" + "2,0 b\n" + "1,1 c\n" + "--\n"
                + "1,0 a\n" + "2,0 b\n" + "1,1 c\n";
        TraceParser parser = genParser();
        ArrayList<EventNode> parsedEvents = parser.parseTraceString(traceStr,
                SynopticTest.getTestName().getMethodName(), -1);
        DAGsTraceGraph inputGraph = parser
                .generateDirectPORelation(parsedEvents);
        exportTestGraph(inputGraph, 0);

        // This returns a set with one node -- INITIAL. It will have two
        // children -- the two "a" nodes, which should be k-equivalent for all
        // k.
        EventNode initNode = inputGraph.getDummyInitialNode();

        List<Transition<EventNode>> initNodeTransitions = initNode
                .getAllTransitions();
        EventNode firstA = initNodeTransitions.get(0).getTarget();
        EventNode secondA = initNodeTransitions.get(1).getTarget();
        for (int k = 0; k < 3; k++) {
            testTrueBothSubsumingAndNotSubsuming(firstA, secondA, k);
        }

        // In this tree the firstA and secondA should _not_ be 1-equivalent (one
        // has children {b,c}, while the other has children {b,d}), but
        // they are still 0-equivalent.
        traceStr = "1,0 a\n" + "2,0 b\n" + "1,1 c\n" + "--\n" + "1,0 a\n"
                + "2,0 b\n" + "1,1 d\n";
        parser = genParser();
        parsedEvents = parser.parseTraceString(traceStr, SynopticTest
                .getTestName().getMethodName(), -1);
        inputGraph = parser.generateDirectPORelation(parsedEvents);
        exportTestGraph(inputGraph, 1);

        initNode = inputGraph.getDummyInitialNode();
        initNodeTransitions = initNode.getAllTransitions();
        firstA = initNodeTransitions.get(0).getTarget();
        secondA = initNodeTransitions.get(1).getTarget();
        testTrueBothSubsumingAndNotSubsuming(firstA, secondA, 0);
        testFalseBothSubsumingAndNotSubsuming(firstA, secondA, 1);
    }

    /**
     * Tests k-equivalence of nodes in two equivalent DAGs.
     * 
     * @throws Exception
     */
    @Test
    public void equalDagGraphsTest() throws Exception {
        // Generate two identical DAGs
        String traceStr = "1,0 a\n" + "2,1 b\n" + "1,2 c\n" + "2,3 d\n"
                + "--\n" + "1,0 a\n" + "2,1 b\n" + "1,2 c\n" + "2,3 d\n";

        TraceParser parser = genParser();
        ArrayList<EventNode> parsedEvents = parser.parseTraceString(traceStr,
                testName.getMethodName(), -1);
        DAGsTraceGraph g1 = parser.generateDirectPORelation(parsedEvents);
        exportTestGraph(g1, 0);

        List<Transition<EventNode>> initNodeTransitions = g1
                .getDummyInitialNode().getAllTransitions();
        EventNode firstA = initNodeTransitions.get(0).getTarget();
        EventNode secondA = initNodeTransitions.get(1).getTarget();
        for (int k = 0; k < 3; k++) {
            testTrueBothSubsumingAndNotSubsuming(firstA, secondA, k);
        }
    }

    /**
     * Tests k-equivalence of nodes in two slightly different DAGs.
     * 
     * @throws Exception
     */
    @Test
    public void diffDagGraphsTest() throws Exception {
        // NOTE: unlike equalDagGraphsTest(), the second trace in this example
        // omits a "d" event.
        String traceStr = "1,0 a\n" + "2,1 c\n" + "1,2 b\n" + "2,3 d\n"
                + "--\n" + "1,0 a\n" + "2,1 b\n" + "1,2 c\n";

        TraceParser parser = genParser();
        ArrayList<EventNode> parsedEvents = parser.parseTraceString(traceStr,
                testName.getMethodName(), -1);
        DAGsTraceGraph g2 = parser.generateDirectPORelation(parsedEvents);
        exportTestGraph(g2, 1);

        List<Transition<EventNode>> initNodeTransitions = g2
                .getDummyInitialNode().getAllTransitions();
        EventNode firstA = initNodeTransitions.get(0).getTarget();
        EventNode secondA = initNodeTransitions.get(1).getTarget();

        testTrueBothSubsumingAndNotSubsuming(firstA, secondA, 0);
        testTrueBothSubsumingAndNotSubsuming(firstA, secondA, 1);

        // The 'd' in g2 makes it different from g1 at k >= 2.
        testFalseBothSubsumingAndNotSubsuming(firstA, secondA, 2);
        testFalseBothSubsumingAndNotSubsuming(firstA, secondA, 3);
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
    private static List<EventNode> addNodesToGraph(ChainsTraceGraph g,
            String[] labels) {
        LinkedList<EventNode> list = new LinkedList<EventNode>();
        for (String label : labels) {
            Event act = new Event(label);
            EventNode e = new EventNode(act);
            g.add(e);
            list.add(e);
        }

        g.tagInitial(list.get(0), Event.defTimeRelationStr);
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
        ChainsTraceGraph g1 = new ChainsTraceGraph();
        List<EventNode> g1Nodes = addNodesToGraph(g1, new String[] { "a", "a",
                "a" });
        // Create a loop in g1, with 3 nodes
        g1Nodes.get(0).addTransition(g1Nodes.get(1), Event.defTimeRelationStr);
        g1Nodes.get(1).addTransition(g1Nodes.get(2), Event.defTimeRelationStr);
        g1Nodes.get(2).addTransition(g1Nodes.get(0), Event.defTimeRelationStr);
        exportTestGraph(g1, 0);

        ChainsTraceGraph g2 = new ChainsTraceGraph();
        List<EventNode> g2Nodes = addNodesToGraph(g2, new String[] { "a", "a" });
        // Create a loop in g2, with 2 nodes
        g2Nodes.get(0).addTransition(g2Nodes.get(1), Event.defTimeRelationStr);
        g2Nodes.get(1).addTransition(g2Nodes.get(0), Event.defTimeRelationStr);
        exportTestGraph(g2, 1);

        testTrueBothSubsumingAndNotSubsuming(g1Nodes.get(0), g2Nodes.get(0), 0);
        testTrueBothSubsumingAndNotSubsuming(g1Nodes.get(0), g2Nodes.get(0), 1);
        testFalseBothSubsumingAndNotSubsuming(g1Nodes.get(0), g2Nodes.get(0), 2);
        testFalseBothSubsumingAndNotSubsuming(g1Nodes.get(0), g2Nodes.get(0), 3);

        ChainsTraceGraph g3 = new ChainsTraceGraph();
        List<EventNode> g3Nodes = addNodesToGraph(g2, new String[] { "a" });
        // Create a loop in g3, from a to itself
        g3Nodes.get(0).addTransition(g3Nodes.get(0), Event.defTimeRelationStr);
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

        ChainsTraceGraph g1 = new ChainsTraceGraph();
        List<EventNode> g1Nodes = addNodesToGraph(g1, new String[] { "a", "b",
                "c", "d" });
        // Create a loop in g1, with 4 nodes
        g1Nodes.get(0).addTransition(g1Nodes.get(1), Event.defTimeRelationStr);
        g1Nodes.get(1).addTransition(g1Nodes.get(2), Event.defTimeRelationStr);
        g1Nodes.get(2).addTransition(g1Nodes.get(3), Event.defTimeRelationStr);
        g1Nodes.get(3).addTransition(g1Nodes.get(0), Event.defTimeRelationStr);
        exportTestGraph(g1, 0);

        // g1.a is k-equivalent to g1.a for all k
        for (int k = 0; k < 5; k++) {
            testTrueBothSubsumingAndNotSubsuming(g1Nodes.get(0),
                    g1Nodes.get(0), k);
        }

        ChainsTraceGraph g2 = new ChainsTraceGraph();
        List<EventNode> g2Nodes = addNodesToGraph(g2, new String[] { "a", "b",
                "c", "d", "a" });
        // Create a chain from a to a'.
        g2Nodes.get(0).addTransition(g2Nodes.get(1), Event.defTimeRelationStr);
        g2Nodes.get(1).addTransition(g2Nodes.get(2), Event.defTimeRelationStr);
        g2Nodes.get(2).addTransition(g2Nodes.get(3), Event.defTimeRelationStr);
        g2Nodes.get(3).addTransition(g2Nodes.get(4), Event.defTimeRelationStr);
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

        ChainsTraceGraph g1 = new ChainsTraceGraph();
        List<EventNode> g1Nodes = addNodesToGraph(g1, new String[] { "a", "b",
                "c", "d", "b", "c" });

        // Create loop1 in g1, with the first 4 nodes.
        g1Nodes.get(0).addTransition(g1Nodes.get(1), Event.defTimeRelationStr);
        g1Nodes.get(1).addTransition(g1Nodes.get(2), Event.defTimeRelationStr);
        g1Nodes.get(2).addTransition(g1Nodes.get(3), Event.defTimeRelationStr);
        g1Nodes.get(3).addTransition(g1Nodes.get(0), Event.defTimeRelationStr);

        // Create loop2 in g1, with the last 2 nodes, plus the initial node.
        g1Nodes.get(0).addTransition(g1Nodes.get(4), Event.defTimeRelationStr);
        g1Nodes.get(4).addTransition(g1Nodes.get(5), Event.defTimeRelationStr);
        g1Nodes.get(5).addTransition(g1Nodes.get(0), Event.defTimeRelationStr);

        exportTestGraph(g1, 0);

        // //////////////////
        // Now create g2, by generating the two identical loops in the reverse
        // order.

        ChainsTraceGraph g2 = new ChainsTraceGraph();
        List<EventNode> g2Nodes = addNodesToGraph(g2, new String[] { "a", "b",
                "c", "d", "b", "c" });

        // Create loop2 in g2, with the last 2 nodes, plus the initial node.
        g2Nodes.get(0).addTransition(g2Nodes.get(4), Event.defTimeRelationStr);
        g2Nodes.get(4).addTransition(g2Nodes.get(5), Event.defTimeRelationStr);
        g2Nodes.get(5).addTransition(g2Nodes.get(0), Event.defTimeRelationStr);

        // Create loop1 in g2, with the first 4 nodes.
        g2Nodes.get(0).addTransition(g2Nodes.get(1), Event.defTimeRelationStr);
        g2Nodes.get(1).addTransition(g2Nodes.get(2), Event.defTimeRelationStr);
        g2Nodes.get(2).addTransition(g2Nodes.get(3), Event.defTimeRelationStr);
        g2Nodes.get(3).addTransition(g2Nodes.get(0), Event.defTimeRelationStr);

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
