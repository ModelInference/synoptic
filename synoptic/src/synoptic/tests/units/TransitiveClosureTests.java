package synoptic.tests.units;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import synoptic.algorithms.graph.TransitiveClosure;
import synoptic.model.Event;
import synoptic.model.EventNode;
import synoptic.model.TraceGraph;
import synoptic.model.Transition;
import synoptic.tests.SynopticTest;

/**
 * Tests for synoptic.algorithms.graph.TransitiveClosure class.
 */

public class TransitiveClosureTests extends SynopticTest {
    /**
     * Test constructor for simple case: a --> b --> c --> d should yield a
     * TransitiveClosure with the following arrows representing true values in
     * its tc map: a --> b b --> c c --> d a --> c b --> d a --> d and no others
     */
    @Test
    public void constructorSimpleTest() {
        TraceGraph g = new TraceGraph();
        EventNode a = new EventNode(new Event("a"));
        EventNode b = new EventNode(new Event("b"));
        EventNode c = new EventNode(new Event("c"));
        EventNode d = new EventNode(new Event("d"));

        a.addTransition(new Transition<EventNode>(a, b, "followed by"));
        b.addTransition(new Transition<EventNode>(b, c, "followed by"));
        c.addTransition(new Transition<EventNode>(c, d, "followed by"));

        g.add(a);
        g.add(b);
        g.add(c);
        g.add(d);
        TransitiveClosure<EventNode> tc = new TransitiveClosure<EventNode>(g,
                "followed by");

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
    }

    @Test
    public void constructorEmptyGraphTest() {
        TraceGraph g = new TraceGraph();
        TransitiveClosure<EventNode> tc = new TransitiveClosure<EventNode>(g,
                "followed by");
        assertTrue(tc.getTC().isEmpty());
    }

    @Test
    public void constructorNullRelationTest() {
        TraceGraph g = new TraceGraph();
        EventNode a = new EventNode(new Event("a"));
        EventNode b = new EventNode(new Event("b"));
        EventNode c = new EventNode(new Event("c"));
        EventNode d = new EventNode(new Event("d"));

        a.addTransition(new Transition<EventNode>(a, b, "followed by"));
        b.addTransition(new Transition<EventNode>(b, c, "followed by"));
        c.addTransition(new Transition<EventNode>(c, d, "followed by"));

        g.add(a);
        g.add(b);
        g.add(c);
        g.add(d);
        TransitiveClosure<EventNode> tc = new TransitiveClosure<EventNode>(g,
                null);
        assertTrue(tc.getTC().isEmpty());
    }

    @Test
    public void constructorTCCase() {
        TraceGraph g = new TraceGraph();
        EventNode a = new EventNode(new Event("a"));
        EventNode b = new EventNode(new Event("b"));
        EventNode c = new EventNode(new Event("c"));
        EventNode d = new EventNode(new Event("d"));

        a.addTransition(new Transition<EventNode>(a, b, "followed by"));
        b.addTransition(new Transition<EventNode>(b, c, "after"));
        c.addTransition(new Transition<EventNode>(c, d, "followed by"));

        g.add(a);
        g.add(b);
        g.add(c);
        g.add(d);

        TransitiveClosure<EventNode> tc = new TransitiveClosure<EventNode>(g,
                "followed by");

        Map<EventNode, Set<EventNode>> tc2 = new LinkedHashMap<EventNode, Set<EventNode>>();
        tc2.put(a, new LinkedHashSet<EventNode>());
        tc2.get(a).add(b);
        tc2.put(c, new LinkedHashSet<EventNode>());
        tc2.get(c).add(d);
        assertTrue(tc2.equals(tc.getTC()));

        tc2.put(d, new LinkedHashSet<EventNode>());
        assertFalse(tc2.equals(tc.getTC()));
    }

    /**
     * Missing link case
     */
    @Test
    public void constructorSimple2Test() {
        TraceGraph g = new TraceGraph();
        EventNode a = new EventNode(new Event("a"));
        EventNode b = new EventNode(new Event("b"));
        EventNode c = new EventNode(new Event("c"));
        EventNode d = new EventNode(new Event("d"));

        a.addTransition(new Transition<EventNode>(a, b, "followed by"));
        b.addTransition(new Transition<EventNode>(b, c, "after"));
        c.addTransition(new Transition<EventNode>(c, d, "followed by"));

        g.add(a);
        g.add(b);
        g.add(c);
        g.add(d);

        TransitiveClosure<EventNode> tc = new TransitiveClosure<EventNode>(g,
                "followed by");

        assertTrue(tc.getTC().containsKey(a) && tc.getTC().get(a).contains(b));
        assertTrue(tc.getTC().containsKey(c) && tc.getTC().get(c).contains(d));

        assertFalse(tc.getTC().get(a).contains(c));
        assertFalse(tc.getTC().get(a).contains(d));
        assertFalse(tc.getTC().get(c).contains(b));
        assertFalse(tc.getTC().get(c).contains(a));
        assertFalse(tc.getTC().containsKey(d));
        assertFalse(tc.getTC().containsKey(b));

        assertEquals(2, tc.getTC().size());
        assertEquals(1, tc.getTC().get(a).size());
        assertEquals(1, tc.getTC().get(c).size());

    }

