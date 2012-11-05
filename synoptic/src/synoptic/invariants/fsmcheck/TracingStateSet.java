package synoptic.invariants.fsmcheck;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import synoptic.invariants.CExamplePath;
import synoptic.invariants.ITemporalInvariant;
import synoptic.model.interfaces.INode;
import synoptic.model.interfaces.ITransition;
import synoptic.util.InternalSynopticException;

/**
 * Abstract NFA state set which keeps the shortest path justifying a given state
 * being inhabited. This allows for synoptic.model checking which yields short
 * counterexample paths for failing synoptic.invariants.
 * 
 * @author Michael Sloan (mgsloan@gmail.com)
 * @param <T>
 *            The node type, used as an input, and stored in path-history.
 */
public abstract class TracingStateSet<T extends INode<T>> implements
        IStateSet<T, TracingStateSet<T>> {
    public static boolean checkPath = false;

    /* Consider creating a HistoryList class to abstract away HistoryNode 
     * juggling in the tracing sets, and consider using boolean flags instead 
     * of null fields to store state.
     * 
     * My gut says prefix sharing and list interfaces are not mutually
     * exclusive.
     */
    /**
     * HistoryNode class used to construct a linked-list path through the
     * synoptic.model graph. This linked list structure is used, rather than
     * explicit lists, in order to benefit from the sharing of prefixes.
     */
    public class HistoryNode implements Comparable<HistoryNode> {
        T node;
        HistoryNode previous;
        int count;

        public HistoryNode(T node, HistoryNode previous, int count) {
            this.node = node;
            this.previous = previous;
            this.count = count;
        }

        /* History nodes/linked lists are equal if they are referentially 
         * equivalent or have the same length.(non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object other) {
            if (super.equals(other)) {
                return true;
            }
            if (!(other instanceof TracingStateSet.HistoryNode)) {
                return false;
            }
            @SuppressWarnings("unchecked")
            HistoryNode hOther = (HistoryNode) other;
            if (this.compareTo(hOther) == 0) {
                return true;
            }
            return false;
        }

        @Override
        public int compareTo(HistoryNode other) {
            return this.count - other.count;
        }

        /**
         * Converts this chain into a RelationPath list.
         */
        public CExamplePath<T> toCounterexample(ITemporalInvariant inv) {
            ArrayList<T> path = new ArrayList<T>();
            HistoryNode cur = this;
            // TODO: why do we require isTerminal here?
            assert (cur.node).isTerminal();
            while (cur != null) {
                path.add(cur.node);
                if (checkPath && cur.previous != null) {
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
            HistoryNode cur = this;
            while (cur != null) {
                sb.append(cur.node.getEType());
                sb.append(" <- ");
                cur = cur.previous;
            }
            return sb.toString();
        }
    }

    /*
     * Helper to extend this history path with another node. If the passed in
     * path is null, then null is yielded.
     */
    public HistoryNode extendIfNonNull(T node, HistoryNode prior) {
        if (prior == null) {
            return null;
        }
        return new HistoryNode(node, prior, prior.count + 1);
    }

    /*
     * Helper to yield the shortest non-null path of the two passed in.
     */
    public HistoryNode yieldShorter(HistoryNode a, HistoryNode b) {
        if (b == null) {
            return a;
        }
        if (a == null) {
            return b;
        }
        if (a.count < b.count) {
            return a;
        }
        return b;
    }

    /**
     * Queries the state for the shortest path which leads to a failing state.
     * 
     * @return The HistoryNode at the head of the linked list of nodes within
     *         the synoptic.model.
     */
    public abstract HistoryNode failpath();

    @Override
    public boolean isFail() {
        return failpath() != null;
    }

    // Utility function in common with all TracingSet toString definitions.
    protected static void appendWNull(StringBuilder s, Object o) {
        if (o == null) {
            s.append("0");
        } else {
            s.append(o.toString());
        }
    }
    
    @Override
    public void transition(T x) {
        transitionEventTest(x);
        transitionHistoryExtend(x);
    }
     
    public abstract void transitionEventTest(T x);
    
    public abstract void transitionHistoryExtend(T x);
    
    public void setInitial(T x) {
        HistoryNode newHistory = setInitialHistoryReset(x);
        setInitialEventTest(x, newHistory);
    }
    
    public abstract void setInitialEventTest(T x, HistoryNode newHistory);
    
    public abstract HistoryNode setInitialHistoryReset(T x);
    
    public void transition(T input, String relation, Set<String> outgoingRelations) {
        throw new UnsupportedOperationException("Multi-relational model" +
                "checking is unimplemented for this class");
    }

    public void setInitial(T x, Set<String> outgoingRelations) {
        throw new UnsupportedOperationException("Multi-relational model" +
                "checking is unimplemented for this class");
    }
}
