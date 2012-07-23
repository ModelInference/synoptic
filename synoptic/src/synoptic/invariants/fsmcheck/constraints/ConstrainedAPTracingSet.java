package synoptic.invariants.fsmcheck.constraints;

import synoptic.invariants.constraints.LowerBoundConstraint;
import synoptic.invariants.constraints.TempConstrainedInvariant;
import synoptic.invariants.constraints.UpperBoundConstraint;
import synoptic.model.event.EventType;
import synoptic.model.interfaces.INode;

/**
 * Represents a set of constrained "A always precedes B" synoptic.invariants to
 * simulate, recording the shortest historical path to reach a particular state.
 * We can assume that every B has a preceding A since this tracing set is used
 * after model checking unconstrained invariants. This finite machine enters a
 * failure state when A precedes B in an amount of time that violates threshold
 * constraint of invariant.
 * 
 * @param <T>
 *            The node type, used as an input, and stored in path-history.
 */
public class ConstrainedAPTracingSet<T extends INode<T>> extends
        ConstrainedTracingStateSet<T> {

    public ConstrainedAPTracingSet(EventType a, EventType b) {
        super(a, b);
    }

    @SuppressWarnings("rawtypes")
    public ConstrainedAPTracingSet(TempConstrainedInvariant inv) {
        this(inv.getFirst(), inv.getSecond());
        this.constr = inv.getConstraint();
        if (constr.getClass().equals(LowerBoundConstraint.class)) {
            dfa = new APLowerDFA<T>(inv);
        } else if (constr.getClass().equals(UpperBoundConstraint.class)) {
            dfa = new APUpperDFA<T>(inv);
        }
    }

    @Override
    public ConstrainedAPTracingSet<T> copy() {
        ConstrainedAPTracingSet<T> result = new ConstrainedAPTracingSet<T>(a, b);
        result.constr = constr;
        result.dfa = dfa;
        result.history = history;
        return result;
    }

    @Override
    public void mergeWith(ConstrainedTracingStateSet<T> other) {
        ConstrainedAPTracingSet<T> casted = (ConstrainedAPTracingSet<T>) other;
        history = preferShorter(history, casted.history);
    }

    @Override
    public boolean isSubset(ConstrainedTracingStateSet<T> other) {
        ConstrainedAPTracingSet<T> casted = (ConstrainedAPTracingSet<T>) other;
        if (casted.history == null) {
            if (history != null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return super.toString("ConstrainedAP: ");
    }
}