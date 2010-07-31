package model;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import util.IterableIterator;


public class MessageToPartitionIterator implements IterableIterator<Partition> {
	private final Set<Partition> seen = new LinkedHashSet<Partition>();
	private final Iterator<MessageEvent> messageIterator;
	private Partition next = null;
	private String relation = null;
	
	public MessageToPartitionIterator(Iterator<MessageEvent> messageIterator) {
		this.messageIterator = messageIterator;
	}
	
	public MessageToPartitionIterator(Iterator<MessageEvent> messageIterator, String relation) {
		this.messageIterator = messageIterator;
		this.relation = relation;
	}

	private Partition getNext() {
		while (messageIterator.hasNext()) {
			final Partition found = messageIterator.next().getParent();
			if (seen.add(found) && (relation == null || found.getAction().equals(relation)))
				return found;
		}
		return null;
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}

	public Partition next() {
		if (!hasNext())
			throw new NoSuchElementException();
		final Partition oldNext = next;
		next = null;
		return oldNext;
	}

	public boolean hasNext() {
		if (next == null)
			next = getNext();
		return next != null;
	}

	@Override
	public Iterator<Partition> iterator() {
		return this;
	}
}
