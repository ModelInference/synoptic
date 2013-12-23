package dynoptic.main;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import dynoptic.DynopticTest;

import synoptic.main.SynopticMain;
import synoptic.model.DAGsTraceGraph;
import synoptic.model.EventNode;
import synoptic.model.Transition;
import synoptic.model.channelid.ChannelId;
import synoptic.model.event.DistEventType;
import synoptic.model.event.Event;

public class SynDagsToDynDagsTests extends DynopticTest {

    public DynopticMain dyn;
    public DynopticOptions opts;

    protected DAGsTraceGraph synDag;

    // Channel: 0 -> 1
    protected ChannelId cid01 = new ChannelId(0, 1, 0);
    // Channel: 1 -> 0
    protected ChannelId cid10 = new ChannelId(1, 0, 0);

    protected EventNode send01A = new EventNode(new Event(
            DistEventType.SendEvent("M!a", cid01)));
    protected EventNode b0 = new EventNode(new Event(DistEventType.LocalEvent(
            "b", 0)));
    protected EventNode recv01A = new EventNode(new Event(
            DistEventType.RecvEvent("M?a", cid01)));
    protected EventNode d0 = new EventNode(new Event(DistEventType.LocalEvent(
            "d", 0)));

    protected EventNode e = new EventNode(new Event("e"));
    protected EventNode f = new EventNode(new Event("f"));

    @Before
    public void setUp() throws Exception {
        super.setUp();
        // Reset the SynopticMain singleton reference.
        SynopticMain.instance = null;

        List<String> args = new ArrayList<String>();
        args.add("-v");
        args.add(super.getMcPath());
        args.add("-o");
        args.add("test-output" + File.separator);
        args.add("-q");
        args.add("M:0->1");
        opts = new DynopticOptions(args.toArray(new String[0]));
        dyn = new DynopticMain(opts);
        dyn.initializeSynoptic();
    }

    /**
     * Builds the DAG:
     * 
     * <pre>
     * 0: M!a -> b -> d
     * 1:  \---> M?a
     * </pre>
     */
    public DAGsTraceGraph buildForkDAG() {
        DAGsTraceGraph forkGraph = new DAGsTraceGraph();
        send01A.addTransition(new Transition<EventNode>(send01A, b0, "t"));
        send01A.addTransition(new Transition<EventNode>(send01A, recv01A, "t"));
        b0.addTransition(new Transition<EventNode>(b0, d0, "t"));
        forkGraph.add(send01A);
        forkGraph.add(b0);
        forkGraph.add(recv01A);
        forkGraph.add(d0);
        forkGraph.tagInitial(send01A, "t");
        forkGraph.tagTerminal(d0, "t");
        forkGraph.tagTerminal(recv01A, "t");
        return forkGraph;
    }

    @Test
    public void convert() {
        synDag = buildForkDAG();
        ArrayList<EventNode> parsedEvents = new ArrayList<EventNode>();
        parsedEvents.addAll(Arrays.asList(new EventNode[] { send01A, b0,
                recv01A, d0 }));
        // dyn.synTraceGraphToDynObsFifoSys(synDag, 2, parsedEvents);
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
        send01A.addTransition(new Transition<EventNode>(send01A, b0, "t"));
        send01A.addTransition(new Transition<EventNode>(send01A, recv01A, "t"));
        b0.addTransition(new Transition<EventNode>(b0, d0, "t"));
        recv01A.addTransition(new Transition<EventNode>(recv01A, d0, "t"));
        d0.addTransition(new Transition<EventNode>(d0, e, "t"));
        d0.addTransition(new Transition<EventNode>(d0, f, "t"));
        forkGraph.add(send01A);
        forkGraph.add(b0);
        forkGraph.add(recv01A);
        forkGraph.add(d0);
        forkGraph.add(e);
        forkGraph.add(f);
        forkGraph.tagInitial(send01A, "t");
        forkGraph.tagTerminal(e, "t");
        forkGraph.tagTerminal(f, "t");
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
        send01A.addTransition(new Transition<EventNode>(send01A, recv01A, "t"));
        b0.addTransition(new Transition<EventNode>(b0, recv01A, "t"));
        recv01A.addTransition(new Transition<EventNode>(recv01A, d0, "t"));
        forkGraph.add(send01A);
        forkGraph.add(b0);
        forkGraph.add(recv01A);
        forkGraph.add(d0);
        forkGraph.tagInitial(send01A, "t");
        forkGraph.tagInitial(b0, "t");
        forkGraph.tagTerminal(d0, "t");
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
        send01A.addTransition(new Transition<EventNode>(send01A, recv01A, "t"));
        send01A.addTransition(new Transition<EventNode>(send01A, d0, "t"));
        b0.addTransition(new Transition<EventNode>(b0, d0, "t"));
        b0.addTransition(new Transition<EventNode>(b0, e, "t"));
        recv01A.addTransition(new Transition<EventNode>(recv01A, f, "t"));
        d0.addTransition(new Transition<EventNode>(d0, f, "t"));
        forkGraph.add(send01A);
        forkGraph.add(b0);
        forkGraph.add(recv01A);
        forkGraph.add(d0);
        forkGraph.add(e);
        forkGraph.add(f);
        forkGraph.tagInitial(send01A, "t");
        forkGraph.tagInitial(b0, "t");
        forkGraph.tagTerminal(f, "t");
        forkGraph.tagTerminal(e, "t");
        return forkGraph;
    }

}
