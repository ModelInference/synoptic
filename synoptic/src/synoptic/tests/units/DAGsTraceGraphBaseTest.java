package synoptic.tests.units;

import synoptic.main.ParseException;
import synoptic.model.DAGsTraceGraph;
import synoptic.model.Event;
import synoptic.model.EventNode;
import synoptic.model.Transition;
import synoptic.tests.SynopticTest;

public class DAGsTraceGraphBaseTest extends SynopticTest {

    @Override
    public void setUp() throws ParseException {
        super.setUp();
    }

    DAGsTraceGraph dag;
    EventNode a = new EventNode(new Event("a"));
    EventNode b = new EventNode(new Event("b"));
    EventNode c = new EventNode(new Event("c"));
    EventNode d = new EventNode(new Event("d"));
    EventNode e = new EventNode(new Event("e"));
    EventNode f = new EventNode(new Event("f"));

    /**
     * Builds the DAG:
     * 
     * <pre>
     * a -> b -> d
     *  \-> c
     * </pre>
     */
    public DAGsTraceGraph buildForkDAG() {
        DAGsTraceGraph forkGraph = new DAGsTraceGraph();
        a.addTransition(new Transition<EventNode>(a, b, "followed by"));
        a.addTransition(new Transition<EventNode>(a, c, "followed by"));
        b.addTransition(new Transition<EventNode>(b, d, "followed by"));
        forkGraph.add(a);
        forkGraph.add(b);
        forkGraph.add(c);
        forkGraph.add(d);
        forkGraph.tagInitial(a, "followed by");
        forkGraph.tagTerminal(d, "followed by");
        forkGraph.tagTerminal(c, "followed by");
        return forkGraph;
    }

    /**
     * Builds the DAG:
     * 
     * <pre>
     * a -> b ---> d -> e
     *  \-> c -/    \-> f
     * </pre>
     */
    public DAGsTraceGraph buildComplexForkDAG() {
        DAGsTraceGraph forkGraph = new DAGsTraceGraph();
        a.addTransition(new Transition<EventNode>(a, b, "followed by"));
        a.addTransition(new Transition<EventNode>(a, c, "followed by"));
        b.addTransition(new Transition<EventNode>(b, d, "followed by"));
        c.addTransition(new Transition<EventNode>(c, d, "followed by"));
        d.addTransition(new Transition<EventNode>(d, e, "followed by"));
        d.addTransition(new Transition<EventNode>(d, f, "followed by"));
        forkGraph.add(a);
        forkGraph.add(b);
        forkGraph.add(c);
        forkGraph.add(d);
        forkGraph.add(e);
        forkGraph.add(f);
        forkGraph.tagInitial(a, "followed by");
        forkGraph.tagTerminal(e, "followed by");
        forkGraph.tagTerminal(f, "followed by");
        return forkGraph;
    }

    /**
     * Builds the DAG:
     * 
     * <pre>
     * a
     *  \
     *   --> c --> d
     *  /
     * b
     * </pre>
     */
    public DAGsTraceGraph buildTwoSourcesDAG() {
        DAGsTraceGraph forkGraph = new DAGsTraceGraph();
        a.addTransition(new Transition<EventNode>(a, c, "followed by"));
        b.addTransition(new Transition<EventNode>(b, c, "followed by"));
        c.addTransition(new Transition<EventNode>(c, d, "followed by"));
        forkGraph.add(a);
        forkGraph.add(b);
        forkGraph.add(c);
        forkGraph.add(d);
        forkGraph.tagInitial(a, "followed by");
        forkGraph.tagInitial(b, "followed by");
        forkGraph.tagTerminal(d, "followed by");
        return forkGraph;
    }

    /**
     * Builds the DAG:
     * 
     * <pre>
     * a --> c --
     *  \        \
     *   --> d --> f
     *  /
     * b --> e
     * </pre>
     */
    public DAGsTraceGraph buildTwoSourcesComplexDAG() {
        DAGsTraceGraph forkGraph = new DAGsTraceGraph();
        a.addTransition(new Transition<EventNode>(a, c, "followed by"));
        a.addTransition(new Transition<EventNode>(a, d, "followed by"));
        b.addTransition(new Transition<EventNode>(b, d, "followed by"));
        b.addTransition(new Transition<EventNode>(b, e, "followed by"));
        c.addTransition(new Transition<EventNode>(c, f, "followed by"));
        d.addTransition(new Transition<EventNode>(d, f, "followed by"));
        forkGraph.add(a);
        forkGraph.add(b);
        forkGraph.add(c);
        forkGraph.add(d);
        forkGraph.add(e);
        forkGraph.add(f);
        forkGraph.tagInitial(a, "followed by");
        forkGraph.tagInitial(b, "followed by");
        forkGraph.tagTerminal(f, "followed by");
        forkGraph.tagTerminal(e, "followed by");
        return forkGraph;
    }

}
