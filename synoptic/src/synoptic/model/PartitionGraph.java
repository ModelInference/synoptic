package synoptic.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import synoptic.algorithms.graph.IOperation;
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
    /** holds all partitions in this graph */
    private LinkedHashSet<Partition> partitions = null;

    /**
     * Holds the initial messages in this graph, grouped by the relation w.r.t.
     * which they are initial. We keep track of initial partitions by keeping
     * track of the initial messages but we need to do this for every relation,
     * which is specified by the first argument to the hash-map.
     */
    private final LinkedHashMap<String, Set<LogEvent>> initialEvents = new LinkedHashMap<String, Set<LogEvent>>();

    /**
     * Holds the terminal messages in this graph. Like the initialMessages
     * above, this hash-map maintains them w.r.t the relations.
     */
    private final LinkedHashMap<String, Set<LogEvent>> terminalEvents = new LinkedHashMap<String, Set<LogEvent>>();

    /** holds synoptic.invariants that were mined when the graph was created */
    private TemporalInvariantSet invariants = null;

    /** holds all relations known to exist in this graph */
    private final Set<String> relations = new LinkedHashSet<String>();

    /** a cache of inter-partition transitions */
    public final LinkedHashMap<Partition, List<Partition>> transitionCache = new LinkedHashMap<Partition, List<Partition>>();

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
    public PartitionGraph(IGraph<LogEvent> g, boolean partitionByLabel,
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

    public PartitionGraph(IGraph<LogEvent> g,
            LinkedList<LinkedHashSet<Integer>> partitioningIndexSets,
            TemporalInvariantSet invariants) {
        for (String relation : g.getRelations()) {
            addInitialMessages(g.getInitialNodes(relation), relation);
            relations.add(relation);
        }

        partitionByIndexSetsAndLabels(g.getNodes(), partitioningIndexSets);
        this.invariants = invariants;
    }

    private void addInitialMessages(Set<LogEvent> initialMessages,
            String relation) {
        if (!initialEvents.containsKey(relation)) {
            initialEvents.put(relation, new LinkedHashSet<LogEvent>());
        }
        initialEvents.get(relation).addAll(initialMessages);
    }

    public TemporalInvariantSet getInvariants() {
        return invariants;
    }

    public Partition partitionFromMessage(LogEvent message) {
        return message.getParent();
    }

    public IOperation apply(IOperation op) {
        transitionCache.clear();
        return op.commit(this);
    }

    /**
     * Returns a list of partitions that are adjacent to pNode. Uses the
     * internal transitionCache for speed.
     * 
     * @param pNode
     * @return
     */
    @Override
    public List<Partition> getAdjacentNodes(Partition pNode) {
        List<Partition> result = transitionCache.get(pNode);
        if (result == null) {
            result = new ArrayList<Partition>();
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
    private void partitionByLabels(Collection<LogEvent> events) {
        partitions = new LinkedHashSet<Partition>();
        final Map<String, Partition> prepartitions = new LinkedHashMap<String, Partition>();
        for (LogEvent message : events) {
            for (ITransition<LogEvent> t : message.getTransitions()) {
                relations.add(t.getRelation());
            }
            if (!prepartitions.containsKey(message.getLabel())) {
                final Partition partition = new Partition(
                        new LinkedHashSet<LogEvent>());
                partitions.add(partition);
                prepartitions.put(message.getLabel(), partition);
            }
            prepartitions.get(message.getLabel()).addMessage(message);
        }
    }

    private void partitionByIndexSetsAndLabels(Collection<LogEvent> events,
            LinkedList<LinkedHashSet<Integer>> partitioningIndexSets) {
        // 1. partition by labels.
        partitionByLabels(events);
        // 2. Map each message to a node in the system.

        // TODO: do this using the new algorithm.
        LinkedHashMap<LogEvent, Integer> messageIndexMap = new LinkedHashMap<LogEvent, Integer>();

        LinkedHashSet<Partition> newPartitions = new LinkedHashSet<Partition>();

        // 3. consider each of the label-partitions and divide these up
        // according to each set of indices.
        for (Partition p : partitions) {

            Partition[] subPartitions = new Partition[partitioningIndexSets
                    .size()];

            for (LogEvent m : p.messages) {
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

    private void partitionByLabelsAndInitial(Collection<LogEvent> events,
            Set<LogEvent> initial) {
        partitions = new LinkedHashSet<Partition>();
        final Map<String, Partition> prepartitions = new LinkedHashMap<String, Partition>();
        for (LogEvent message : events) {
            for (ITransition<LogEvent> t : message.getTransitions()) {
                relations.add(t.getRelation());
            }
            if (!prepartitions.containsKey(message.getLabel())) {
                final Partition partition = new Partition(
                        new LinkedHashSet<LogEvent>());
                prepartitions.put(message.getLabel(), partition);
            }
            prepartitions.get(message.getLabel()).addMessage(message);
        }
        for (Partition t : prepartitions.values()) {
            LinkedHashSet<LogEvent> iSet = new LinkedHashSet<LogEvent>();
            for (LogEvent e : t.getMessages()) {
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
    private void partitionSeparately(Collection<LogEvent> events) {
        partitions = new LinkedHashSet<Partition>();
        final Map<LogEvent, Partition> prepartitions = new LinkedHashMap<LogEvent, Partition>();
        for (LogEvent message : events) {
            if (!prepartitions.containsKey(message)) {
                final Partition partition = new Partition(
                        new LinkedHashSet<LogEvent>());
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
            LinkedHashMap<String, Set<LogEvent>> map) {
        Set<Partition> ret = new LinkedHashSet<Partition>();
        for (String relation : getRelations()) {
            ret.addAll(getRelationLogEventPartitions(relation, map));
        }
        return ret;
    }

    private Set<Partition> getRelationLogEventPartitions(String relation,
            LinkedHashMap<String, Set<LogEvent>> map) {
        Set<Partition> ret = new LinkedHashSet<Partition>();
        Set<LogEvent> events = map.get(relation);
        if (events == null) {
            return ret;
        }
        for (LogEvent m : events) {
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
        for (LogEvent m : node.getMessages()) {
            relations.addAll(m.getRelations());
        }
        partitions.add(node);
        transitionCache.clear();
    }

    @Override
    public void tagInitial(Partition initialNode, String relation) {
        partitions.add(initialNode);
        addInitialMessages(initialNode.getMessages(), relation);
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

    public Set<LogEvent> getInitialMessages() {
        Set<LogEvent> initial = new LinkedHashSet<LogEvent>();
        for (Set<LogEvent> set : initialEvents.values()) {
            initial.addAll(set);
        }
        return initial;
    }

    /**
     * Check that all partitions are non-empty and disjunct.
     */
    public void checkSanity() {
        int totalCount = 0;
        Set<LogEvent> all = new LinkedHashSet<LogEvent>();
        for (Partition p : getNodes()) {
            if (p.size() == 0) {
                throw new InternalSynopticException(
                        "bisim produced empty partition!");
            }
            all.addAll(p.getMessages());
            totalCount += p.size();
        }
        if (totalCount != all.size()) {
            throw new InternalSynopticException(
                    "partitions are not partitioning messages (overlap)!");
        }
    }
}