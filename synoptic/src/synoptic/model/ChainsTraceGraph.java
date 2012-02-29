package synoptic.model;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import synoptic.algorithms.graph.TransitiveClosure;

/**
 * This structure holds all the totally ordered executions extracted from the
 * input log(s). Each of these executions is a linear "chain" graph. The
 * ChainsTraceGraph contains a root node INITIAL, which has an edge to the first
 * node in each of these chain traces. It also contains a TERMINAL node that has
 * an edge from the last node in each of the chain traces.
 */
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
        createIfNotExistsDummyTerminalNode(termEvent, relation);
        super.tagTerminal(terminalNode, relation);
    }

    public void tagInitial(EventNode initialNode, String relation) {
        createIfNotExistsDummyInitialNode(initEvent, relation);
        super.tagInitial(initialNode, relation);
        traceIdToInitNodes.put(initialNode.getTraceID(), initialNode);
    }
    
    /**
     * Creates transitions from INITIAL to initialNode for each string in 
     * the relations collection.
     * @param initialNode
     * @param relations
     */
    public void tagInitial(EventNode initialNode, Collection<String> relations) {
    	for (String relation : relations) {
    		tagInitial(initialNode, relation);
    	}
    }

    /**
     * Returns the number of trace ids that are immediately reachable from the
     * initNode.
     */
    public int getNumTraces() {
        return traceIdToInitNodes.size();
    }

    /**
     * Transitive closure construction for a ChainsTraceGraph is simple: iterate
     * through each chain independently and add all successors of a node in a
     * chain to it's transitive closure set. <br/>
     * <br/>
     * NOTE: an assumption of this code is that although there might be multiple
     * relations, the graph remains a linear chain.
     */
    public TransitiveClosure getTransitiveClosure(String relation) {
        assert super.dummyInitialNodes.size() != 0;
        assert super.dummyTerminalNodes.size() != 0;

        TransitiveClosure transClosure = new TransitiveClosure(relation);
        List<EventNode> prevNodes = new LinkedList<EventNode>();
        for (EventNode firstNode : traceIdToInitNodes.values()) {
            EventNode curNode = firstNode;

            while (!curNode.isTerminal()) {
                while (curNode.getTransitions(relation).size() != 0) {
                    for (EventNode prevNode : prevNodes) {
                        transClosure.addReachable(prevNode, curNode);
                    }
                    prevNodes.add(curNode);
                    curNode = curNode.getTransitions(relation).get(0)
                            .getTarget();
                }

                if (!curNode.isTerminal()) {
                    for (EventNode prevNode : prevNodes) {
                        transClosure.addReachable(prevNode, curNode);
                    }
                }

                prevNodes.clear();

                if (!curNode.isTerminal()) {
                    curNode = curNode.getTransitions().get(0).getTarget();
                }
            }
        }
        return transClosure;
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
