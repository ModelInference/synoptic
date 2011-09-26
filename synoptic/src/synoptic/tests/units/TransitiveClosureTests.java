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
import synoptic.model.ChainsTraceGraph;
import synoptic.model.DAGsTraceGraph;
import synoptic.model.Event;
import synoptic.model.EventNode;
import synoptic.model.Transition;
import synoptic.tests.SynopticTest;

/**
 * Tests for synoptic.algorithms.graph.TransitiveClosure class.
 */
public class TransitiveClosureTests extends SynopticTest {
    /**
     * Test constructor for simple case: a->b->c->d should yield a
     * TransitiveClosure with a tc map: a->b, b->c, c->d, a->c, b->d, a->d and
     * no others.
     */
    @Test
    public void constructorSimpleTest() {
        ChainsTraceGraph g = new ChainsTraceGraph();
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
        g.tagInitial(a, "followed by");
        g.tagTerminal(d, "followed by");

        TransitiveClosure tc = g.getTransitiveClosure("followed by");

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
        ChainsTraceGraph g = new ChainsTraceGraph();
        TransitiveClosure tc = g.getTransitiveClosure("followed by");
        assertTrue(tc.getTC().isEmpty());
    }

    @Test
    public void constructorNullRelationTest() {
        ChainsTraceGraph g = new ChainsTraceGraph();
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
        TransitiveClosure tc = g.getTransitiveClosure(null);
        assertTrue(tc.getTC().isEmpty());
    }

    @Test
    public void constructorTCCase() {
        ChainsTraceGraph g = new ChainsTraceGraph();
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

        g.tagInitial(a, "followed by");
        g.tagTerminal(c, "after");
        g.tagTerminal(d, "followed by");

        TransitiveClosure tc = g.getTransitiveClosure("followed by");

        Map<EventNode, Set<EventNode>> tc2 = new LinkedHashMap<EventNode, Set<EventNode>>();
        tc2.put(a, new LinkedHashSet<EventNode>());
        tc2.get(a).add(b);
        tc2.put(c, new LinkedHashSet<EventNode>());
        tc2.get(c).add(d);
        logger.info("built tc: " + tc.getTC().toString());
        assertTrue(tc2.equals(tc.getTC()));

        tc2.put(d, new LinkedHashSet<EventNode>());
        assertFalse(tc2.equals(tc.getTC()));
    }

