package synoptic.tests.units;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import synoptic.algorithms.FloydWarshall;
import synoptic.algorithms.TransitiveClosure;
import synoptic.model.ChainsTraceGraph;
import synoptic.model.EventNode;
import synoptic.model.event.Event;
import synoptic.model.interfaces.IGraph;
import synoptic.tests.SynopticTest;

/**
 * Test for the FloydWarshall algorithm.
 */
public class FloydWarshallTests extends SynopticTest {

    /**
     * Empty graph -- base case.
     */
    @Test
    public void listConstructorTest() {
        IGraph<EventNode> g = new ChainsTraceGraph();
        TransitiveClosure tc = FloydWarshall.warshallAlg(g,
                Event.defTimeRelationSet);
        assertTrue(tc.getTC().keySet().size() == 0);
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
}
