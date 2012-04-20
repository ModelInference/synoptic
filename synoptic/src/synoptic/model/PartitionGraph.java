package synoptic.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
import synoptic.model.event.EventType;
import synoptic.model.interfaces.IGraph;
import synoptic.model.interfaces.INode;
import synoptic.util.InternalSynopticException;

/**
 * This class implements a partition graph. Nodes are {@code Partition}
 * instances, which are sets of messages -- ( {@code EventNode}) -- and edges
 * are not maintained explicitly, but generated on-the-fly by class
 * {@code Partition}. PartitionGraphs can only be modified via the method
 * {@code apply} which takes a object implementing {@code IOperation}.
 * Operations must perform changes on both representations.
 * 
 * @author sigurd
 */
public class PartitionGraph implements IGraph<Partition> {

    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger("PartitionGraph Logger");

    /** Holds all partitions in this graph. */
    private LinkedHashSet<Partition> partitions = null;

    /**
     * Holds the initial messages in this graph, grouped by the relation w.r.t.
     * which they are initial. We keep track of initial partitions by keeping
     * track of the initial messages but we need to do this for every relation,
     * which is specified by the first argument to the hash-map.
     */
    private EventNode dummyInitialNode = null;

    /**
     * Holds the terminal messages in this graph. Like the initialMessages
     * above, this hash-map maintains them w.r.t the relations.
     */
    private final LinkedHashMap<String, Set<EventNode>> terminalEvents = new LinkedHashMap<String, Set<EventNode>>();

    /** Holds synoptic.invariants that were mined when the graph was created. */
    private TemporalInvariantSet invariants = null;

    /** Holds all relations known to exist in this graph. */
    private final Set<String> relations = new LinkedHashSet<String>();

    /** A cache of inter-partition transitions. */
    private final LinkedHashMap<Partition, Set<Partition>> transitionCache = new LinkedHashMap<Partition, Set<Partition>>();

    /** An ordered list of all partition splits applied to the graph so far. */
    private final LinkedList<PartitionMultiSplit> appliedSplits = new LinkedList<PartitionMultiSplit>();

    /** Initial trace graph. */
    private ChainsTraceGraph traceGraph;

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
    public PartitionGraph(ChainsTraceGraph g, boolean partitionByLabel,
            TemporalInvariantSet invariants) {
        this(g, invariants);

        if (partitionByLabel) {
            partitionByLabels(g.getNodes());
        } else {
            partitionSeparately(g.getNodes());
        }
    }

    public PartitionGraph(ChainsTraceGraph g,
            List<LinkedHashSet<Integer>> partitioningIndexSets,
            TemporalInvariantSet invariants) {
        this(g, invariants);

        partitionByIndexSetsAndLabels(g.getNodes(), partitioningIndexSets);
    }

    /**
     * Creates a partition graph without any partitions. Takes care of setting
     * up the internal initialEvents, invariants, and traceGraph data
     * structures.
     * 
     * @param g
     * @param invariants
     */
    private PartitionGraph(ChainsTraceGraph g, TemporalInvariantSet invariants) {
        dummyInitialNode = g.getDummyInitialNode();
        relations.addAll(g.getRelations());

        this.invariants = invariants;
        this.traceGraph = g;
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
        if (transitionCache.containsKey(pNode)) {
            return transitionCache.get(pNode);
        }

        Set<Partition> adjPartitions = pNode.getAllSuccessors();
        transitionCache.put(pNode, adjPartitions);
        return adjPartitions;
    }

    /**
     * All messages with identical labels are mapped to the same partition.
     * 
     * @param events
     *            Set of message which to be partitioned
     */
    private void partitionByLabels(Collection<EventNode> events) {
        Map<EventType, Set<EventNode>> prepartitions = new LinkedHashMap<EventType, Set<EventNode>>();
        for (EventNode e : events) {
            // Add the event node to a set corresponding to it's event type.
            EventType eType = e.getEType();
            if (!prepartitions.containsKey(eType)) {
                Set<EventNode> eNodes = new LinkedHashSet<EventNode>();
                prepartitions.put(eType, eNodes);
            }
            prepartitions.get(eType).add(e);
        }

        // For each set of event nodes with the same event type, create
        // one partition.
        partitions = new LinkedHashSet<Partition>();
        for (Set<EventNode> eNodes : prepartitions.values()) {
            partitions.add(new Partition(eNodes));
        }

        transitionCache.clear();
    }

