package synoptic.invariants.miners;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.model.ChainsTraceGraph;
import synoptic.model.DAGsTraceGraph;
import synoptic.model.EventNode;
import synoptic.model.TraceGraph;
import synoptic.model.Transition;
import synoptic.model.event.DistEventType;
import synoptic.model.event.Event;
import synoptic.model.event.EventType;
import synoptic.model.interfaces.ITransition;

/**
 * TODO: all calls to getTransitions should be getTransitions(relation) where
 * relation is the argument to computeInvariants(). <br />
 * <br/>
 * Implements a temporal invariant mining algorithm for partially ordered logs,
 * by walking the corresponding DAG trace structure in the forward and reverse
 * directions. This algorithm is a generalization of the ChainWalkingTOInvMiner
 * (therefore it passes the same unit tests). <br/>
 * <br/>
 * The algorithm uses followed-by and precedes counts to mine the invariants.
 * That is, it counts the number of times x is followed-by y, and the number of
 * times x precedes y for all event types x and y in the traces. It then re-uses
 * TemporalInvariantSet.extractInvariantsFromWalkCounts() to turn these counts
 * into valid temporal invariants.
 */
public class DAGWalkingPOInvMiner extends CountingInvariantMiner implements
        IPOInvariantMiner, ITOInvariantMiner {

    // TODO: we can set the initial capacity of the following HashMaps more
    // optimally, e.g. (N / 0.75) + 1 where N is the total number of event
    // types. See:
    // http://stackoverflow.com/questions/434989/hashmap-intialization-parameters-load-initialcapacity

    // Tracks global event counts globally -- across all traces.
    Map<EventType, Integer> gEventCnts = new LinkedHashMap<EventType, Integer>();

    // Tracks global followed-by counts -- across all traces.
    Map<EventType, Map<EventType, Integer>> gFollowedByCnts = new LinkedHashMap<EventType, Map<EventType, Integer>>();

    // Tracks global precedence counts -- across all traces.
    Map<EventType, Map<EventType, Integer>> gPrecedesCnts = new LinkedHashMap<EventType, Map<EventType, Integer>>();

    // Tracks which events were observed across all traces.
    Set<EventType> gAlwaysFollowsINITIALSet = null;

    // The set of all event types seen in a single trace.
    Set<EventType> tSeenETypes = new LinkedHashSet<EventType>();

    // Maps a node in the trace DAG to the number of parents this node has
    // in the DAG. This is computed during a pre-traversal of the DAG
    // (modified in the forward trace traversal).
    Map<EventNode, Integer> tNodeToNumParentsMap = new LinkedHashMap<EventNode, Integer>();

    // Maps a node in the trace to the number of children that are below it.
    // This is computed during pre-traversal (modified in reverse trace
    // traversal).
    Map<EventNode, Integer> tNodeToNumChildrenMap = new LinkedHashMap<EventNode, Integer>();

    // Maps a node to a set of nodes that immediately precede this node (the
    // node's parents). Build during pre-traversal, and used for mining
    // FollowedBy counts. We do this because nodes only know about their
    // children, and not their parents.
    Map<EventNode, List<EventNode>> tNodeParentsMap = new LinkedHashMap<EventNode, List<EventNode>>();

    // Given two event types e1, e2. if gEventCoOccurrences[e1] contains e2
    // then there are instances of e1 and e2 that appeared in the same
    // trace.
    Map<EventType, Set<EventType>> gEventCoOccurrences = new LinkedHashMap<EventType, Set<EventType>>();

    // Counts the number of times an event type appears in a trace.
    Map<EventType, Integer> tEventCnts = new LinkedHashMap<EventType, Integer>();

    // /////////////////////////////////////
    // Data structures used by the version of the algorithm that does not
    // mine the NeverConcurrentWith (\nparallel) invariant

    // For an EventNode n, and an event type e, maintains the count of event
    // instances of type e that followed n
    Map<EventNode, Map<EventType, Integer>> tNodeFollowingTypeCnts = new LinkedHashMap<EventNode, Map<EventType, Integer>>();

    // For an EventNode n, and an event type e, maintains the count of event
    // instances of type e that preceded n
    Map<EventNode, Map<EventType, Integer>> tNodePrecedingTypeCnts = new LinkedHashMap<EventNode, Map<EventType, Integer>>();

    // /////////////////////////////////////

    // For an EventNode n, maintains the set of nodes that followed this
    // node in the trace. In practice, only nodes with multiple children
    // will have a record.
    Map<EventNode, Set<EventNode>> tFollowingNodeSets = new LinkedHashMap<EventNode, Set<EventNode>>();

    // For an EventNode n, maintains the set of nodes that preceded this
    // node in the trace. In practice, only nodes with multiple parents will
    // have a record.
    Map<EventNode, Set<EventNode>> tPrecedingNodeSets = new LinkedHashMap<EventNode, Set<EventNode>>();

    // For "a NeverConcurrentWith b" to hold it must be the case that for
    // an a instance and b instance that co-occur in a trace, the two
    // instances must be totally ordered. Therefore, either a is followed by
    // b or a is preceded by b. Whatever the case, the sum total of b's that
    // the a instance must be preceded by AND followed by must be the TOTAL
    // number of b instances in the trace. Notice that if this property
    // holds for all a instances, then it must likewise hold for all b
    // instances (it is symmetric, and the NeverConcurrentWith
    // invariant is symmetric for the same reason).
    //
    // Therefore, to check if "a NeverConcurrentWith b" is true, we can sum
    // the total number of b's that follow or precede each a and check if
    // this total equals number of a's * number of b's. If yes, then the
    // invariant is true. To check if the invariant is true across
    // all traces, we perform the same computation on each trace. However,
    // to amortize the cost of checking across multiple traces we do this
    // check with aggregates.
    //
    // The tTypeFollowingTypeCnts and tTypePrecedingTypeCnts structures are
    // used for maintaining per-trace counts described above. The
    // gEventTypesOrderedBalances structure is computed globally, for a pair
    // of types (e1, e2) it maintains the difference between the all
    // per-trace precedes/follows counts for the two types and the product
    // of their total counts (number of e1's * number of e2's).

    // For two event types e1, e2 in a trace; at the end of the trace
    // traversal tTypeFollowingTypeCnts[e1][e2] will represent the total
    // count of e2 event instances that followed each of the e1 event
    // instances.
    //
    // For example, if the trace is linear: a,a,b,a,b
    // Then tTypeFollowingTypeCnts[a][b] = 5
    // 2 b's follow the first and second a, and 1 b follows the 3rd a, so
    // 2+2+1 =5
    Map<EventType, Map<EventType, Integer>> tTypeFollowingTypeCnts = new LinkedHashMap<EventType, Map<EventType, Integer>>();

    // For two event types e1, e2 in a trace; at the end of the trace
    // traversal tTypePrecedingTypeCnts[e1][e2] will represent the total
    // count of e2 event instances that preceded each of the e1 event
    // instances.
    //
    // Using the prior linear trace example: a,a,b,a,b
    // tTypePrecedingTypeCnts[a][b] = 1 because
    // 0 b's precede the first two a's, and 1 b precedes the third a, so
    // 0+0+1 =1
    //
    // Notice that tTypePrecedingTypeCnts[a][b] +
    // tTypeFollowingTypeCnts[a][b] = 6
    // and number of a's * number of b's = 2*3 = 6
    // since 6 == 6, the invariant "a NeverConcurrentWith b" is true for
    // this example trace.
    Map<EventType, Map<EventType, Integer>> tTypePrecedingTypeCnts = new LinkedHashMap<EventType, Map<EventType, Integer>>();

    // For two event types e1, e2 across all the traces,
    // gEventTypesOrderedBalances[e1][e2] represents the ordering balance.
    // That is, if gEventTypesOrderedBalances[e1][e2] = 0 then every
    // instance of e1 and every instance of e2 that appeared in the same
    // trace were totally ordered. Otherwise,
    // gEventTypesOrderedBalances[e1][e2] is negative, indicating that in
    // some trace some instance of e1 and some instance of e2 were in the
    // same trace but were not ordered.
    Map<EventType, Map<EventType, Integer>> gEventTypesOrderedBalances = new LinkedHashMap<EventType, Map<EventType, Integer>>();

    boolean mineNeverConcurrentWith;

    /**
     * Whether or not distributed invariants of the form (a AlwaysConcurrentWith
     * b, a NeverConcurrentWith b) will be mined and also returned.
     */
    boolean mineConcurrencyInvariants = false;

    public DAGWalkingPOInvMiner() {
        // By default, mine the NeverConcurrentWith invariant.
        mineNeverConcurrentWith = true;
    }

    public DAGWalkingPOInvMiner(boolean mineNeverConcurrentWith) {
        this.mineNeverConcurrentWith = mineNeverConcurrentWith;
    }

    public boolean getMineNeverConcurrentWith() {
        return mineNeverConcurrentWith;
    }

    public TemporalInvariantSet computeInvariants(DAGsTraceGraph g) {
        mineConcurrencyInvariants = true;
        return computeInvariants(g, Event.defTimeRelationStr);
    }

    public TemporalInvariantSet computeInvariants(ChainsTraceGraph g, 
            boolean multipleRelations) {
        mineConcurrencyInvariants = false;
        return computeInvariants(g, Event.defTimeRelationStr);
    }

    /**
     * Computes invariants for a graph g. mineConcurrencyInvariants determines
     * 
     * @param g
     *            input graph to mine invariants over
     * @param mineConcurrencyInvariants
     * @param mineNeverConcurrentWith
     *            whether or not to mine the NeverConcurrentWith invariant
     * @return the set of mined invariants
     */
    public TemporalInvariantSet computeInvariants(TraceGraph<?> g,
            String relation) {
        EventNode initNode = g.getDummyInitialNode(); // relation);

        // TODO: we have to make sure to traverse just those edges that are
        // marked with relation arg.

        gEventCnts.clear();

        // Build the set of all event types in the graph. We will use this set
        // to pre-seed the various maps below. Also, since we're iterating over
        // all nodes, we might as well count up the total counts of instances
        // for each event type.
        Set<EventType> eTypes = new LinkedHashSet<EventType>();
        for (EventNode node : g.getNodes()) {
            EventType e = node.getEType();
            if (e.isSpecialEventType()) {
                continue;
            }

            eTypes.add(e);
            if (!gEventCnts.containsKey(e)) {
                gEventCnts.put(e, 1);
            } else {
                gEventCnts.put(e, gEventCnts.get(e) + 1);
            }
        }

        // A couple of hash sets for containing parents of special nodes.
        List<EventNode> initNodeList = new ArrayList<EventNode>();
        initNodeList.add(initNode);
        List<EventNode> emptyNodeHashSet = new ArrayList<EventNode>();

        gFollowedByCnts.clear();
        gPrecedesCnts.clear();
        gAlwaysFollowsINITIALSet = null;
        tSeenETypes.clear();
        tNodeToNumParentsMap.clear();
        tNodeToNumChildrenMap.clear();
        tNodeParentsMap.clear();
        gEventCoOccurrences.clear();
        tEventCnts.clear();
        tNodeFollowingTypeCnts.clear();
        tNodePrecedingTypeCnts.clear();
        tFollowingNodeSets.clear();
        tPrecedingNodeSets.clear();
        tTypeFollowingTypeCnts.clear();
        tTypePrecedingTypeCnts.clear();
        gEventTypesOrderedBalances.clear();

        // Initialize the event-type contents of the maps that persist
        // across traces (global counts maps).
        for (EventType e : eTypes) {
            Map<EventType, Integer> mapF = new LinkedHashMap<EventType, Integer>();
            Map<EventType, Integer> mapP = new LinkedHashMap<EventType, Integer>();
            Map<EventType, Integer> mapB = new LinkedHashMap<EventType, Integer>();
            gFollowedByCnts.put(e, mapF);
            gPrecedesCnts.put(e, mapP);
            gEventTypesOrderedBalances.put(e, mapB);
            for (EventType e2 : eTypes) {
                mapF.put(e2, 0);
                mapP.put(e2, 0);
                mapB.put(e2, 0);
            }
        }

        // Iterate through all the traces.
        for (Set<EventNode> initTraceNodes : g.getTraceIdToInitNodes().values()) {
            tNodeParentsMap.put(initNode, emptyNodeHashSet);

            // ///////////////////
            // TODO: this assumes that we have a single terminal node. But a PO
            // trace could have multiple terminals. We need to treat terminals
            // as we do with initial nodes -- maintain a termTraceNodes list.
            // ///////////////////

            EventNode termNode = null, termNodeNew = null;
            for (EventNode curNode : initTraceNodes) {
                tNodeParentsMap.put(curNode, initNodeList);
                // A pre-processing step: builds the parent\child counts maps,
                // the parents map, the tSeenETypes set, and determines the
                // terminal node in the trace.
                termNodeNew = preTraverseTrace(curNode);
                if (termNodeNew != null) {
                    termNode = termNodeNew;
                }
            }
            assert (termNode != null);

            // For every pair of event types in the trace record that the two
            // types have event instances that co-occur in some trace.
            Set<EventType> toVisitETypes = new LinkedHashSet<EventType>();
            toVisitETypes.addAll(tSeenETypes);
            for (EventType e1 : tSeenETypes) {
                // We don't consider (e1, e1) as these would be useful for local
                // invariants and we don't use conditional counts for mining
                // local invariants; and we do not consider (e1,e2) if we've
                // already considered (e2,e1).
                toVisitETypes.remove(e1);
                for (EventType e2 : toVisitETypes) {
                    // Optimization: We won't be using event co-occurrence
                    // information for
                    // events that are local.
                    if (e1 == e2) {
                        continue;
                    }
                    if (!(e1 instanceof DistEventType)
                            || !(e2 instanceof DistEventType)) {
                        continue;
                    }
                    if (((DistEventType) e1).getProcessName().equals(
                            ((DistEventType) e2).getProcessName())) {

                        continue;
                    }
                    // </Optimization>

                    // Record that e1 and e2 co-occur
                    if (!gEventCoOccurrences.containsKey(e1)) {
                        gEventCoOccurrences.put(e1,
                                new LinkedHashSet<EventType>());
                    }
                    gEventCoOccurrences.get(e1).add(e2);
                }
            }

            // logger.info("co-occur-cnts: " +
            // traceCoOccurrenceCnts.toString());

            // ////////////////////////////////////////////////////////////////////////
            // Precedes relations recording: traverse the trace rooted at each
            // initial node in the forward direction.
            for (EventNode curNode : initTraceNodes) {
                if (mineNeverConcurrentWith) {
                    forwardTraverseTrace(curNode, null);
                } else {
                    forwardTraverseTraceWithoutNeverConcurrent(curNode, null);
                }
            }

            // ////////////////////////////////////////////////////////////////////////
            // FollowedBy relations recording: traverse the trace rooted at
            // termNode in the reverse direction (following the
            // tNodeParentsMap).
            if (mineNeverConcurrentWith) {
                reverseTraverseTrace(termNode, null);
            } else {
                reverseTraverseTraceWithoutNeverConcurrent(termNode, null);
            }

            if (mineNeverConcurrentWith) {
                // Compute the gEventTypesOrderedBalances for the current trace.

                // TODO: Because the NCwith invariant is symmetric, we only need
                // to consider one of the permutations -- just (e1,e2) and not
                // both (e1,e2) and (e2,e1).

                for (EventType e1 : tSeenETypes) {
                    for (EventType e2 : tSeenETypes) {
                        // Optimization: we won't be using ordering balance for
                        // events that are local.
                        if (e1 == e2) {
                            continue;
                        }
                        if (!(e1 instanceof DistEventType)
                                || !(e2 instanceof DistEventType)) {
                            continue;
                        }
                        if (((DistEventType) e1).getProcessName().equals(
                                ((DistEventType) e2).getProcessName())) {
                            continue;
                        }
                        // </Optimization>

                        int typeFtypeCnt = 0;
                        if (tTypeFollowingTypeCnts.containsKey(e1)
                                && tTypeFollowingTypeCnts.get(e1).containsKey(
                                        e2)) {
                            typeFtypeCnt = tTypeFollowingTypeCnts.get(e1).get(
                                    e2);
                        }

                        int numE1 = tEventCnts.get(e1);
                        int numE2 = tEventCnts.get(e2);

                        int typePtypeCnt = 0;
                        if (tTypePrecedingTypeCnts.containsKey(e1)
                                && tTypePrecedingTypeCnts.get(e1).containsKey(
                                        e2)) {
                            typePtypeCnt = tTypePrecedingTypeCnts.get(e1).get(
                                    e2);
                        }

                        int prevBalance = gEventTypesOrderedBalances.get(e1)
                                .get(e2);

                        // NOTE: since numE1 * numE2 is always >= typeFtypeCnt +
                        // typePtypeCnt, the value is always <= 0. With 0
                        // indicating that \forall \hat{e1}, \forall \hat{e2} e1
                        // \precedes e2 or e2 \precedes e1. We use this to
                        // deduce that e1 and e2 are never concurrent.
                        gEventTypesOrderedBalances.get(e1).put(
                                e2,
                                prevBalance + typeFtypeCnt + typePtypeCnt
                                        - (numE1 * numE2));
                    }
                }
            }

            // Update the AlwaysFollowsINITIALSet set of events by
            // intersecting it with all events seen in this partition.
            if (gAlwaysFollowsINITIALSet == null) {
                // This is the first trace we've processed.
                gAlwaysFollowsINITIALSet = new LinkedHashSet<EventType>(
                        tSeenETypes);
            } else {
                gAlwaysFollowsINITIALSet.retainAll(tSeenETypes);
            }

            // Clear all the per-trace structures to prepare for the next trace.
            tNodeToNumParentsMap.clear();
            tNodeParentsMap.clear();
            tNodeToNumChildrenMap.clear();
            tEventCnts.clear();
            tSeenETypes.clear();
            tFollowingNodeSets.clear();
            tPrecedingNodeSets.clear();

            if (mineNeverConcurrentWith) {
                tTypeFollowingTypeCnts.clear();
                tTypePrecedingTypeCnts.clear();
            } else {
                tNodeFollowingTypeCnts.clear();
                tNodePrecedingTypeCnts.clear();
            }

            // At this point, we've completed all counts computation for the
            // trace rooted at curNode.
        }

        // Extract the AFby, NFby, AP invariants based on counts.
        Set<ITemporalInvariant> pathInvs = extractPathInvariantsFromWalkCounts(
                relation, gEventCnts, gFollowedByCnts, gPrecedesCnts,
                gEventCoOccurrences, gAlwaysFollowsINITIALSet, false);

        if (mineConcurrencyInvariants) {
            // Extract the concurrency invariants based on counts.
            Set<ITemporalInvariant> concurInvs = extractConcurrencyInvariantsFromWalkCounts(
                    mineNeverConcurrentWith, relation, gEventCnts,
                    gPrecedesCnts, gFollowedByCnts, gEventCoOccurrences,
                    gEventTypesOrderedBalances);
            // Merge the two sets.
            pathInvs.addAll(concurInvs);
        }
        // Return pathInvs, which at this point contains any non-redundant
        // concurrency invariants (if these were also mined -- see above).
        return new TemporalInvariantSet(pathInvs);
    } // /computeInvariants

    /**
     * Recursively, depth-first traverses the trace forward to build the
     * parent\child counts maps, the parents map, the tSeenETypes set, and to
     * determine the terminal node in the trace.
     * 
     * @param curNode
     * @param tNodeToNumParentsMap
     * @param tNodeToNumChildrenMap
     * @param tNodeParentsMap
     * @param tSeenETypes
     * @return the terminal node for this trace
     */
    public EventNode preTraverseTrace(EventNode curNode) {

        List<EventNode> parentNodes;
        EventNode childNode;
        EventNode node = curNode;

        while (true) {
            EventType a = node.getEType();

            if (!mineNeverConcurrentWith) {
                if (!tNodeFollowingTypeCnts.containsKey(node)) {
                    tNodeFollowingTypeCnts.put(node,
                            new LinkedHashMap<EventType, Integer>());
                }
                if (!tNodePrecedingTypeCnts.containsKey(node)) {
                    tNodePrecedingTypeCnts.put(node,
                            new LinkedHashMap<EventType, Integer>());
                }
            }

            // Store the total number of children that this node has.
            if (!tNodeToNumChildrenMap.containsKey(node)) {
                // If we haven't visited this node yet...
                tNodeToNumChildrenMap
                        .put(node, node.getAllTransitions().size());
                // Also, increment the count of the corresponding event types.
                if (tEventCnts.containsKey(a)) {
                    tEventCnts.put(a, tEventCnts.get(a) + 1);
                } else {
                    tEventCnts.put(a, 1);
                }
            }

            // Increment the number of parents for the current node.
            if (!tNodeToNumParentsMap.containsKey(node)) {
                // First time we've seen this node -- it has one parent, and we
                // are going to traverse its sub-tree depth first.
                tNodeToNumParentsMap.put(node, 1);
            } else {
                // We've already visited this node -- it has one more parent
                // than before.
                tNodeToNumParentsMap.put(node,
                        tNodeToNumParentsMap.get(node) + 1);

                // Terminate current traversal, because we've already traversed
                // downward from this point.
                return null;
            }

            // We've hit a TERMINAL node, stop.
            if (node.getAllTransitions().size() == 0) {
                // TODO: Why doesn't this work -- if (node.isTerminal()) ?
                return node;
            }

            // Record that we've seen the curNode eType. Note that this
            // set deliberately omits the INITIAL\TERMINAL types.
            tSeenETypes.add(a);

            // curNode has multiple children -- handle them
            // outside of the while loop.
            if (node.getAllTransitions().size() != 1) {
                break;
            }

            // Save the parent-child relationship between this node and the
            // immediately next node (based on the above condition that is just
            // one).
            childNode = node.getAllTransitions().get(0).getTarget();
            if (!tNodeParentsMap.containsKey(childNode)) {
                parentNodes = new ArrayList<EventNode>();
                tNodeParentsMap.put(childNode, parentNodes);
            } else {
                parentNodes = tNodeParentsMap.get(childNode);
            }
            parentNodes.add(node);

            // Move on to the next node in the linear sub-trace without
            // recursing.
            node = childNode;
        }

        // Handle each of the node's child branches recursively.
        EventNode termNode = null;
        for (ITransition<EventNode> trans : node.getAllTransitions()) {
            childNode = trans.getTarget();

            // Build up the parents map for all the children.
            if (!tNodeParentsMap.containsKey(childNode)) {
                parentNodes = new ArrayList<EventNode>();
                tNodeParentsMap.put(childNode, parentNodes);
            } else {
                parentNodes = tNodeParentsMap.get(childNode);
            }
            parentNodes.add(node);

            EventNode ret = preTraverseTrace(childNode);
            if (ret != null) {
                termNode = ret;
            }
        }
        return termNode;

    } // /preTraverseTrace

    /**
     * Recursively, depth-first traverses the trace in the reverse direction to
     * collect event followed-by count statistics. <br />
     * <br />
     * NOTE/TODO: The reverseTraverseTrace and forwardTraverseTrace methods are
     * very similar -- they both traverse DAGs (the trace DAG itself, or the
     * reverse version of the trace DAG), and collect the same kinds of
     * information. Ideally these two methods would be merged into a single
     * traverse method that would abstract the direction (reverse/forward) of
     * the traversal.
     * 
     * @param curNode
     * @param tNodeToNumChildrenMap
     * @param tNodeParentsMap
     * @param tNodeFollowsSetMap
     * @param tFollowingTypeCnts
     * @param gFollowedByCnts
     */
    public void reverseTraverseTrace(EventNode curNode,
            Set<EventNode> tFollowingNodes) {

        // Merge the nodes following the above branch, including the branching
        // node into the set of nodes preceding curNode.
        Set<EventNode> tFollowingNodeSetsNew = new LinkedHashSet<EventNode>();
        if (tFollowingNodes != null) {
            tFollowingNodeSetsNew.addAll(tFollowingNodes);
        }

        EventNode node = curNode;
        Set<EventType> visitedTypes = new LinkedHashSet<EventType>();
        while (true) {
            if (tFollowingNodeSets.containsKey(node)) {
                tFollowingNodeSetsNew.addAll(tFollowingNodeSets.get(node));
            }

            // This guarantees that we only process curNode once we have
            // traversed all of its children (while accumulating the preceding
            // types in the tFollowsNodeFollowsSetMapingTypes above).
            if (tNodeToNumChildrenMap.get(node) > 1) {
                tNodeToNumChildrenMap.put(node,
                        tNodeToNumChildrenMap.get(node) - 1);

                if (!tFollowingNodeSets.containsKey(node)) {
                    tFollowingNodeSets
                            .put(node, new LinkedHashSet<EventNode>());
                }
                tFollowingNodeSets.get(node).addAll(tFollowingNodeSetsNew);
                return;
            }
            // NOTE: We don't need to decrement tNodeToNumChildrenMap[curNode]
            // because we are guaranteed to never pass through this node again.

            // The current event is 'a', and all following events are 'b' --
            // this notation indicates that an 'a' always occurs prior to a
            // 'b' in the trace.
            EventType a = node.getEType();

            // Update the global precedes counts based on the a events that
            // preceded the current b event in this trace.

            // TODO: these counts are re-computed for each node in the DAG. They
            // can be cached and efficiently maintained instead.

            for (EventNode n : tFollowingNodeSetsNew) {
                EventType b = n.getEType();
                if (visitedTypes.contains(b)) {
                    continue;
                }
                if (!a.isTerminalEventType() && !b.isTerminalEventType()) {
                    gFollowedByCnts.get(a).put(b,
                            gFollowedByCnts.get(a).get(b) + 1);
                }
                visitedTypes.add(b);
            }
            visitedTypes.clear();

            if (!tTypeFollowingTypeCnts.containsKey(a)) {
                tTypeFollowingTypeCnts.put(a,
                        new LinkedHashMap<EventType, Integer>());
            }
            mergeNodesSetIntoMap(tTypeFollowingTypeCnts.get(a),
                    tFollowingNodeSetsNew);

            tFollowingNodeSetsNew.add(node);

            // Nodes with multiple parents are handled outside the loop.
            List<EventNode> parents = tNodeParentsMap.get(node);
            if (parents.size() != 1) {
                break;
            }

            // Move on to the next node in the trace without recursion.
            node = parents.get(0);

            // We've hit the INITIAL node, stop.
            if (tNodeParentsMap.get(node).size() == 0) {
                // TODO: why doesn't this work --
                // if(node.getEType().isInitialEventType()) ?
                return;
            }
        }

        // Handle each of the node's parent branches recursively.
        List<EventNode> parents = tNodeParentsMap.get(node);
        for (int i = 0; i < parents.size(); i++) {
            EventNode parentNode = parents.get(i);

            // We do not create a new copy of following types for each parent,
            // because each parent already has its own -- maintained as part of
            // tNodeFollowsSetMap (built in preTraverseTrace()).

            // Only process those parents that are not INITIAL nodes.
            if (tNodeParentsMap.get(parentNode).size() != 0) {
                reverseTraverseTrace(parentNode, tFollowingNodeSetsNew);
            }
        }
        return;
    }// /reverseTraverseTrace

    /**
     * Recursively, depth-first traverse the trace in the forward direction to
     * collect event precedence count statistics.
     * 
     * @param curNode
     * @param tNodeToNumParentsMap
     * @param tNodePrecedesSetMap
     * @param tPrecedingTypeCnts
     * @param gPrecedesCnts
     */
    public void forwardTraverseTrace(EventNode curNode,
            Set<EventNode> tPrecedingNodes) {

        // Merge the nodes preceding the above branch, including the branching
        // node into the set of nodes preceding curNode.
        Set<EventNode> tPrecedingNodesNew = new LinkedHashSet<EventNode>();
        if (tPrecedingNodes != null) {
            tPrecedingNodesNew.addAll(tPrecedingNodes);
        }

        EventNode node = curNode;
        Set<EventType> visitedTypes = new LinkedHashSet<EventType>();
        while (true) {
            if (tPrecedingNodeSets.containsKey(node)) {
                tPrecedingNodesNew.addAll(tPrecedingNodeSets.get(node));
            }

            // This guarantees that we only process curNode once we have
            // traversed all of its parents (while accumulating the preceding
            // types in tPrecedingNodeSets).
            if (tNodeToNumParentsMap.get(node) > 1) {
                tNodeToNumParentsMap.put(node,
                        tNodeToNumParentsMap.get(node) - 1);

                if (!tPrecedingNodeSets.containsKey(node)) {
                    tPrecedingNodeSets
                            .put(node, new LinkedHashSet<EventNode>());
                }
                tPrecedingNodeSets.get(node).addAll(tPrecedingNodesNew);
                return;
            }
            // NOTE: We don't need to decrement tNodeToNumParentsMap[curNode]
            // because we are guaranteed to never pass through this node again.

            // The current event is 'b', and all prior events are 'a' --
            // this notation indicates that an 'a' always occurs prior to a
            // 'b' in the trace.
            EventType b = node.getEType();

            // Update the global precedes counts based on the a event types that
            // preceded the current b event in this trace.
            // i.e., gPrecedesCnts[a][b]++
            // TODO: these counts are re-computed for each node in the DAG. They
            // can be cached and efficiently maintained instead.
            for (EventNode n : tPrecedingNodesNew) {
                EventType a = n.getEType();
                if (visitedTypes.contains(a)) {
                    continue;
                }
                gPrecedesCnts.get(a).put(b, gPrecedesCnts.get(a).get(b) + 1);
                visitedTypes.add(a);
            }
            visitedTypes.clear();

            if (!tTypePrecedingTypeCnts.containsKey(b)) {
                tTypePrecedingTypeCnts.put(b,
                        new LinkedHashMap<EventType, Integer>());
            }
            mergeNodesSetIntoMap(tTypePrecedingTypeCnts.get(b),
                    tPrecedingNodesNew);

            tPrecedingNodesNew.add(node);

            // Nodes with multiple children are handled outside the loop.
            if (node.getAllTransitions().size() != 1) {
                break;
            }

            // Move on to the next node in the trace without recursion.
            node = node.getAllTransitions().get(0).getTarget();

            // We've hit a TERMINAL node, stop.
            if (node.getAllTransitions().size() == 0) {
                return;
            }
        }

        // Handle each of the node's child branches recursively.
        List<Transition<EventNode>> transitions = node.getAllTransitions();
        for (int i = 0; i < transitions.size(); i++) {
            ITransition<EventNode> trans = transitions.get(i);
            EventNode childNode = trans.getTarget();

            // We do not create a new copy of preceding types for each child,
            // because each child already has its own -- maintained as part of
            // tNodePrecedesSetMap (built in preTraverseTrace()).

            // Only process children that are not TERMINAL nodes.
            if (childNode.getAllTransitions().size() > 0) {
                forwardTraverseTrace(childNode, tPrecedingNodesNew);
            }
        }
        return;
    } // /forwardTraverseTrace

    /**
     * Merges the count of event types from the set of nodes in src into dst.
     * For example, if src contains instances {a,a',b} and dst is empty then dst
     * will contain {a: 2, b:1}.
     * 
     * @param dst
     * @param src
     */
    private void mergeNodesSetIntoMap(Map<EventType, Integer> dst,
            Set<EventNode> src) {
        if (src == null) {
            return;
        }
        for (EventNode n : src) {
            EventType e = n.getEType();
            if (!dst.containsKey(e)) {
                dst.put(e, 1);
            } else {
                dst.put(e, dst.get(e) + 1);
            }
        }
    }

    // ///////////////////////////////////////////////////////////////////////////////
    // Without NeverConcurrent invariant versions.

    /**
     * Recursively, depth-first traverses the trace in the reverse direction to
     * collect event followed-by count statistics.
     * 
     * @param curNode
     * @param tNodeToNumChildrenMap
     * @param tNodeParentsMap
     * @param tNodeFollowsSetMap
     * @param tFollowingTypeCnts
     * @param gFollowedByCnts
     */
    public void reverseTraverseTraceWithoutNeverConcurrent(EventNode curNode,
            Map<EventType, Integer> tFollowingTypeCnts) {

        while (true) {
            // If we reach a node that has nodes we haven't seen followed before
            // then we want to include them in the tFollowingTypes.
            mergeIntegerMapsWithAddition(tNodeFollowingTypeCnts.get(curNode),
                    tFollowingTypeCnts);
            tFollowingTypeCnts = tNodeFollowingTypeCnts.get(curNode);

            // This guarantees that we only process curNode once we have
            // traversed all of its children (while accumulating the preceding
            // types in the tFollowsNodeFollowsSetMapingTypes above).
            if (tNodeToNumChildrenMap.get(curNode) > 1) {
                tNodeToNumChildrenMap.put(curNode,
                        tNodeToNumChildrenMap.get(curNode) - 1);
                return;
            }
            // NOTE: We don't need to decrement tNodeToNumChildrenMap[curNode]
            // because we are guaranteed to never pass through this node again.

            // The current event is 'a', and all following events are 'b' --
            // this notation indicates that an 'a' always occurs prior to a
            // 'b' in the trace.
            EventType a = curNode.getEType();

            // Update the global precedes counts based on the a events that
            // preceded the current b event in this trace.
            for (EventType b : tFollowingTypeCnts.keySet()) {
                if (!a.isTerminalEventType() && !b.isTerminalEventType()) {
                    gFollowedByCnts.get(a).put(b,
                            gFollowedByCnts.get(a).get(b) + 1);
                }
            }

            if (!tFollowingTypeCnts.containsKey(a)) {
                tFollowingTypeCnts.put(a, 1);
            } else {
                tFollowingTypeCnts.put(a, tFollowingTypeCnts.get(a) + 1);
            }

            // Nodes with multiple parents are handled outside the loop.
            List<EventNode> parents = tNodeParentsMap.get(curNode);
            if (parents.size() != 1) {
                break;
            }

            // Move on to the next node in the trace without recursion.
            curNode = parents.get(0);

            // We've hit the INITIAL node, stop.
            if (tNodeParentsMap.get(curNode).size() == 0) {
                return;
            }
        }

        // Handle each of the node's parent branches recursively.
        List<EventNode> parents = tNodeParentsMap.get(curNode);
        for (int i = 0; i < parents.size(); i++) {
            EventNode parentNode = parents.get(i);

            // We do not create a new copy of following types for each parent,
            // because each parent already has its own -- maintained as part of
            // tNodeFollowsSetMap (built in preTraverseTrace()).

            // Only process those parents that are not INITIAL nodes.
            if (tNodeParentsMap.get(parentNode).size() != 0) {
                reverseTraverseTraceWithoutNeverConcurrent(parentNode,
                        tFollowingTypeCnts);
            }
        }
        return;
    } // /reverseTraverseTraceWithoutNeverConcurrent

    /**
     * Recursively, depth-first traverse the trace in the forward direction to
     * collect event precedence count statistics.
     * 
     * @param curNode
     * @param tNodeToNumParentsMap
     * @param tNodePrecedesSetMap
     * @param tPrecedingTypeCnts
     * @param gPrecedesCnts
     */
    public void forwardTraverseTraceWithoutNeverConcurrent(EventNode curNode,
            Map<EventType, Integer> tPrecedingTypeCnts) {

        while (true) {
            // If we reach a node that has nodes preceding it
            // then we want to include them in the tPrecedingTypes and we want
            // to save the nodes that preceded us so far in the same map.
            mergeIntegerMapsWithAddition(tNodePrecedingTypeCnts.get(curNode),
                    tPrecedingTypeCnts);
            tPrecedingTypeCnts = tNodePrecedingTypeCnts.get(curNode);

            // This guarantees that we only process curNode once we have
            // traversed all of its parents (while accumulating the preceding
            // types in the tNodePrecedesSetMap above).
            if (tNodeToNumParentsMap.get(curNode) > 1) {
                tNodeToNumParentsMap.put(curNode,
                        tNodeToNumParentsMap.get(curNode) - 1);
                return;
            }
            // NOTE: We don't need to decrement tNodeToNumParentsMap[curNode]
            // because we are guaranteed to never pass through this node again.

            // The current event is 'b', and all prior events are 'a' --
            // this notation indicates that an 'a' always occurs prior to a
            // 'b' in the trace.
            EventType b = curNode.getEType();

            // Update the global precedes counts based on the a events that
            // preceded the current b event in this trace.
            for (EventType a : tPrecedingTypeCnts.keySet()) {
                gPrecedesCnts.get(a).put(b, gPrecedesCnts.get(a).get(b) + 1);
            }

            if (!tPrecedingTypeCnts.containsKey(b)) {
                tPrecedingTypeCnts.put(b, 1);
            } else {
                tPrecedingTypeCnts.put(b, tPrecedingTypeCnts.get(b) + 1);
            }

            // Nodes with multiple children are handled outside the loop.
            if (curNode.getAllTransitions().size() != 1) {
                break;
            }

            // Move on to the next node in the trace without recursion.
            curNode = curNode.getAllTransitions().get(0).getTarget();

            // We've hit a TERMINAL node, stop.
            if (curNode.getAllTransitions().size() == 0) {
                return;
            }
        }

        // Handle each of the node's child branches recursively.
        for (ITransition<EventNode> trans : curNode.getAllTransitions()) {
            EventNode childNode = trans.getTarget();
            // We do not create a new copy of preceding types for each child,
            // because each child already has its own -- maintained as part of
            // tNodePrecedesSetMap (built in preTraverseTrace()).

            // Only process children that are not TERMINAL nodes.
            if (childNode.getAllTransitions().size() > 0) {
                forwardTraverseTraceWithoutNeverConcurrent(childNode,
                        tPrecedingTypeCnts);
            }
        }
        return;
    } // /forwardTraverseTraceWithoutNeverConcurrent

    /**
     * A helper function to merge the src map into the dst map, by adding the
     * leaf values, or adding to an implicit 0 in the dst map if it doesn't
     * contain the requisite keys.
     * 
     * @param dst
     * @param src
     */
    private void mergeIntegerMapsWithAddition(Map<EventType, Integer> dst,
            Map<EventType, Integer> src) {
        if (src == null) {
            return;
        }

        for (Entry<EventType, Integer> eEntry : src.entrySet()) {
            if (!dst.containsKey(eEntry.getKey())) {
                dst.put(eEntry.getKey(), eEntry.getValue());
            } else {
                dst.put(eEntry.getKey(),
                        dst.get(eEntry.getKey()) + eEntry.getValue());
            }
        }
    }

    // /Without NeverConcurrent invariant versions
    // /////////////////////////////////////////////////////////////////////////////////
}
