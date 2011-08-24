package synoptic.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import synoptic.model.interfaces.IGraph;
import synoptic.model.interfaces.ITransition;
import synoptic.util.Pair;
import synoptic.util.Predicate.BinaryTrue;
import synoptic.util.Predicate.IBinary;

/**
 * A graph implementation that provides a merge operation to merge another graph
 * into it. The graph can be modified by adding nodes to it. Whether edges can
 * be added depends on the capability of the {@code EventNode} class.
 * 
 * @author sigurd
 * @param <EventNode>
 *            the class of a node in the graph
 */
public class TraceGraph implements IGraph<EventNode> {
    /**
     * The nodes of the graph. The edges between nodes are managed by the nodes.
     */
    private final Set<EventNode> nodes = new LinkedHashSet<EventNode>();

    /**
     * Maps a relation to the set of initial nodes in this relation. That is,
     * the set of nodes that have an edge from the dummy initial node (i.e.,
     * from a EventNode x such that x.isInitial() is true).
     */
    private final Map<String, Set<EventNode>> initialNodes = new LinkedHashMap<String, Set<EventNode>>();

    /**
     * Maintains a map of trace id to the set of initial nodes in the trace.
     */
    private final Map<Integer, Set<EventNode>> traceIdToInitNodes = new LinkedHashMap<Integer, Set<EventNode>>();

    /**
     * Every terminal node maintains a transition to this special node to
     * indicate that the source node is a terminal. We must have this node in
     * this graph, and not just in partition graph because invariants are mined
     * over this graph.
     */
    private EventNode dummyTerminalNode = null;

    /** The node which has transitions to all the initial nodes in the graph. */
    private EventNode dummyInitialNode = null;

    /** Whether or not the traces this graph represents are partially ordered */
    private boolean partiallyOrderedTraces = false;

    private Set<String> cachedRelations = null;

    /**
     * Create a graph from nodes.
     * 
     * @param nodes
     *            the nodes of the graph
     */
    public TraceGraph(Collection<EventNode> nodes) {
        this.nodes.addAll(nodes);
    }

    /**
     * Create an empty graph.
     */
    public TraceGraph() {
        // Empty constructor used by unit tests.
    }

    @Override
    public void setPartiallyOrdered(boolean po) {
        partiallyOrderedTraces = po;
    }

    @Override
    public boolean isPartiallyOrdered() {
        return partiallyOrderedTraces;
    }

    /**
     * Returns all the nodes in this graph. This does NOT include the
     * dummyTerminalNode.
     */
    @Override
    public Set<EventNode> getNodes() {
        return nodes;
    }

    /**
     * Returns all the initial nodes in this graph.
     */
    @Override
    public Set<EventNode> getInitialNodes() {
        // FIXME: Graph.java is mixing up the notion of dummy initial node and
        // the set of nodes that have an edge from the dummy initial node.
        Set<EventNode> copy = new LinkedHashSet<EventNode>();
        copy.add(dummyInitialNode);
        return copy;
    }

    /**
     * Returns all the initial nodes in this graph that are initial in the given
     * relation.
     */
    @Override
    public Set<EventNode> getInitialNodes(String relation) {
        if (!initialNodes.containsKey(relation)) {
            return Collections.emptySet();
        }
        return initialNodes.get(relation);
    }

    /**
     * Returns the set of relations that are present in this graph.
     */
    @Override
    public Set<String> getRelations() {
        if (cachedRelations != null) {
            return cachedRelations;
        }
        cachedRelations = new LinkedHashSet<String>();
        for (EventNode node : nodes) {
            for (Iterator<? extends ITransition<EventNode>> iter = node
                    .getTransitionsIterator(); iter.hasNext();) {
                cachedRelations.add(iter.next().getRelation());
            }
        }
        return cachedRelations;
    }

    /**
     * Adds a node to this graph.
     */
    @Override
    public void add(EventNode node) {
        nodes.add(node);
        // Invalidate the relations cache.
        cachedRelations = null;
    }

    /**
     * Sets the dummy terminal node to which all terminal nodes in the graph
     * have a transition.
     * 
     * @param dummyTerminal
     *            the dummy terminal node to use for this graph
     */
    public void setDummyTerminal(EventNode dummyTerminal) {
        dummyTerminalNode = dummyTerminal;
        nodes.add(dummyTerminal);
    }

