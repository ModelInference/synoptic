package synoptic.invariants.fsmcheck;

import java.util.ArrayList;
import java.util.Collections;

import synoptic.invariants.CExamplePath;
import synoptic.invariants.ITemporalInvariant;
import synoptic.model.interfaces.INode;
import synoptic.model.interfaces.ITransition;
import synoptic.util.InternalSynopticException;

/**
 * HistoryNode class used to construct a linked-list path through the
 * synoptic.model graph. This linked list structure is used, rather than
 * explicit lists, in order to benefit from the sharing of prefixes.
 */
public class HistoryNode<T extends INode<T>> implements
        Comparable<HistoryNode<T>> {
    T node;
    HistoryNode<T> previous;
    int count;

    public HistoryNode(T node, HistoryNode<T> previous, int count) {
        this.node = node;
        this.previous = previous;
        this.count = count;
    }

    @Override
    public boolean equals(Object other) {
        if (super.equals(other)) {
            return true;
        }
        if (!(other instanceof HistoryNode)) {
            return false;
        }
        @SuppressWarnings("unchecked")
        HistoryNode<T> hOther = (HistoryNode<T>) other;
        if (this.compareTo(hOther) == 0) {
            return true;
        }
        return false;
    }

    @Override
    public int compareTo(HistoryNode<T> other) {
        return this.count - other.count;
    }

    /**
     * Converts this chain into a RelationPath list.
     */
    public CExamplePath<T> toCounterexample(ITemporalInvariant inv) {
        ArrayList<T> path = new ArrayList<T>();
        HistoryNode<T> cur = this;
        // TODO: why do we require isTerminal here?
        assert (cur.node).isTerminal();
        while (cur != null) {
            path.add(cur.node);
            if (TracingStateSet.checkPath && cur.previous != null) {
                T prev = cur.previous.node;
                boolean found = false;
                for (ITransition<T> trans : prev.getAllTransitions()) {
                    if (trans.getTarget().equals(cur.node)) {
                        found = true;
                        break;
                    }
                }
                assert found;
            }
            cur = cur.previous;
        }
        Collections.reverse(path);

        CExamplePath<T> rpath = new CExamplePath<T>(inv, inv.shorten(path));

        if (rpath.path == null) {
            throw new InternalSynopticException(
                    "counter-example shortening returned null for " + inv
                            + " and c-example trace " + path);
        }
        return rpath;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        HistoryNode<T> cur = this;
        while (cur != null) {
            sb.append(cur.node.getEType());
            sb.append(" <- ");
            cur = cur.previous;
        }
        return sb.toString();
    }
}
