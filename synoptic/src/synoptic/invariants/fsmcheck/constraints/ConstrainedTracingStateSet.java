package synoptic.invariants.fsmcheck.constraints;

import synoptic.invariants.constraints.IThresholdConstraint;
import synoptic.invariants.constraints.LowerBoundConstraint;
import synoptic.invariants.fsmcheck.HistoryNode;
import synoptic.model.EventNode;
import synoptic.model.event.EventType;
import synoptic.model.interfaces.INode;
import synoptic.model.interfaces.ITransition;
import synoptic.util.time.ITime;

/**
 * Abstract NFA state set which keeps the shortest path justifying a given state
 * being inhabited. This allows for synoptic.model checking which yields short
 * counterexample paths for failing synoptic.invariants.
 * 
 * @author Michael Sloan (mgsloan@gmail.com)
 * @param <T>
 *            The node type, used as an input, and stored in path-history.
 */
public abstract class ConstrainedTracingStateSet<T extends INode<T>> implements
        IConstrainedStateSet<T, ConstrainedTracingStateSet<T>> {
    public static boolean checkPath = false;

    protected HistoryNode<T> history;
    protected EventType a, b;
    protected IDFA<T> dfa;
    protected IThresholdConstraint constr;

    public ConstrainedTracingStateSet(EventType a, EventType b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public void setInitial(T x) {
        HistoryNode<T> newHistory = new HistoryNode<T>(x, null, 1);
        history = newHistory;
    }

    @Override
    public void transition(T x, ITransition<T> trans) {
        ITime time;
        if (x instanceof EventNode) {
        	time = trans.getTimeDelta();
        } else { // Partition node
	        if (constr.getClass().equals(LowerBoundConstraint.class)) {
	            time = trans.getDeltaSeries().getMinDelta();
	        } else {
	            time = trans.getDeltaSeries().getMaxDelta();
	        }
        }
        dfa.transition(x, time);
        history = extend(x, history);
    }

    /**
     * Queries the state for the shortest path which leads to a failing state.
     * 
     * @return The HistoryNode at the head of the linked list of nodes within
     *         the synoptic.model.
     */
    public HistoryNode<T> failpath() {
        if (dfa.getState().isSuccess()) {
            return null;
        }
        return history;
    }

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
    public HistoryNode<T> preferShorter(HistoryNode<T> aa, HistoryNode<T> bb) {
        if (bb == null) {
            return aa;
        }
        if (aa == null) {
            return bb;
        }
        if (aa.count < bb.count) {
            return aa;
        }
        return bb;
    }

    @Override
    public boolean isFail() {
        return failpath() != null;
    }

    protected String toString(String headerStr) {
        StringBuilder result = new StringBuilder();
        result.append(headerStr);

        if (history == null) {
            result.append("null");
        } else {
            result.append(history.toString());
        }

        return result.toString();
    }
}
