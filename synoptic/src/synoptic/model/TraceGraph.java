package synoptic.model;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Logger;

import synoptic.algorithms.TransitiveClosure;
import synoptic.model.event.Event;
import synoptic.model.event.EventType;
import synoptic.model.interfaces.IGraph;
import synoptic.model.interfaces.ITransition;
import synoptic.util.Pair;
import synoptic.util.Predicate.IBoolBinary;

/**
 * A graph implementation that provides a merge operation to merge another graph
 * into it. The graph can be modified by adding nodes to it. Whether edges can
 * be added depends on the capability of the {@code EventNode} class.
 * 
 * @param <EventNode>
 *            the class of a node in the graph
 */
public abstract class TraceGraph<EType extends EventType> implements
        IGraph<EventNode> {

    public static Logger logger = Logger.getLogger("TraceGraph Logger");

    /**
     * The nodes of the graph. The edges between nodes are managed by the nodes.
     * This set contains all the TERMINAL and INITIAL nodes in the graph (see
     * below).
     */
    protected final Set<EventNode> nodes = new LinkedHashSet<EventNode>();

    protected EventNode dummyTerminalNode = null;

    protected EventNode dummyInitialNode = null;

    private Set<String> cachedRelations = null;

    /**
     * Create a graph from nodes.
     * 
     * @param nodes
     *            the nodes of the graph
     */
    public TraceGraph(Collection<EventNode> nodes, Event initEvent,
            Event termEvent) {
        this(initEvent, termEvent);
        this.nodes.addAll(nodes);
    }

    /**
     * Create an empty graph.
     */
    public TraceGraph(Event initEvent, Event termEvent) {
        dummyInitialNode = new EventNode(initEvent);
        dummyTerminalNode = new EventNode(termEvent);
        nodes.add(dummyInitialNode);
        nodes.add(dummyTerminalNode);
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
     * Returns the INITIAL node for this graph.
     */
    public EventNode getDummyInitialNode() {
        return dummyInitialNode;
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
            cachedRelations.addAll(node.getNodeRelations());
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
        //
        // NOTE: The reason we do not update the relations here is because the
        // node might not be finalized yet. That is, the node's transitions
        // might not be created/added yet, so at this point we do not know the
        // exact set of relations associated with this node.
        cachedRelations = null;
    }

    public abstract TransitiveClosure getTransitiveClosure(Set<String> relation);

    public TransitiveClosure getTransitiveClosure(String relation) {
        Set<String> relations = new LinkedHashSet<String>();
        relations.add(relation);
        return getTransitiveClosure(relations);
    }

    public abstract int getNumTraces();

    public void tagTerminal(EventNode terminalNode, String relation) {
        Set<String> relations = new LinkedHashSet<String>();
        relations.add(relation);
        this.tagTerminal(terminalNode, relations);
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
    public void tagTerminal(EventNode terminalNode, Set<String> relations) {
        assert nodes.contains(terminalNode) : "Node tagged as terminal must be added to the TraceGraph first.";
        terminalNode.addTransition(dummyTerminalNode, relations);
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
    public void tagInitial(EventNode initialNode, Set<String> relations) {
        assert nodes.contains(initialNode) : "Node tagged as initial must be added to the TraceGraph first.";
        dummyInitialNode.addTransition(initialNode, relations);
    }

    /**
     * Tests for trace graph equality.
     */
    public boolean equalsWith(TraceGraph<?> other,
            IBoolBinary<EventNode, EventNode> pred) {

        EventNode unusedOther = other.getDummyInitialNode();
        EventNode n1 = this.getDummyInitialNode();
        EventNode n2 = unusedOther;

        if (pred.eval(n1, n2) && transitionEquality(n1, n2, pred)) {
            return true;
        }
        return false;
    }

    /**
     * Helper for equalsWith.
     */
    private boolean transitionEquality(EventNode a, EventNode b,
            IBoolBinary<EventNode, EventNode> pred) {
        Set<EventNode> visited = new LinkedHashSet<EventNode>();
        Stack<synoptic.util.Pair<EventNode, EventNode>> toVisit = new Stack<synoptic.util.Pair<EventNode, EventNode>>();
        toVisit.push(new Pair<EventNode, EventNode>(a, b));
        while (!toVisit.isEmpty()) {
            Pair<EventNode, EventNode> tv = toVisit.pop();
            visited.add(tv.getLeft());
            for (ITransition<EventNode> trans1 : tv.getLeft()
                    .getAllTransitions()) {
                boolean foundMatch = false;
                for (ITransition<EventNode> trans2 : tv.getRight()
                        .getAllTransitions()) {
                    if (pred.eval(trans1.getTarget(), trans2.getTarget())) {
                        if (!visited.contains(trans1.getTarget())) {
                            toVisit.push(new Pair<EventNode, EventNode>(trans1
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

    @Override
    public Set<EventNode> getAdjacentNodes(EventNode node) {
        Set<EventNode> result = new LinkedHashSet<EventNode>();
        for (ITransition<EventNode> trans : node.getAllTransitions()) {
            result.add(trans.getTarget());
        }
        return result;
    }

    public abstract Map<Integer, Set<EventNode>> getTraceIdToInitNodes();
}
