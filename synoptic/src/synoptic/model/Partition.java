package synoptic.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.lang.NotImplementedException;

import synoptic.algorithms.graph.PartitionSplit;
import synoptic.model.interfaces.INode;
import synoptic.model.interfaces.ITransition;
import synoptic.util.IIterableIterator;
import synoptic.util.InternalSynopticException;

/**
 * Implements a partition in a partition graph. Partitions are nodes, but they
 * do not explicitly represent their edges. Instead, they know the MessageEvents
 * they contain, and generate the edges on the fly using existential
 * abstraction. The class is complicated by the fact that in the state based
 * view, each partition corresponds to possibly several transitions. The
 * implementation here can only handle state based views where a partition
 * corresponds to a set of transitions that all have the same target (but
 * possibly different sources).
 * 
 * @author sigurd
 */
public class Partition implements INode<Partition> {
    /** All the events this partition contains */
    protected final Set<EventNode> events;
    private String label;

    /**
     * Whether or not this partition is a terminal partition (whether or not it
     * contains a terminal message event).
     */
    private boolean isTerminal;

    public Partition(Set<EventNode> messages) {
        events = new LinkedHashSet<EventNode>(messages);
        for (final EventNode m : messages) {
            m.setParent(this);
        }

        // A partition is final if it contains a message event that is a
        // terminal node in some input trace.
        isTerminal = false;
        for (EventNode e : messages) {
            if (e.isTerminal()) {
                isTerminal = true;
            }
        }
    }

    public void addMessage(EventNode message) {
        events.add(message);
        message.setParent(this);
        isTerminal |= message.isTerminal();
    }

    public void addAllMessages(Collection<EventNode> messages) {
        events.addAll(messages);
        for (final EventNode m : messages) {
            m.setParent(this);
            isTerminal |= m.isTerminal();
        }
    }

    /**
     * Transitions between partitions are not stored but generated on demand
     * using this iterator
     */
    @Override
    public IIterableIterator<Transition<Partition>> getTransitionsIterator() {
        return getTransitionsIterator(null);
    }

    public Set<EventNode> getEvents() {
        return events;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        // str.append("Partition " + hashCode());
        str.append("P." + getLabel());
        str.append("." + events.size());
        return str.toString();
    }

    /**
     * Split the events according to the presence of an outgoing transition
     * trans (the source in trans is ignored here). Note that the returned
     * candidate must be a valid split, therefore we cannot split on a
     * transition that is the sole transition between two partitions -- a split
     * candidate would create a partition with 0 events, which is an invalid
     * split.
     * 
     * @param trans
     *            the transition that will be checked for
     * @return the resulting split
     */
    public PartitionSplit getCandidateDivisionBasedOnOutgoing(
            ITransition<Partition> trans) {
        PartitionSplit ret = null;
        for (final EventNode event : events) {
            if (fulfillsStrong(event, trans)) {
                if (ret != null) {
                    ret.addEventToSplit(event);
                }
            } else {
                // We only create the split once we find an event that would
                // not be placed in the split.
                if (ret == null) {
                    // Add all events before event to the split (because ret is
                    // null, each of these events is guaranteed to fulfill the
                    // splitting criteria).
                    ret = new PartitionSplit(this);
                    for (final EventNode event2 : events) {
                        if (event2.equals(event)) {
                            break;
                        }
                        ret.addEventToSplit(event2);
                    }
                }
            }
        }
        return ret;
    }

    /**
     * Split the partition according to an incoming transition from
     * {@code previous} labeled with {@code relation}.
     * 
     * @param previous
     *            the partition the transition should be incoming from
     * @param relation
     *            provides the relation name to consider
     * @return returns the resulting split
     */
    public PartitionSplit getCandidateDivisionBasedOnIncoming(
            Partition previous, String relation) {
        Set<EventNode> eventsReachableFromPrevious = new LinkedHashSet<EventNode>();
        for (final EventNode prevEvent : previous.events) {
            eventsReachableFromPrevious.addAll(prevEvent
                    .getSuccessors(relation));
        }

        // Intersect the set of events that follows events from previous
        // Partition with events in this partition.
        eventsReachableFromPrevious.retainAll(events);

        // If all events from the previous Partition end up
        // in this partition then we return Null to indicate that no _valid_
        // split is possible.
        if (eventsReachableFromPrevious.size() == 0
                || eventsReachableFromPrevious.size() == events.size()) {
            return null;
        }

        PartitionSplit candidateSplit = new PartitionSplit(this);
        for (EventNode m : events) {
            if (eventsReachableFromPrevious.contains(m)) {
                // TODO: allow to add a collection instead of iterating.
                candidateSplit.addEventToSplit(m);
            }
        }
        return candidateSplit;
    }

