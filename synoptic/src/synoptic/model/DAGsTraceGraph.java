package synoptic.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import synoptic.algorithms.FloydWarshall;
import synoptic.algorithms.TransitiveClosure;
import synoptic.model.event.DistEventType;
import synoptic.model.event.Event;
import synoptic.model.interfaces.ITransition;

public class DAGsTraceGraph extends TraceGraph<DistEventType> {
    static Event initEvent = Event.newInitialDistEvent();
    static Event termEvent = Event.newTerminalDistEvent();

    /**
     * Maintains a map of trace id to the set of initial nodes in the trace.
     */
    private final Map<Integer, Set<EventNode>> traceIdToInitNodes = new LinkedHashMap<Integer, Set<EventNode>>();

    public DAGsTraceGraph(Collection<EventNode> nodes) {
        super(nodes, initEvent, termEvent);
    }

    public DAGsTraceGraph() {
        super(initEvent, termEvent);
    }

    public Map<Integer, Set<EventNode>> getTraceIdToInitNodes() {
        return traceIdToInitNodes;
    }

    public void tagInitial(EventNode initialNode, String relation) {
        Set<String> relations = new LinkedHashSet<String>();
        relations.add(relation);
        tagInitial(initialNode, relations);
    }

    public void tagInitial(EventNode initialNode, Set<String> relations) {
        super.tagInitial(initialNode, relations);

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

    public TransitiveClosure getTransitiveClosure(String relation,
            boolean useFloydWarshall) {
        Set<String> relations = new LinkedHashSet<String>();
        relations.add(relation);
        return this.getTransitiveClosure(relations, useFloydWarshall);
    }

    /**
     * Returns the transitive closure of the DAG trace graph. Computes the
     * transitive closure using Floyd Warshall algorithm (if useFloydWarshall ==
     * true), otherwise uses the more optimized Goralcikova algorithm.
     */
    public TransitiveClosure getTransitiveClosure(Set<String> relations,
            boolean useFloydWarshall) {
        if (useFloydWarshall) {
            return FloydWarshall.warshallAlg(this, relations);
        }

        return goralcikovaAlg(relations);
    }

    @Override
    public TransitiveClosure getTransitiveClosure(Set<String> relations) {
        return getTransitiveClosure(relations, true);
    }

    /**
     * This function takes a collection of transitions, and a list of nodes that
     * are somehow canonically ordered. It returns a list of nodes that the
     * transitions point to, in an order that is compatible with the canonical
     * ordering.
     * 
     * @param unorderedTrans
     * @param orderedSuperList
     * @return
     */
    private List<EventNode> getSubSortedList(
            Collection<? extends ITransition<EventNode>> unorderedTrans,
            List<EventNode> orderedSuperList) {

        // TODO: this is inefficient because in the worst case it is n^2.
        // Ideally, this would do the following:
        // 1. Add all nodes in unordered to subList
        // 2. Sort subList using a comparator that uses the relative position of
        // elements in the orderedSuperList for computing the order of elements.

        List<EventNode> subList = new LinkedList<EventNode>();
        Set<EventNode> unorderedNodes = new LinkedHashSet<EventNode>();
        for (ITransition<EventNode> trans : unorderedTrans) {
            unorderedNodes.add(trans.getTarget());
        }

        for (EventNode n : orderedSuperList) {
            if (unorderedNodes.contains(n)) {
                subList.add(n);
            }
        }
        return subList;
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
    private TransitiveClosure goralcikovaAlg(Set<String> relations) {
        TransitiveClosure transClosure = new TransitiveClosure(relations);
        Map<EventNode, Set<EventNode>> tc = transClosure.getTC();

        List<EventNode> sortedNodes = new LinkedList<EventNode>();

        // Maps a node to its parents in the transitive closure. This map is
        // cleared whenever we start processing a new DAG/Chain (a new PO/TO
        // trace).
        HashMap<EventNode, HashSet<EventNode>> tcParents = new HashMap<EventNode, HashSet<EventNode>>();

        Set<EventNode> bfsPerimeter = new LinkedHashSet<EventNode>();
        List<EventNode> topoOrder;
        List<EventNode> reverseTopoOrder = new LinkedList<EventNode>();

        // Maps a node to a set of parent nodes -- nodes that have a transitions
        // to this node.
        Map<EventNode, Set<EventNode>> parentsMap = new LinkedHashMap<EventNode, Set<EventNode>>();

        // Maps a node in the topological order to its position in the order.
        Map<EventNode, Integer> orderMap = new LinkedHashMap<EventNode, Integer>();

        List<EventNode> subSortedList;

        // Process each DAG separately.
        for (Set<EventNode> dagInits : traceIdToInitNodes.values()) {

            // 1. Get the nodes sorted in some topological order.
            topoOrder = computeTopologicalOrder(dagInits, relations);

            // TODO: Potential optimization
            // 2. Build the order map -- this maps a node from the topological
            // order to its position in the order. We use this map to derive
            // sublists that are sorted from sets of nodes (that are not
            // necessarily contiguous in the order).
            // int counter = 0;
            // for (EventNode n : topoOrder) {
            // orderMap.put(n, counter);
            // counter++;
            // }

            // 2. Construct the implicit reverse graph -- the graph that is
            // formed by reversing all the edges (by constructing the
            // parentsMap).
            bfsPerimeter.addAll(dagInits);
            while (bfsPerimeter.size() != 0) {
                for (EventNode m : bfsPerimeter) {
                    addToBFSPerimeter(bfsPerimeter, m, null, parentsMap,
                            relations);
                    bfsPerimeter.remove(m);
                    break;
                }
            }
            bfsPerimeter.clear();

            // 3. Traverse the reverse graph from the terminal nodes, building
            // up the transitive relation for a node in an order specified by
            // the REVERSE topological order of the node's children (see paper
            // for more details).

            // TODO: figure out how topoOrder.retainAll() works -- what does
            // retainAll mean for a List?

            reverseTopoOrder.addAll(topoOrder);
            Collections.reverse(reverseTopoOrder);

            for (EventNode m : reverseTopoOrder) {
                // logger.fine("tc map is: " + tc.toString());
                // logger.fine("Handling node " + m.toString());

                // Retrieve a sorted list of m's children based on the topoOrder
                // that was computed earlier for all the nodes in the graph.
                subSortedList = getSubSortedList(
                        m.getTransitionsWithExactRelations(relations),
                        topoOrder);

                for (EventNode child : subSortedList) {
                    if (!transClosure.isReachable(m, child)) {
                        // NOTE: addReachable is necessary before
                        // mergeReachables because the set of reachable nodes
                        // corresponding to m does not exist initially and
                        // mergeReachables does not check for existence of this
                        // set (should it?)
                        transClosure.recordTransitiveReachability(m, child);
                        transClosure.mergeReachables(child, m);
                    }
                }
            }
            // We do not need the following information between different
            // traces:
            tcParents.clear();
            parentsMap.clear();
            reverseTopoOrder.clear();
            topoOrder.clear();
        }
        // logger.fine("FINAL tc map is: " + tc.toString());
        return transClosure;
    }

    /**
     * TODO: Needs testing. <br/>
     * <br/>
     * Computes a valid (one of many possible ones) topological ordering of
     * vertices in the trace corresponding to traceid.
     * 
     * @param traceid
     */
    public List<EventNode> computeTopologicalOrder(int traceid,
            Set<String> relations) {
        assert traceIdToInitNodes.containsKey(traceid);
        return computeTopologicalOrder(traceIdToInitNodes.get(traceid),
                relations);
    }

    /**
     * Computes a valid (one of many possible ones) topological ordering of
     * vertices in the trace DAG that is reachable by the node set dagInits.
     * 
     * @param dagInits
     */
    public List<EventNode> computeTopologicalOrder(Set<EventNode> dagInits,
            Set<String> relations) {
        Map<EventNode, Integer> parentsCountMap = new LinkedHashMap<EventNode, Integer>();
        Set<EventNode> bfsPerimeter = new LinkedHashSet<EventNode>();
        List<EventNode> topoOrder = new LinkedList<EventNode>();

        // Traverse the trace, starting from the nodes dagInits to determine
        // the number of parents that each node has.
        bfsPerimeter.addAll(dagInits);
        while (bfsPerimeter.size() != 0) {
            for (EventNode m : bfsPerimeter) {
                addToBFSPerimeter(bfsPerimeter, m, parentsCountMap, null,
                        relations);
                bfsPerimeter.remove(m);
                break;
            }
        }
        bfsPerimeter.clear();

        // Traverse the trace, respecting topological order, and build up
        // topoOrder list.
        for (EventNode m : dagInits) {
            addToTopoOrder(topoOrder, m, parentsCountMap, relations);
            addToBFSPerimeter(bfsPerimeter, m, relations);
        }

        while (bfsPerimeter.size() != 0) {
            for (EventNode m : bfsPerimeter) {
                if (parentsCountMap.get(m) == 0) {
                    addToTopoOrder(topoOrder, m, parentsCountMap, relations);
                    bfsPerimeter.remove(m);
                    addToBFSPerimeter(bfsPerimeter, m, relations);
                    break;
                }
            }
        }
        return topoOrder;
    }

    /**
     * Adds node to the bfsPerimeter set of nodes, and updates the parents
     * counts and map corresponding to the node.
     * 
     * @param bfsPerimeter
     *            The perimeter of the search -- we will add all the children of
     *            node to this set.
     * @param node
     *            The node to consider
     * @param parentsCountMap
     *            Maps a node to the number of preceding nodes (parents) that
     *            this node has.
     * @param parentsMap
     *            Maps a node to its set of preceding nodes (parents)
     */
    private void addToBFSPerimeter(Set<EventNode> bfsPerimeter, EventNode node,
            Map<EventNode, Integer> parentsCountMap,
            Map<EventNode, Set<EventNode>> parentsMap, Set<String> relations) {
        // Iterate through all the transitions from the node.
        for (ITransition<EventNode> trans : node
                .getTransitionsWithExactRelations(relations)) {
            EventNode dest = trans.getTarget();
            if (dest.isTerminal()) {
                continue;
            }
            // Update the parents counts for the node that the transition is
            // pointing to.
            if (parentsCountMap != null) {
                if (!parentsCountMap.containsKey(dest)) {
                    parentsCountMap.put(dest, Integer.valueOf(1));
                } else {
                    parentsCountMap.put(dest, parentsCountMap.get(dest) + 1);
                }
            }
            // Update the set of parents for the node that is pointed to by the
            // transition.
            if (parentsMap != null) {
                Set<EventNode> parents;
                if (!parentsMap.containsKey(dest)) {
                    parents = new LinkedHashSet<EventNode>();
                    parentsMap.put(dest, parents);
                } else {
                    parents = parentsMap.get(dest);
                }
                parents.add(node);
            }
            // Add the node to the BFS perimeter -- it can now be processed on
            // the next call to this function.
            bfsPerimeter.add(dest);
        }
    }

    private void addToBFSPerimeter(Set<EventNode> bfsPerimeter, EventNode node,
            Set<String> relations) {
        for (ITransition<EventNode> trans : node
                .getTransitionsWithExactRelations(relations)) {
            EventNode dest = trans.getTarget();
            if (dest.isTerminal()) {
                continue;
            }
            bfsPerimeter.add(dest);
        }
    }

    private void addToTopoOrder(List<EventNode> topoOrder, EventNode node,
            Map<EventNode, Integer> parentsCountMap, Set<String> relations) {
        topoOrder.add(node);
        for (ITransition<EventNode> trans : node
                .getTransitionsWithExactRelations(relations)) {
            EventNode dest = trans.getTarget();
            if (dest.isTerminal()) {
                continue;
            }
            parentsCountMap.put(dest, parentsCountMap.get(dest) - 1);
        }
    }
}
