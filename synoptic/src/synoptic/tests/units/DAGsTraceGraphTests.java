package synoptic.tests.units;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import synoptic.algorithms.graph.TransitiveClosure;
import synoptic.main.ParseException;
import synoptic.model.DAGsTraceGraph;
import synoptic.model.Event;
import synoptic.model.EventNode;
import synoptic.model.Transition;
import synoptic.tests.SynopticTest;

/**
 * Tests for the DAGsTraceGraph.
 */
public class DAGsTraceGraphTests extends SynopticTest {

    DAGsTraceGraph g;
    EventNode a = new EventNode(new Event("a"));
    EventNode b = new EventNode(new Event("b"));
    EventNode c = new EventNode(new Event("c"));
    EventNode d = new EventNode(new Event("d"));

    @Override
    public void setUp() throws ParseException {
        super.setUp();
    }

    /***********************************************************************/

    /**
     * Tests the TC of a DAG that looks like:
     * 
     * <pre>
     * a -> b -> d
     *  \-> c
     * </pre>
     */
    @Test
    public void forkGraphTCTest() {
        g = new DAGsTraceGraph();

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
}
