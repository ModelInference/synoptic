package synoptic.tests.units;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import synoptic.algorithms.TransitiveClosure;
import synoptic.main.parser.ParseException;
import synoptic.model.ChainsTraceGraph;
import synoptic.model.EventNode;
import synoptic.model.Transition;
import synoptic.model.event.Event;
import synoptic.tests.SynopticTest;

/**
 * Tests for the ChainsTraceGraph class.
 */
public class ChainsTraceGraphTests extends SynopticTest {

    ChainsTraceGraph g;
    EventNode a = new EventNode(new Event("a"));
    EventNode b = new EventNode(new Event("b"));
    EventNode c = new EventNode(new Event("c"));
    EventNode d = new EventNode(new Event("d"));

    @Override
    public void setUp() throws ParseException {
        super.setUp();
    }

    /**
     * Helper: builds and returns a graph of four nodes: a->b->c->d.
     */
    private ChainsTraceGraph buildFourNodeGraph1() {
        ChainsTraceGraph newG = new ChainsTraceGraph();

        a.addTransition(new Transition<EventNode>(a, b, "followed by"));
        b.addTransition(new Transition<EventNode>(b, c, "followed by"));
        c.addTransition(new Transition<EventNode>(c, d, "followed by"));

        newG.add(a);
        newG.add(b);
        newG.add(c);
        newG.add(d);
        newG.tagInitial(a, "followed by");
        newG.tagTerminal(d, "followed by");
        return newG;
    }

    /***********************************************************************/

    /**
     * Tests empty graph creation and its properties.
     */
    @Test
    public void constructorEmptyGraphTest() {
        g = new ChainsTraceGraph();
        assertTrue(g.getNumTraces() == 0);
    }

    /**
     * Tests graph construction with two nodes.
     */
    @Test
    public void constructorTwoNodesTest() {
        g = new ChainsTraceGraph();
        a.addTransition(new Transition<EventNode>(a, b, "followed by"));
        g.add(a);
        g.add(b);
        g.tagInitial(a, "followed by");
        g.tagTerminal(b, "followed by");
        assertTrue(g.getNumTraces() == 1);
    }

    /**
     * Tests the second constructor for ChainsTraceGraph, which takes a
     * collection of nodes.
     */
    @Test
    public void listConstructorTest() {
        List<EventNode> events = new LinkedList<EventNode>();
        events.add(a);
        events.add(b);

        g = new ChainsTraceGraph(events);
        a.addTransition(new Transition<EventNode>(a, b, "followed by"));
        g.tagInitial(a, "followed by");
        g.tagTerminal(b, "followed by");
        assertTrue(g.getNumTraces() == 1);
    }

    /**
     * Tests that a chain graph of a->b->c->d yields a TransitiveClosure of:
     * a->b, b->c, c->d, a->c, b->d, a->d.
     */
    @Test
    public void fourNodeGraphTCTest() {
        g = buildFourNodeGraph1();

        TransitiveClosure tc = g.getTransitiveClosure("followed by");

        // 1. Check that tc.getTC returns the right structure:

        assertTrue(tc.getTC().containsKey(a) && tc.getTC().get(a).contains(b)
                && tc.getTC().get(a).contains(c)
                && tc.getTC().get(a).contains(d));

        assertTrue(tc.getTC().containsKey(b) && tc.getTC().get(b).contains(c)
                && tc.getTC().get(b).contains(d));
        assertTrue(tc.getTC().containsKey(c) && tc.getTC().get(c).contains(d));

        assertFalse(tc.getTC().get(b).contains(a));
        assertFalse(tc.getTC().get(c).contains(b));
        assertFalse(tc.getTC().get(c).contains(a));
        assertFalse(tc.getTC().containsKey(d));

        assertEquals(3, tc.getTC().size());
        assertEquals(3, tc.getTC().get(a).size());
        assertEquals(2, tc.getTC().get(b).size());
        assertEquals(1, tc.getTC().get(c).size());

        // 2. Check that tc reachability is correct:

        ArrayList<EventNode> nodes = new ArrayList<EventNode>();
        nodes.add(a);
        nodes.add(b);
        nodes.add(c);
        nodes.add(d);

        for (EventNode n1 : nodes) {
            Set<EventNode> reachables = null;

            for (EventNode n2 : nodes) {
                if (nodes.indexOf(n1) < nodes.indexOf(n2)) {
                    if (reachables == null) {
                        reachables = new LinkedHashSet<EventNode>();
                    }
                    assertTrue(tc.isReachable(n1, n2));
                    reachables.add(n2);
                } else {
                    assertFalse(tc.isReachable(n1, n2));
                }
            }

            assertEquals(tc.getReachableNodes(n1), reachables);
        }
    }

    /**
     * Tests the transitive closure of a trace that has two relations along the
     * same chain.
     */
    @Test
    public void twoRelationsTCTest() {
        g = new ChainsTraceGraph();

        // a --f--> b --a--> c --f--> d:
        a.addTransition(new Transition<EventNode>(a, b, "followed by"));
        b.addTransition(new Transition<EventNode>(b, c, "after"));
        c.addTransition(new Transition<EventNode>(c, d, "followed by"));

        g.add(a);
        g.add(b);
        g.add(c);
        g.add(d);

        g.tagInitial(b, "after");
        g.tagInitial(a, "followed by");
        g.tagTerminal(c, "after");
        g.tagTerminal(d, "followed by");

        TransitiveClosure tcGenerated;
        Map<EventNode, Set<EventNode>> tcTrue;

        tcTrue = new LinkedHashMap<EventNode, Set<EventNode>>();
        tcTrue.put(a, new LinkedHashSet<EventNode>());
        tcTrue.get(a).add(b);
        tcTrue.put(c, new LinkedHashSet<EventNode>());
        tcTrue.get(c).add(d);
        tcGenerated = g.getTransitiveClosure("followed by");
        assertTrue(tcTrue.equals(tcGenerated.getTC()));

        tcTrue.clear();
        tcTrue.put(b, new LinkedHashSet<EventNode>());
        tcTrue.get(b).add(c);
        tcGenerated = g.getTransitiveClosure("after");
        assertTrue(tcTrue.equals(tcGenerated.getTC()));
    }
}
