package synoptic.model;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.lang.NotImplementedException;

import synoptic.algorithms.graph.PartitionSplit;
import synoptic.model.interfaces.INode;
import synoptic.model.interfaces.ITransition;
import synoptic.util.IterableIterator;

/**
 * Implements a partition in a partition graph. Partitions are nodes, but they
 * do not explicitly represent their edges. Instead, they know the MessageEvents
 * they contain, and generate the edges on the fly using existential
 * abstraction.
 * 
 * The class is complicated by the fact that in the state based view, each
 * partition corresponds to possibly several transitions. The implementation
 * here can only handle state based views where a partition corresponds to a set
 * of transitions that all have the same target (but possibly different
 * sources).
 * 
 * @author sigurd
 * 
 */
public class Partition implements INode<Partition>, Comparable<Partition> {
	protected final Set<MessageEvent> messages;
	private String label;

	public Partition(Set<MessageEvent> messages) {
		this.messages = new LinkedHashSet<MessageEvent>(messages);
		for (final MessageEvent m : messages)
			m.setParent(this);
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

	/**	
	 * Transitions between partitions are not stored but generated on demand using this iterator 
	 */
	public IterableIterator<Relation<Partition>> getTransitionsIterator() {
		return getTransitionsIterator(null);
	}

	public Set<MessageEvent> getMessages() {
		return messages;
	}

	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("Partition " + hashCode());
		// for (StateWrapper w : messageWrappers) {
		// str.append(", " + w.label.toString());
		// }
		str.append(" [" + messages.size() + "].");
		return str.toString();
	}

	/**
	 * Split the messages according to the presence of an outgoing transition
	 * trans (the source in trans is ignored here)
	 * 
	 * @param trans the transition that will be checked for
	 * @return the resulting split
	 */
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
			}
		}
		if (ret == null) {
			ret = PartitionSplit.onlyFulfills(this);
		}
		return ret;
	}

	/**
	 * Split the partition according to an incoming transition from from
	 * labeled with {@code relation}.
	 * 
	 * @param previous the partition the transition should be incoming from
	 * @param relation provides the relation name to consider
	 * @return returns the resulting split
	 */
	public PartitionSplit getCandidateDivisionBasedOnIncoming(Partition previous,
			String relation) {
		PartitionSplit candidateSplit = new PartitionSplit(this);
		Set<MessageEvent> messagesReachableFromPrevious = new HashSet<MessageEvent>();
		for (final MessageEvent otherExpr : previous.messages) {
			messagesReachableFromPrevious.addAll(otherExpr.getSuccessors(relation));
			messagesReachableFromPrevious.retainAll(messages);
		}
		for (MessageEvent m : messages) {
			if (messagesReachableFromPrevious.contains(m))
				candidateSplit.addFulfills(m);
		}

		return candidateSplit;
	}

	private static boolean fulfillsStrong(MessageEvent otherExpr,
			ITransition<Partition> trans) {
		for (final ITransition<MessageEvent> t : otherExpr.getTransitions())
			if (t.getRelation().equals(trans.getRelation())
					&& t.getTarget().getParent().equals(trans.getTarget()))
				return true;
		return false;
	}

	public String getLabel() {
		if (label != null)
			return label;
		return messages.iterator().next().getLabel();
	}

	public int size() {
		return messages.size();
	}
	

	/**
	 * This method returns the set of transitions. It augments the edges with
	 * information about frequency and number of observation.
	 */
	public List<Relation<Partition>> getTransitions() {
		List<Relation<Partition>> result = new ArrayList<Relation<Partition>>();
		for (Relation<Partition> tr : getTransitionsIterator()) {
			result.add(tr);
			PartitionSplit s = getCandidateDivision(tr);
			// List<Invariant> all = TemporalInvariantSet.generateInvariants(tr
			// .getSource().getMessages());
			// List<Invariant> sInv = TemporalInvariantSet.generateInvariants(s
			// .getFulfills());
			// List<Invariant> sInvNot =
			// TemporalInvariantSet.generateInvariants(s
			// .getFulfillsNot());
			// List<Invariant> rel = TemporalInvariantSet.getRelevantInvariants(
			// sInv, sInvNot, all);
			// List<Invariant> flow =
			// TemporalInvariantSet.generateFlowInvariants(
			// tr.getSource().getMessages(), tr.getAction(), tr
			// .getTarget().getAction().getLabel());
			// tr.setInvariants(rel);
			//System.out.println(s.getFulfills().size() + " "
			//		+ s.getFulfillsNot().size());

			tr.setFrequency((double) s.getFulfills().size()
					/ (double) tr.getSource().getMessages().size());
			tr.addWeight(s.getFulfills().size());
			// System.out.println(flow);
		}
		return result;
	}

	/**
	 * Generate Edges on the fly. We examine all contained messages and find the
	 * appropriate successor messages. We then check to which partition the
	 * successor messages belong and create an edge between the partitions.
	 * Duplicates are eliminated.
	 */
	@Override
	public IterableIterator<Relation<Partition>> getTransitionsIterator(
			final String act) {
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
										.getRelation());
						if (seen.add(transToPart))
							return transToPart;
					} else
						transItr = act == null ? msgItr.next().getTransitions()
								.iterator() : msgItr.next().getTransitions(act)
								.iterator();
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
	public ITransition<Partition> getTransition(Partition iNode, String action) {
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
	public String toStringConcise() {
		return getLabel();
		// return toString();
	}


	public void setLabel(String str) {
		this.label = str;
	}

	//TODO: benchmark how expensive this is -- cache and maintain?
	public boolean isFinal() {
		for (MessageEvent e : messages) {
			if (e.isFinal()) return true;
		}
		return false;
	}

	@Override
	public int compareTo(Partition other) {
		// compare references
		if (this == other) {
			return 0;
		}
		
		// 1. compare label strings
		int labelCmp = this.getLabel().compareTo(other.getLabel());
		if (labelCmp != 0) {
			return labelCmp;
		}
		
		// 2. compare number of children
		List<Relation<Partition>> tnsThis = getTransitions();
		List<Relation<Partition>> tnsOther = getTransitions();
		int childrenCmp = ((Integer) tnsThis.size()).compareTo(tnsOther.size());
		if (childrenCmp != 0) {
			return childrenCmp;
		}
		
		// 3. compare labels of children
		Collections.sort(tnsThis);
		Collections.sort(tnsOther);
		int index = 0;
		int childCmp;
		for (Relation<Partition> p : tnsThis) {
			// sizes of tnsThis and tnsOther were checked to be equal above
			Relation<Partition> p2 = tnsOther.get(index);
			childCmp = p.compareTo(p2);
			if (childCmp != 0) {
				return childCmp;
			}
		}
		return 0;
	}
}
