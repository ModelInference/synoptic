package synoptic.model.nets;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.commons.lang.NotImplementedException;

import synoptic.util.IIterableIterator;

public class SuccessorToEdgeIterator<S, T> implements
		IIterableIterator<Edge<S, T>> {
	private final S source;
	private final Iterator<T> targetIterator;
	private Edge<S, T> next = null;
	private int weight;
	
	public SuccessorToEdgeIterator(S source, Iterator<T> targetIterator, int weight) {
		this.source = source;
		this.targetIterator = targetIterator;
		this.weight  = weight;
	}

	private Edge<S, T> getNext() {
		if (targetIterator.hasNext()) {
			return new Edge<S, T>(source, targetIterator.next(), weight);
		}
		return null;
	}

	@Override
	public boolean hasNext() {
		if (next == null)
			next = getNext();
		return next != null;
	}

	@Override
	public Edge<S, T> next() {
		if (!hasNext())
			throw new NoSuchElementException();
		final Edge<S, T> oldNext = next;
		next = null;
		return oldNext;
	}

	@Override
	public void remove() {
		throw new NotImplementedException();
	}

	@Override
	public Iterator<Edge<S, T>> iterator() {
		return this;
	}
}
