package synoptic.invariants.fsmcheck;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import synoptic.invariants.CExamplePath;
import synoptic.invariants.ITemporalInvariant;
import synoptic.model.EventNode;
import synoptic.model.interfaces.INode;
import synoptic.model.interfaces.ITransition;
import synoptic.util.time.ITime;

/**
 * An extension of a HistoryNode which also records time deltas
 */
public class ConstrainedHistoryNode<T extends INode<T>> extends HistoryNode<T> {
    /**
     * Concrete edge(s) used to arrive at this node
     */
    List<ITransition<EventNode>> transitions;
    ITime tDelta;
    ConstrainedHistoryNode<T> previousConst;
    int violationStart;
    int violationEnd;

    /**
     * Create a new history node that will be the head of this history linked
     * list. All parameters must be non-null.
     * 
     * @param node
     *            The node where this history is to start
     * @param tDelta
     *            An ITime which should have a zero time value
     */
    public ConstrainedHistoryNode(T node, ITime tDelta) {
        super(node, null, 0);

        // Neither parameter can be null
        assert node != null;
        assert tDelta != null;

        this.tDelta = tDelta;
    }

    /**
     * Create a new history node that adds to this history linked list given the
     * previous history node. All parameters must be non-null.
     * 
     * @param node
     * @param previous
     * @param count
     * @param transitions
     * @param tDelta
     */
    public ConstrainedHistoryNode(T node, ConstrainedHistoryNode<T> previous,
            int count, List<ITransition<EventNode>> transitions, ITime tDelta) {
        super(node, previous, count);

        // No parameter can be null
        assert node != null;
        assert previous != null;
        assert transitions != null;
        assert tDelta != null;

        this.transitions = transitions;
        this.tDelta = tDelta;
        previousConst = previous;
    }

    /**
     * Set the start of the violation subpath to this node. This node's label
     * should always be the invariant's first predicate.
     */
    public void startViolationHere() {
        violationStart = count;
    }

    /**
     * Set the end of the violation subpath to this node. This node's label
     * should always be the invariant's second predicate.
     */
    public void endViolationHere() {
        violationEnd = count;
    }

    /**
     * Converts this chain into a RelationPath list with time deltas
     */
    @Override
    public CExamplePath<T> toCounterexample(ITemporalInvariant inv) {

        List<T> path = new ArrayList<T>();
        List<List<ITransition<EventNode>>> transitionsList = new ArrayList<List<ITransition<EventNode>>>();
        List<ITime> tDeltas = new ArrayList<ITime>();
        ConstrainedHistoryNode<T> cur = this;

        // Traverse the path of ConstrainedHistoryNodes recording T nodes,
        // transitions, and running time deltas in lists
        while (cur != null) {
            path.add(cur.node);
            transitionsList.add(cur.transitions);
            tDeltas.add(cur.tDelta);
            cur = cur.previousConst;
        }
        Collections.reverse(path);
        Collections.reverse(transitionsList);
        Collections.reverse(tDeltas);

        // Constrained invariants maintain the violation subpath and do not
        // need to be shortened but do require transitions and running time
        // deltas
        CExamplePath<T> rpath = new CExamplePath<T>(inv, path, transitionsList,
                tDeltas, violationStart, violationEnd);

        return rpath;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        ConstrainedHistoryNode<T> cur = this;
        while (cur != null) {
            sb.append(cur.node.getEType());
            sb.append("(");

            // Include time if it's available
            if (cur.transitions != null && cur.transitions.get(0) != null) {
                ITransition<EventNode> trans = cur.transitions.get(0);
                if (trans.getTimeDelta() != null
                        && !trans.getSource().isInitial()
                        && !trans.getSource().isTerminal()
                        && !trans.getTarget().isInitial()
                        && !trans.getTarget().isTerminal()) {
                    sb.append(cur.transitions.get(0).getTimeDelta());
                }
            }

            sb.append(")<-");
            cur = cur.previousConst;
        }
        return sb.toString();
    }
}
