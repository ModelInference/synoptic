package model;

import invariants.TemporalInvariantSet;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import daikon.inv.Invariant;

import model.interfaces.IMultiSourceTransition;
import model.interfaces.INode;
import model.interfaces.ISuccessorProvider;
import model.interfaces.ITransition;

import algorithms.graph.PartitionSplit;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import util.IterableIterator;

public class Partition implements
		IMultiSourceTransition<SystemState<Partition>>, INode<Partition>,
		ISuccessorProvider<Partition> {
	protected final Set<MessageEvent> messages;
	private MultiSourceTransition<SystemState<Partition>> transition;

	public Partition(Set<MessageEvent> messages,
			Set<SystemState<Partition>> sources, SystemState<Partition> target) {
		this.messages = messages;
		for (final MessageEvent m : messages)
			m.setParent(this);
		transition = new MultiSourceTransition<SystemState<Partition>>(sources,
				target, null);
	}

	@Override
	public Action getAction() {
		return messages.iterator().next().getAction();
	}

	public void addMessage(MessageEvent message) {
		messages.add(message);
		message.setParent(this);
	}

	public void addAllMessages(Collection<MessageEvent> messages) {
		this.messages.addAll(messages);
		for (final MessageEvent m : messages) {
			m.setParent(this);
		}
	}

	public IterableIterator<Relation<Partition>> getTransitionsIterator() {
		return getTransitionsIterator(null);
	}

	public Set<MessageEvent> getMessages() {
		return messages;
	}

	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("Partition " + hashCode() + " " + getAction().getLabel());
		// for (StateWrapper w : messageWrappers) {
		// str.append(", " + w.label.toString());
		// }
		str.append(" [" + messages.size() + "].");
		return str.toString();
	}

	public PartitionSplit getCandidateDivision(ITransition<Partition> trans) {
		PartitionSplit ret = null;
		for (final MessageEvent otherExpr : messages) {
			if (fulfillsStrong(otherExpr, trans)) {
				if (ret != null)
					ret.addFulfills(otherExpr);
			} else {
				if (ret == null) {
					ret = new PartitionSplit(this);
					for (final MessageEvent e2 : messages) {
						if (e2.equals(otherExpr))
							break;
						ret.addFulfills(e2);
					}
				}
				ret.addFulfillsNot(otherExpr);
			}
		}
		if (ret == null) {
			ret = PartitionSplit.onlyFulfills(this);
		}
		return ret;
	}

	private static boolean fulfillsStrong(MessageEvent otherExpr,
			ITransition<Partition> trans) {
		for (final ITransition<MessageEvent> t : otherExpr.getTransitions())
			if (t.getAction().equals(trans.getAction())
					&& t.getTarget().getParent().equals(trans.getTarget()))
				return true;
		return false;
	}

	public String getLabel() {
		return messages.iterator().next().getLabel();
	}

	public int size() {
		return messages.size();
	}
	
	public Set<Relation<Partition>> getTransitions() {
		Set<Relation<Partition>> set = new HashSet<Relation<Partition>>();
		for (Relation<Partition> tr : getTransitionsIterator()) {
			set.add(tr);
			PartitionSplit s = getCandidateDivision(tr);
			List<Invariant> all = TemporalInvariantSet.generateInvariants(tr.getSource().getMessages()); 
			List<Invariant> sInv = TemporalInvariantSet.generateInvariants(s.getFulfills());
			List<Invariant> sInvNot = TemporalInvariantSet.generateInvariants(s.getFulfillsNot());
			List<Invariant> rel = TemporalInvariantSet.getRelevantInvariants(sInv, sInvNot, all);
			List<Invariant> flow = TemporalInvariantSet.generateFlowInvariants(tr.getSource().getMessages(), tr.getAction(), tr.getTarget().getAction().getLabel());
			tr.setInvariants(flow);
			tr.setFrequency(s.getFulfills().size()/(double)tr.getSource().getMessages().size());
			System.out.println(flow);
		}
		return set;
	}

	@Override
	public IterableIterator<Relation<Partition>> getTransitionsIterator(
			final Action act) {
		return new IterableIterator<Relation<Partition>>() {
			private final Set<ITransition<Partition>> seen = new HashSet<ITransition<Partition>>();
			private final Iterator<MessageEvent> msgItr = messages.iterator();
			private Iterator<? extends ITransition<MessageEvent>> transItr = act == null ? msgItr
					.next().getTransitions().iterator()
					: msgItr.next().getTransitions(act).iterator();
			private Relation<Partition> next = null;

			private Relation<Partition> getNext() {
				while (transItr.hasNext() || msgItr.hasNext()) {
					if (transItr.hasNext()) {
						final ITransition<MessageEvent> found = transItr.next();
						final Relation<Partition> transToPart = new Relation<Partition>(
								found.getSource().getParent(), found
										.getTarget().getParent(), found
										.getAction());
						if (seen.add(transToPart))
							return transToPart;
					} else
						transItr = act == null ? msgItr.next()
								.getTransitions().iterator() : msgItr.next()
								.getTransitions(act).iterator();
				}

				return null;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}

			public Relation<Partition> next() {
				if (!hasNext())
					throw new NoSuchElementException();
				final Relation<Partition> oldNext = next;
				next = null;
				return oldNext;
			}

			public boolean hasNext() {
				if (next == null)
					next = getNext();
				return next != null;
			}

			@Override
			public Iterator<Relation<Partition>> iterator() {
				return this;
			}

		};
	}

	@Override
	public ITransition<Partition> getTransition(Partition iNode, Action action) {
		for (Iterator<Relation<Partition>> iter = getTransitionsIterator(action); iter
				.hasNext();) {
			ITransition<Partition> t = iter.next();
			if (t.getTarget().equals(iNode))
				return t;
		}
		return null;
	}

	@Override
	public Partition getParent() {
		throw new NotImplementedException();
	}

	@Override
	public void setParent(Partition parent) {
		throw new NotImplementedException();
	}

	public void removeMessages(Set<MessageEvent> messageList) {
		messages.removeAll(messageList);
	}

	@Override
	public SystemState<Partition> getTarget() {
		return transition.getTarget();
	}

	@Override
	public void setTarget(SystemState<Partition> target) {
		transition.setTarget(target);
	}

	@Override
	public String toStringConcise() {
		return getAction().getLabel();
	}

	@Override
	public void addSource(SystemState<Partition> source) {
		transition.addSource(source);
	}

	@Override
	public Set<SystemState<Partition>> getSources() {
		return transition.getSources();
	}

	@Override
	public void addSources(Set<SystemState<Partition>> sources) {
		transition.addSources(sources);
	}

	@Override
	public void clearSources() {
		transition.clearSources();
	}

	@Override
	public void addCount(int count) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public SystemState<Partition> getSource() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSource(SystemState<Partition> source) {
		// TODO Auto-generated method stub

	}

	@Override
	public IterableIterator<Partition> getSuccessorIterator() {
		return getSuccessorIterator(null);
	}

	@Override
	public IterableIterator<Partition> getSuccessorIterator(final Action act) {
		return new IterableIterator<Partition>() {
			private final Set<Partition> seen = new HashSet<Partition>();
			private final Iterator<Relation<Partition>> trnsItr = getTransitionsIterator(act);
			private Partition next = null;

			private Partition getNext() {
				while (trnsItr.hasNext()) {
					final Partition found = trnsItr.next().getTarget();
					if (seen.add(found))
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

		};
	}
}
