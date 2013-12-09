package synoptic.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import daikonizer.DaikonInvariants;

import synoptic.algorithms.graphops.PartitionSplit;
import synoptic.main.SynopticMain;
import synoptic.model.event.EventType;
import synoptic.model.interfaces.INode;
import synoptic.model.interfaces.ITransition;
import synoptic.model.state.State;
import synoptic.model.state.SynDaikonizer;
import synoptic.util.NotImplementedException;
import synoptic.util.time.ITime;

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
     * Cached transitions with Daikon invariants.
     * We need to cache these transitions because Daikon invariants are
     * expensive to compute, and we may revisit the same transition many times
     * while deriving abstract tests from a model (i.e., a PartitionGraph).
     */
    private final List<Transition<Partition>> cachedTransitionsWithInvs;
    
    /**
     * Creates a new partition that will contain a set of event nodes.
     * 
     * @param eNodes
     */
    public Partition(Set<EventNode> eNodes) {
        assert eNodes.size() > 0;
        events = new LinkedHashSet<EventNode>();
        addEventNodes(eNodes);
        cachedTransitionsWithInvs = new ArrayList<Transition<Partition>>();
    }

    /**
     * Creates a new partition that will contain a single event node.
     * 
     * @param eNodes
     */
    public Partition(EventNode eNode) {
        events = new LinkedHashSet<EventNode>();
        addOneEventNode(eNode);
        cachedTransitionsWithInvs = new ArrayList<Transition<Partition>>();
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
            // A Partition is allowed to contain only EventNode instances of the
            // same event type.
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
        }
        eNode.setParent(this);
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
        return eType.isTerminalEventType();
    }

    /**
     * Whether or not this is the dummy initial partition.
     */
    @Override
    public boolean isInitial() {
        assert initialized;
        return eType.isInitialEventType();
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

        for (ITransition<EventNode> t : event.getAllTransitions()) {
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
            Set<String> relations) {
        assert initialized;

        if (relations.size() != 1) {
            throw new NotImplementedException(
                    "Multi-relational support missing in method getCandidateSplitBasedOnIncoming()");
        }

        Set<EventNode> eventsReachableFromPrevious = new LinkedHashSet<EventNode>();
        for (EventNode prevEvent : previous.events) {
            for (ITransition<EventNode> t : prevEvent
                    .getTransitionsWithExactRelations(relations)) {
                eventsReachableFromPrevious.add(t.getTarget());
            }
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
     * This method returns the set of transitions accessible through iterator
     * iter, augmenting each transition with information about frequency and
     * number of observations.
     */
    @Override
    public List<? extends ITransition<Partition>> getWeightedTransitions() {
        assert initialized;

        List<? extends ITransition<Partition>> transitions = getAllTransitions();

        // TODO: Cache whether or not the fractions/counts have already been set
        // on the transitions.

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
                    .getAllSuccessors();

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
            for (ITransition<Partition> tr : transitions) {
                int numOutgoing = transitionsPerChildPartition.get(tr
                        .getTarget());
                double probability = (double) numOutgoing
                        / (double) totalChildren;

                tr.setProbability(probability);
                tr.setCount(numOutgoing);
            }

        } else {
            // Non-INITIAL partition case.

            int totalAtSource = events.size();
            for (ITransition<Partition> tr : transitions) {
                int numOutgoing = 0;
                for (final EventNode event : events) {
                    if (fulfillsStrong(event, tr)) {
                        numOutgoing += 1;
                    }
                }

                double probability = (double) numOutgoing
                        / (double) totalAtSource;

                tr.setProbability(probability);
                tr.setCount(numOutgoing);

            }
        }
        return transitions;
    }

    public ITransition<Partition> getTransitionWithExactRelation(Partition p,
            Set<String> relations) {
        assert initialized;

        for (ITransition<Partition> t : getTransitionsWithExactRelations(relations)) {
            if (t.getTarget().equals(p)) {
                return t;
            }
        }
        return null;
    }

    /**
     * Get all event transitions with specified relations and which are from
     * this partition to partition p
     * 
     * @param p
     *            The partition to which desired transitions should go
     * @param relations
     *            Get event transitions with these relations
     * @return Event transitions with specified relations and with source in
     *         this partition and target in partition p
     */
    public Set<ITransition<EventNode>> getEventTransitionsWithExactRelations(
            Partition p, Set<String> relations) {

        // Set of event transitions to be returned
        Set<ITransition<EventNode>> evTransitions = new HashSet<ITransition<EventNode>>();

        // Consider all transitions out of this partition's events
        for (EventNode thisEv : events) {
            for (ITransition<EventNode> thisEvTrans : thisEv
                    .getTransitionsWithExactRelations(relations)) {

                // This transition's target event is in partition p
                if (thisEvTrans.getTarget().getParent().equals(p)) {
                    evTransitions.add(thisEvTrans);
                }
            }
        }

        return evTransitions;
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
        List<? extends ITransition<Partition>> tnsThis = this
                .getWeightedTransitions();
        List<? extends ITransition<Partition>> tnsOther = other
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
        for (ITransition<Partition> p : tnsThis) {
            // Sizes of tnsThis and tnsOther were checked to be equal above.
            ITransition<Partition> p2 = tnsOther.get(index);
            transCmp = p.compareTo(p2);
            if (transCmp != 0) {
                return transCmp;
            }
        }
        return 0;
    }

    @Override
    public Partition getParent() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setParent(Partition parent) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Return a set containing the timestamp of every Event in this Partition
     */
    public Set<ITime> getAllTimes() {

        HashSet<ITime> allDeltas = new HashSet<ITime>();

        for (EventNode ev : events) {
            allDeltas.add(ev.getTime());
        }

        return allDeltas;
    }

    private static void updateTransitionDeltas(EventNode srcENode,
            EventNode targetENode, ITransition<Partition> tx) {
        if (!SynopticMain.getInstanceWithExistenceCheck().options.enablePerfDebugging) {
            return;
        }
        ITime srcTime = srcENode.getTime();
        ITime targetTime = targetENode.getTime();
        if (targetTime != null && srcTime != null) {
            ITime d = targetTime.computeDelta(srcTime);
            tx.addTimeDeltaToSeries(d);
        }
    }

    @Override
    public List<? extends ITransition<Partition>> getAllTransitions() {
        // TODO: implement a transition cache optimization.
        Map<Partition, Transition<Partition>> transitions = new HashMap<Partition, Transition<Partition>>();

        for (EventNode e : events) {
            for (ITransition<EventNode> tr : e.getAllTransitions()) {
                Partition childP = tr.getTarget().getParent();
                Transition<Partition> tx;

                // Create the transition if it doesn't exist, or retrieve it if it does
                if (transitions.containsKey(childP)) {
                    tx = transitions.get(childP);
                } else {
                    tx = new Transition<Partition>(this, childP, tr.getRelation());
                    transitions.put(childP, tx);
                }
                
                // TODO: calling updateTransitionDeltas() is a fragile kind of
                // initialization -- we have to remember to call this method
                // whenever creating a new ITransition<Partition> instance.
                // Refactor this into a new kind of Transition constructor? Or a
                // helper method.
                updateTransitionDeltas(e, tr.getTarget(), tx);
            }
        }
        
        // Return final list of transitions
        List<Transition<Partition>> ret = new ArrayList<Transition<Partition>>(transitions.values());
        return ret;
    }

    @Override
    public Set<Partition> getAllSuccessors() {
        Set<Partition> successors = new LinkedHashSet<Partition>();
        for (EventNode e : events) {
            for (EventNode eSucc : e.getAllSuccessors()) {
                successors.add(eSucc.getParent());
            }
        }
        return successors;
    }

    // TODO: this code is very similar to getAllTransitions(), refactor it.
    @Override
    public List<? extends ITransition<Partition>> getTransitionsWithExactRelations(
            Set<String> relations) {
        
        Map<Partition, Transition<Partition>> transitions = new HashMap<Partition, Transition<Partition>>();
        
        for (EventNode e : events) {
            for (ITransition<EventNode> tr : e.getTransitionsWithExactRelations(relations)) {
                Partition childP = tr.getTarget().getParent();
                Transition<Partition> tx;

                // Create the transition if it doesn't exist, or retrieve it if it does
                if (transitions.containsKey(childP)) {
                    tx = transitions.get(childP);
                } else {
                    tx = new Transition<Partition>(this, childP, tr.getRelation());
                    transitions.put(childP, tx);
                }
                
                updateTransitionDeltas(e, tr.getTarget(), tx);
            }
        }
        
        // Return final list of transitions
        List<Transition<Partition>> ret = new ArrayList<Transition<Partition>>(transitions.values());
        return ret;
    }

    @Override
    public List<? extends ITransition<Partition>> getTransitionsWithSubsetRelations(
            Set<String> relations) {
        // TODO: implement.
        throw new NotImplementedException();
    }

    @Override
    public List<? extends ITransition<Partition>> getTransitionsWithIntersectingRelations(
            Set<String> relations) {
        // TODO: implement.
        throw new NotImplementedException();
    }

    /**
     * Generates and caches outgoing transitions of this partition, each of which
     * has DaikonInvariants labeled on it.
     * 
     * NOTE:
     * 1) This method must be called only when state processing logic is enabled.
     * 2) Since this method caches transitions, it must be called only after
     *    the final model is yield (i.e., no more changes to this Partition).
     * 
     * @return transitions with Daikon invariants
     * @throws Exception
     */
    public List<? extends ITransition<Partition>> getTransitionsWithDaikonInvariants() {
        assert (SynopticMain.getInstanceWithExistenceCheck().options.stateProcessing);
        
        if (!cachedTransitionsWithInvs.isEmpty() || isTerminal()) {
            return cachedTransitionsWithInvs;
        }
        
        for (Partition childP : getAllSuccessors()) {
            Transition<Partition> tx = null;
            SynDaikonizer daikonizer = new SynDaikonizer();
            
            if (isInitial()) {
                // This is a dummy initial partition and its dummy event node
                // has no post-event state. Instead, we need to get
                // pre-event states of the successor event nodes.
                // This partition contains only a single dummy event node.
                assert events.size() == 1;
                EventNode dummyInitEvent = events.iterator().next();
                
                for (ITransition<EventNode> tr : dummyInitEvent.getAllTransitions()) {
                    // Add only states that are on transitions to childP
                    // to daikonizer.
                    boolean stateAdded = addStateToDaikonizer(
                            tr, childP, daikonizer, false);
                    // Create transition to childP iff it doesn't already exist
                    // and tr's destination is childP.
                    if (stateAdded && tx == null) {
                        tx = createDaikonInvTransition(tr);
                        cachedTransitionsWithInvs.add(tx);
                    }
                }
            } else {
                // This is NOT a dummy initial partition. Its event nodes have
                // post-event states.
                for (EventNode event : events) {
                    List<Transition<EventNode>> transitions = event.getAllTransitions();
                    // Events are totally ordered.
                    assert transitions.size() == 1;
                    ITransition<EventNode> tr = transitions.iterator().next();
                    boolean stateAdded = addStateToDaikonizer(
                            tr, childP, daikonizer, true);
                    if (stateAdded && tx == null) {
                        tx = createDaikonInvTransition(tr);
                        cachedTransitionsWithInvs.add(tx);
                    }
                }
            }
            assert (tx != null);
            // Generate invariants of tx.
            DaikonInvariants daikonInvs = daikonizer.getDaikonEnterInvariants();
            // Label tx with Daikon invariants.
            tx.labels.setLabel(TransitionLabelType.DAIKON_INVARIANTS_LABEL,
                    daikonInvs);
        }
        return cachedTransitionsWithInvs;
    }
    
    /**
     * Adds a state that is on eventTrans to daikonizer iff the target of
     * eventTrans is in targetPartition.
     * 
     * @return true iff the state is added to daikonizer.
     * @throws Exception
     */
    private static boolean addStateToDaikonizer(ITransition<EventNode> eventTrans,
            Partition targetPartition, SynDaikonizer daikonizer, boolean post) {
        EventNode srcEvent = eventTrans.getSource();
        EventNode dstEvent = eventTrans.getTarget();
        Partition dstPartition = dstEvent.getParent();
        
        if (dstPartition.compareTo(targetPartition) == 0) {
            State state = post ? srcEvent.getPostEventState() : dstEvent
                    .getPreEventState();
            daikonizer.addInstance(state);
            return true;
        }
        return false;
    }
    
    /**
     * Creates Partition transition from the given EventNode transition.
     * This transition will be used to store Daikon invariants.
     */
    private static Transition<Partition> createDaikonInvTransition(
            ITransition<EventNode> eventTrans) {
        EventNode srcNode = eventTrans.getSource();
        EventNode targetNode = eventTrans.getTarget();
        Transition<Partition> tx = new Transition<Partition>(srcNode.getParent(),
                targetNode.getParent(), eventTrans.getRelation());
        updateTransitionDeltas(srcNode, targetNode, tx);
        // But, tx has no invariants associated with it yet.
        return tx;
    }
}
