package synoptic.model;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
 * not maintained explicitly, but generated on-the-fly by class {@code
 * Partition}. PartitionGraphs can only be modified via the method {@code apply}
 * which takes a object implementing {@code IOperation}. Operations must perform
 * changes on both representations.
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
    private final LinkedHashMap<String, Set<LogEvent>> initialMessages = new LinkedHashMap<String, Set<LogEvent>>();

    /**
     * Holds the terminal messages in this graph. Like the initialMessages
     * above, this hash-map maintains them w.r.t the relations.
     */
    private final LinkedHashMap<String, Set<LogEvent>> terminalMessages = new LinkedHashMap<String, Set<LogEvent>>();

    /** holds synoptic.invariants that were mined when the graph was created */
    private TemporalInvariantSet invariants = null;

    /** holds all relations known to exist in this graph */
    private final Set<String> relations = new LinkedHashSet<String>();

    public PartitionGraph(IGraph<LogEvent> g) {
        this(g, false);
    }

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
    public PartitionGraph(IGraph<LogEvent> g, boolean partitionByLabel) {
        for (String relation : g.getRelations()) {
            addInitialMessages(g.getInitialNodes(relation), relation);
            relations.add(relation);
        }

        if (partitionByLabel) {
            partitionByLabels(g.getNodes());
        } else {
            partitionSeparately(g.getNodes());
        }

        // Compute the invariants of the input graph.
        invariants = TemporalInvariantSet.computeInvariants(g);
        invariants.filterOutTautologicalInvariants();
    }

    private void addInitialMessages(Set<LogEvent> initialMessages,
            String relation) {
        if (!this.initialMessages.containsKey(relation)) {
            this.initialMessages.put(relation, new LinkedHashSet<LogEvent>());
        }
        this.initialMessages.get(relation).addAll(initialMessages);
    }

    public TemporalInvariantSet getInvariants() {
        return invariants;
    }

    public Partition partitionFromMessage(LogEvent message) {
        return message.getParent();
    }

    public IOperation apply(IOperation op) {
        return op.commit(this);
    }

    /**
     * All messages with identical labels are mapped to the same partition.
     * 
     * @param messages
     *            Set of message which to be partitioned
     */
    private void partitionByLabels(Collection<LogEvent> messages) {
        partitions = new LinkedHashSet<Partition>();
        final Map<String, Partition> prepartitions = new LinkedHashMap<String, Partition>();
        for (LogEvent message : messages) {
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

    private void partitionByLabelsAndInitial(Collection<LogEvent> messages,
            Set<LogEvent> initial) {
        partitions = new LinkedHashSet<Partition>();
        final Map<String, Partition> prepartitions = new LinkedHashMap<String, Partition>();
        for (LogEvent message : messages) {
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
     * Each message is mapped to its own unique partition. This is the most
     * direct means of mapping a graph into a partition graph.
     * 
     * @param messages
     *            Set of message to map
     */
    private void partitionSeparately(Collection<LogEvent> messages) {
        partitions = new LinkedHashSet<Partition>();
        final Map<LogEvent, Partition> prepartitions = new LinkedHashMap<LogEvent, Partition>();
        for (LogEvent message : messages) {
            if (!prepartitions.containsKey(message)) {
                final Partition partition = new Partition(
                        new LinkedHashSet<LogEvent>());
                partitions.add(partition);
                prepartitions.put(message, partition);
            }
            prepartitions.get(message).addMessage(message);
        }
    }

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

    public Set<Partition> getInitialNodes() {
        return getAllRelationsLogEventPartitions(initialMessages);
    }

    public Set<Partition> getTerminalNodes() {
        return getAllRelationsLogEventPartitions(terminalMessages);
    }

    public Set<Partition> getTerminalNodes(String relation) {
        return getRelationLogEventPartitions(relation, terminalMessages);
    }

    public Set<Partition> getInitialNodes(String relation) {
        return getRelationLogEventPartitions(relation, initialMessages);
    }

    public Set<String> getRelations() {
        return relations;
    }

    public void add(Partition node) {
        for (LogEvent m : node.getMessages()) {
            relations.addAll(m.getRelations());
        }
        partitions.add(node);
    }

    public void tagInitial(Partition initialNode, String relation) {
        partitions.add(initialNode);
        addInitialMessages(initialNode.getMessages(), relation);
    }

    public void remove(Partition node) {
        partitions.remove(node);
    }

    public void tagTerminal(Partition terminalNode, String relation) {
        throw new InternalSynopticException(
                "PartitionGraph tags terminal partitions implicitly -- by considering the LogEvents the partition contains.");
    }

    public Set<LogEvent> getInitialMessages() {
        Set<LogEvent> initial = new LinkedHashSet<LogEvent>();
        for (Set<LogEvent> set : initialMessages.values()) {
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