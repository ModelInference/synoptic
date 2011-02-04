package synoptic.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
    private final Set<NodeType> nodes = new HashSet<NodeType>();

    /**
     * Maps a relation to the set of initial nodes in this relation.
     */
    private final Map<String, Set<NodeType>> initialNodes = new HashMap<String, Set<NodeType>>();

    /**
     * Maps a relation to the set of terminal nodes in this relation.
     */
    private final Map<String, Set<NodeType>> terminalNodes = new HashMap<String, Set<NodeType>>();

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
     * Helper function that returns a copy of the set of elements that all
     * strings in the toCopy map passed as argument map to.
     * 
     * @param toCopy
     *            A map whose values should be merged and returned
     * @return A set of values from the toCopy map.
     */
    private Set<NodeType> getNodeSetcopy(Map<String, Set<NodeType>> toCopy) {
        Set<NodeType> copy = new HashSet<NodeType>();
        for (Set<NodeType> v : toCopy.values()) {
            copy.addAll(v);
        }
        return copy;
    }

    /**
     * Returns all the nodes in this graph.
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
        return getNodeSetcopy(initialNodes);
    }

    /**
     * Returns all the terminal nodes in this graph.
     */
    @Override
    public Set<NodeType> getTerminalNodes() {
        return getNodeSetcopy(terminalNodes);
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
     * Returns all the initial nodes in this graph that are initial in the given
     * relation.
     */
    @Override
    public Set<NodeType> getTerminalNodes(String relation) {
        if (!terminalNodes.containsKey(relation)) {
            return Collections.emptySet();
        }
        return terminalNodes.get(relation);
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
     * Helped function for tagging initial\final nodes.
     * 
     * @param node
     *            node to tag
     * @param relation
     *            the relation to which the node belongs
     * @param tagMap
     *            the tagMap to use
     */
    private void tagNode(NodeType node, String relation,
            Map<String, Set<NodeType>> tagMap) {
        if (node == null) {
            throw new InternalSynopticException(new IllegalArgumentException(
                    "Null node argument"));
        }
        if (!tagMap.containsKey(relation)) {
            tagMap.put(relation, new HashSet<NodeType>());
        }
        tagMap.get(relation).add(node);
        cachedRelations = null;
    }

    /**
     * Tags a node as initial.
     */
    @Override
    public void tagInitial(NodeType initialNode, String relation) {
        tagNode(initialNode, relation, initialNodes);
    }

    /**
     * Tags a node as terminal.
     */
    @Override
    public void tagTerminal(NodeType terminalNode, String relation) {
        tagNode(terminalNode, relation, terminalNodes);
        // NOTE: this graph does not maintain the TERMINAL node. See
        // PartionGraph instead.
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
                initialNodes.put(key, new HashSet<NodeType>());
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
                if (np.eval(n1, n2) && transitionEquality(n1, n2, np, rp)) {
                    foundMatch = true;
                    unusedOther.remove(n2);
                    break;
                }
            }
            if (!foundMatch) {
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
        Set<NodeType> visited = new HashSet<NodeType>();
        Stack<synoptic.util.Pair<NodeType, NodeType>> toVisit = new Stack<synoptic.util.Pair<NodeType, NodeType>>();
        toVisit.push(new Pair<NodeType, NodeType>(a, b));
        while (!toVisit.isEmpty()) {
            Pair<NodeType, NodeType> tv = toVisit.pop();
            visited.add(tv.getLeft());
            for (ITransition<NodeType> trans1 : tv.getLeft().getTransitions()) {
                boolean foundMatch = false;
                for (ITransition<NodeType> trans2 : tv.getRight()
                        .getTransitions()) {
                    // System.out.println("comparing " + trans1.getRelation() +
                    // " with " +
                    // trans2.getRelation());
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
                    return false;
                }
            }
        }
        return true;
    }
}
