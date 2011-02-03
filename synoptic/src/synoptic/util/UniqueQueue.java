package synoptic.util;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * A Queue in which every element is only inserted once. Further insertions have
 * no effect.
 * 
 * @author Clemens Hammacher
 */

public class UniqueQueue<E> extends ArrayDeque<E> {
    private static final long serialVersionUID = -2015019927214711123L;
    private final Set<E> seen;

    public UniqueQueue() {
        super();
        seen = new HashSet<E>();
    }

    public UniqueQueue(Collection<? extends E> c) {
        super(c);
        seen = new HashSet<E>(c);
    }

    public UniqueQueue(Comparator<E> cmp) {
        super();
        seen = new TreeSet<E>(cmp);
    }

    public UniqueQueue(int numElements) {
        super(numElements);
        seen = new HashSet<E>(numElements);
    }

    public Set<E> getSeen() {
        return Collections.unmodifiableSet(seen);
    }

    @Override
    public void addFirst(E e) {
        if (seen.add(e)) {
            super.addFirst(e);
        }
    }

    @Override
    public void addLast(E e) {
        if (seen.add(e)) {
            super.addLast(e);
        }
    }

    @Override
    public boolean add(E e) {
        if (!seen.add(e)) {
            return false;
        }
        super.addLast(e);
        return true;
    }

    @Override
    public boolean offer(E e) {
        if (!seen.add(e)) {
            return false;
        }
        super.addLast(e);
        return true;
    }

    @Override
    public boolean offerFirst(E e) {
        if (!seen.add(e)) {
            return false;
        }
        super.addFirst(e);
        return true;
    }

    @Override
    public boolean offerLast(E e) {
        if (!seen.add(e)) {
            return false;
        }
        super.addLast(e);
        return true;
    }

    /**
     * Resets the set of seen elements. After this operation, every elements is
     * again accepted exactly once.
     */
    public void clearSeen() {
        seen.clear();
    }

}
