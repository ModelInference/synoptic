package synoptic.invariants.concurrency;

import java.util.List;
import java.util.Set;

import synoptic.invariants.BinaryInvariant;
import synoptic.model.event.EventType;
import synoptic.model.interfaces.INode;

/**
 * Base class for all concurrency invariants. A key feature of concurrency
 * invariants in Synoptic is that they are symmetric -- inv(x,y) = inv(y,x).
 * Relying on this observation this class implements hashCode() and equals().
 */
public abstract class ConcurrencyInvariant extends BinaryInvariant {

    public ConcurrencyInvariant(EventType typeFirst, EventType typeSecond,
            Set<String> relations) {
        super(typeFirst, typeSecond, relations);
    }

    public ConcurrencyInvariant(EventType typeFirst, EventType typeSecond,
            String relation) {
        super(typeFirst, typeSecond, relation);
    }

    /**
     * TODO: Concurrency invariants cannot be easily shortened?
     */
    @Override
    public <T extends INode<T>> List<T> shorten(List<T> trace) {
        return trace;
    }

    /**
     * NOTE: concurrency invariants are symmetric. For example:
     * 
     * <pre>
     * AlwaysConcurrentWith(x,y) == AlwaysConcurrentWith(y,x)
     * NeverConcurrentWith(x,y) == NeverConcurrentWith(y,x)
     * </pre>
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = getClass().hashCode();

        int f = (first == null ? 0 : first.hashCode());
        int s = (second == null ? 0 : second.hashCode());

        result = prime * result + (f + s);
        result = prime * result
                + (relations == null ? 0 : relations.hashCode());
        return result;
    }

    /**
     * NOTE: concurrency invariants are symmetric. For example:
     * 
     * <pre>
     * AlwaysConcurrentWith(x,y) == AlwaysConcurrentWith(y,x)
     * NeverConcurrentWith(x,y) == NeverConcurrentWith(y,x)
     * </pre>
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        BinaryInvariant other = (BinaryInvariant) obj;
        if (first == null) {
            if (other.getFirst() != null) {
                return false;
            }
        }
        if (second == null) {
            if (other.getSecond() != null) {
                return false;
            }
        }

        if (!(first.equals(other.getFirst()) && second
                .equals(other.getSecond()))
                && !(second.equals(other.getFirst()) && first.equals(other
                        .getSecond()))) {
            return false;
        }

        if (relations == null) {
            if (other.getRelations() != null) {
                return false;
            }
        } else if (!relations.equals(other.getRelations())) {
            return false;
        }

        return true;
    }
}
