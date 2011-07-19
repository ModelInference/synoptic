package synoptic.model;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import synoptic.algorithms.graph.IOperation;
import synoptic.algorithms.graph.PartitionMultiSplit;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.model.interfaces.IGraph;
import synoptic.model.interfaces.ITransition;
import synoptic.util.InternalSynopticException;

/**
 * This class implements a partition graph. Nodes are {@code Partition}
 * instances, which are sets of messages -- ( {@code LogEvent}) -- and edges are
 * not maintained explicitly, but generated on-the-fly by class
 * {@code Partition}. PartitionGraphs can only be modified via the method
 * {@code apply} which takes a object implementing {@code IOperation}.
 * Operations must perform changes on both representations.
 * 
 * @author sigurd
 */
public class PartitionGraph implements IGraph<Partition> {

    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger("PartitionGraph Logger");

    /** holds all partitions in this graph */
    private LinkedHashSet<Partition> partitions = null;

    /**
     * Holds the initial messages in this graph, grouped by the relation w.r.t.
     * which they are initial. We keep track of initial partitions by keeping
     * track of the initial messages but we need to do this for every relation,
     * which is specified by the first argument to the hash-map.
     */
    private final LinkedHashMap<String, Set<EventNode>> initialEvents = new LinkedHashMap<String, Set<EventNode>>();

    /**
     * Holds the terminal messages in this graph. Like the initialMessages
     * above, this hash-map maintains them w.r.t the relations.
     */
    private final LinkedHashMap<String, Set<EventNode>> terminalEvents = new LinkedHashMap<String, Set<EventNode>>();

    /** holds synoptic.invariants that were mined when the graph was created */
    private TemporalInvariantSet invariants = null;

    /** holds all relations known to exist in this graph */
    private final Set<String> relations = new LinkedHashSet<String>();

    /** a cache of inter-partition transitions */
    public final LinkedHashMap<Partition, Set<Partition>> transitionCache = new LinkedHashMap<Partition, Set<Partition>>();

    /* cache of partition splits */
    private final LinkedList<PartitionMultiSplit> appliedSplits = new LinkedList<PartitionMultiSplit>();

    /**
     * Construct a PartitionGraph. Invariants from {@code g} will be extracted
     * and stored. If partitionByLabel is true, all messages with identical
     * labels in {@code g} will become one partition. Otherwise, every message
     * gets its own partition (useful if only coarsening is to be performed).
     * 
     * @param g
     *            The initial graph
     * @param partitionByLabel
     *            Whether initial partitioning by label should be done
     */
    public PartitionGraph(IGraph<EventNode> g, boolean partitionByLabel,
            TemporalInvariantSet invariants) {
        for (String relation : g.getRelations()) {
            addInitialMessages(g.getInitialNodes(relation), relation);
            relations.add(relation);
        }

        if (partitionByLabel) {
            partitionByLabels(g.getNodes());
        } else {
            partitionSeparately(g.getNodes());
        }
        this.invariants = invariants;
    }

    public PartitionGraph(IGraph<EventNode> g,
            LinkedList<LinkedHashSet<Integer>> partitioningIndexSets,
            TemporalInvariantSet invariants) {
        for (String relation : g.getRelations()) {
            addInitialMessages(g.getInitialNodes(relation), relation);
            relations.add(relation);
        }

        partitionByIndexSetsAndLabels(g.getNodes(), partitioningIndexSets);
        this.invariants = invariants;
    }

    private void addInitialMessages(Set<EventNode> initialMessages,
            String relation) {
        if (!initialEvents.containsKey(relation)) {
            initialEvents.put(relation, new LinkedHashSet<EventNode>());
        }
        initialEvents.get(relation).addAll(initialMessages);
    }

    public TemporalInvariantSet getInvariants() {
        return invariants;
    }

    public Partition partitionFromMessage(EventNode message) {
        return message.getParent();
    }

    public IOperation apply(IOperation op) {
        if (op.getClass() == PartitionMultiSplit.class) {
            // if a PartitionSplit, add to cache of splits
            appliedSplits.push((PartitionMultiSplit) op);
        }
        return op.commit(this);
    }

    /**
     * Returns the most recently applied PartitionMultiSplit, null if no splits
     * have been made
     */
    public PartitionMultiSplit getMostRecentSplit() {
        return appliedSplits.peek();
    }

