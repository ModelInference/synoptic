package model;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import util.IterableIterator;


public class MessageToPartitionIterator implements IterableIterator<Partition> {
	private final Set<Partition> seen = new HashSet<Partition>();
	private final Iterator<MessageEvent> messageIterator;
	private Partition next = null;
	private Action act = null;
	
	public MessageToPartitionIterator(Iterator<MessageEvent> messageIterator) {
		this.messageIterator = messageIterator;
	}
	
	public MessageToPartitionIterator(Iterator<MessageEvent> messageIterator, Action act) {
		this.messageIterator = messageIterator;
		this.act = act;
	}

	private Partition getNext() {
		while (messageIterator.hasNext()) {
			final Partition found = messageIterator.next().getParent();
			if (seen.add(found) && (act == null || found.getAction().equals(act)))
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
