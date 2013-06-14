package synoptic.invariants.fsmcheck;

import java.util.List;

import synoptic.invariants.BinaryInvariant;
import synoptic.model.event.EventType;
import synoptic.model.interfaces.INode;
import synoptic.model.interfaces.ITransition;

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
    HistoryNode wasA; // Indicates that A was seen more recently than B (failing
    // state)
    HistoryNode wasB; // Indicates that B was seen more recently than A
    EventType a, b;

    public AFbyTracingSet(EventType a, EventType b) {
        this.a = a;
        this.b = b;
    }

    public AFbyTracingSet(BinaryInvariant inv) {
        this(inv.getFirst(), inv.getSecond());
    }

    @Override
    public <Node extends INode<Node>> void setInitial(T input, List<? extends ITransition<Node>> transitions) {
        EventType name = input.getEType();
        HistoryNode newHistory = new HistoryNode(input, null, 1);
        if (name.equals(a)) {
            wasB = null;
            wasA = newHistory;
        } else {
            wasB = newHistory;
            wasA = null;
        }
    }

    @Override
    public <Node extends INode<Node>> void transition(T input, List<? extends ITransition<Node>> transitions) {
        EventType name = input.getEType();
        if (a.equals(name)) {
            wasA = preferShorter(wasB, wasA);
            wasB = null;
        } else if (b.equals(name)) {
            wasB = preferShorter(wasA, wasB);
            wasA = null;
        }
        wasA = extend(input, wasA);
        wasB = extend(input, wasB);
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
        wasA = preferShorter(wasA, casted.wasA);
        wasB = preferShorter(wasB, casted.wasB);
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