    private void partitionByIndexSetsAndLabels(Collection<EventNode> events,
            List<LinkedHashSet<Integer>> partitioningIndexSets) {
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
                            subPartitions[i] = new Partition(m);
                        } else {
                            subPartitions[i].addOneEventNode(m);
                        }
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
            if (!prepartitions.containsKey(message.getEType())) {
                final Partition partition = new Partition(
                        new LinkedHashSet<EventNode>());
                prepartitions.put(message.getEType(), partition);
            }
            prepartitions.get(message.getEType()).addOneEventNode(message);
        }
        for (Partition t : prepartitions.values()) {
            LinkedHashSet<EventNode> iSet = new LinkedHashSet<EventNode>();
            for (EventNode e : t.getEventNodes()) {
                if (initial.contains(e)) {
                    iSet.add(e);
                }
            }
            if (iSet.size() == 0) {
                partitions.add(t);
            } else {
                t.removeEventNodes(iSet);
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
        Set<EventNode> seenENodes = new LinkedHashSet<EventNode>();
        for (EventNode e : events) {
            if (seenENodes.contains(e)) {
                continue;
            }
            Partition partition = new Partition(e);
            partitions.add(partition);
            seenENodes.add(e);
        }
        transitionCache.clear();
    }

    @Override
    public Set<Partition> getNodes() {
        return partitions;
    }

    /**
     * Returns a set of partitions that corresponds to EventNodes in the union
     * of the sets of the input map.values.
     */
    // private Set<Partition> getEventNodePartitions(
    // LinkedHashMap<String, Set<EventNode>> map) {
    // Set<Partition> ret = new LinkedHashSet<Partition>();
    // for (Set<EventNode> eNodes : map.values()) {
    // ret.addAll(getEventNodePartitions(eNodes));
    // }
    // return ret;
    // }

    /**
     * Returns a set of partitions that corresponds to the input eNodes.
     */
    private Set<Partition> getEventNodePartitions(Set<EventNode> eNodes) {
        Set<Partition> ret = new LinkedHashSet<Partition>();
        for (EventNode m : eNodes) {
            ret.add(m.getParent());
        }
        return ret;
    }

    @Override
    public Partition getDummyInitialNode() {
        return dummyInitialNode.getParent();
    }

    // public Set<Partition> getTerminalNodes() {
    // return getEventNodePartitions(terminalEvents);
    // }

    public Set<Partition> getTerminalNodes(String relation) {
        if (!terminalEvents.containsKey(relation)) {
            return Collections.emptySet();
        }
        return getEventNodePartitions(terminalEvents.get(relation));
    }

    @Override
    public Set<String> getRelations() {
        return relations;
    }

    @Override
    public void add(Partition node) {
        for (EventNode m : node.getEventNodes()) {
            relations.addAll(m.getNodeRelations());
        }
        partitions.add(node);

        clearNodeAdjacentsCache(node);
    }

    public void mergeAdjacentsCache(Partition from, Partition to) {
        for (Iterator<Entry<Partition, Set<Partition>>> pIter = transitionCache
                .entrySet().iterator(); pIter.hasNext();) {
            Set<Partition> parts = pIter.next().getValue();
            if (parts.contains(from)) {
                parts.remove(from);
                parts.add(to);
            }
        }
    }

    public void clearNodeAdjacentsCache(Partition node) {
        transitionCache.remove(node);
        // System.out.println("Cache size: " + transitionCache.size());
        for (Iterator<Entry<Partition, Set<Partition>>> pIter = transitionCache
                .entrySet().iterator(); pIter.hasNext();) {
            if (pIter.next().getValue().contains(node)) {
                // System.out.println("cache rm");
                pIter.remove();
            }
        }
        // System.out.println("Cache' size: " + transitionCache.size());
    }

    public void removeFromCache(Partition node) {
        transitionCache.remove(node);
    }

    public void removePartition(Partition node) {
        partitions.remove(node);
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
            all.addAll(p.getEventNodes());
            totalCount += p.size();
        }
        if (totalCount != all.size()) {
            throw new InternalSynopticException(
                    "partitions are not partitioning messages (overlap)!");
        }
    }

    /**
     * Extracts any synthetic traces from the initial log. A synthetic trace is
     * identified as any path in the partition graph that does not match an
     * initial trace from the log.
     * 
     * @return traces Set<List<Partition>> containing the synthetic traces
     */
    public Set<List<Partition>> getSyntheticTraces() {
        Set<List<Partition>> traces = getAllTraces();
        traces.removeAll(getInitialLogTraces());
        return traces;
    }

    /**
     * Traverses the partition graph and returns a set of all possible traces.
     * 
     * @return
     */
    public Set<List<Partition>> getAllTraces() {
        // This will contain all the traces
        Set<List<Partition>> allTraces = new HashSet<List<Partition>>();
        // Constructs the set of all traces
        recursivelyAddTracesToSet(dummyInitialNode.getParent(), allTraces,
                new ArrayList<Partition>());
        return allTraces;
    }

    /**
     * Helper method for {@code getAllTraces}. Recursively finds all possible
     * paths from the partition graph and adds them to a set.
     * 
     * <pre>
     * TODO: This will calculate any subset of nodes multiple times if the topmost
     * node of said subset has multiple parent nodes. A cache should be used for
     * these nodes to make this process more efficient.
     * 
     * TODO: Return a set rather than alter a pointer.
     * 
     * TODO: Make a proper cycle-checking algorithm.
     * </pre>
     * 
     * @param pNode
     *            The current node
     * @param allTraces
     *            The pointer to the set of all paths.
     * @param prefixTrace
     *            The path of all preceding nodes.
     * @see findAllTraces
     */
    private void recursivelyAddTracesToSet(Partition pNode,
            Set<List<Partition>> allTraces, List<Partition> prefixTrace) {
        Set<Partition> adjPartitions = getAdjacentNodes(pNode);
        // Check to see if the path has had a single cycle.
        boolean isCyclic = prefixTrace.contains(pNode);
        // Add the node to the prefix.
        prefixTrace.add(pNode);
        // If the node is terminal, then we have a path and it can
        // be added to the set.
        if (pNode.isTerminal()) {
            List<Partition> trace = new ArrayList<Partition>();
            trace.addAll(prefixTrace);
            allTraces.add(trace);
            return;
        }

        // Process all adjacent nodes.
        for (Partition adjPNode : adjPartitions) {
            // Negation of:
            // "If there has been a cycle and the next
            // node is one that has been encountered."
            if (!isCyclic || !prefixTrace.contains(adjPNode)) {
                recursivelyAddTracesToSet(adjPNode, allTraces, prefixTrace);
                // Remove anything on the end after returning from the call
                // stack.
                prefixTrace.remove(prefixTrace.size() - 1);
            }
        }
    }

    /**
     * Assumes a total-ordered log input. Returns the set of initial log traces.
     * That is, each list of partitions contains some input trace (a sequence of
     * EventNodes containing Event instance with the same trace id).
     * 
     * @return initialTraces
     */
    public Set<List<Partition>> getInitialLogTraces() {
        // Will contain all of the initial traces
        Set<List<Partition>> initialTraces = new HashSet<List<Partition>>();

        // For each initial event, add the entire trace that it belongs to.
        for (EventNode initE : dummyInitialNode.getAllSuccessors()) {
            initialTraces.add(getInitialLogTraceFromEventNode(initE));
        }
        return initialTraces;
    }

    public List<Partition> getInitialLogTraceFromEventNode(EventNode initE) {
        // TODO: this assert sometimes does not hold during testing (on
        // purpose), but it must hold true in deployment.
        //
        // assert initE.isInitial();

        List<Partition> currentTrace = new ArrayList<Partition>();
        currentTrace.add(dummyInitialNode.getParent());

        EventNode currentEvent = initE;
        while (!currentEvent.isTerminal()) {
            currentTrace.add(currentEvent.getParent());
            // We are assuming a totally-ordered input.
            Set<EventNode> nextEvents = currentEvent.getAllSuccessors();
            assert nextEvents.size() == 1;
            currentEvent = nextEvents.iterator().next();
        }

        currentTrace.add(currentEvent.getParent());
        return currentTrace;
    }

    /**
     * Returns paths through a set of partition nodes in the form of a map. The
     * returned map maps a traceID to a path (list of partitions) that passes
     * through ALL of the input set of partitions.
     * 
     * @param parts
     * @return A mapping of trace IDs to a set of transitions that make up a
     *         path
     */
    public Map<Integer, List<Partition>> getPathsThroughPartitions(
            Set<INode<Partition>> parts) {

        if (parts == null || parts.isEmpty()) {
            throw new IllegalArgumentException(
                    "Expected a non-null and non-empty set of partition nodes.");
        }

        // The list of trace IDs that go through the selected partitions.
        List<Set<Integer>> traceIDs = new ArrayList<Set<Integer>>();

        // Loop over all the partitions, and collect the trace IDs of the events
        // in the partitions into a list.
        for (INode<Partition> p : parts) {
            // Temporary trace IDs to be added to the overall list.
            Set<Integer> tempIDs = new HashSet<Integer>();
            for (EventNode e : ((Partition) p).getEventNodes()) {
                tempIDs.add(e.getTraceID());
            }

            if (!tempIDs.isEmpty()) {
                traceIDs.add(tempIDs);
            }
        }

        if (traceIDs.isEmpty()) {
            return Collections.emptyMap();
        }

        // Filter through all of the IDs and keep only
        // the ones that intersect with the selected nodes.
        Set<Integer> intersectionOfIDs = traceIDs.get(0);
        for (int i = 1; i < traceIDs.size(); i++) {
            intersectionOfIDs.retainAll(traceIDs.get(i));
        }

        // If there are no traces through the selected partitions.
        if (intersectionOfIDs.isEmpty()) {
            return Collections.emptyMap();
        }

        final Map<Integer, List<Partition>> paths = new HashMap<Integer, List<Partition>>();

        // For each initial event, add the entire trace that it belongs to.
        for (EventNode initE : dummyInitialNode.getAllSuccessors()) {
            int traceID = initE.getTraceID();
            if (intersectionOfIDs.contains(traceID)) {
                paths.put(traceID, getInitialLogTraceFromEventNode(initE));
            }
        }

        return paths;
    }

    /**
     * Returns a reference to a partition node based on the ID of the node
     * passed. If the node is not found within the graph, null is returned.
     */
    public Partition getNodeByID(int id) {
        for (Partition p : this.getNodes()) {
            if (p.hashCode() == id)
                return p;
        }

        return null;
    }

    /** Returns the initial trace graph. */
    public ChainsTraceGraph getTraceGraph() {
        return traceGraph;
    }
}