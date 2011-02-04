package synoptic.model;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import synoptic.util.IIterableIterator;

/**
 * This adapter can transform an iterator over messages into an iterator over
 * transitions.
 * 
 * @author Sigurd Schneider
 */
public class MessageToPartitionIterator implements IIterableIterator<Partition> {
    private final Set<Partition> seen = new LinkedHashSet<Partition>();
    private final Iterator<LogEvent> messageIterator;
    private Partition next = null;
    private String relation = null;

    public MessageToPartitionIterator(Iterator<LogEvent> messageIterator) {
        this.messageIterator = messageIterator;
    }

    public MessageToPartitionIterator(Iterator<LogEvent> messageIterator,
            String relation) {
        this.messageIterator = messageIterator;
        this.relation = relation;
    }

    private Partition getNext() {
        while (messageIterator.hasNext()) {
            final Partition found = messageIterator.next().getParent();
            if (seen.add(found)
                    && (relation == null || found.getLabel().equals(relation))) {
                return found;
            }
        }
        return null;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public Partition next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        final Partition oldNext = next;
        next = null;
        return oldNext;
    }

    public boolean hasNext() {
        if (next == null) {
            next = getNext();
        }
        return next != null;
    }

    @Override
    public Iterator<Partition> iterator() {
        return this;
    }
}
