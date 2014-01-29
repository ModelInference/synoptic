package synoptic.invariants.fsmcheck;

import synoptic.invariants.BinaryInvariant;
import synoptic.model.event.EventType;
import synoptic.model.interfaces.INode;

public class IntrByTracingSet<T extends INode<T>> extends TracingStateSet<T> {
    HistoryNode<T> none;
    HistoryNode<T> singleA;
    HistoryNode<T> failure;

    EventType a, b;

    public IntrByTracingSet(EventType a, EventType b) {
        this.a = a;
        this.b = b;
    }

    public IntrByTracingSet(BinaryInvariant inv) {
        this(inv.getFirst(), inv.getSecond());
    }

    @Override
    public void setInitial(T x) {
        EventType name = x.getEType();
        HistoryNode<T> newHistory = new HistoryNode<T>(x, null, 1);
        none = singleA = failure = null;
        if (a.equals(name)) {
            singleA = newHistory;
        } else {
            none = newHistory;
        }
    }

    @Override
    public void transition(T x) {
        EventType name = x.getEType();

        boolean isA = a.equals(name);
        boolean isB = b.equals(name);

        if (isA) {
            failure = preferShorter(singleA, failure);
            singleA = none;
            none = null;
        }

        if (isB) {
            none = preferShorter(singleA, none);
            singleA = null;
        }

        // Advance history for all states.
        none = extend(x, none);
        singleA = extend(x, singleA);
        failure = extend(x, failure);
    }

    @Override
    public HistoryNode<T> failpath() {
        return failure;
    }

    @Override
    public IntrByTracingSet<T> copy() {
        IntrByTracingSet<T> result = new IntrByTracingSet<T>(a, b);
        result.none = none;
        result.singleA = singleA;
        result.failure = failure;
        return result;
    }

    @Override
    public void mergeWith(TracingStateSet<T> other) {
        IntrByTracingSet<T> casted = (IntrByTracingSet<T>) other;
        none = preferShorter(none, casted.none);
        singleA = preferShorter(singleA, casted.singleA);
        failure = preferShorter(failure, casted.failure);
    }

    @Override
    public boolean isSubset(TracingStateSet<T> other) {
        IntrByTracingSet<T> casted = (IntrByTracingSet<T>) other;
        if (casted.none == null) {
            if (none != null) {
                return false;
            }
        }
        if (casted.singleA == null) {
            if (singleA != null) {
                return false;
            }
        }
        if (casted.failure == null) {
            if (failure != null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("IntrBy: ");
        appendWNull(result, failure); // Failure case first.
        result.append(" | ");
        appendWNull(result, singleA);
        result.append(" | ");
        appendWNull(result, none);
        return result.toString();
    }
}
