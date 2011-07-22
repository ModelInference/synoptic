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
import synoptic.model.interfaces.INode;
import synoptic.model.interfaces.ITransition;
import synoptic.util.InternalSynopticException;
import synoptic.util.Pair;
import synoptic.util.Predicate.BinaryTrue;
import synoptic.util.Predicate.IBinary;

/**
 * A graph implementation that provides a merge operation to merge another graph
 * into it. The graph can be modified by adding nodes to it. Whether edges can
 * be added depends on the capability of the {@code NodeType} class.
 * 
 * @author sigurd
 * @param <NodeType>
 *            the class of a node in the graph
 */
public class Graph<NodeType extends INode<NodeType>> implements
        IGraph<NodeType> {
    /**
     * The nodes of the graph. The edges between nodes are managed by the nodes.
     */
    private final Set<NodeType> nodes = new LinkedHashSet<NodeType>();

    /**
     * Maps a relation to the set of initial nodes in this relation. That is,
     * the set of nodes that have an edge from the dummy initial node (i.e.,
     * from a NodeType x such that x.isInitial() is true).
     */
    private final Map<String, Set<NodeType>> initialNodes = new LinkedHashMap<String, Set<NodeType>>();

    /**
     * Every terminal node maintains a transition to this special node to
     * indicate that the source node is a terminal. We must have this node in
     * this graph, and not just in partition graph because invariants are mined
     * over this graph.
     */
    private NodeType dummyTerminalNode = null;

    /**
     * The node which has transitions to all the initial nodes in the graph.
     */
    private NodeType dummyInitialNode = null;

    private Set<String> cachedRelations = null;

    /**
     * Create a graph from nodes.
     * 
     * @param nodes
     *            the nodes of the graph
     */
    public Graph(Collection<NodeType> nodes) {
        this.nodes.addAll(nodes);
    }

    /**
     * Create an empty graph.
     */
    public Graph() {
    }

    /**
     * Returns all the nodes in this graph. This does NOT include the
     * dummyTerminalNode.
     */
    @Override
    public Set<NodeType> getNodes() {
        return nodes;
    }

    /**
     * Returns all the initial nodes in this graph.
     */
    @Override
    public Set<NodeType> getInitialNodes() {
        // FIXME: Graph.java is mixing up the notion of dummy initial node and
        // the set of nodes that have an edge from the dummy initial node.
        Set<NodeType> copy = new LinkedHashSet<NodeType>();
        copy.add(dummyInitialNode);
        return copy;
    }

    /**
     * Returns all the initial nodes in this graph that are initial in the given
     * relation.
     */
    @Override
    public Set<NodeType> getInitialNodes(String relation) {
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
        for (NodeType node : nodes) {
            for (Iterator<? extends ITransition<NodeType>> iter = node
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
    public void add(NodeType node) {
        nodes.add(node);
        // Invalidate the relations cache.
        cachedRelations = null;
    }

    /**
     * Removes a node from this graph.
     */
    @Override
    public void remove(NodeType node) {
        nodes.remove(node);
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
    public void setDummyTerminal(NodeType dummyTerminal) {
        this.dummyTerminalNode = dummyTerminal;
        this.nodes.add(dummyTerminal);
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
    public void setDummyInitial(NodeType dummyInitial, String relation) {
        this.dummyInitialNode = dummyInitial;
        this.nodes.add(dummyInitial);
        if (!initialNodes.containsKey(relation)) {
            initialNodes.put(relation, new LinkedHashSet<NodeType>());
        }
        initialNodes.get(relation).add(dummyInitial);
        cachedRelations = null;
    }

    /**
     * Tags a node as terminal.
     */
    @Override
    public void tagTerminal(NodeType terminalNode, String relation) {
        if (dummyTerminalNode == null) {
            throw new InternalSynopticException(
                    "Must call setDummyTerminal() prior to tagTerminal().");
        }
        terminalNode.addTransition(dummyTerminalNode, relation);
    }

    /**
     * Tags a node as initial.
     */
    @Override
    public void tagInitial(NodeType initialNode, String relation) {
        if (dummyInitialNode == null) {
            throw new InternalSynopticException(
                    "Must call setDummyInitial() prior to tagInitial().");
        }
        dummyInitialNode.addTransition(initialNode, relation);
    }

    /**
     * Merge {@code graph} into this graph.
     * 
     * @param graph
     *            the graph to merge into this one
     */
    public void merge(Graph<NodeType> graph) {
        nodes.addAll(graph.getNodes());
        for (String key : graph.initialNodes.keySet()) {
            if (!initialNodes.containsKey(key)) {
                initialNodes.put(key, new LinkedHashSet<NodeType>());
            }
            initialNodes.get(key).addAll(graph.initialNodes.get(key));
        }
        cachedRelations = null;
    }

    /**
     * Tests for generic graph equality.
     */
    public boolean equalsWith(Graph<NodeType> other,
            IBinary<NodeType, NodeType> np) {
        return equalsWith(other, np, new BinaryTrue<String, String>());
    }

    public boolean equalsWith(Graph<NodeType> other,
            IBinary<NodeType, NodeType> np, IBinary<String, String> rp) {
        Set<NodeType> unusedOther = other.getInitialNodes();
        for (NodeType n1 : this.getInitialNodes()) {
            boolean foundMatch = false;
            for (NodeType n2 : unusedOther) {
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
    private boolean transitionEquality(NodeType a, NodeType b,
            IBinary<NodeType, NodeType> np, IBinary<String, String> rp) {
        Set<NodeType> visited = new LinkedHashSet<NodeType>();
        Stack<synoptic.util.Pair<NodeType, NodeType>> toVisit = new Stack<synoptic.util.Pair<NodeType, NodeType>>();
        toVisit.push(new Pair<NodeType, NodeType>(a, b));
        while (!toVisit.isEmpty()) {
            Pair<NodeType, NodeType> tv = toVisit.pop();
            visited.add(tv.getLeft());
            for (ITransition<NodeType> trans1 : tv.getLeft().getTransitions()) {
                boolean foundMatch = false;
                for (ITransition<NodeType> trans2 : tv.getRight()
                        .getTransitions()) {
                    if (rp.eval(trans1.getRelation(), trans2.getRelation())
                            && np.eval(trans1.getTarget(), trans2.getTarget())) {
                        if (!visited.contains(trans1.getTarget())) {
                            toVisit.push(new Pair<NodeType, NodeType>(trans1
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
    public Set<NodeType> getAdjacentNodes(NodeType node) {
        Set<NodeType> result = new LinkedHashSet<NodeType>();
        for (ITransition<NodeType> trans : node.getTransitions()) {
            result.add(trans.getTarget());
        }
        return result;
    }
}
