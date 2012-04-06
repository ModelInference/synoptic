package synoptic.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import synoptic.algorithms.graph.TransitiveClosure;
import synoptic.main.ParseException;

/**
 * This structure holds all the totally ordered executions extracted from the
 * input log(s). Each of these executions is a linear "chain" graph. The
 * ChainsTraceGraph contains a root node INITIAL, which has an edge to the first
 * node in each of these chain traces. It also contains a TERMINAL node that has
 * an edge from the last node in each of the chain traces. With the addition of
 * multiple relations, there can be chains composed of disconnected graphs over
 * non-temporal relations that are transitively closed and made connected
 * through the time relation. Multiple relations are interposed with existing
 * connected time chains.
 */
public class ChainsTraceGraph extends TraceGraph<StringEventType> {
    static Event initEvent = Event.newInitialStringEvent();
    static Event termEvent = Event.newTerminalStringEvent();

    /**
     * Maintains a map of trace id to the set of initial nodes in the trace.
     */
    private final Map<Integer, EventNode> traceIdToInitNodes = new LinkedHashMap<Integer, EventNode>();

    private final List<Trace> traces = new ArrayList<Trace>();

    public ChainsTraceGraph(Collection<EventNode> nodes) {
        super(nodes, initEvent, termEvent);
    }

    public ChainsTraceGraph() {
        super(initEvent, termEvent);
    }

    public void tagTerminal(EventNode terminalNode, Set<String> relations) {
        super.tagTerminal(terminalNode, relations);
    }

    public void tagTerminal(EventNode terminalNode, String relation) {
        super.tagTerminal(terminalNode, relation);
    }

    public void tagInitial(EventNode initialNode, String relation) {
        Set<String> relations = new LinkedHashSet<String>();
        relations.add(relation);
        this.tagInitial(initialNode, relations);
    }

    public void tagInitial(EventNode initialNode, Set<String> relations) {
        super.tagInitial(initialNode, relations);
        traceIdToInitNodes.put(initialNode.getTraceID(), initialNode);
    }

