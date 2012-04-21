package synoptic.algorithms;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import synoptic.model.EventNode;

/**
 * Computes and maintains the transitive closure. Code based on
 * http://www.cs.princeton.edu/courses/archive/fall05/cos226/lectures
 * /digraph.pdf
 */
public class TransitiveClosure {
    // Reachability map.
    // If y is reachable from x then tc.get(x).contains(y) == true,
    // otherwise tc.get(x).contains(y) == false
    Map<EventNode, Set<EventNode>> tc = new LinkedHashMap<EventNode, Set<EventNode>>();

    private final Set<String> relations;

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
    public TransitiveClosure(Set<String> relation) {
        this.relations = relation;
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

    // WARNING: Assumes that 'into' has a non-null entry in tc.
    public void mergeReachables(EventNode from, EventNode into) {
        Set<EventNode> srcSet = tc.get(from);

        if (srcSet != null) {
            Set<EventNode> destSet = tc.get(into);
            assert destSet != null;
            destSet.addAll(srcSet);
        }
    }

    /**
     * Records the fact that m can transitively reach n.
     */
    public void recordTransitiveReachability(EventNode m, EventNode n) {
        Set<EventNode> r;
        if (!tc.containsKey(m)) {
            r = new LinkedHashSet<EventNode>();
            tc.put(m, r);
        } else {
            r = tc.get(m);
        }
        r.add(n);
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
        if (!this.relations.equals(other.relations)) {
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

    public String toString() {
        return tc.toString();
    }

    /**
     * @return tc
     */
    public Map<EventNode, Set<EventNode>> getTC() {
        return tc;
    }

}
