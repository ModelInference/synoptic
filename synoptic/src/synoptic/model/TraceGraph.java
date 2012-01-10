package synoptic.model;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Logger;

import synoptic.algorithms.graph.TransitiveClosure;
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
public abstract class TraceGraph<EType extends EventType> implements
        IGraph<EventNode> {

    public static Logger logger = Logger.getLogger("TransitiveClosure Logger");

    /**
     * The nodes of the graph. The edges between nodes are managed by the nodes.
     * This set contains all the TERMINAL and INITIAL nodes in the graph (see
     * below).
     */
    protected final Set<EventNode> nodes = new LinkedHashSet<EventNode>();

    /**
     * Maintains a 1-1 map between relation strings and artificial TERMINAL
     * nodes. Every terminal node in a trace maintains exactly one transition to
     * such a TERMINAL node to indicate that the source node is a terminal. We
     * must have these TERMINAL nodes in this graph, and not just in partition
     * graph because invariants are mined over this graph.
     */
    protected final Map<String, EventNode> dummyTerminalNodes = new LinkedHashMap<String, EventNode>();

    /**
     * Maintains a 1-1 map between relation strings and artificial INITIAL
     * nodes. Each initial node in a trace has a one of these INITIAL nodes as a
     * parent.
     */
    protected final Map<String, EventNode> dummyInitialNodes = new LinkedHashMap<String, EventNode>();

    private Set<String> cachedRelations = null;

    /**
     * Create a graph from nodes.
     * 
     * @param nodes
     *            the nodes of the graph
     */
    public TraceGraph(Collection<EventNode> nodes) {
        this();
        this.nodes.addAll(nodes);
    }

    /**
     * Create an empty graph.
     */
    public TraceGraph() {
        cachedRelations = null;
    }

    /**
     * Returns all the nodes in this graph.
     */
    @Override
    public Set<EventNode> getNodes() {
        return nodes;
    }

    /**
     * Returns all the initial nodes in this graph.
     */
    public Set<EventNode> getDummyInitialNodes() {
        return new LinkedHashSet<EventNode>(dummyInitialNodes.values());
    }

    /**
     * Returns all the initial nodes in this graph that are initial in the given
     * relation.
     */
    @Override
    public EventNode getDummyInitialNode(String relation) {
        assert dummyInitialNodes.containsKey(relation);
        return dummyInitialNodes.get(relation);
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

    public abstract TransitiveClosure getTransitiveClosure(String relation);

    public abstract int getNumTraces();

    /**
     * Mark {@code terminalNode} as terminal with respect to {@code relation} by
     * creating a transition from this node to the dummy terminal node.
     * 
     * @param terminalNode
     *            the node to mark as terminal
     * @param relation
     *            the relation with respect to which the node should be terminal
     */
    protected void tagTerminal(EventNode terminalNode, String relation) {
        assert dummyTerminalNodes.containsKey(relation) : "A dummy terminal node for the relation must exist prior tagTerminal().";
        assert nodes.contains(terminalNode) : "Node tagged as terminal must be added to the TraceGraph first.";

        terminalNode.addTransition(dummyTerminalNodes.get(relation), relation);
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
    protected void tagInitial(EventNode initialNode, String relation) {
        assert dummyInitialNodes.containsKey(relation) : "A dummy initial node for the relation must exist prior tagInitial().";
        assert nodes.contains(initialNode) : "Node tagged as initial must be added to the TraceGraph first.";

        dummyInitialNodes.get(relation).addTransition(initialNode, relation);
    }

    protected void createIfNotExistsDummyTerminalNode(Event termEvent,
            String relation) {
        createIfNotExistsSpecialNode(termEvent, relation, dummyTerminalNodes);
    }

    protected void createIfNotExistsDummyInitialNode(Event initEvent,
            String relation) {
        createIfNotExistsSpecialNode(initEvent, relation, dummyInitialNodes);
    }

    private void createIfNotExistsSpecialNode(Event event, String relation,
            Map<String, EventNode> specialNodes) {
        if (!specialNodes.containsKey(relation)) {
            EventNode node = new EventNode(event);
            specialNodes.put(relation, node);
            nodes.add(node);
        }
    }

    /**
     * Tests for generic graph equality.
     */
    public boolean equalsWith(TraceGraph<?> other,
            IBinary<EventNode, EventNode> np) {
        return equalsWith(other, np, new BinaryTrue<String, String>());
    }

    public boolean equalsWith(TraceGraph<?> other,
            IBinary<EventNode, EventNode> np, IBinary<String, String> rp) {
        Set<EventNode> unusedOther = other.getDummyInitialNodes();
        for (EventNode n1 : this.getDummyInitialNodes()) {
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

    public abstract Map<Integer, Set<EventNode>> getTraceIdToInitNodes();
}