    /**
     * Returns a set of partitions that are adjacent to pNode. Uses the internal
     * transitionCache for speed.
     * 
     * @param pNode
     * @return set of adjacent partitions to pNode
     */
    @Override
    public Set<Partition> getAdjacentNodes(Partition pNode) {
        Set<Partition> result = transitionCache.get(pNode);
        if (result == null) {
            result = new LinkedHashSet<Partition>();
            List<Transition<Partition>> relations = pNode.getTransitions();
            for (int i = 0; i < relations.size(); i++) {
                result.add(relations.get(i).getTarget());
            }
            transitionCache.put(pNode, result);
        }
        return result;
    }

    /**
     * All messages with identical labels are mapped to the same partition.
     * 
     * @param events
     *            Set of message which to be partitioned
     */
    private void partitionByLabels(Collection<EventNode> events) {
        partitions = new LinkedHashSet<Partition>();
        final Map<EventType, Partition> prepartitions = new LinkedHashMap<EventType, Partition>();
        for (EventNode message : events) {
            for (ITransition<EventNode> t : message.getTransitions()) {
                relations.add(t.getRelation());
            }
            if (!prepartitions.containsKey(message.getEType())) {
                final Partition partition = new Partition(
                        new LinkedHashSet<EventNode>());
                partitions.add(partition);
                prepartitions.put(message.getEType(), partition);
            }
            prepartitions.get(message.getEType()).addMessage(message);
        }
        transitionCache.clear();
    }

    private void partitionByIndexSetsAndLabels(Collection<EventNode> events,
            LinkedList<LinkedHashSet<Integer>> partitioningIndexSets) {
        // 1. partition by labels.
        partitionByLabels(events);
        // 2. Map each message to a node in the system.

        // TODO: do this using the new algorithm.
        LinkedHashMap<EventNode, Integer> messageIndexMap = new LinkedHashMap<EventNode, Integer>();

        LinkedHashSet<Partition> newPartitions = new LinkedHashSet<Partition>();

        // 3. consider each of the label-partitions and divide these up
        // according to each set of indices.
        for (Partition p : partitions) {

            Partition[] subPartitions = new Partition[partitioningIndexSets
                    .size()];

            for (EventNode m : p.events) {
                Integer index = messageIndexMap.get(m);
                if (index == null) {
                    throw new InternalSynopticException(
                            "Failed to map LogEvent [" + m.toString()
                                    + "] to a node index.");
                }
                int i = 0;
                boolean added = false;
                for (LinkedHashSet<Integer> indexPartition : partitioningIndexSets) {
                    if (indexPartition.contains(index)) {
                        if (subPartitions[i] == null) {
                            subPartitions[i] = new Partition(null);
                        }
                        subPartitions[i].addMessage(m);
                        added = true;
                        break;
                    }
                    i++;
                }

                if (!added) {
                    throw new InternalSynopticException(
                            "Unable to find index in the partitioning -- they must be complete!");
                }

                // TODO: consider the case where all the messages remain in the
                // same partition -- just keep it then?
                //

                // Add the newly created partitions, if any.
                for (Partition subPartition : subPartitions) {
                    if (subPartition != null) {
                        newPartitions.add(subPartition);
                    }
                }
            }
        }

    }

    private void partitionByLabelsAndInitial(Collection<EventNode> events,
            Set<EventNode> initial) {
        partitions = new LinkedHashSet<Partition>();
        final Map<EventType, Partition> prepartitions = new LinkedHashMap<EventType, Partition>();
        for (EventNode message : events) {
            for (ITransition<EventNode> t : message.getTransitions()) {
                relations.add(t.getRelation());
            }
            if (!prepartitions.containsKey(message.getEType())) {
                final Partition partition = new Partition(
                        new LinkedHashSet<EventNode>());
                prepartitions.put(message.getEType(), partition);
            }
            prepartitions.get(message.getEType()).addMessage(message);
        }
        for (Partition t : prepartitions.values()) {
            LinkedHashSet<EventNode> iSet = new LinkedHashSet<EventNode>();
            for (EventNode e : t.getEvents()) {
                if (initial.contains(e)) {
                    iSet.add(e);
                }
            }
            if (iSet.size() == 0) {
                partitions.add(t);
            } else {
                t.removeMessages(iSet);
                partitions.add(t);
                partitions.add(new Partition(iSet));
            }
        }
    }