    /**
     * Circular case
     */
    @Test
    public void constructorSimple3Test() {
        TraceGraph g = new TraceGraph();
        EventNode a = new EventNode(new Event("a"));
        EventNode b = new EventNode(new Event("b"));
        EventNode c = new EventNode(new Event("c"));
        EventNode d = new EventNode(new Event("d"));

        a.addTransition(new Transition<EventNode>(a, b, "followed by"));
        b.addTransition(new Transition<EventNode>(b, c, "followed by"));
        c.addTransition(new Transition<EventNode>(c, d, "followed by"));
        d.addTransition(new Transition<EventNode>(d, a, "followed by"));

        g.add(a);
        g.add(b);
        g.add(c);
        g.add(d);

        TransitiveClosure<EventNode> tc = new TransitiveClosure<EventNode>(g,
                "followed by");

        for (EventNode z : g.getNodes()) {
            for (EventNode y : g.getNodes()) {
                assertTrue(tc.getTC().containsKey(z));
                logger.fine("testing for key z=" + z.toString() + " and key y="
                        + y.toString());
                assertTrue(tc.getTC().get(z).contains(y));
            }
        }

        assertEquals(4, tc.getTC().size());
        assertEquals(4, tc.getTC().get(a).size());
        assertEquals(4, tc.getTC().get(b).size());
        assertEquals(4, tc.getTC().get(c).size());
        assertEquals(4, tc.getTC().get(d).size());

    }

    @Test
    public void constructorSimple4Test() {
        TraceGraph g = new TraceGraph();
        EventNode a = new EventNode(new Event("a"));
        EventNode b = new EventNode(new Event("b"));
        EventNode c = new EventNode(new Event("c"));
        EventNode d = new EventNode(new Event("d"));
        a.addTransition(new Transition<EventNode>(a, b, "followed by"));
        a.addTransition(new Transition<EventNode>(a, c, "followed by"));
        b.addTransition(new Transition<EventNode>(b, d, "followed by"));
        g.add(a);
        g.add(b);
        g.add(c);
        g.add(d);
        TransitiveClosure<EventNode> tc = new TransitiveClosure<EventNode>(g,
                "followed by");

        Map<EventNode, Set<EventNode>> tc2 = new LinkedHashMap<EventNode, Set<EventNode>>();
        tc2.put(a, new LinkedHashSet<EventNode>());
        tc2.get(a).add(c);
        tc2.get(a).add(b);
        tc2.get(a).add(d);

        tc2.put(b, new LinkedHashSet<EventNode>());
        tc2.get(b).add(d);

        assertTrue(tc2.equals(tc.getTC()));

        tc2.put(d, new LinkedHashSet<EventNode>());
        assertFalse(tc2.equals(tc.getTC()));
    }

    @Test
    public void constructorSimple5Test() {
        TraceGraph g = new TraceGraph();
        EventNode a = new EventNode(new Event("a"));
        EventNode b = new EventNode(new Event("b"));
        EventNode c = new EventNode(new Event("c"));
        g.add(a);
        g.add(b);
        g.add(c);

        TransitiveClosure<EventNode> tc = new TransitiveClosure<EventNode>(g,
                "followed by");
        assertEquals(0, tc.getTC().size());

        a.addTransition(new Transition<EventNode>(a, b, "followed by"));
        a.addTransition(new Transition<EventNode>(a, c, "followed by"));
        TransitiveClosure<EventNode> tc2 = new TransitiveClosure<EventNode>(g,
                "after");
        assertEquals(0, tc2.getTC().size());

    }

    @Test
    public void constructorSelfTest() {
        TraceGraph g = new TraceGraph();
        EventNode a = new EventNode(new Event("a"));

        a.addTransition(new Transition<EventNode>(a, a, "followed by"));

        g.add(a);

        TransitiveClosure<EventNode> tc = new TransitiveClosure<EventNode>(g,
                "followed by");
        assertTrue(tc.getTC().containsKey(a) && tc.getTC().get(a).contains(a));
        assertEquals(1, tc.getTC().size());
        assertEquals(1, tc.getTC().get(a).size());
    }

