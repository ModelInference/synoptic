package synoptic.algorithms.graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import synoptic.model.EventNode;
import synoptic.model.TraceGraph;
import synoptic.model.interfaces.ITransition;

/**
 * Computes and maintains the transitive closure. Code based on
 * http://www.cs.princeton.edu/courses/archive/fall05/cos226/lectures
 * /digraph.pdf
 */
public class TransitiveClosure {
    // Reachability map.
    // If y is reachable from x then tc.get(x).contains(y) == true,
    // otherwise tc.get(x).contains(y) == false
    private final Map<EventNode, Set<EventNode>> tc = new LinkedHashMap<EventNode, Set<EventNode>>();

    private final String relation;
    private final TraceGraph graph;

    /**
     * Create the transitive closure of {@code graph} for the Relation
     * {@code relation} using one of two algorithms -- an iterative Warshall's
     * algorithm or the recursive older algorithm.
     * 
     * @param graph
     *            the graph
     * @param relation
     *            the relation
     * @param useWarshall
     *            whether or not to use Warshall's algorithm
     */
    public TransitiveClosure(TraceGraph graph, String relation,
            boolean useWarshall) {
        this.relation = relation;
        this.graph = graph;

        if (useWarshall) {
            warshallAlg();
        } else {
            oldTCAlg();
        }
    }

    /**
     * Create the transitive closure of {@code graph} for the Relation
     * {@code relation} using Warshall's algorithm.
     * 
     * @param graph
     *            the graph
     * @param relation
     *            the relation
     */
    public TransitiveClosure(TraceGraph graph, String relation) {
        this(graph, relation, true);
    }

    /**
     * <pre>
     * The algorithm by Goralčíková and Koubek:
     * "A reduct-and-closure algorithm for graphs" by Alla Goralčíková
     * and Václav Koubek. MATHEMATICAL FOUNDATIONS OF COMPUTER SCIENCE 1979
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
    private void goralcikovaAlg() {
        Set<EventNode> allNodes = graph.getNodes();
        List<EventNode> sortedNodes = new LinkedList<EventNode>();

        // 1. Get the nodes sorted in some topological order, and at the same
        // time construct a reverse graph -- the graph that is formed by
        // reversing all the edges.

        // TODO

        // 2. Traverse the reverse graph from the terminal nodes, building up
        // the transitive relation for a node in an order specified by the
        // topological order of the node's children (see paper for more
        // details).

        // TODO
    }

    /**
     * Warshall's Algorithm.
     */
    private void warshallAlg() {
        Set<EventNode> allNodes = graph.getNodes();

        // Maps a node to its parents in the transitive closure.
        HashMap<EventNode, HashSet<EventNode>> tcParents = new HashMap<EventNode, HashSet<EventNode>>();

        // Logger logger = Logger.getLogger("TransitiveClosure Logger");
        for (EventNode m : allNodes) {
            // logger.fine("tc map is: " + tc.toString());
            // logger.fine("Handling node " + m.toString());
            Iterator<? extends ITransition<EventNode>> transIter = m
                    .getTransitionsIterator(relation);
            /**
             * Iterate through all children of m and for each child do 2 things:
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
             * Now that we're done compiling the downward transitive closure of
             * m, its time to push that information to m's parent nodes. For
             * each tc parent p of m we do 2 things:
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
        // logger.fine("FINAL tc map is: " + tc.toString());
    }

    /**
     * The old recursive TC algorithm.
     */
    private void oldTCAlg() {
        for (EventNode m : graph.getNodes()) {
            Iterator<? extends ITransition<EventNode>> i = m
                    .getTransitionsIterator(relation);
            while (i.hasNext()) {
                ITransition<EventNode> t = i.next();
                if (!graph.getNodes().contains(t.getTarget())) {
                    continue;
                }
                dfs(m, t.getTarget());
            }
        }
    }

    /**
     * Mark that {@code m} can reach {@code n}, and start a DFS between
     * {@code m} and nodes immediately reachable from {@code n}.
     * 
     * @param m
     *            the node to start DFS at
     * @param n
     *            a node that can be reached from m
     */
    private void dfs(EventNode m, EventNode n) {
        if (!tc.containsKey(m)) {
            tc.put(m, new LinkedHashSet<EventNode>());
        }
        tc.get(m).add(n);
        for (Iterator<? extends ITransition<EventNode>> i = n
                .getTransitionsIterator(relation); i.hasNext();) {
            ITransition<EventNode> t = i.next();
            if (!graph.getNodes().contains(t.getTarget())) {
                continue;
            }
            if (!tc.get(m).contains(t.getTarget())) {
                dfs(m, t.getTarget());
            }
        }
    }

    /**
     * Check whether there is an edge in the transitive closure between
     * {@code m} and {@code n}.
     * 
     * @param m
     *            a node
     * @param n
     *            a node
     * @return true if {@code m} can reach {@code n}
     */
    public boolean isReachable(EventNode m, EventNode n) {
        Set<EventNode> i = tc.get(m);
        if (i == null) {
            return false;
        }
        return i.contains(n);
    }

    /**
     * Returns the set of nodes that are reachable from a source node
     * 
     * @param source
     *            the node from which the reachability closure is computed.
     */
    public Set<EventNode> getReachableNodes(EventNode source) {
        return tc.get(source);
    }

    /**
     * Equality for transitive closure
     * 
     * @param other
     * @return if {@code o} describes the same relation is {@code this}
     */
    public boolean isEqual(TransitiveClosure other) {
        if (!this.relation.equals(other.relation)) {
            return false;
        }

        for (EventNode u : other.tc.keySet()) {
            for (EventNode v : other.tc.get(u)) {
                // v is reachable from u in other.tc, check that same is true
                // for this.tc:
                if (!isReachable(u, v)) {
                    return false;
                }
            }
        }

        for (EventNode u : tc.keySet()) {
            for (EventNode v : tc.get(u)) {
                // v is reachable from u in this.tc, check that same is true for
                // other.tc:
                if (!other.isReachable(u, v)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * @return tc
     */
    public Map<EventNode, Set<EventNode>> getTC() {
        return tc;
    }
}