    /**
     * Missing link case
     */
    @Test
    public void constructorSimple2Test() {
        ChainsTraceGraph g = new ChainsTraceGraph();
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

        g.tagInitial(a, "followed by");
        g.tagTerminal(d, "followed by");
        g.tagTerminal(c, "after");
        TransitiveClosure tc = g.getTransitiveClosure("followed by");

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
    /*
     * @Test public void constructorSimple3Test() { TraceGraph g = new
     * TraceGraph(); EventNode a = new EventNode(new Event("a")); EventNode b =
     * new EventNode(new Event("b")); EventNode c = new EventNode(new
     * Event("c")); EventNode d = new EventNode(new Event("d"));
     * 
     * a.addTransition(new Transition<EventNode>(a, b, "followed by"));
     * b.addTransition(new Transition<EventNode>(b, c, "followed by"));
     * c.addTransition(new Transition<EventNode>(c, d, "followed by"));
     * d.addTransition(new Transition<EventNode>(d, a, "followed by"));
     * 
     * g.add(a); g.add(b); g.add(c); g.add(d);
     * 
     * Event dummyAct = Event.newInitialStringEvent(); g.setDummyInitial(new
     * EventNode(dummyAct), "followed by"); g.tagInitial(a, "followed by");
     * TransitiveClosure tc = new TransitiveClosure(g, "followed by");
     * 
     * for (EventNode z : g.getNodes()) { for (EventNode y : g.getNodes()) {
     * assertTrue(tc.getTC().containsKey(z)); logger.fine("testing for key z=" +
     * z.toString() + " and key y=" + y.toString());
     * assertTrue(tc.getTC().get(z).contains(y)); } }
     * 
     * assertEquals(4, tc.getTC().size()); assertEquals(4,
     * tc.getTC().get(a).size()); assertEquals(4, tc.getTC().get(b).size());
     * assertEquals(4, tc.getTC().get(c).size()); assertEquals(4,
     * tc.getTC().get(d).size());
     * 
     * }
     */

    /*
     * @Test public void expListRetainAllSet() { List<String> l = new
     * LinkedList<String>(); l.add(new String("a")); l.add(new String("b"));
     * l.add(new String("c")); l.add(new String("d"));
     * 
     * Set<String> s = new LinkedHashSet<String>(); s.add(new String("b"));
     * s.add(new String("d")); l.retainAll(s);
     * 
     * List<String> expected = new LinkedList<String>(); l.add(new String("a"));
     * l.add(new String("c"));
     * 
     * assertTrue(l.equals(expected)); }
     */

    @Test
    public void constructorSimple4Test() {
        DAGsTraceGraph g = new DAGsTraceGraph();
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

        g.tagInitial(a, "followed by");
        g.tagTerminal(d, "followed by");
        g.tagTerminal(c, "followed by");

        TransitiveClosure tc = g.getTransitiveClosure("followed by");

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

    // NOTE: this next test is invalid since ChainTraceGraph construction is
    // constrained to constructing fully formed chains.

    // @Test
    // public void constructorSimple5Test() {
    // ChainsTraceGraph g = new ChainsTraceGraph();
    // EventNode a = new EventNode(new Event("a"));
    // EventNode b = new EventNode(new Event("b"));
    // EventNode c = new EventNode(new Event("c"));
    // g.add(a);
    // g.add(b);
    // g.add(c);
    //
    // g.tagInitial(a, TraceParser.defaultRelation);
    // g.tagTerminal(c, TraceParser.defaultRelation);
    //
    // TransitiveClosure tc = g.getTransitiveClosure("followed by");
    // assertEquals(0, tc.getTC().size());
    //
    // a.addTransition(new Transition<EventNode>(a, b, "followed by"));
    // a.addTransition(new Transition<EventNode>(a, c, "followed by"));
    // TransitiveClosure tc2 = g.getTransitiveClosure("after");
    // assertEquals(0, tc2.getTC().size());
    //
    // }

    // @Test
    // public void constructorSelfTest() {
    // TraceGraph g = new TraceGraph();
    // EventNode a = new EventNode(new Event("a"));
    //
    // a.addTransition(new Transition<EventNode>(a, a, "followed by"));
    //
    // g.add(a);
    //
    // TransitiveClosure tc = new TransitiveClosure(g, "followed by");
    // assertTrue(tc.getTC().containsKey(a) && tc.getTC().get(a).contains(a));
    // assertEquals(1, tc.getTC().size());
    // assertEquals(1, tc.getTC().get(a).size());
    // }

    @Test
    public void isReachableSimpleTest() {
        ChainsTraceGraph g = new ChainsTraceGraph();
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
        g.tagInitial(a, "followed by");
        g.tagTerminal(d, "followed by");

        TransitiveClosure tc = g.getTransitiveClosure("followed by");

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

    // @Test
    // public void isReachableSelfTest() {
    // TraceGraph g = new TraceGraph();
    // EventNode a = new EventNode(new Event("a"));
    // a.addTransition(new Transition<EventNode>(a, a, "followed by"));
    //
    // g.add(a);
    //
    // TransitiveClosure tc = new TransitiveClosure(g, "followed by");
    // assertTrue(tc.isReachable(a, a));
    // }

    @Test
    public void isEqualSimpleTest() {
        EventNode a = new EventNode(new Event("a"));
        EventNode b = new EventNode(new Event("b"));
        EventNode c = new EventNode(new Event("c"));
        EventNode d = new EventNode(new Event("d"));

        a.addTransition(new Transition<EventNode>(a, b, "followed by"));
        b.addTransition(new Transition<EventNode>(b, c, "followed by"));
        c.addTransition(new Transition<EventNode>(c, d, "followed by"));

        ChainsTraceGraph g = new ChainsTraceGraph();
        g.add(a);
        g.add(b);
        g.add(c);
        g.add(d);
        TransitiveClosure tc = g.getTransitiveClosure("followed by");

        ChainsTraceGraph g2 = new ChainsTraceGraph();
        g2.add(a);
        g2.add(b);
        g2.add(c);
        g2.add(d);
        TransitiveClosure tc2 = g2.getTransitiveClosure("followed by");
        assertTrue(tc.isEqual(tc2));
    }

    // @Test
    // public void isEqualTest() {
    // EventNode a = new EventNode(new Event("a"));
    // EventNode b = new EventNode(new Event("b"));
    // EventNode c = new EventNode(new Event("c"));
    // EventNode d = new EventNode(new Event("d"));
    //
    // a.addTransition(new Transition<EventNode>(a, b, "followed by"));
    // b.addTransition(new Transition<EventNode>(b, c, "followed by"));
    // c.addTransition(new Transition<EventNode>(c, d, "followed by"));
    //
    // TraceGraph g = new TraceGraph();
    // g.add(a);
    // g.add(b);
    // g.add(c);
    // g.add(d);
    // TransitiveClosure tc = new TransitiveClosure(g, "followed by");
    //
    // TraceGraph g2 = new TraceGraph();
    // g2.add(a);
    // g2.add(b);
    // g2.add(c);
    // g2.add(d);
    // EventNode e = new EventNode(new Event("e"));
    // e.addTransition(new Transition<EventNode>(e, a, "followed by"));
    // g2.add(e);
    // TransitiveClosure tc2 = new TransitiveClosure(g2, "followed by");
    //
    // assertTrue(tc2.isReachable(e, b));
    //
    // assertFalse(tc2.isEqual(tc));
    //
    // assertFalse(tc.isEqual(tc2));
    //
    // }

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

        ChainsTraceGraph g = new ChainsTraceGraph();
        g.add(a);
        g.add(b);
        g.add(c);
        g.add(d);
        TransitiveClosure tc = g.getTransitiveClosure("followed by");

        ChainsTraceGraph g2 = new ChainsTraceGraph();
        g2.add(a);
        g2.add(b);
        g2.add(c);
        g2.add(d);
        TransitiveClosure tc2 = g2.getTransitiveClosure("after");

        assertFalse(tc2.isEqual(tc));

    }
}