    /**
     * Whether or not there exists a transition t' emanating from an event that
     * (1) matches the relation of another (inter-partition) transition t, and
     * (2) matches the destination partition of t
     * 
     * @param event
     * @param trans
     * @return whether or not event satisfies the conditions above.
     */
    private static boolean fulfillsStrong(EventNode event,
            ITransition<Partition> trans) {
        for (final ITransition<EventNode> t : event.getTransitions()) {
            if (t.getRelation().equals(trans.getRelation())
                    && t.getTarget().getParent().equals(trans.getTarget())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getLabel() {
        if (label != null) {
            return label;
        }
        return events.iterator().next().getLabel();
    }

    public int size() {
        return events.size();
    }

    /**
     * This method returns the set of transitions. It augments the edges with
     * information about frequency and number of observation.
     */
    @Override
    public List<Transition<Partition>> getTransitions() {
        List<Transition<Partition>> result = new ArrayList<Transition<Partition>>();
        for (Transition<Partition> tr : getTransitionsIterator()) {
            result.add(tr);
        }
        return result;
    }

    @Override
    public List<WeightedTransition<Partition>> getWeightedTransitions() {
        List<WeightedTransition<Partition>> result = new ArrayList<WeightedTransition<Partition>>();
        for (Transition<Partition> tr : getTransitionsIterator()) {
            // Use splitting to compute the transition probabilities\labels.
            PartitionSplit s = getCandidateDivisionBasedOnOutgoing(tr);
            if (s == null) {
                s = PartitionSplit.newSplitWithAllEvents(this);
            }
            int numOutgoing = s.getSplitEvents().size();
            int totalAtSource = tr.getSource().getEvents().size();
            double freq = (double) numOutgoing / (double) totalAtSource;
            WeightedTransition<Partition> trWeighted = new WeightedTransition<Partition>(
                    tr.getSource(), tr.getTarget(), tr.getRelation(), freq,
                    numOutgoing);
            result.add(trWeighted);
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
    public IIterableIterator<Transition<Partition>> getTransitionsIterator(
            final String act) {
        return new IIterableIterator<Transition<Partition>>() {
            private final Set<ITransition<Partition>> seen = new LinkedHashSet<ITransition<Partition>>();
            private final Iterator<EventNode> msgItr = events.iterator();

            private Iterator<? extends ITransition<EventNode>> transItr = (act == null) ? msgItr
                    .next().getTransitions().iterator()
                    : msgItr.next().getTransitions(act).iterator();

            private Transition<Partition> next = null;

            private Transition<Partition> getNext() {
                while (transItr.hasNext() || msgItr.hasNext()) {
                    if (transItr.hasNext()) {
                        final ITransition<EventNode> found = transItr.next();
                        final Transition<Partition> transToPart = new Transition<Partition>(
                                found.getSource().getParent(), found
                                        .getTarget().getParent(),
                                found.getRelation());
                        if (seen.add(transToPart)) {
                            return transToPart;
                        }
                    } else {
                        if (act == null) {
                            transItr = msgItr.next().getTransitions()
                                    .iterator();
                        } else {
                            transItr = msgItr.next().getTransitions(act)
                                    .iterator();
                        }
                    }
                }

                return null;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Transition<Partition> next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                final Transition<Partition> oldNext = next;
                next = null;
                return oldNext;
            }

            @Override
            public boolean hasNext() {
                if (next == null) {
                    next = getNext();
                }
                return next != null;
            }

            @Override
            public Iterator<Transition<Partition>> iterator() {
                return this;
            }
        };
    }

    @Override
    public ITransition<Partition> getTransition(Partition iNode, String action) {
        for (Iterator<Transition<Partition>> iter = getTransitionsIterator(action); iter
                .hasNext();) {
            ITransition<Partition> t = iter.next();
            if (t.getTarget().equals(iNode)) {
                return t;
            }
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

    public void removeMessages(Set<EventNode> messageList) {
        events.removeAll(messageList);
    }

    public void setLabel(String str) {
        label = str;
    }

    /**
     * Whether or not this partition is final (contains a terminal message
     * event).
     */
    @Override
    public boolean isTerminal() {
        return isTerminal;
    }

    @Override
    public Comparator<Partition> getComparator() {
        class PartitionComparator implements Comparator<Partition> {
            @Override
            public int compare(Partition arg0, Partition arg1) {
                // compare references
                if (arg0 == arg1) {
                    return 0;
                }

                // 1. compare label strings
                int labelCmp = getLabel().compareTo(arg1.getLabel());
                if (labelCmp != 0) {
                    return labelCmp;
                }

                // 2. compare number of children
                List<WeightedTransition<Partition>> tnsArg0 = arg0
                        .getWeightedTransitions();
                List<WeightedTransition<Partition>> tnsArg1 = arg1
                        .getWeightedTransitions();
                int childrenCmp = ((Integer) tnsArg0.size()).compareTo(tnsArg1
                        .size());
                if (childrenCmp != 0) {
                    return childrenCmp;
                }

                // 3. compare transitions to children
                Collections.sort(tnsArg0);
                Collections.sort(tnsArg1);
                int index = 0;
                int childCmp;
                for (WeightedTransition<Partition> p : tnsArg0) {
                    // sizes of tnsArg0 and tnsArg1 were checked to be equal
                    // above
                    WeightedTransition<Partition> p2 = tnsArg1.get(index);
                    childCmp = p.compareTo(p2);
                    if (childCmp != 0) {
                        return childCmp;
                    }
                }
                return 0;
            }
        }
        return new PartitionComparator();
    }

    @Override
    public void addTransition(Partition dest, String relation) {
        throw new InternalSynopticException(
                "Partitions manipulate edges implicitly through LogEvent instances they maintain. Cannot modify Partition transition directly.");
    }

}
