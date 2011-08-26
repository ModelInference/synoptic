package synoptic.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import synoptic.algorithms.graph.TransitiveClosure;
import synoptic.model.interfaces.ITransition;

public class DAGsTraceGraph extends TraceGraph<DistEventType> {
    static Event initEvent = Event.newInitialDistEvent();
    static Event termEvent = Event.newTerminalDistEvent();

    /**
     * Maintains a map of trace id to the set of initial nodes in the trace.
     */
    private final Map<Integer, Set<EventNode>> traceIdToInitNodes = new LinkedHashMap<Integer, Set<EventNode>>();

    public DAGsTraceGraph(Collection<EventNode> nodes) {
        super(nodes);
    }

    public DAGsTraceGraph() {
        super();
    }

    public Map<Integer, Set<EventNode>> getTraceIdToInitNodes() {
        return traceIdToInitNodes;
    }

    public void tagTerminal(EventNode terminalNode, String relation) {
        createIfNotExistsDummyTerminalNode(termEvent, relation);
        super.tagTerminal(terminalNode, relation);
    }

    public void tagInitial(EventNode initialNode, String relation) {
        createIfNotExistsDummyInitialNode(initEvent, relation);
        super.tagInitial(initialNode, relation);

        /**
         * Build a map of trace id to the set of initial nodes in the trace.
         * This is used for partially ordered traces, where it is not possible
         * to determine which initial nodes (pointed to from the dummy initial)
         * are in the same trace.
         */
        Integer tid = initialNode.getTraceID();
        Set<EventNode> initTraceNodes;
        if (!traceIdToInitNodes.containsKey(tid)) {
            initTraceNodes = new LinkedHashSet<EventNode>();
            traceIdToInitNodes.put(tid, initTraceNodes);
        } else {
            initTraceNodes = traceIdToInitNodes.get(tid);
        }
        initTraceNodes.add(initialNode);
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
        return goralcikovaAlg(relation);
    }

