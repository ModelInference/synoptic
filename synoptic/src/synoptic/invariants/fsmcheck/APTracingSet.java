package synoptic.invariants.fsmcheck;

import synoptic.invariants.BinaryInvariant;
import synoptic.model.event.EventType;
import synoptic.model.interfaces.INode;

/**
 * Represents a set of "A always precedes B" synoptic.invariants to simulate,
 * recording the shortest historical path to reach a particular state. This
 * finite state machine enters a permanent success state upon encountering A,
 * and enters a permanent failure state upon encountering B. This reflects the
 * fact that the first of the two events encountered is the only thing relevant
 * to the failure state of the invariant.
 * 
 * @author Michael Sloan (mgsloan@gmail.com)
 * @param <T>
 *            The node type, used as an input, and stored in path-history.
 * @see APInvFsms
 * @see TracingStateSet
 */
public class APTracingSet<T extends INode<T>> extends TracingStateSet<T> {
    HistoryNode neitherSeen; // Neither A or B yet seen
    HistoryNode firstA; // A seen before B (permanent success)
    HistoryNode firstB; // B seen before A (permanent failure)
    EventType a, b;

    public APTracingSet(EventType a, EventType b) {
        this.a = a;
        this.b = b;
    }

    public APTracingSet(BinaryInvariant inv) {
        this(inv.getFirst(), inv.getSecond());
    }

    @Override
    public void setInitial(T x) {
        EventType name = x.getEType();
        HistoryNode newHistory = new HistoryNode(x, null, 1);
        neitherSeen = firstA = firstB = null;
        if (a.equals(name)) {
            firstA = newHistory;
        } else if (b.equals(name)) {
            firstB = newHistory;
        } else {
            neitherSeen = newHistory;
        }
    }

    @Override
    public void transition(T x) {
        EventType name = x.getEType();
        if (a.equals(name)) {
            firstA = preferShorter(neitherSeen, firstA);
            neitherSeen = null;
        } else if (b.equals(name)) {
            firstB = preferShorter(neitherSeen, firstB);
            neitherSeen = null;
        }
        neitherSeen = extend(x, neitherSeen);
        firstA = extend(x, firstA);
        firstB = extend(x, firstB);
    }

    @Override
    public HistoryNode failpath() {
        return firstB;
    }

    @Override
    public APTracingSet<T> copy() {
        APTracingSet<T> result = new APTracingSet<T>(a, b);
        result.neitherSeen = neitherSeen;
        result.firstA = firstA;
        result.firstB = firstB;
        return result;
    }

    @Override
    public void mergeWith(TracingStateSet<T> other) {
        APTracingSet<T> casted = (APTracingSet<T>) other;
        neitherSeen = preferShorter(neitherSeen, casted.neitherSeen);
        firstA = preferShorter(firstA, casted.firstA);
        firstB = preferShorter(firstB, casted.firstB);
    }

    @Override
    public boolean isSubset(TracingStateSet<T> other) {
        APTracingSet<T> casted = (APTracingSet<T>) other;
        if (casted.neitherSeen == null) {
            if (neitherSeen != null) {
                return false;
            }
        }
        if (casted.firstA == null) {
            if (firstA != null) {
                return false;
            }
        }
        if (casted.firstB == null) {
            if (firstB != null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("AP: ");
        appendWNull(result, firstB); // Failure case first.
        result.append(" | ");
        appendWNull(result, firstA);
        result.append(" | ");
        appendWNull(result, neitherSeen);
        return result.toString();
    }
}
