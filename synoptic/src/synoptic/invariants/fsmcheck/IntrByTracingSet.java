package synoptic.invariants.fsmcheck;

import synoptic.invariants.BinaryInvariant;
import synoptic.model.event.EventType;
import synoptic.model.interfaces.INode;

public class IntrByTracingSet<T extends INode<T>> extends TracingStateSet<T> {
    HistoryNode<T> aNotSeen;
    HistoryNode<T> aSeenOnce;
    HistoryNode<T> aSeenMoreThanOnce;

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
        aNotSeen = aSeenOnce = aSeenMoreThanOnce = null;
        if (a.equals(name)) {
            aSeenOnce = newHistory;
        } else {
            aNotSeen = newHistory;
        }
    }

    @Override
    public void transition(T x) {
        EventType name = x.getEType();

        boolean isA = a.equals(name);
        boolean isB = b.equals(name);

        if (isA) {
            aSeenMoreThanOnce = preferShorter(aSeenOnce, aSeenMoreThanOnce);
            aSeenOnce = aNotSeen;
            aNotSeen = null;
        }

        if (isB) {
            aNotSeen = preferShorter(aSeenOnce, aNotSeen);
            aSeenOnce = null;
        }

        // Advance history for all states.
        aNotSeen = extend(x, aNotSeen);
        aSeenOnce = extend(x, aSeenOnce);
        aSeenMoreThanOnce = extend(x, aSeenMoreThanOnce);
    }

    @Override
    public HistoryNode<T> failpath() {
        return aSeenMoreThanOnce;
    }

    @Override
    public IntrByTracingSet<T> copy() {
        IntrByTracingSet<T> result = new IntrByTracingSet<T>(a, b);
        result.aNotSeen = aNotSeen;
        result.aSeenOnce = aSeenOnce;
        result.aSeenMoreThanOnce = aSeenMoreThanOnce;
        return result;
    }

    @Override
    public void mergeWith(TracingStateSet<T> other) {
        IntrByTracingSet<T> casted = (IntrByTracingSet<T>) other;
        aNotSeen = preferShorter(aNotSeen, casted.aNotSeen);
        aSeenOnce = preferShorter(aSeenOnce, casted.aSeenOnce);
        aSeenMoreThanOnce = preferShorter(aSeenMoreThanOnce, casted.aSeenMoreThanOnce);
    }

    @Override
    public boolean isSubset(TracingStateSet<T> other) {
        IntrByTracingSet<T> casted = (IntrByTracingSet<T>) other;
        if (casted.aNotSeen == null) {
            if (aNotSeen != null) {
                return false;
            }
        }
        if (casted.aSeenOnce == null) {
            if (aSeenOnce != null) {
                return false;
            }
        }
        if (casted.aSeenMoreThanOnce == null) {
            if (aSeenMoreThanOnce != null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("IntrBy: ");
        appendWNull(result, aSeenMoreThanOnce); // Failure case first.
        result.append(" | ");
        appendWNull(result, aSeenOnce);
        result.append(" | ");
        appendWNull(result, aNotSeen);
        return result.toString();
    }
}
