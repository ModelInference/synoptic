package synoptic.invariants.fsmcheck;


import synoptic.model.interfaces.INode;

/**
 * Abstract NFA state set which keeps the shortest path justifying a given state
 * being inhabited. This allows for synoptic.model checking which yields short
 * counterexample paths for failing synoptic.invariants.
 * 
 * @param <T>
 *            The node type, used as an input, and stored in path-history.
 */
public abstract class TracingStateSet<T extends INode<T>> implements
        IStateSet<T, TracingStateSet<T>> {
    public static boolean checkPath = false;

    /*
     * Helper to extend this history path with another node. If the passed in
     * path is null, then null is yielded.
     */
    public HistoryNode<T> extend(T node, HistoryNode<T> prior) {
        if (prior == null) {
            return null;
        }
        return new HistoryNode<T>(node, prior, prior.count + 1);
    }

    /*
     * Helper to yield the shortest non-null path of the two passed in.
     */
    public <HNode extends HistoryNode<T>> HNode preferShorter(HNode a, HNode b) {
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
    public abstract HistoryNode<T> failpath();

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

}
