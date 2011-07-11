package synoptic.algorithms.graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import synoptic.model.interfaces.IGraph;
import synoptic.model.interfaces.INode;
import synoptic.model.interfaces.ITransition;

/**
 * Computes the transitive closure. Code based on
 * http://www.cs.princeton.edu/courses/archive/fall05/cos226/lectures
 * /digraph.pdf
 * 
 * @author Sigurd
 */
public class TransitiveClosure<NodeType extends INode<NodeType>> {
    // Reachability map. Maps a node x to a map Mx which maintains reachability
    // information for x:
    // If y is reachable from x then Mx[y] == true,
    // otherwise Mx[y] == false or Mx.contains(y) == false
    private final HashMap<NodeType, HashMap<NodeType, Boolean>> tc = new HashMap<NodeType, HashMap<NodeType, Boolean>>();
    private final String relation;
    private final IGraph<NodeType> graph;

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
    public TransitiveClosure(IGraph<NodeType> graph, String relation,
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
    public TransitiveClosure(IGraph<NodeType> graph, String relation) {
        this(graph, relation, true);
    }

    /**
     * Warshall's Algorithm.
     */
    private void warshallAlg() {
        Set<NodeType> allNodes = graph.getNodes();

        // Since in our graphs a node doesn't know its parent(s), we maintain a
        // tcParents map that specifies which maps a node to its parents in the
        // transitive closure.
        HashMap<NodeType, HashSet<NodeType>> tcParents = new HashMap<NodeType, HashSet<NodeType>>();

        // Logger logger = Logger.getLogger("TransitiveClosure Logger");
        for (NodeType m : allNodes) {
            // logger.fine("tc map is: " + tc.toString());
            // logger.fine("Handling node " + m.toString());
            Iterator<? extends ITransition<NodeType>> i = m
                    .getTransitionsIterator(relation);
            /**
             * Iterate through all children c of m and for each c do 2 things:
             * 
             * <pre>
             * 1. create a tc link between m and c and add m to tcParents[c]
             * 2. create a tc link between m and every node n to which c is already linked to in tc and add m to tcParents[n]
             * </pre>
             */
            while (i.hasNext()) {
                // Create new tc map for node m.
                if (!tc.containsKey(m)) {
                    tc.put(m, new HashMap<NodeType, Boolean>());
                }

                ITransition<NodeType> t = i.next();
                NodeType c = t.getTarget();
                if (!allNodes.contains(c)) {
                    continue;
                }
                if (!tcParents.containsKey(c)) {
                    tcParents.put(c, new HashSet<NodeType>());
                }

                // Link m to c
                tc.get(m).put(c, true);
                tcParents.get(c).add(m);

                // Link m to all nodes that c is linked to in tc
                if (tc.containsKey(c)) {
                    for (NodeType n : tc.get(c).keySet()) {
                        if (tc.get(c).get(n) == true) {
                            tc.get(m).put(n, true);
                            if (!tcParents.containsKey(n)) {
                                tcParents.put(n, new HashSet<NodeType>());
                            }
                            tcParents.get(n).add(m);
                        }
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
                for (NodeType p : tcParents.get(m)) {
                    for (NodeType n : tc.get(m).keySet()) {
                        if (tc.get(m).get(n)) {
                            // P has a tc entry because its already part of
                            // tcParents of m (so we've already processed it)
                            // previously
                            tc.get(p).put(n, true);
                            // n has a tcParents entry because m is a tc parent
                            // of n and it must have been set above.
                            tcParents.get(n).add(p);
                        }
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
        for (NodeType m : graph.getNodes()) {
            Iterator<? extends ITransition<NodeType>> i = m
                    .getTransitionsIterator(relation);
            while (i.hasNext()) {
                ITransition<NodeType> t = i.next();
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
    private void dfs(NodeType m, NodeType n) {
        if (!tc.containsKey(m)) {
            tc.put(m, new HashMap<NodeType, Boolean>());
        }
        tc.get(m).put(n, true);
        for (Iterator<? extends ITransition<NodeType>> i = n
                .getTransitionsIterator(relation); i.hasNext();) {
            ITransition<NodeType> t = i.next();
            if (!graph.getNodes().contains(t.getTarget())) {
                continue;
            }
            Boolean r = tc.get(m).get(t.getTarget());
            if (r == null || r == false) {
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
    public boolean isReachable(NodeType m, NodeType n) {
        HashMap<NodeType, Boolean> i = tc.get(m);
        if (i == null) {
            return false;
        }
        Boolean r = i.get(n);
        if (r == null) {
            return false;
        }
        return r;
    }

    /**
     * Equality for transitive closure
     * 
     * @param o
     * @return if {@code o} describes the same relation is {@code this}
     */
    public boolean isEqual(TransitiveClosure<NodeType> o) {
        if (!this.relation.equals(o.relation)) {
            return false;
        }

        for (NodeType u : o.tc.keySet()) {
            for (NodeType v : o.tc.get(u).keySet()) {
                if (isReachable(u, v) != o.isReachable(u, v)) {
                    return false;
                }
            }
        }

        for (NodeType u : tc.keySet()) {
            for (NodeType v : tc.get(u).keySet()) {
                if (isReachable(u, v) != o.isReachable(u, v)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * @return tc
     */
    public HashMap<NodeType, HashMap<NodeType, Boolean>> getTC() {
        return tc;
    }
}
