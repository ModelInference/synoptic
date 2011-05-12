package synoptic.invariants.miners;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import synoptic.invariants.TemporalInvariantSet;
import synoptic.main.TraceParser;
import synoptic.model.EventNode;
import synoptic.model.EventType;
import synoptic.model.interfaces.IGraph;
import synoptic.model.interfaces.ITransition;
import synoptic.util.InternalSynopticException;

/**
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
public class DAGWalkingPOInvMiner extends InvariantMiner {

    @Override
    public TemporalInvariantSet computeInvariants(IGraph<EventNode> g) {
        String relation = TraceParser.defaultRelation;

        if (g.getInitialNodes().isEmpty() || g.getInitialNodes().size() != 1) {
            throw new InternalSynopticException(
                    "Cannot compute invariants over a graph that doesn't have exactly one INITIAL node.");
        }

        EventNode initNode = g.getInitialNodes().iterator().next();
        if (!initNode.getEType().isInitialEventType()) {
            throw new InternalSynopticException(
                    "Cannot compute invariants over a graph that doesn't have exactly one INITIAL node.");
        }

        // TODO: we can set the initial capacity of the following HashMaps more
        // optimally, e.g. (N / 0.75) + 1 where N is the total number of event
        // types. See:
        // http://stackoverflow.com/questions/434989/hashmap-intialization-parameters-load-initialcapacity

        // Tracks global event counts globally -- across all traces.
        LinkedHashMap<EventType, Integer> gEventCnts = new LinkedHashMap<EventType, Integer>();
        // Tracks global followed-by counts -- across all traces.
        LinkedHashMap<EventType, LinkedHashMap<EventType, Integer>> gFollowedByCnts = new LinkedHashMap<EventType, LinkedHashMap<EventType, Integer>>();
        // Tracks global precedence counts -- across all traces.
        LinkedHashMap<EventType, LinkedHashMap<EventType, Integer>> gPrecedesCnts = new LinkedHashMap<EventType, LinkedHashMap<EventType, Integer>>();
        // Tracks which events were observed across all traces.
        LinkedHashSet<EventType> gAlwaysFollowsINITIALSet = null;

        // The set of all event types seen in a single trace.
        LinkedHashSet<EventType> tSeenETypes = new LinkedHashSet<EventType>();
        // Maps a node in the trace DAG to the number of parents this node has
        // in the DAG. This is computed during a pre-traversal of the DAG
        // (modified in the forward trace traversal).
        LinkedHashMap<EventNode, Integer> tNodeToNumParentsMap = new LinkedHashMap<EventNode, Integer>();
        // Maps a node in the trace to the number of children that are below it.
        // This is computed during pre-traversal (modified in reverse trace
        // traversal).
        LinkedHashMap<EventNode, Integer> tNodeToNumChildrenMap = new LinkedHashMap<EventNode, Integer>();

        // Maps a node to a set of event types that precede it. In practice,
        // this only maps those nodes that are either (1) have a parent with
        // multiple children, or (2) have multiple parents.
        LinkedHashMap<EventNode, LinkedHashSet<EventType>> tNodePrecedesSetMap = new LinkedHashMap<EventNode, LinkedHashSet<EventType>>();
        // Maps a node to a set of event types that follow it it. In practice,
        // this only maps those nodes that are either (1) have multiple
        // children, or (2) have a child with multiple parents.
        LinkedHashMap<EventNode, LinkedHashSet<EventType>> tNodeFollowsSetMap = new LinkedHashMap<EventNode, LinkedHashSet<EventType>>();

        // Maps a node to a set of nodes that immediately precede this node (the
        // node's parents). Build during pre-traversal, and used for mining
        // FollowedBy counts. We do this because nodes only know about their
        // children, and not their parents.
        LinkedHashMap<EventNode, LinkedHashSet<EventNode>> tNodeParentsMap = new LinkedHashMap<EventNode, LinkedHashSet<EventNode>>();

        // Maintains a map of trace id to the set of initial nodes in the trace.
        LinkedHashMap<Integer, LinkedHashSet<EventNode>> traceIdToInitNodes = buildTraceIdToInitNodesMap(initNode);

        // A couple of hash sets for containing parents of special nodes.
        LinkedHashSet<EventNode> initNodeHashSet = new LinkedHashSet<EventNode>();
        initNodeHashSet.add(initNode);
        LinkedHashSet<EventNode> emptyNodeHashSet = new LinkedHashSet<EventNode>();

        // Iterate through all the traces.
        for (LinkedHashSet<EventNode> initTraceNodes : traceIdToInitNodes
                .values()) {
            tNodeParentsMap.put(initNode, emptyNodeHashSet);
            EventNode termNode = null, termNodeNew = null;
            for (EventNode curNode : initTraceNodes) {
                tNodeParentsMap.put(curNode, initNodeHashSet);
                // A pre-processing step: builds the parent\child counts maps,
                // the parents map, the tSeenETypes set, and determines the
                // terminal node in the trace.
                termNodeNew = preTraverseTrace(curNode, tNodeToNumParentsMap,
                        tNodeToNumChildrenMap, tNodeParentsMap, tSeenETypes);
                if (termNodeNew != null) {
                    termNode = termNodeNew;
                }
            }
            assert (termNode != null);

            // AP counts collection: traverse the trace rooted at each initial
            // node in the forward direction.
            for (EventNode curNode : initTraceNodes) {
                forwardTraverseTrace(curNode, tNodeToNumParentsMap,
                        tNodePrecedesSetMap, null, gPrecedesCnts, gEventCnts);
            }

            // AFby\NFby counts collection: traverse the trace rooted at
            // termNode in the reverse direction (following the
            // tNodeParentsMap).
            reverseTraverseTrace(termNode, tNodeToNumChildrenMap,
                    tNodeParentsMap, tNodeFollowsSetMap, null, gFollowedByCnts);

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
            tNodeFollowsSetMap.clear();
            tNodePrecedesSetMap.clear();
            tNodeToNumChildrenMap.clear();
            tSeenETypes.clear();

            // At this point, we've completed all counts computation for the
            // trace rooted at curNode.
        }
        return extractInvariantsFromWalkCounts(relation, gEventCnts,
                gFollowedByCnts, gPrecedesCnts, gAlwaysFollowsINITIALSet);
    }

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
    public EventNode preTraverseTrace(EventNode curNode,
            LinkedHashMap<EventNode, Integer> tNodeToNumParentsMap,
            LinkedHashMap<EventNode, Integer> tNodeToNumChildrenMap,
            LinkedHashMap<EventNode, LinkedHashSet<EventNode>> tNodeParentsMap,
            LinkedHashSet<EventType> tSeenETypes) {

        LinkedHashSet<EventNode> parentNodes;
        EventNode childNode;

        while (true) {

            // Store the total number of children that this node has.
            if (!tNodeToNumChildrenMap.containsKey(curNode)) {
                tNodeToNumChildrenMap.put(curNode, curNode.getTransitions()
                        .size());
            }

            // Increment the number of parents for the current node.
            if (!tNodeToNumParentsMap.containsKey(curNode)) {
                // First time we've seen this node -- it has one parent, and we
                // are going to traverse its sub-tree depth first.
                tNodeToNumParentsMap.put(curNode, 1);
            } else {
                // We've already visited this node -- it has one more parent
                // than before.
                tNodeToNumParentsMap.put(curNode,
                        tNodeToNumParentsMap.get(curNode) + 1);

                // Terminate current traversal, because we've already traversed
                // downward from this point.
                return null;
            }

            // We've hit a TERMINAL node, stop.
            if (curNode.getTransitions().size() == 0) {
                return curNode;
            }

            // Record that we've seen the curNode eType. Note that this
            // set will deliberately miss INITIAL\TERMINAL types.
            tSeenETypes.add(curNode.getEType());

            // curNode has multiple children -- handle them
            // outside of the while loop.
            if (curNode.getTransitions().size() != 1) {
                break;
            }

            // Save the parent-child relationship between this node and the
            // immediately next node (based on the above condition that is just
            // one).
            childNode = curNode.getTransitions().get(0).getTarget();
            if (!tNodeParentsMap.containsKey(childNode)) {
                parentNodes = new LinkedHashSet<EventNode>();
                tNodeParentsMap.put(childNode, parentNodes);
            } else {
                parentNodes = tNodeParentsMap.get(childNode);
            }
            parentNodes.add(curNode);

            // Move on to the next node in the linear sub-trace without
            // recursing.
            curNode = childNode;
        }

        // Handle each of the node's child branches recursively.
        EventNode termNode = null;
        for (ITransition<EventNode> trans : curNode.getTransitions()) {
            childNode = trans.getTarget();

            // Build up the parents map for all the children.
            if (!tNodeParentsMap.containsKey(childNode)) {
                parentNodes = new LinkedHashSet<EventNode>();
                tNodeParentsMap.put(childNode, parentNodes);
            } else {
                parentNodes = tNodeParentsMap.get(childNode);
            }
            parentNodes.add(curNode);

            EventNode ret = preTraverseTrace(childNode, tNodeToNumParentsMap,
                    tNodeToNumChildrenMap, tNodeParentsMap, tSeenETypes);
            if (ret != null) {
                termNode = ret;
            }
        }
        return termNode;

    } // /preTraverseTrace

    /**
     * Recursively, depth-first traverses the trace in the reverse direction to
     * collect event followed-by count statistics.
     * 
     * @param curNode
     * @param tNodeToNumChildrenMap
     * @param tNodeParentsMap
     * @param tNodeFollowsSetMap
     * @param tFollowingTypes
     * @param gFollowedByCnts
     */
    public void reverseTraverseTrace(
            EventNode curNode,
            LinkedHashMap<EventNode, Integer> tNodeToNumChildrenMap,
            LinkedHashMap<EventNode, LinkedHashSet<EventNode>> tNodeParentsMap,
            LinkedHashMap<EventNode, LinkedHashSet<EventType>> tNodeFollowsSetMap,
            LinkedHashSet<EventType> tFollowingTypes,
            LinkedHashMap<EventType, LinkedHashMap<EventType, Integer>> gFollowedByCnts) {

        if (!tNodeFollowsSetMap.containsKey(curNode)) {
            tNodeFollowsSetMap.put(curNode, new LinkedHashSet<EventType>());
            if (tFollowingTypes == null) {
                tFollowingTypes = tNodeFollowsSetMap.get(curNode);
            }
        }
        if (tFollowingTypes != null) {
            tNodeFollowsSetMap.get(curNode).addAll(tFollowingTypes);
        }

        while (true) {
            // If we reach a node that has nodes we haven't seen followed before
            // then we want to include them in the tFollowingTypes.
            if (tNodeFollowsSetMap.containsKey(curNode)) {
                tNodeFollowsSetMap.get(curNode).addAll(tFollowingTypes);
                tFollowingTypes = tNodeFollowsSetMap.get(curNode);
            }

            // This guarantees that we only process curNode once we have
            // traversed all of its children (while accumulating the preceding
            // types in the tFollowtNodeFollowsSetMapingTypes above).
            if (tNodeToNumChildrenMap.get(curNode) > 1) {
                tNodeToNumChildrenMap.put(curNode,
                        tNodeToNumChildrenMap.get(curNode) - 1);
                if (!tNodeFollowsSetMap.containsKey(curNode)) {
                    tNodeFollowsSetMap.put(curNode,
                            new LinkedHashSet<EventType>());
                }
                tNodeFollowsSetMap.get(curNode).addAll(tFollowingTypes);
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
            for (EventType b : tFollowingTypes) {
                LinkedHashMap<EventType, Integer> precedingLabelCnts;
                if (!gFollowedByCnts.containsKey(a)) {
                    precedingLabelCnts = new LinkedHashMap<EventType, Integer>();
                    gFollowedByCnts.put(a, precedingLabelCnts);
                } else {
                    precedingLabelCnts = gFollowedByCnts.get(a);
                }
                if (!precedingLabelCnts.containsKey(b)) {
                    precedingLabelCnts.put(b, 1);
                } else {
                    precedingLabelCnts.put(b, precedingLabelCnts.get(b) + 1);
                }
            }
            tFollowingTypes.add(a);

            // Nodes with multiple parents are handled outside the loop.
            if (tNodeParentsMap.get(curNode).size() != 1) {
                break;
            }

            // Move on to the next node in the trace without recursion.
            curNode = tNodeParentsMap.get(curNode).iterator().next();

            // We've hit the INITIAL node, stop.
            if (tNodeParentsMap.get(curNode).size() == 0) {
                return;
            }
        }

        // Handle each of the node's parent branches recursively.
        Iterator<EventNode> iter = tNodeParentsMap.get(curNode).iterator();

        while (iter.hasNext()) {
            EventNode parentNode = iter.next();
            // We do not create a new copy of following types for each parent,
            // because each parent already has its own -- maintained as part of
            // tNodeFollowsSetMap (built in preTraverseTrace()).

            // Only process those parents that are not INITIAL nodes.
            if (tNodeParentsMap.get(parentNode).size() != 0) {
                reverseTraverseTrace(parentNode, tNodeToNumChildrenMap,
                        tNodeParentsMap, tNodeFollowsSetMap, tFollowingTypes,
                        gFollowedByCnts);
            }
        }
        return;
    } // /reverseTraverseTrace

    /**
     * Recursively, depth-first traverse the trace in the forward direction to
     * collect event precedence count statistics.
     * 
     * @param curNode
     * @param tNodeToNumParentsMap
     * @param tNodePrecedesSetMap
     * @param tPrecedingTypes
     * @param gPrecedesCnts
     * @param gEventCnts
     */
    public void forwardTraverseTrace(
            EventNode curNode,
            LinkedHashMap<EventNode, Integer> tNodeToNumParentsMap,
            LinkedHashMap<EventNode, LinkedHashSet<EventType>> tNodePrecedesSetMap,
            LinkedHashSet<EventType> tPrecedingTypes,
            LinkedHashMap<EventType, LinkedHashMap<EventType, Integer>> gPrecedesCnts,
            LinkedHashMap<EventType, Integer> gEventCnts) {

        if (!tNodePrecedesSetMap.containsKey(curNode)) {
            tNodePrecedesSetMap.put(curNode, new LinkedHashSet<EventType>());
            if (tPrecedingTypes == null) {
                tPrecedingTypes = tNodePrecedesSetMap.get(curNode);
            }
        }
        if (tPrecedingTypes != null) {
            tNodePrecedesSetMap.get(curNode).addAll(tPrecedingTypes);
        }

        while (true) {
            // If we reach a node that has nodes we haven't seen preceded before
            // then we want to include them in the tFollowingTypes.
            if (tNodePrecedesSetMap.containsKey(curNode)) {
                tNodePrecedesSetMap.get(curNode).addAll(tPrecedingTypes);
                tPrecedingTypes = tNodePrecedesSetMap.get(curNode);
            }

            // This guarantees that we only process curNode once we have
            // traversed all of its parents (while accumulating the preceding
            // types in the tNodePrecedesSetMap above).
            if (tNodeToNumParentsMap.get(curNode) > 1) {
                tNodeToNumParentsMap.put(curNode,
                        tNodeToNumParentsMap.get(curNode) - 1);
                if (!tNodePrecedesSetMap.containsKey(curNode)) {
                    tNodePrecedesSetMap.put(curNode,
                            new LinkedHashSet<EventType>());
                }
                tNodePrecedesSetMap.get(curNode).addAll(tPrecedingTypes);
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
            for (EventType a : tPrecedingTypes) {
                LinkedHashMap<EventType, Integer> precedingLabelCnts;
                if (!gPrecedesCnts.containsKey(a)) {
                    precedingLabelCnts = new LinkedHashMap<EventType, Integer>();
                    gPrecedesCnts.put(a, precedingLabelCnts);
                } else {
                    precedingLabelCnts = gPrecedesCnts.get(a);
                }
                if (!precedingLabelCnts.containsKey(b)) {
                    precedingLabelCnts.put(b, 1);
                } else {
                    precedingLabelCnts.put(b, precedingLabelCnts.get(b) + 1);
                }
            }
            tPrecedingTypes.add(b);

            // Update the global event counts.
            if (!gEventCnts.containsKey(b)) {
                gEventCnts.put(b, 1);
            } else {
                gEventCnts.put(b, gEventCnts.get(b) + 1);
            }

            // Nodes with multiple children are handled outside the loop.
            if (curNode.getTransitions().size() != 1) {
                break;
            }

            // Move on to the next node in the trace without recursion.
            curNode = curNode.getTransitions().get(0).getTarget();

            // We've hit a TERMINAL node, stop.
            if (curNode.getTransitions().size() == 0) {
                return;
            }
        }

        // Handle each of the node's child branches recursively.
        for (ITransition<EventNode> trans : curNode.getTransitions()) {
            EventNode childNode = trans.getTarget();
            // We do not create a new copy of preceding types for each child,
            // because each child already has its own -- maintained as part of
            // tNodePrecedesSetMap (built in preTraverseTrace()).

            // Only process children that are not TERMINAL nodes.
            if (childNode.getTransitions().size() > 0) {
                forwardTraverseTrace(childNode, tNodeToNumParentsMap,
                        tNodePrecedesSetMap, tPrecedingTypes, gPrecedesCnts,
                        gEventCnts);
            }
        }
        return;
    } // /forwardTraverseTrace
}
