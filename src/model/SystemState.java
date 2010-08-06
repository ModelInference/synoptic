package model;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;


import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import util.IterableIterator;

import model.Action;
import model.SystemState;
import model.interfaces.IMultiSourceTransition;
import model.interfaces.INode;
import model.interfaces.ISuccessorProvider;
import model.interfaces.ITransition;

public class SystemState<T extends ITransition<SystemState<T>>> implements
		INode<SystemState<T>> {
	public final String label;
	private boolean initialState;
	private Set<ISuccessorProvider<T>> successorProviders = new LinkedHashSet<ISuccessorProvider<T>>();
	private Partition parent;

	public SystemState(String label) {
		this.label = label;
	}

	public String toString() {
		return label;
	}

	public boolean isInitialState() {
		return this.initialState;
	}

	public void setInitialState(boolean initialState) {
		this.initialState = initialState;
	}

	public String getLabel() {
		return label;
	}

	@Override
	public Partition getParent() {
		return parent;
	}

	@Override
	public void setParent(Partition parent) {
		this.parent = parent;
	}

	@Override
	public Relation<SystemState<T>> getTransition(SystemState<T> iNode,
			String relation) {
		throw new NotImplementedException();
	}

	@Override
	public IterableIterator<ITransition<SystemState<T>>> getTransitionsIterator() {
		return getTransitionsIterator(null);
	}

	@Override
	public IterableIterator<ITransition<SystemState<T>>> getTransitionsIterator(
			final String act) {
		return new IterableIterator<ITransition<SystemState<T>>>() {
			private final Set<ITransition<SystemState<T>>> seen = new HashSet<ITransition<SystemState<T>>>();
			private final Iterator<ISuccessorProvider<T>> spItr = successorProviders
					.iterator();
			private Iterator<T> tItr = spItr.next().getSuccessorIterator();
			private ITransition<SystemState<T>> next = null;

			private ITransition<SystemState<T>> getNext() {
				while (spItr.hasNext() || tItr.hasNext()) {
					if (tItr.hasNext()) {
						final T found = tItr.next();
						final ITransition<SystemState<T>> transToPart = new Transition<SystemState<T>>(
								SystemState.this, found.getTarget(), found
										.getRelation());
						if (seen.add(transToPart)
								&& (act == null || transToPart.getRelation()
										.equals(act)))
							return transToPart;
					} else
						tItr = spItr.next().getSuccessorIterator();
				}
				return null;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}

			public ITransition<SystemState<T>> next() {
				if (!hasNext())
					throw new NoSuchElementException();
				final ITransition<SystemState<T>> oldNext = next;
				next = null;
				return oldNext;
			}

			public boolean hasNext() {
				if (next == null)
					next = getNext();
				return next != null;
			}

			@Override
			public Iterator<ITransition<SystemState<T>>> iterator() {
				return this;
			}

		};
	}

	@Override
	public String toStringConcise() {
		return getLabel();
	}

	public void addSuccessorProvider(ISuccessorProvider<T> successorProvider) {
		this.successorProviders.add(successorProvider);
		for (Iterator<T> iter = successorProvider.getSuccessorIterator(); iter
				.hasNext();) {
			T next = iter.next();
			// TODO: improve this (by design)
			if (next instanceof IMultiSourceTransition<?>)
				((IMultiSourceTransition<SystemState<T>>) next).addSource(this);
		}
	}

	public Set<ISuccessorProvider<T>> getSuccessorProviders() {
		return successorProviders;
	}

	public void addSuccessorProviders(
			Set<ISuccessorProvider<T>> successorProviders) {
		this.successorProviders.addAll(successorProviders);
	}

	public void removeSuccessorProviders(
			Set<ISuccessorProvider<Partition>> fulfills) {
		this.successorProviders.removeAll(fulfills);
	}

	public IterableIterator<T> getSuccessorIterator(final Action act) {
		return new IterableIterator<T>() {
			private final Set<T> seen = new HashSet<T>();
			private final Iterator<ISuccessorProvider<T>> spItr = successorProviders
					.iterator();
			private Iterator<T> tItr = spItr.next().getSuccessorIterator();
			private T next = null;

			private T getNext() {
				while (spItr.hasNext() || tItr.hasNext()) {
					if (tItr.hasNext()) {
						final T found = tItr.next();
						if (seen.add(found)
								&& (act == null || found.getRelation()
										.equals(act)))
							return found;
					} else
						tItr = spItr.next().getSuccessorIterator();
				}
				return null;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}

			public T next() {
				if (!hasNext())
					throw new NoSuchElementException();
				final T oldNext = next;
				next = null;
				return oldNext;
			}

			public boolean hasNext() {
				if (next == null)
					next = getNext();
				return next != null;
			}

			@Override
			public Iterator<T> iterator() {
				return this;
			}

		};
	}

	public IterableIterator<T> getSuccessorIterator() {
		return getSuccessorIterator(null);
	}

	@Override
	public Set<? extends ITransition<SystemState<T>>> getTransitions() {
		Set<ITransition<SystemState<T>>> set = new HashSet<ITransition<SystemState<T>>>();
		for (ITransition<SystemState<T>> tr : getTransitionsIterator()) {
			set.add(tr);
		}
		return set;
	}
}
