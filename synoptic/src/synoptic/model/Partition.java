package synoptic.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import synoptic.algorithms.graph.PartitionSplit;
import synoptic.model.interfaces.INode;
import synoptic.model.interfaces.ITransition;
import synoptic.util.IIterableIterator;
import synoptic.util.NotImplementedException;

/**
 * Implements a partition in a partition graph. Partitions are nodes, but they
 * do not explicitly represent their edges. Instead, they know the EventNodes
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
    /**
     * All the events this partition contains. A partition is constrained to
     * contain EventNodes that have Events of the same EventType.
     */
    protected final Set<EventNode> events;

    /**
     * Partitions can be initialized (belong to a partition graph) or be
     * uninitialized (do not belong to any partition graph, but possibly cached
     * and re-used later). This variable indicates whether or not this partition
     * has been initialized.
     */
    private boolean initialized = false;

    /**
     * The EvenType of this partition -- all EventNode instances MUST contain
     * Event instances that contains this type of event.
     */
    private EventType eType = null;

    /**
     * Creates a new partition that will contain a set of event nodes.
     * 
     * @param eNodes
     */
    public Partition(Set<EventNode> eNodes) {
        assert eNodes.size() > 0;
        events = new LinkedHashSet<EventNode>();
        initialize(eNodes.iterator().next());
        addEventNodes(eNodes);
    }

    /**
     * Creates a new partition that will contain a single event node.
     * 
     * @param eNodes
     */
    public Partition(EventNode eNode) {
        events = new LinkedHashSet<EventNode>();
        initialize(eNode);
        addOneEventNode(eNode);
    }

    public void initialize(EventNode eNode) {
        eType = eNode.getEType();
        initialized = true;
    }

    /**
     * Adds a collection of event nodes to the existing partition. Updates the
     * partition's isTerminal flag.
     * 
     * @param eNodes
     *            event nodes to add
     */
    public void addEventNodes(Collection<EventNode> eNodes) {
        if (!initialized) {
            initialize(eNodes.iterator().next());
        }

        events.addAll(eNodes);
        for (final EventNode e : eNodes) {
            e.setParent(this);
            // A partition is final if it contains a message event that is a
            // terminal node in some input trace.
            assert eType.equals(e.getEType());
        }
    }

    /**
     * Adds a single event node to the current partition.
     * 
     * @param eNode
     *            event node to add
     */
    public void addOneEventNode(EventNode eNode) {
        if (!initialized) {
            initialize(eNode);
        } else {
            assert eType.equals(eNode.getEType());
            eNode.setParent(this);
        }
        events.add(eNode);
    }

    /**
     * Returns the set of event nodes contained in this partition.
     */
    public Set<EventNode> getEventNodes() {
        return events;
    }

    /**
     * Removes a set of event nodes. NOTE: this method cannot be used to remove
     * all the messages in the partition. For this, use the removeAllMessages()
     * method.
     * 
     * @param eventNodes
     */
    public void removeEventNodes(Set<EventNode> eventNodes) {
        events.removeAll(eventNodes);
        assert events.size() > 0;
    }

    /**
     * Removes all the event nodes from this partition.
     */
    public void removeAllEventNodes() {
        events.clear();
        initialized = false;
    }

    /**
     * Whether or not this is the dummy terminal partition.
     */
    @Override
    public boolean isTerminal() {
        assert initialized;
        return eType.isTerminalEventType;
    }

    /**
     * Whether or not this is the dummy initial partition.
     */
    @Override
    public boolean isInitial() {
        assert initialized;
        return eType.isInitialEventType;
    }

    /**
     * Returns the event type of this partition.
     */
    @Override
    public EventType getEType() {
        assert initialized;
        return eType;
    }

    /**
     * Returns the number of event nodes this partition contains.
     */
    public int size() {
        assert initialized;
        return events.size();
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        // str.append("Partition " + hashCode());
        str.append(!initialized ? "UNINIT." : "");
        str.append("P." + getEType());
        str.append("." + events.size());
        return str.toString();
    }

    /**
     * Split the event nodes according to the presence of an outgoing transition
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
    public PartitionSplit getCandidateSplitBasedOnOutgoing(
            ITransition<Partition> trans) {
        assert initialized;

        PartitionSplit split = null;
        for (final EventNode event : events) {
            if (fulfillsStrong(event, trans)) {
                if (split != null) {
                    split.addEventToSplit(event);
                }
            } else {
                // We only create the split once we find an event that would
                // not be placed in the split.
                if (split == null) {
                    // Add all events before event to the split (because ret is
                    // null, each of these events is guaranteed to fulfill the
                    // splitting criteria).
                    split = new PartitionSplit(this);
                    for (final EventNode event2 : events) {
                        if (event2.equals(event)) {
                            break;
                        }
                        split.addEventToSplit(event2);
                    }
                }
            }
        }
        return split;
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
                // TODO: Shouldn't this check and return true only if the
                // condition holds for _all_ transitions t (not just some
                // transition t) ?
                return true;
            }
        }
        return false;
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
    public PartitionSplit getCandidateSplitBasedOnIncoming(Partition previous,
            String relation) {
        assert initialized;

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
     * This method returns the set of transitions. It augments the edges with
     * information about frequency and number of observation.
     */
    @Override
    public List<Transition<Partition>> getTransitions(String relation) {
        assert initialized;

        List<Transition<Partition>> result = new ArrayList<Transition<Partition>>();
        for (Transition<Partition> tr : getTransitionsIterator(relation)) {
            result.add(tr);
        }
        return result;
    }

    @Override
    public List<Transition<Partition>> getTransitions() {
        assert initialized;

        List<Transition<Partition>> result = new ArrayList<Transition<Partition>>();
        for (Transition<Partition> tr : getTransitionsIterator(null)) {
            result.add(tr);
        }
        return result;
    }

    /**
     * This method returns the set of transitions accessible through iterator
     * iter, augmenting each transition with information about frequency and
     * number of observations.
     */
    private List<WeightedTransition<Partition>> getWeightedTransitions(
            IIterableIterator<Transition<Partition>> iter) {
        assert initialized;

        List<WeightedTransition<Partition>> trsWeighted = new ArrayList<WeightedTransition<Partition>>();

        if (this.isInitial()) {
            // We handle INITIAL partitions differently because we optimized the
            // code by including just a single EventNode with type INITIAL for
            // all traces (instead of one maintaining one per trace). This one
            // INITIAL EventNode contains transitions to all first nodes of
            // traces.
            assert (this.getEventNodes().size() == 1);

            // The _EventNode_ (child) instances that the one EventNode
            // in this partition contains transitions to.
            Set<EventNode> children = this.getEventNodes().iterator().next()
                    .getSuccessors();

            int totalChildren = children.size();

            // Iterate through all children, building up the map of number of
            // transitions per (child) partition.
            Map<Partition, Integer> transitionsPerChildPartition = new LinkedHashMap<Partition, Integer>();
            for (EventNode child : children) {
                Partition childP = child.getParent();
                if (transitionsPerChildPartition.containsKey(childP)) {
                    transitionsPerChildPartition.put(childP,
                            transitionsPerChildPartition.get(childP) + 1);
                } else {
                    transitionsPerChildPartition.put(childP, 1);
                }
            }

            // Now, use the map to determine transition probabilities.
            for (Transition<Partition> tr : iter) {
                int numOutgoing = transitionsPerChildPartition.get(tr
                        .getTarget());
                double probability = (double) numOutgoing
                        / (double) totalChildren;
                WeightedTransition<Partition> trWeighted = new WeightedTransition<Partition>(
                        this, tr.getTarget(), tr.getRelation(), probability,
                        numOutgoing);
                trsWeighted.add(trWeighted);
            }

        } else {
            // Non-INITIAL partition case.

            int totalAtSource = events.size();
            for (Transition<Partition> tr : iter) {
                int numOutgoing = 0;
                for (final EventNode event : events) {
                    if (fulfillsStrong(event, tr)) {
                        numOutgoing += 1;
                    }
                }

                double probability = (double) numOutgoing
                        / (double) totalAtSource;
                WeightedTransition<Partition> trWeighted = new WeightedTransition<Partition>(
                        this, tr.getTarget(), tr.getRelation(), probability,
                        numOutgoing);
                trsWeighted.add(trWeighted);
            }
        }
        return trsWeighted;
    }

    @Override
    public List<WeightedTransition<Partition>> getWeightedTransitions(
            String relation) {
        assert initialized;

        return getWeightedTransitions(getTransitionsIterator(relation));
    }

    @Override
    public List<WeightedTransition<Partition>> getWeightedTransitions() {
        assert initialized;

        return getWeightedTransitions(getTransitionsIterator(null));
    }

    /**
     * Transitions between partitions are not stored but generated on demand
     * using this iterator
     */
    @Override
    public IIterableIterator<Transition<Partition>> getTransitionsIterator() {
        assert initialized;

        return getTransitionsIterator(null);
    }

    /**
     * Generate Edges on the fly. We examine all contained messages and find the
     * appropriate successor messages. We then check to which partition the
     * successor messages belong and create an edge between the partitions.
     * Duplicates are eliminated.
     */
    @Override
    public IIterableIterator<Transition<Partition>> getTransitionsIterator(
            final String relation) {
        assert initialized;

        return new IIterableIterator<Transition<Partition>>() {

            private final Set<ITransition<Partition>> seen = new LinkedHashSet<ITransition<Partition>>();
            private final Iterator<EventNode> msgItr = events.iterator();

            private Iterator<? extends ITransition<EventNode>> transItr = (relation == null) ? msgItr
                    .next().getTransitions().iterator()
                    : msgItr.next().getTransitions(relation).iterator();

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
                        if (relation == null) {
                            transItr = msgItr.next().getTransitions()
                                    .iterator();
                        } else {
                            transItr = msgItr.next().getTransitions(relation)
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
        assert initialized;

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
    public int compareTo(Partition other) {
        assert initialized;

        // 0. Compare references.
        if (this == other) {
            return 0;
        }

        // 1. Compare label strings.
        int labelCmp = eType.compareTo(other.getEType());
        if (labelCmp != 0) {
            return labelCmp;
        }

        // 2. Compare number of children.
        List<WeightedTransition<Partition>> tnsThis = this
                .getWeightedTransitions();
        List<WeightedTransition<Partition>> tnsOther = other
                .getWeightedTransitions();
        int childrenCmp = ((Integer) tnsThis.size()).compareTo(tnsOther.size());
        if (childrenCmp != 0) {
            return childrenCmp;
        }

        // 3. Compare transitions.
        Collections.sort(tnsThis);
        Collections.sort(tnsOther);
        int index = 0;
        int transCmp;
        for (WeightedTransition<Partition> p : tnsThis) {
            // Sizes of tnsThis and tnsOther were checked to be equal above.
            WeightedTransition<Partition> p2 = tnsOther.get(index);
            transCmp = p.compareTo(p2);
            if (transCmp != 0) {
                return transCmp;
            }
        }
        return 0;
    }

    @Override
    public Partition getParent() throws NotImplementedException {
        throw new NotImplementedException();
    }

    @Override
    public void setParent(Partition parent) throws NotImplementedException {
        throw new NotImplementedException();
    }

    /*
     * @Override public void addTransition(Partition dest, String relation) {
     * throw new InternalSynopticException(
     * "Partitions manipulate edges implicitly through LogEvent instances they maintain. Cannot modify Partition transition directly."
     * ); }
     */
}