    /**
     * Adds the event nodes to the graph and creates appropriate transitions for
     * regular and closure relations. Also generates Trace and RelationPath data
     * structures.
     * 
     * @param events
     *            List of EventNodes in trace order
     * @throws ParseException
     *             if two events have identical timestamp.
     */
    public void addTrace(List<EventNode> events) throws ParseException {
        assert events.size() > 0;

        // Sort the events in this group/trace.
        Collections.sort(events, new Comparator<EventNode>() {
            @Override
            public int compare(EventNode e1, EventNode e2) {
                return e1.getTime().compareTo(e2.getTime());
            }
        });

        Trace trace = new Trace();
        traces.add(trace);

        Map<String, EventNode> lastSeenNodeForRelation = new HashMap<String, EventNode>();
        EventNode firstNode = events.get(0);
        EventNode prevNode = null;

        /**
         * <pre>
         * Process first node's relations:
         * - Adds relation to list of relations
         * - Tags node as initial over relation
         * - Marks node as the last node seen over the relation
         * - Adds a RelationPath to the Trace
         * 
         * In this case, there is an edge of type relation from INITIAL
         * to curNode
         * </pre>
         */
        // for (Relation relation : prevNode.getEventRelations()) {
        // lastSeenNodeForRelation.put(relation.getRelation(), prevNode);
        // trace.addRelationPath(relation.getRelation(), prevNode, false);
        // }
        // tagInitial(prevNode, prevNode.getEventStringRelations());

        // Create transitions to connect the nodes in the sorted trace.
        // for (EventNode curNode : events.subList(1, events.size())) {
        for (EventNode curNode : events) {

            if (prevNode != null
                    && prevNode.getTime().equals(curNode.getTime())) {
                String error = "Found two events with identical timestamps: (1) "
                        + prevNode.toString() + " (2) " + curNode.toString();
                logger.severe(error);
                throw new ParseException(error);
            }

            // Process node's relations:
            Map<EventNode, Set<String>> srcNodeToTxRelations = new LinkedHashMap<EventNode, Set<String>>();

            for (Relation relation : curNode.getEventRelationsObjects()) {

                EventNode txNode;
                if (relation.isClosure()) {
                    // Closure relations create transitions between the current
                    // node and the last seen node for the relation.
                    txNode = lastSeenNodeForRelation
                            .get(relation.getRelation());
                } else {
                    // Otherwise, the transition is from the previous node in
                    // the chain.
                    txNode = prevNode;
                }

                // Add the relation to set of relations associated with the
                // transition from txNode (if one exists). If one doesn't exist,
                // then create a new set of relations.
                Set<String> txRelations;
                if (!srcNodeToTxRelations.containsKey(txNode)) {
                    txRelations = new LinkedHashSet<String>();
                    srcNodeToTxRelations.put(txNode, txRelations);
                } else {
                    txRelations = srcNodeToTxRelations.get(txNode);
                }
                txRelations.add(relation.getRelation());
            }

            // Create a transition for each node that should be connected to
            // curNode.
            for (EventNode srcNode : srcNodeToTxRelations.keySet()) {
                Set<String> relations = srcNodeToTxRelations.get(srcNode);

                if (srcNode == null) {
                    // In this case, the srcNode is considered to be INITIAL, so
                    // we tag curNode as initial and add a new relation path to
                    // the trace.
                    tagInitial(curNode, relations);
                    boolean initialConnected = (curNode == firstNode);

                    for (String r : relations) {
                        trace.addRelationPath(r, curNode, initialConnected);
                    }
                } else {
                    // Otherwise, there is a specific previous srcNode, and we
                    // connect curNode to this node.
                    srcNode.addTransition(curNode, relations);
                }

                // Update the lastSeednNodeForRelation map.
                for (String r : relations) {
                    lastSeenNodeForRelation.put(r, curNode);
                }
            }
            prevNode = curNode;
        }

        // Tag the final node as terminal:
        tagTerminal(prevNode, prevNode.getEventRelationsStrings());

        /*
         * If trace doesn't contain a relation path for a relation r, then r was
         * seen while the trace was traversed, and there are no closure
         * relations of type r. This means there are subgraphs of r which are
         * transitively connected, as opposed to directly connected, to the
         * initial node. Currently, these relations are transitively connected
         * over time.
         * 
         * In other words, there isn't an edge of type relation from INITIAL to
         * the first node in the relation subgraph.
         */
        // for (String relation : relations) {
        // if (!trace.hasRelation(relation)) {
        // trace.addRelationPath(relation, firstNode, true);
        // }
        // }

        /*
         * Bound existing traces. Some relation paths are not non-transitively
         * connected to the terminal node and do not need to be counted beyond
         * the last node containing the given relation type.
         */
        for (String relation : lastSeenNodeForRelation.keySet()) {
            EventNode finalNode = lastSeenNodeForRelation.get(relation);
            trace.markRelationPathFinalNode(relation, finalNode);
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
    public TransitiveClosure getTransitiveClosure(Set<String> relations) {
        assert relations != null;

        TransitiveClosure transClosure = new TransitiveClosure(relations);
        List<EventNode> prevNodes = new LinkedList<EventNode>();
        for (EventNode firstNode : traceIdToInitNodes.values()) {
            EventNode curNode = firstNode;

            while (!curNode.isTerminal()) {
                while (curNode.getTransitionsWithExactRelations(relations)
                        .size() != 0) {
                    for (EventNode prevNode : prevNodes) {
                        transClosure.addReachable(prevNode, curNode);
                    }
                    prevNodes.add(curNode);
                    curNode = curNode
                            .getTransitionsWithExactRelations(relations).get(0)
                            .getTarget();
                }

                if (!curNode.isTerminal()) {
                    for (EventNode prevNode : prevNodes) {
                        transClosure.addReachable(prevNode, curNode);
                    }
                }

                prevNodes.clear();

                if (!curNode.isTerminal()) {
                    curNode = curNode
                            .getTransitionsWithExactRelations(relations).get(0)
                            .getTarget();
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

    public List<Trace> getTraces() {
        return Collections.unmodifiableList(traces);
    }

}