    /**
     * Sets the dummy initial node to which all the initial nodes in the trace
     * transition.
     * 
     * @param dummyInitial
     *            the dummy initial node to use for this graph
     * @param relation
     *            the relation with which this initial node is associated
     */
    public void setDummyInitial(EventNode dummyInitial, String relation) {
        dummyInitialNode = dummyInitial;
        nodes.add(dummyInitial);
        if (!initialNodes.containsKey(relation)) {
            initialNodes.put(relation, new LinkedHashSet<EventNode>());
        }
        initialNodes.get(relation).add(dummyInitial);
        cachedRelations = null;
    }

    /**
     * Mark {@code terminalNode} as terminal with respect to {@code relation} by
     * creating a transition from this node to the dummy terminal node.
     * 
     * @param terminalNode
     *            the node to mark as terminal
     * @param relation
     *            the relation with respect to which the node should be terminal
     */
    public void tagTerminal(EventNode terminalNode, String relation) {
        assert dummyTerminalNode != null : "Must call setDummyTerminal() prior to tagTerminal().";

        terminalNode.addTransition(dummyTerminalNode, relation);
    }

    /**
     * Mark {@code initialNode} as initial with respect to {@code relation} by
     * creating a transition from the dummy initial node to this node.
     * 
     * @param initialNode
     *            the node to mark as initial
     * @param relation
     *            the relation with respect to which the node should be initial
     */
    public void tagInitial(EventNode initialNode, String relation) {
        assert dummyInitialNode != null : "Must call setDummyInitial() prior to tagInitial().";

        dummyInitialNode.addTransition(initialNode, relation);

        /**
         * Build a map of trace id to the set of initial nodes in the trace.
         * This is used for partially ordered traces, where it is not possible
         * to determine which initial nodes (pointed to from the synthetic
         * INITIAL node) are in the same trace.
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

    public Map<Integer, Set<EventNode>> getTraceIdToInitNodes() {
        return traceIdToInitNodes;
    }

    /**
     * Tests for generic graph equality.
     */
    public boolean equalsWith(TraceGraph other, IBinary<EventNode, EventNode> np) {
        return equalsWith(other, np, new BinaryTrue<String, String>());
    }

    public boolean equalsWith(TraceGraph other,
            IBinary<EventNode, EventNode> np, IBinary<String, String> rp) {
        Set<EventNode> unusedOther = other.getInitialNodes();
        for (EventNode n1 : this.getInitialNodes()) {
            boolean foundMatch = false;
            for (EventNode n2 : unusedOther) {
                // logger.fine("Comparing " + n1 + " against " + n2);
                if (np.eval(n1, n2) && transitionEquality(n1, n2, np, rp)) {
                    foundMatch = true;
                    unusedOther.remove(n2);
                    break;
                }
            }
            if (!foundMatch) {
                // logger.fine("Could not find a match for node " +
                // n1.toString());
                return false;
            }
        }
        return true;
    }

    /**
     * Helper for equalsWith.
     */
    private boolean transitionEquality(EventNode a, EventNode b,
            IBinary<EventNode, EventNode> np, IBinary<String, String> rp) {
        Set<EventNode> visited = new LinkedHashSet<EventNode>();
        Stack<synoptic.util.Pair<EventNode, EventNode>> toVisit = new Stack<synoptic.util.Pair<EventNode, EventNode>>();
        toVisit.push(new Pair<EventNode, EventNode>(a, b));
        while (!toVisit.isEmpty()) {
            Pair<EventNode, EventNode> tv = toVisit.pop();
            visited.add(tv.getLeft());
            for (ITransition<EventNode> trans1 : tv.getLeft().getTransitions()) {
                boolean foundMatch = false;
                for (ITransition<EventNode> trans2 : tv.getRight()
                        .getTransitions()) {
                    if (rp.eval(trans1.getRelation(), trans2.getRelation())
                            && np.eval(trans1.getTarget(), trans2.getTarget())) {
                        if (!visited.contains(trans1.getTarget())) {
                            toVisit.push(new Pair<EventNode, EventNode>(trans1
                                    .getTarget(), trans2.getTarget()));
                        }
                        foundMatch = true;
                        break;
                    }
                }
                if (!foundMatch) {
                    // logger.fine("Could not find a match for transition: " +
                    // trans1.toString());
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public Set<EventNode> getAdjacentNodes(EventNode node) {
        Set<EventNode> result = new LinkedHashSet<EventNode>();
        for (ITransition<EventNode> trans : node.getTransitions()) {
            result.add(trans.getTarget());
        }
        return result;
    }
}
