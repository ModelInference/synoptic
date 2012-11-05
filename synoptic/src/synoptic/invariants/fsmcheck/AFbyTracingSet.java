package synoptic.invariants.fsmcheck;

import synoptic.invariants.BinaryInvariant;
import synoptic.model.event.EventType;
import synoptic.model.interfaces.INode;

/**
 * Represents a "A always followed by B" synoptic.invariants to simulate,
 * recording the shortest historical path to reach a particular state. This
 * finite state machine enters a failure state when A is encountered, and enters
 * a success state when B is encountered. This means that the failure state upon
 * encountering a final node indicates which, of A and B, was last encountered.
 * NOTE: ensure this documentation stays consistent with AlwaysFollowedSet.
 * 
 * @author Michael Sloan (mgsloan@gmail.com)
 * @param <T>
 *            The node type, used as an input, and stored in path-history.
 * @see AFbyInvFsms
 * @see TracingStateSet
 */
public class AFbyTracingSet<T extends INode<T>> extends TracingStateSet<T> {
    // If non null, then A was seen more recently than B (failing state) 
    HistoryNode wasA;
    // If non null, then B was seen more recently than A 
    HistoryNode wasB;
    EventType a, b;

    public AFbyTracingSet(EventType a, EventType b) {
        this.a = a;
        this.b = b;
    }

    public AFbyTracingSet(BinaryInvariant inv) {
        this(inv.getFirst(), inv.getSecond());
    }

    // Can initialize to any state based on x's EventType
    @Override
    public void setInitial(T x) {
        EventType name = x.getEType();
        HistoryNode newHistory = new HistoryNode(x, null, 1);
        if (name.equals(a)) {
            wasB = null;
            wasA = newHistory;
        } else {
            wasB = newHistory;
            wasA = null;
        }
    }
    
    @Override
    public void transitionEventTest(T x) {
        EventType name = x.getEType();
        // Transitions to wasA or wasB based on x's EventType
        if (a.equals(name)) {
            wasA = yieldShorter(wasB, wasA);
            wasB = null;
        } else if (b.equals(name)) {
            wasB = yieldShorter(wasA, wasB);
            wasA = null;
        }
    }
    
    @Override
    public void transitionHistoryExtend(T x) {
        wasA = extendIfNonNull(x, wasA);
        wasB = extendIfNonNull(x, wasB);
    }

    @Override
     public HistoryNode failpath() {
        return wasA;
    }

    @Override
    public AFbyTracingSet<T> copy() {
        AFbyTracingSet<T> result = new AFbyTracingSet<T>(a, b);
        result.wasA = wasA;
        result.wasB = wasB;
        return result;
    }

    @Override
    public void mergeWith(TracingStateSet<T> other) {
        AFbyTracingSet<T> casted = (AFbyTracingSet<T>) other;
        wasA = yieldShorter(wasA, casted.wasA);
        wasB = yieldShorter(wasB, casted.wasB);
    }

    @Override
    public boolean isSubset(TracingStateSet<T> other) {
        AFbyTracingSet<T> casted = (AFbyTracingSet<T>) other;
        if (casted.wasA == null) {
            if (wasA != null) {
                return false;
            }
        }
        if (casted.wasB == null) {
            if (wasB != null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("AFby: ");
        appendWNull(result, wasA); // Failure case first.
        result.append(" | ");
        appendWNull(result, wasB);
        return result.toString();
    }
}
