package synoptic.tests.units;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;

import synoptic.algorithms.graph.TransitiveClosure;
import synoptic.model.Event;
import synoptic.model.Graph;
import synoptic.model.EventNode;
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
        Graph<EventNode> g = new Graph<EventNode>();
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

        assertTrue(tc.getTC().containsKey(a) && tc.getTC().get(a).get(b)
                && tc.getTC().get(a).get(c) && tc.getTC().get(a).get(d));

        assertTrue(tc.getTC().containsKey(b) && tc.getTC().get(b).get(c)
                && tc.getTC().get(b).get(d));
        assertTrue(tc.getTC().containsKey(c) && tc.getTC().get(c).get(d));

        assertFalse(tc.getTC().get(b).containsKey(a));
        assertFalse(tc.getTC().get(c).containsKey(b));
        assertFalse(tc.getTC().get(c).containsKey(a));
        assertFalse(tc.getTC().containsKey(d));

        assertEquals(3, tc.getTC().size());
        assertEquals(3, tc.getTC().get(a).size());
        assertEquals(2, tc.getTC().get(b).size());
        assertEquals(1, tc.getTC().get(c).size());
    }

    @Test
    public void constructorEmptyGraphTest() {
        Graph<EventNode> g = new Graph<EventNode>();
        TransitiveClosure<EventNode> tc = new TransitiveClosure<EventNode>(g,
                "followed by");
        assertTrue(tc.getTC().isEmpty());
    }

    @Test
    public void constructorNullRelationTest() {
        Graph<EventNode> g = new Graph<EventNode>();
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
        Graph<EventNode> g = new Graph<EventNode>();
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

        HashMap<EventNode, HashMap<EventNode, Boolean>> tc2 = new HashMap<EventNode, HashMap<EventNode, Boolean>>();
        tc2.put(a, new HashMap<EventNode, Boolean>());
        tc2.get(a).put(b, true);
        tc2.put(c, new HashMap<EventNode, Boolean>());
        tc2.get(c).put(d, true);
        assertTrue(tc2.equals(tc.getTC()));

        tc2.put(d, new HashMap<EventNode, Boolean>());
        assertFalse(tc2.equals(tc.getTC()));
    }

    /**
     * Missing link case
     */
    @Test
    public void constructorSimple2Test() {
        Graph<EventNode> g = new Graph<EventNode>();
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

        assertTrue(tc.getTC().containsKey(a) && tc.getTC().get(a).get(b));
        assertTrue(tc.getTC().containsKey(c) && tc.getTC().get(c).get(d));

        assertFalse(tc.getTC().get(a).containsKey(c));
        assertFalse(tc.getTC().get(a).containsKey(d));
        assertFalse(tc.getTC().get(c).containsKey(b));
        assertFalse(tc.getTC().get(c).containsKey(a));
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
        Graph<EventNode> g = new Graph<EventNode>();
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
                assertTrue(tc.getTC().get(z).containsKey(y));
                assertTrue(tc.getTC().get(z).get(y));
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
        Graph<EventNode> g = new Graph<EventNode>();
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

        HashMap<EventNode, HashMap<EventNode, Boolean>> tc2 = new HashMap<EventNode, HashMap<EventNode, Boolean>>();
        tc2.put(a, new HashMap<EventNode, Boolean>());
        tc2.get(a).put(c, true);
        tc2.get(a).put(b, true);
        tc2.get(a).put(d, true);

        tc2.put(b, new HashMap<EventNode, Boolean>());
        tc2.get(b).put(d, true);

        assertTrue(tc2.equals(tc.getTC()));

        tc2.put(d, new HashMap<EventNode, Boolean>());
        assertFalse(tc2.equals(tc.getTC()));
    }

    @Test
    public void constructorSimple5Test() {
        Graph<EventNode> g = new Graph<EventNode>();
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
        Graph<EventNode> g = new Graph<EventNode>();
        EventNode a = new EventNode(new Event("a"));

        a.addTransition(new Transition<EventNode>(a, a, "followed by"));

        g.add(a);

        TransitiveClosure<EventNode> tc = new TransitiveClosure<EventNode>(g,
                "followed by");
        assertTrue(tc.getTC().containsKey(a) && tc.getTC().get(a).get(a));
        assertEquals(1, tc.getTC().size());
        assertEquals(1, tc.getTC().get(a).size());
    }

    @Test
    public void isReachableSimpleTest() {
        Graph<EventNode> g = new Graph<EventNode>();
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
        Graph<EventNode> g = new Graph<EventNode>();
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

        Graph<EventNode> g = new Graph<EventNode>();
        g.add(a);
        g.add(b);
        g.add(c);
        g.add(d);
        TransitiveClosure<EventNode> tc = new TransitiveClosure<EventNode>(g,
                "followed by");

        Graph<EventNode> g2 = new Graph<EventNode>();
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

        Graph<EventNode> g = new Graph<EventNode>();
        g.add(a);
        g.add(b);
        g.add(c);
        g.add(d);
        TransitiveClosure<EventNode> tc = new TransitiveClosure<EventNode>(g,
                "followed by");

        Graph<EventNode> g2 = new Graph<EventNode>();
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

        Graph<EventNode> g = new Graph<EventNode>();
        g.add(a);
        g.add(b);
        g.add(c);
        g.add(d);
        TransitiveClosure<EventNode> tc = new TransitiveClosure<EventNode>(g,
                "followed by");

        Graph<EventNode> g2 = new Graph<EventNode>();
        g2.add(a);
        g2.add(b);
        g2.add(c);
        g2.add(d);
        TransitiveClosure<EventNode> tc2 = new TransitiveClosure<EventNode>(g2,
                "after");

        assertFalse(tc2.isEqual(tc));

    }
}