    /**
     * Each event is mapped to its own unique partition. This is the most direct
     * means of mapping a graph into a partition graph.
     * 
     * @param events
     *            Set of message to map
     */
    private void partitionSeparately(Collection<EventNode> events) {
        partitions = new LinkedHashSet<Partition>();
        final Map<EventNode, Partition> prepartitions = new LinkedHashMap<EventNode, Partition>();
        for (EventNode message : events) {
            if (!prepartitions.containsKey(message)) {
                final Partition partition = new Partition(
                        new LinkedHashSet<EventNode>());
                partitions.add(partition);
                prepartitions.put(message, partition);
            }
            prepartitions.get(message).addMessage(message);
        }
    }

    @Override
    public Set<Partition> getNodes() {
        return partitions;
    }

    private Set<Partition> getAllRelationsLogEventPartitions(
            LinkedHashMap<String, Set<EventNode>> map) {
        Set<Partition> ret = new LinkedHashSet<Partition>();
        for (String relation : getRelations()) {
            ret.addAll(getRelationLogEventPartitions(relation, map));
        }
        return ret;
    }

    private Set<Partition> getRelationLogEventPartitions(String relation,
            LinkedHashMap<String, Set<EventNode>> map) {
        Set<Partition> ret = new LinkedHashSet<Partition>();
        Set<EventNode> events = map.get(relation);
        if (events == null) {
            return ret;
        }
        for (EventNode m : events) {
            ret.add(m.getParent());
        }
        return ret;
    }

    @Override
    public Set<Partition> getInitialNodes() {
        return getAllRelationsLogEventPartitions(initialEvents);
    }

    public Set<Partition> getTerminalNodes() {
        return getAllRelationsLogEventPartitions(terminalEvents);
    }

    public Set<Partition> getTerminalNodes(String relation) {
        return getRelationLogEventPartitions(relation, terminalEvents);
    }

    @Override
    public Set<Partition> getInitialNodes(String relation) {
        return getRelationLogEventPartitions(relation, initialEvents);
    }

    @Override
    public Set<String> getRelations() {
        return relations;
    }

    @Override
    public void add(Partition node) {
        for (EventNode m : node.getEvents()) {
            relations.addAll(m.getRelations());
        }
        partitions.add(node);

        clearNodeAdjacentsCache(node);
    }

    public void mergeAdjacentsCache(Partition from, Partition to) {
        for (Iterator<Entry<Partition, Set<Partition>>> pIter = transitionCache
                .entrySet().iterator(); pIter.hasNext();) {
            Set<Partition> partitions = pIter.next().getValue();
            if (partitions.contains(from)) {
                partitions.remove(from);
                partitions.add(to);
            }
        }
    }

    public void clearNodeAdjacentsCache(Partition node) {
        transitionCache.remove(node);
        // System.out.println("Cache size: " + transitionCache.size());
        for (Iterator<Entry<Partition, Set<Partition>>> pIter = transitionCache
                .entrySet().iterator(); pIter.hasNext();) {
            Set<Partition> partitions = pIter.next().getValue();
            if (partitions.contains(node)) {
                // System.out.println("cache rm");
                pIter.remove();
            }
        }
        // System.out.println("Cache' size: " + transitionCache.size());
    }

    @Override
    public void tagInitial(Partition initialNode, String relation) {
        partitions.add(initialNode);
        addInitialMessages(initialNode.getEvents(), relation);
        transitionCache.clear();
    }

    @Override
    public void remove(Partition node) {
        partitions.remove(node);
    }

    @Override
    public void tagTerminal(Partition terminalNode, String relation) {
        throw new InternalSynopticException(
                "PartitionGraph tags terminal partitions implicitly -- by considering the LogEvents the partition contains.");
    }

    public Set<EventNode> getInitialMessages() {
        Set<EventNode> initial = new LinkedHashSet<EventNode>();
        for (Set<EventNode> set : initialEvents.values()) {
            initial.addAll(set);
        }
        return initial;
    }

    /**
     * Check that all partitions are non-empty and disjunct.
     */
    public void checkSanity() {
        int totalCount = 0;
        Set<EventNode> all = new LinkedHashSet<EventNode>();
        for (Partition p : getNodes()) {
            if (p.size() == 0) {
                throw new InternalSynopticException(
                        "bisim produced empty partition!");
            }
            all.addAll(p.getEvents());
            totalCount += p.size();
        }
        if (totalCount != all.size()) {
            throw new InternalSynopticException(
                    "partitions are not partitioning messages (overlap)!");
        }
    }

}