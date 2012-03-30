package synoptic.model;

import java.util.Collection;
import java.util.Iterator;
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
    protected EventNode dummyTerminalNode = null;

    /**
     * Maintains a 1-1 map between relation strings and artificial INITIAL
     * nodes. Each initial node in a trace has a one of these INITIAL nodes as a
     * parent.
     */

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
            for (Iterator<? extends ITransition<EventNode>> iter = node
                    .getTransitionsIterator(); iter.hasNext();) {
                cachedRelations.addAll(iter.next().getRelations());
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
     * Tests for generic graph equality.
     */
    public boolean equalsWith(TraceGraph<?> other,
            IBinary<EventNode, EventNode> np) {
        return equalsWith(other, np, new BinaryTrue<Set<String>, Set<String>>());
    }

    public boolean equalsWith(TraceGraph<?> other,
            IBinary<EventNode, EventNode> np,
            IBinary<Set<String>, Set<String>> rp) {
        // Set<EventNode> unusedOther = other.getDummyInitialNode();
        EventNode unusedOther = other.getDummyInitialNode();
        // for (EventNode n1 : this.getDummyInitialNode()) {
        EventNode n1 = this.getDummyInitialNode();
        boolean foundMatch = false;
        EventNode n2 = unusedOther;

        // TODO: If this works, then clean this by removing the various
        // comments.

        // logger.fine("Comparing " + n1 + " against " + n2);
        if (np.eval(n1, n2) && transitionEquality(n1, n2, np, rp)) {
            foundMatch = true;
            // unusedOther.remove(n2);
            // break;
        }
        // }
        if (!foundMatch) {
            // logger.fine("Could not find a match for node " +
            // n1.toString());
            return false;
        }
        // }
        return true;
    }

    /**
     * Helper for equalsWith.
     */
    private boolean transitionEquality(EventNode a, EventNode b,
            IBinary<EventNode, EventNode> np,
            IBinary<Set<String>, Set<String>> rp) {
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
                    if (rp.eval(trans1.getRelations(), trans2.getRelations())
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
