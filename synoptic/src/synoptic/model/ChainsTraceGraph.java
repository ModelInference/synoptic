package synoptic.model;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import synoptic.algorithms.graph.FloydWarshall;
import synoptic.algorithms.graph.TransitiveClosure;

public class ChainsTraceGraph extends TraceGraph<StringEventType> {
    static Event initEvent = Event.newInitialStringEvent();
    static Event termEvent = Event.newTerminalStringEvent();

    /**
     * Maintains a map of trace id to the set of initial nodes in the trace.
     */
    private final Map<Integer, EventNode> traceIdToInitNodes = new LinkedHashMap<Integer, EventNode>();

    public ChainsTraceGraph(Collection<EventNode> nodes) {
        super(nodes);
    }

    public ChainsTraceGraph() {
        super();
    }

    public void tagTerminal(EventNode terminalNode, String relation) {
        // add(terminalNode);
        createIfNotExistsDummyTerminalNode(termEvent, relation);
        super.tagTerminal(terminalNode, relation);
    }

    public void tagInitial(EventNode initialNode, String relation) {
        // add(initialNode);
        createIfNotExistsDummyInitialNode(initEvent, relation);
        super.tagInitial(initialNode, relation);
        traceIdToInitNodes.put(initialNode.getTraceID(), initialNode);
    }

    /**
     * Returns the number of trace ids that are immediately reachable from the
     * initNode -- this is useful for PO traces since the number of transitions
     * from the initial node is not necessarily the number of traces since it
     * might be connected to two nodes in the same trace (that were concurrent
     * at start).
     */
    public int getNumTraces() {
        return traceIdToInitNodes.size();
    }

    public TransitiveClosure getTransitiveClosure(String relation) {
        return FloydWarshall.warshallAlg(this, relation);
    }

    // Used by tests only (so that DAGWalking invariant miner can operate on
    // ChainsTraceGraph)
    public Map<Integer, Set<EventNode>> getTraceIdToInitNodes() {
        Map<Integer, Set<EventNode>> map = new LinkedHashMap<Integer, Set<EventNode>>();
        for (Integer k : traceIdToInitNodes.keySet()) {
            Set<EventNode> set = new LinkedHashSet<EventNode>();
            set.add(traceIdToInitNodes.get(k));
            map.put(k, set);
        }
        return map;
    }

}
