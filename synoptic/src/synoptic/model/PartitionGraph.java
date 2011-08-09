package synoptic.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import synoptic.model.interfaces.IGraph;
import synoptic.model.interfaces.ITransition;
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
    private final LinkedHashMap<String, Set<EventNode>> initialEvents = new LinkedHashMap<String, Set<EventNode>>();

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

    /** Whether or not the traces this graph represents are partially ordered */
    private boolean partiallyOrderedTraces = false;

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
            List<LinkedHashSet<Integer>> partitioningIndexSets,
            TemporalInvariantSet invariants) {
        for (String relation : g.getRelations()) {
            addInitialMessages(g.getInitialNodes(relation), relation);
            relations.add(relation);
        }

        partitionByIndexSetsAndLabels(g.getNodes(), partitioningIndexSets);
        this.invariants = invariants;
    }

    @Override
    public void setPartiallyOrdered(boolean po) {
        partiallyOrderedTraces = po;
    }

    @Override
    public boolean isPartiallyOrdered() {
        return partiallyOrderedTraces;
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
        if (transitionCache.containsKey(pNode)) {
            return transitionCache.get(pNode);
        }

        Set<Partition> adjPartitions = new LinkedHashSet<Partition>();
        for (Transition<Partition> tr : pNode.getTransitionsIterator(null)) {
            adjPartitions.add(tr.getTarget());
        }
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
            // Update the set of known relations based on transitions from the
            // event node.
            for (ITransition<EventNode> t : e.getTransitions()) {
                relations.add(t.getRelation());
            }
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
        for (EventType eType : prepartitions.keySet()) {
            partitions.add(new Partition(prepartitions.get(eType)));
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
            for (ITransition<EventNode> t : message.getTransitions()) {
                relations.add(t.getRelation());
            }
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
    private Set<Partition> getEventNodePartitions(
            LinkedHashMap<String, Set<EventNode>> map) {
        Set<Partition> ret = new LinkedHashSet<Partition>();
        for (Set<EventNode> eNodes : map.values()) {
            ret.addAll(getEventNodePartitions(eNodes));
        }
        return ret;
    }

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
    public Set<Partition> getInitialNodes() {
        return getEventNodePartitions(initialEvents);
    }

    public Set<Partition> getTerminalNodes() {
        return getEventNodePartitions(terminalEvents);
    }

    public Set<Partition> getTerminalNodes(String relation) {
        if (!terminalEvents.containsKey(relation)) {
            return Collections.emptySet();
        }
        return getEventNodePartitions(terminalEvents.get(relation));
    }

    @Override
    public Set<Partition> getInitialNodes(String relation) {
        if (!initialEvents.containsKey(relation)) {
            return Collections.emptySet();
        }
        return getEventNodePartitions(initialEvents.get(relation));
    }

    @Override
    public Set<String> getRelations() {
        return relations;
    }

    @Override
    public void add(Partition node) {
        for (EventNode m : node.getEventNodes()) {
            relations.addAll(m.getRelations());
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

    @Override
    public void remove(Partition node) {
        partitions.remove(node);
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
            all.addAll(p.getEventNodes());
            totalCount += p.size();
        }
        if (totalCount != all.size()) {
            throw new InternalSynopticException(
                    "partitions are not partitioning messages (overlap)!");
        }
    }

//    /**
//     * Extracts any synthetic traces from the initial log. A synthetic trace is
//     * identified as any path in the partition graph that does not match an
//     * initial trace from the log.
//     *
//     * @return traces
//     *		Set<List<Partition>> containing the synthetic traces
//     */
//    public Set<List<Partition>> getSyntheticTraces() {
//    	Set<List<Partition>> traces = getAllTraces();
//    	traces.removeAll(getInitialLogTraces());
//    	return traces;
//    }


    public HashSet<String> getAllTraces() {
    	// This will contain all the traces
    	HashSet<String> allTraces = new HashSet<String>();
    	// Constructs the set of all traces
    	for (Partition pNode : getInitialNodes()) {
			getAllTracesHelper(pNode, allTraces, new ArrayList<Partition>());
    	}
    	return allTraces;
    }

    /**
     * Helper method for {@code findAllTraces}. Recursively finds all possible
     * paths from the partition graph and adds them to a set.
     *
     * TODO: This will calculate any subset of nodes multiple times if
     * the topmost node of said subset has multiple parent nodes.  A cache
     * should be used for these nodes to make this process more optimal.
     *
     * @param pNode
     * 		The current node
     * @param allTraces
     * 		The pointer to the set of all paths.
     * @param prefixTrace
     * 		The path of all preceding nodes.
     * @see findAllTraces
     */
    private void getAllTracesHelper(Partition pNode,
    		Set<String> allTraces,
    		ArrayList<Partition> prefixTrace) {
    	Set<Partition> adjPartitions = getAdjacentNodes(pNode);
    	// Check to see if the path has had a single cycle.
    	boolean isCycled = prefixTrace.contains(pNode) ? true : false;
    	// Add the node to the prefix.
    	prefixTrace.add(pNode);
    	// If the node is terminal, then we have a path and it can
    	// be added to the set.
    	if (pNode.isTerminal()) {
    		String path = "";
    		for (Partition p : prefixTrace) {
    			// Convert the paths into a string (currently split by pipes).
    			path += p.getEType().toString() + (p.isTerminal() ? "" : " | ");
    		}
    		allTraces.add(path);
    		return;
    	}
    	// Process all adjacent nodes.
    	for (Partition adjPNode : adjPartitions) {
    		// Negation of:
    		// "If there has been a cycle and the next
    		// node is one that has been encountered."
    		// TODO: Could it be tail-recursive?
    		if (!isCycled || !prefixTrace.contains(pNode))
    			getAllTracesHelper(adjPNode, allTraces, prefixTrace);
    		// Remove anything on the end after returning from the call stack.
    		prefixTrace.remove(prefixTrace.size() - 1);
    	}
    }

    /**
     * Returns the initial log traces as a Set<List<Partition>>
     *
     * @return initialTraces
     */
    public Set<List<Partition>> getInitialLogTraces() {
    	// Will contain all of the initial traces
    	Set<List<Partition>> initialTraces = new HashSet<List<Partition>>();

    	// This will contain the list of all EventNodes in the PartitionGraph
        // List<EventNode> allEvents = new ArrayList<EventNode>();

    	// Adding EventNodes
        // for (Partition pNode : partitions) {
        //	allEvents.addAll(pNode.getEventNodes());
        // }

    	// Find initial events and add the results from each iteration to initialTraces
    	for (Partition pNode : getInitialNodes()) {
    		for (EventNode event : pNode.getEventNodes()) {
    			if (event.isInitial()) {
    				initialTraces.addAll(getInitialLogTracesFromCurrentPartition(pNode, event,
    						new ArrayList<Partition>()));
    			}
    		}
    	}
    	return initialTraces;
    }

    /**
     *
     * @param currentPartition
     * 		The node to start from
     * 		@currentEvent should be from @currentPartition
     * @param currentEvent
     * 		The event to start from
     * @param currentTrace
     * 		The trace to add to
     * @param allEvents
     * 		The pool of events from which to find the next EventNode
     * @return
     * 		Returns a set containing the traces starting from currentPartition and currentEvent
     */
    private Set<List<Partition>> getInitialLogTracesFromCurrentPartition(Partition currentPartition,
    		EventNode currentEvent, List<Partition> currentTrace) {
    	// Will hold traces through the current partition
    	Set<List<Partition>> traces = new HashSet<List<Partition>>();
    	currentTrace.add(currentPartition);

    	// allEvents.remove(currentEvent);

    	if (currentEvent.isTerminal()) {
    		traces.add(currentTrace);
    	} else {

    		// Set<EventNode> nextEvents = EventNode.getDirectSuccessors(currentEvent, allEvents,
			//		!partiallyOrderedTraces);

    		// Gets the next event with relation to time.
    		Set<EventNode> nextEvents = currentEvent.getSuccessors("t");
    		// Finds the next partition to enter with the correct event.
    		for (Partition pNode : getAdjacentNodes(currentPartition)) {
    			for (EventNode possibleNextEvent : pNode.getEventNodes()) {
    				if (nextEvents.contains(possibleNextEvent)) {
    					List<Partition> cloneTrace = new ArrayList<Partition>();
    					cloneTrace.addAll(currentTrace);
    					traces.addAll(getInitialLogTracesFromCurrentPartition(pNode, possibleNextEvent,
    							cloneTrace));
    				}
    			}
    		}
    	}
    	return traces;
    }
}