    /**
     * <pre>
     * The algorithm by Goralcikova and Koubek:
     * "A reduct-and-closure algorithm for graphs" by Alla Goralcikova
     * and Vaclav Koubek. MATHEMATICAL FOUNDATIONS OF COMPUTER SCIENCE 1979
     * Lecture Notes in Computer Science, 1979, Volume 74/1979, 301-307,
     * DOI: 10.1007/3-540-09526-8_27
     * 
     * A more modern/concise description can be found in:
     * "An improved algorithm for transitive closure on acyclic digraphs" by
     * Klaus Simon, AUTOMATA, LANGUAGES AND PROGRAMMING Lecture Notes in
     * Computer Science, 1986, Volume 226/1986, 376-386,
     * DOI: 10.1007/3-540-16761-7_87
     * </pre>
     */
    @SuppressWarnings("unused")
    private TransitiveClosure goralcikovaAlg(String relation) {
        TransitiveClosure transClosure = new TransitiveClosure(relation);
        Map<EventNode, Set<EventNode>> tc = transClosure.getTC();

        List<EventNode> sortedNodes = new LinkedList<EventNode>();

        // 1. Get the nodes sorted in some topological order, and at the same
        // time construct a reverse graph -- the graph that is formed by
        // reversing all the edges.

        // Maps a node to its parents in the transitive closure. This map is
        // cleared whenever we start processing a new DAG/Chain (a new PO/TO
        // trace).
        HashMap<EventNode, HashSet<EventNode>> tcParents = new HashMap<EventNode, HashSet<EventNode>>();

        Map<EventNode, Integer> parentsCountMap = new LinkedHashMap<EventNode, Integer>();
        Set<EventNode> bfsPerimeter = new LinkedHashSet<EventNode>();
        List<EventNode> topoOrder = new LinkedList<EventNode>();

        // Logger logger = Logger.getLogger("TransitiveClosure Logger");
        for (Set<EventNode> dagInits : traceIdToInitNodes.values()) {
            // logger.info("dagInits: " + dagInits.toString());
            // Traverse the trace, starting from the nodes dagInits to determine
            // the number of parents that each node has.
            bfsPerimeter.addAll(dagInits);
            while (bfsPerimeter.size() != 0) {
                for (EventNode m : bfsPerimeter) {
                    addToBFSPerimeter(bfsPerimeter, m, parentsCountMap);
                    bfsPerimeter.remove(m);
                    break;
                }
            }
            bfsPerimeter.clear();

            // Traverse the trace, respecting topological order, and build up
            // topoOrder list.
            for (EventNode m : dagInits) {
                addToTopoOrder(topoOrder, m, parentsCountMap);
                addToBFSPerimeter(bfsPerimeter, m);
            }

            // boolean here = false;
            while (bfsPerimeter.size() != 0) {
                for (EventNode m : bfsPerimeter) {
                    if (parentsCountMap.get(m) == 0) {
                        addToTopoOrder(topoOrder, m, parentsCountMap);
                        bfsPerimeter.remove(m);
                        addToBFSPerimeter(bfsPerimeter, m);
                        // here = true;
                        break;
                    }
                }
            }

            // 2. Traverse the reverse graph from the terminal nodes, building
            // up the transitive relation for a node in an order specified by
            // the topological order of the node's children (see paper for more
            // details).

            // Traverse the trace in a topological order, building up the
            // transitive closure.

            // descendingIterator()
            for (EventNode m : topoOrder) {
                // logger.fine("tc map is: " + tc.toString());
                // logger.fine("Handling node " + m.toString());
                Iterator<? extends ITransition<EventNode>> transIter = m
                        .getTransitionsIterator(relation);
                /**
                 * Iterate through all children of m and for each child do 2
                 * things:
                 * 
                 * <pre>
                 * 1. create a tc link between m and child and add m to tcParents[child]
                 * 2. create a tc link between m and every node n to which child is already
                 *    linked to in tc and add m to tcParents[n]
                 * </pre>
                 */
                while (transIter.hasNext()) {
                    // Create new tc map for node m.
                    if (!tc.containsKey(m)) {
                        tc.put(m, new LinkedHashSet<EventNode>());
                    }

                    EventNode child = transIter.next().getTarget();

                    if (!tcParents.containsKey(child)) {
                        tcParents.put(child, new HashSet<EventNode>());
                    }

                    // Link m to c
                    tc.get(m).add(child);
                    tcParents.get(child).add(m);

                    // Link m to all nodes that c is linked to in tc
                    if (tc.containsKey(child)) {
                        // m can reach nodes the child can reach transitively:
                        tc.get(m).addAll(tc.get(child));
                        // nodes that child can reach have m as a tc parent:
                        for (EventNode n : tc.get(child)) {
                            if (!tcParents.containsKey(n)) {
                                tcParents.put(n, new HashSet<EventNode>());
                            }
                            tcParents.get(n).add(m);
                        }
                    }
                }

                /**
                 * Now that we're done compiling the downward transitive closure
                 * of m, its time to push that information to m's parent nodes.
                 * For each tc parent p of m we do 2 things:
                 * 
                 * <pre>
                 * 1. For each node n in tc of m, add tc link between p and n
                 * 2. For each node n in tc of m, add p to tcParents[n]
                 * </pre>
                 */
                if (tcParents.containsKey(m) && tc.containsKey(m)) {
                    for (EventNode p : tcParents.get(m)) {
                        // P has a tc entry because its already part of
                        // tcParents of m (so we've already processed it)
                        // previously.
                        tc.get(p).addAll(tc.get(m));
                        for (EventNode n : tc.get(m)) {
                            // n has a tcParents entry because m is a tc parent
                            // of n and it must have been set above.
                            tcParents.get(n).add(p);
                        }
                    }
                }
            }
            // We do not need tcParents information between traces.
            tcParents.clear();
            topoOrder.clear();
            bfsPerimeter.clear();
            parentsCountMap.clear();
        }
        // logger.fine("FINAL tc map is: " + tc.toString());
        return transClosure;
    }

    private void addToBFSPerimeter(Set<EventNode> bfsPerimeter, EventNode node,
            Map<EventNode, Integer> parentsCountMap) {
        for (ITransition<EventNode> trans : node.getTransitions()) {
            EventNode dest = trans.getTarget();
            if (dest.isTerminal()) {
                continue;
            }
            if (!parentsCountMap.containsKey(dest)) {
                parentsCountMap.put(dest, new Integer(1));
            } else {
                parentsCountMap.put(dest, parentsCountMap.get(dest) + 1);
            }
            bfsPerimeter.add(dest);
        }
    }

    private void addToBFSPerimeter(Set<EventNode> bfsPerimeter, EventNode node) {
        for (ITransition<EventNode> trans : node.getTransitions()) {
            EventNode dest = trans.getTarget();
            if (dest.isTerminal()) {
                continue;
            }
            bfsPerimeter.add(dest);
        }
    }

    private void addToTopoOrder(List<EventNode> topoOrder, EventNode node,
            Map<EventNode, Integer> parentsCountMap) {
        topoOrder.add(node);
        for (ITransition<EventNode> trans : node.getTransitions()) {
            EventNode dest = trans.getTarget();
            if (dest.isTerminal()) {
                continue;
            }
            parentsCountMap.put(dest, parentsCountMap.get(dest) - 1);
        }
    }
}