    @Test
    public void isReachableSimpleTest() {
        TraceGraph g = new TraceGraph();
        EventNode a = new EventNode(new Event("a"));
        EventNode b = new EventNode(new Event("b"));
        EventNode c = new EventNode(new Event("c"));
        EventNode d = new EventNode(new Event("d"));

        a.addTransition(new Transition<EventNode>(a, b, "followed by"));
        b.addTransition(new Transition<EventNode>(b, c, "followed by"));
        c.addTransition(new Transition<EventNode>(c, d, "followed by"));

        g.add(a);
        g.add(b);
        g.add(c);
        g.add(d);

        TransitiveClosure<EventNode> tc = new TransitiveClosure<EventNode>(g,
                "followed by");

        ArrayList<EventNode> list = new ArrayList<EventNode>();
        list.add(a);
        list.add(b);
        list.add(c);
        list.add(d);

        for (EventNode z : list) {
            for (EventNode y : list) {
                if (list.indexOf(z) < list.indexOf(y)) {
                    assertTrue(tc.isReachable(z, y));
                } else {
                    assertFalse(tc.isReachable(z, y));
                }
            }
        }
    }

    @Test
    public void isReachableSelfTest() {
        TraceGraph g = new TraceGraph();
        EventNode a = new EventNode(new Event("a"));
        a.addTransition(new Transition<EventNode>(a, a, "followed by"));

        g.add(a);

        TransitiveClosure<EventNode> tc = new TransitiveClosure<EventNode>(g,
                "followed by");
        assertTrue(tc.isReachable(a, a));
    }

    @Test
    public void isEqualSimpleTest() {
        EventNode a = new EventNode(new Event("a"));
        EventNode b = new EventNode(new Event("b"));
        EventNode c = new EventNode(new Event("c"));
        EventNode d = new EventNode(new Event("d"));

        a.addTransition(new Transition<EventNode>(a, b, "followed by"));
        b.addTransition(new Transition<EventNode>(b, c, "followed by"));
        c.addTransition(new Transition<EventNode>(c, d, "followed by"));

        TraceGraph g = new TraceGraph();
        g.add(a);
        g.add(b);
        g.add(c);
        g.add(d);
        TransitiveClosure<EventNode> tc = new TransitiveClosure<EventNode>(g,
                "followed by");

        TraceGraph g2 = new TraceGraph();
        g2.add(a);
        g2.add(b);
        g2.add(c);
        g2.add(d);
        TransitiveClosure<EventNode> tc2 = new TransitiveClosure<EventNode>(g2,
                "followed by");
        assertTrue(tc.isEqual(tc2));
    }

    @Test
    public void isEqualTest() {
        EventNode a = new EventNode(new Event("a"));
        EventNode b = new EventNode(new Event("b"));
        EventNode c = new EventNode(new Event("c"));
        EventNode d = new EventNode(new Event("d"));

        a.addTransition(new Transition<EventNode>(a, b, "followed by"));
        b.addTransition(new Transition<EventNode>(b, c, "followed by"));
        c.addTransition(new Transition<EventNode>(c, d, "followed by"));

        TraceGraph g = new TraceGraph();
        g.add(a);
        g.add(b);
        g.add(c);
        g.add(d);
        TransitiveClosure<EventNode> tc = new TransitiveClosure<EventNode>(g,
                "followed by");

        TraceGraph g2 = new TraceGraph();
        g2.add(a);
        g2.add(b);
        g2.add(c);
        g2.add(d);
        EventNode e = new EventNode(new Event("e"));
        e.addTransition(new Transition<EventNode>(e, a, "followed by"));
        g2.add(e);
        TransitiveClosure<EventNode> tc2 = new TransitiveClosure<EventNode>(g2,
                "followed by");

        assertTrue(tc2.isReachable(e, b));

        assertFalse(tc2.isEqual(tc));

        assertFalse(tc.isEqual(tc2));

    }

    @Test
    public void isEqualDiffRelationsTest() {
        EventNode a = new EventNode(new Event("a"));
        EventNode b = new EventNode(new Event("b"));
        EventNode c = new EventNode(new Event("c"));
        EventNode d = new EventNode(new Event("d"));

        a.addTransition(new Transition<EventNode>(a, b, "followed by"));
        b.addTransition(new Transition<EventNode>(b, c, "followed by"));
        c.addTransition(new Transition<EventNode>(c, d, "followed by"));
        a.addTransition(new Transition<EventNode>(a, b, "after"));
        b.addTransition(new Transition<EventNode>(b, c, "after"));
        c.addTransition(new Transition<EventNode>(c, d, "after"));

        TraceGraph g = new TraceGraph();
        g.add(a);
        g.add(b);
        g.add(c);
        g.add(d);
        TransitiveClosure<EventNode> tc = new TransitiveClosure<EventNode>(g,
                "followed by");

        TraceGraph g2 = new TraceGraph();
        g2.add(a);
        g2.add(b);
        g2.add(c);
        g2.add(d);
        TransitiveClosure<EventNode> tc2 = new TransitiveClosure<EventNode>(g2,
                "after");

        assertFalse(tc2.isEqual(tc));

    }
}