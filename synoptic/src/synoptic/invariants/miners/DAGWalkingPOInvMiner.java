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
 * by walking the corresponding DAG trace structure. This algorithm is a
 * generalization of the ChainWalkingTOInvMiner.
 */
public class DAGWalkingPOInvMiner extends InvariantMiner {

    @Override
    public TemporalInvariantSet computeInvariants(IGraph<EventNode> g) {
        String relation = TraceParser.defaultRelation;

        // TODO: we can set the initial capacity of the following HashMaps more
        // optimally, e.g. (N / 0.75) + 1 where N is the total number of event
        // types. See:
        // http://stackoverflow.com/questions/434989/hashmap-intialization-parameters-load-initialcapacity

        // Tracks event counts globally -- across all traces.
        LinkedHashMap<EventType, Integer> gEventCnts = new LinkedHashMap<EventType, Integer>();
        // Tracks followed-by counts.
        LinkedHashMap<EventType, LinkedHashMap<EventType, Integer>> gFollowedByCnts = new LinkedHashMap<EventType, LinkedHashMap<EventType, Integer>>();
        // Tracks precedence counts.
        LinkedHashMap<EventType, LinkedHashMap<EventType, Integer>> gPrecedesCnts = new LinkedHashMap<EventType, LinkedHashMap<EventType, Integer>>();
        // Tracks which events were observed across all traces.
        LinkedHashSet<EventType> AlwaysFollowsINITIALSet = null;

        if (g.getInitialNodes().isEmpty() || g.getInitialNodes().size() != 1) {
            throw new InternalSynopticException(
                    "Cannot compute invariants over a graph that doesn't have exactly one INITIAL node.");
        }

        EventNode initNode = g.getInitialNodes().iterator().next();
        if (!initNode.getEType().isInitialEventType()) {
            throw new InternalSynopticException(
                    "Cannot compute invariants over a graph that doesn't have exactly one INITIAL node.");
        }

        // The set of nodes seen prior to some point in the trace.
        LinkedHashSet<EventType> tSeenETypes = new LinkedHashSet<EventType>();
        // Maintains the current event count in the trace.
        LinkedHashMap<EventType, Integer> tEventCnts = new LinkedHashMap<EventType, Integer>();
        // Maintains the current FollowedBy count for the trace.
        // tFollowedByCnts[a][b] = cnt iff the number of a's that appeared
        // before this b is cnt.
        LinkedHashMap<EventType, LinkedHashMap<EventType, Integer>> tFollowedByCnts = new LinkedHashMap<EventType, LinkedHashMap<EventType, Integer>>();
        // Maintains the set of trace nodes that have already been
        // processed\seen in the traversal.
        LinkedHashSet<EventNode> tSeenNodes = new LinkedHashSet<EventNode>();

        // Maps a node in the trace DAG to the number of parents this node has
        // in the DAG. This is computed during a pre-traversal of the DAG.
        LinkedHashMap<EventNode, Integer> tNodeToNumParentsMap = new LinkedHashMap<EventNode, Integer>();

        // Maps a node in the trace to the number of children that are below it.
        // This is computed during pre-traversal.
        LinkedHashMap<EventNode, Integer> tNodeToNumChildrenMap = new LinkedHashMap<EventNode, Integer>();

        // Maps a node to a set of event types that precede it.
        LinkedHashMap<EventNode, LinkedHashSet<EventType>> tNodePrecedesSetMap = new LinkedHashMap<EventNode, LinkedHashSet<EventType>>();

        // Maps a node to a set of event types that follow it it.
        LinkedHashMap<EventNode, LinkedHashSet<EventType>> tNodeFollowsSetMap = new LinkedHashMap<EventNode, LinkedHashSet<EventType>>();

        // Keeps track of the event types that precede our current point in the
        // traversal.
        LinkedHashSet<EventType> tPrecedingTypes = new LinkedHashSet<EventType>();

        // Keeps track of the event types that follow our current point in the
        // traversal.
        LinkedHashSet<EventType> tFollowingTypes = new LinkedHashSet<EventType>();

        // Maps a node to a set of nodes that immediately precede this node (the
        // node's parents). Build during pre-traversal, and used for mining
        // FollowedBy counts.
        LinkedHashMap<EventNode, LinkedHashSet<EventNode>> tNodeParentsMap = new LinkedHashMap<EventNode, LinkedHashSet<EventNode>>();

        // Iterate through all the traces -- each transition from the INITIAL
        // node connects\holds a single trace.
        LinkedHashSet<EventNode> emptyHashSet = new LinkedHashSet<EventNode>();
        for (ITransition<EventNode> initTrans : initNode.getTransitions()) {
            EventNode curNode = initTrans.getTarget();

            tNodeParentsMap.put(curNode, emptyHashSet);
            EventNode termNode = preTraverseTrace(curNode,
                    tNodeToNumParentsMap, tNodeToNumChildrenMap,
                    tNodePrecedesSetMap, tNodeFollowsSetMap, tNodeParentsMap);

            assert (termNode != null);

            // nodeToPrecedesMap

            // ////////////////////////
            // AP counts collection: Forward traverse the trace that is rooted
            // at curNode.
            traverseTrace(curNode, tNodeToNumParentsMap, tNodePrecedesSetMap,
                    tPrecedingTypes, gPrecedesCnts, gEventCnts);

            // ////////////////////////
            // AFby\NFby counts collection: Reverse traverse the trace that is
            // rooted at termNode, and is implicitly connected via
            // tNodeParentsMap.
            reverseTraverseTrace(termNode, tNodeToNumChildrenMap,
                    tNodeParentsMap, tNodeFollowsSetMap, tFollowingTypes,
                    tFollowedByCnts);

            // ////////////////////////
            // Update the global event followed by counts based on followed by
            // counts collected in this trace. We merge the counts with
            // addition.
            for (EventType a : tFollowedByCnts.keySet()) {
                if (!gFollowedByCnts.containsKey(a)) {
                    gFollowedByCnts.put(a, tFollowedByCnts.get(a));
                } else {
                    for (EventType b : tFollowedByCnts.get(a).keySet()) {
                        if (!gFollowedByCnts.get(a).containsKey(b)) {
                            gFollowedByCnts.get(a).put(b,
                                    tFollowedByCnts.get(a).get(b));
                        } else {
                            gFollowedByCnts.get(a).put(
                                    b,
                                    gFollowedByCnts.get(a).get(b)
                                            + tFollowedByCnts.get(a).get(b));
                        }
                    }
                }
            }

            // Update the AlwaysFollowsINITIALSet set of events by
            // intersecting it with all events seen in this partition.
            if (AlwaysFollowsINITIALSet == null) {
                // This is the first trace we've processed.
                AlwaysFollowsINITIALSet = new LinkedHashSet<EventType>(
                        tSeenETypes);
            } else {
                AlwaysFollowsINITIALSet.retainAll(tSeenETypes);
            }

            // Clear all the per-trace structures to prepare for the next trace.
            tNodeParentsMap.clear();
            tNodeToNumParentsMap.clear();
            tNodePrecedesSetMap.clear();
            tPrecedingTypes.clear();
            tSeenNodes.clear();
            tSeenETypes.clear();
            tEventCnts.clear();
            tFollowedByCnts.clear();

            // At this point, we've completed all counts computation for the
            // current trace.
        }

        return extractInvariantsFromWalkCounts(relation, gEventCnts,
                gFollowedByCnts, gPrecedesCnts, AlwaysFollowsINITIALSet);
    }

    public EventNode preTraverseTrace(
            EventNode curNode,
            LinkedHashMap<EventNode, Integer> tNodeToNumParentsMap,
            LinkedHashMap<EventNode, Integer> tNodeToNumChildrenMap,
            LinkedHashMap<EventNode, LinkedHashSet<EventType>> tNodePrecedesSetMap,
            LinkedHashMap<EventNode, LinkedHashSet<EventType>> tNodeFollowsSetMap,
            LinkedHashMap<EventNode, LinkedHashSet<EventNode>> tNodeParentsMap) {

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
                // A node with multiple parents must have its own precedes set.
                if (!tNodePrecedesSetMap.containsKey(curNode)) {
                    tNodePrecedesSetMap.put(curNode,
                            new LinkedHashSet<EventType>());
                }
                // Terminate current traversal, because we've already traversed
                // downward from this point.
                return null;
            }

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

            // Move on to the next node in the trace without recursion.
            curNode = childNode;

            // We've hit a TERMINAL node, stop.
            if (curNode.getTransitions().size() == 0) {
                return curNode;
            }
        }

        // Handle each of the node's child branches recursively.
        EventNode termNode = null;
        for (ITransition<EventNode> trans : curNode.getTransitions()) {
            childNode = trans.getTarget();

            // Each child gets its own precedes set.
            if (!tNodePrecedesSetMap.containsKey(childNode)) {
                tNodePrecedesSetMap.put(childNode,
                        new LinkedHashSet<EventType>());
            }

            // Build up the parents map for all the children.
            if (!tNodeParentsMap.containsKey(childNode)) {
                parentNodes = new LinkedHashSet<EventNode>();
                tNodeParentsMap.put(childNode, parentNodes);
            } else {
                parentNodes = tNodeParentsMap.get(childNode);
            }
            parentNodes.add(curNode);

            EventNode ret = preTraverseTrace(childNode, tNodeToNumParentsMap,
                    tNodeToNumChildrenMap, tNodePrecedesSetMap,
                    tNodeFollowsSetMap, tNodeParentsMap);
            if (ret != null) {
                termNode = ret;
            }
        }
        return termNode;

    }

    /**
     * Traverse the trace that is rooted at curNode, collecting event
     * followed-by count statistics in a depth-first manner.
     **/
    public void reverseTraverseTrace(
            EventNode curNode,
            LinkedHashMap<EventNode, Integer> tNodeToNumChildrenMap,
            LinkedHashMap<EventNode, LinkedHashSet<EventNode>> tNodeParentsMap,
            LinkedHashMap<EventNode, LinkedHashSet<EventType>> tNodeFollowsSetMap,
            LinkedHashSet<EventType> tFollowingTypes,
            LinkedHashMap<EventType, LinkedHashMap<EventType, Integer>> tFollowedByCnts) {

        if (!tNodeFollowsSetMap.containsKey(curNode)) {
            tNodeFollowsSetMap.put(curNode, new LinkedHashSet<EventType>());
        }
        tNodeFollowsSetMap.get(curNode).addAll(tFollowingTypes);
        tFollowingTypes = tNodeFollowsSetMap.get(curNode);

        while (true) {
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

            // The current event is 'a', and all following events are 'b' --
            // this notation indicates that an 'a' always occurs prior to a
            // 'b' in the trace.
            EventType a = curNode.getEType();

            // Update the global precedes counts based on the a events that
            // preceded the current b event in this trace.
            for (EventType b : tFollowingTypes) {
                LinkedHashMap<EventType, Integer> precedingLabelCnts;
                if (!tFollowedByCnts.containsKey(a)) {
                    precedingLabelCnts = new LinkedHashMap<EventType, Integer>();
                    tFollowedByCnts.put(a, precedingLabelCnts);
                } else {
                    precedingLabelCnts = tFollowedByCnts.get(a);
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

        // Handle each of the node's child branches recursively.
        Iterator<EventNode> iter = tNodeParentsMap.get(curNode).iterator();

        while (iter.hasNext()) {
            EventNode parentNode = iter.next();
            // We do not create a new copy of preceding types for each parent,
            // because each parent already has its own -- maintained as part of
            // tNodeFollowsSetMap (built in preTraverseTrace()).
            reverseTraverseTrace(parentNode, tNodeToNumChildrenMap,
                    tNodeParentsMap, tNodeFollowsSetMap, tFollowingTypes,
                    tFollowedByCnts);
        }
        return;
    } // /reverseTraverseTrace

    /**
     * Traverse the trace that is rooted at curNode, collecting event precedence
     * count statistics in a depth-first manner.
     **/
    public void traverseTrace(
            EventNode curNode,
            LinkedHashMap<EventNode, Integer> tNodeToNumParentsMap,
            LinkedHashMap<EventNode, LinkedHashSet<EventType>> tNodePrecedesSetMap,
            LinkedHashSet<EventType> tPrecedingTypes,
            LinkedHashMap<EventType, LinkedHashMap<EventType, Integer>> gPrecedesCnts,
            LinkedHashMap<EventType, Integer> gEventCnts) {

        tNodePrecedesSetMap.get(curNode).addAll(tPrecedingTypes);
        tPrecedingTypes = tNodePrecedesSetMap.get(curNode);

        while (true) {
            // This guarantees that we only process curNode once we have
            // traversed all of its parents (while accumulating the preceding
            // types in the tNodePrecedesSetMap above).
            if (tNodeToNumParentsMap.get(curNode) > 1) {
                tNodeToNumParentsMap.put(curNode,
                        tNodeToNumParentsMap.get(curNode) - 1);
                tNodePrecedesSetMap.get(curNode).addAll(tPrecedingTypes);
                return;
            }

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
            traverseTrace(childNode, tNodeToNumParentsMap, tNodePrecedesSetMap,
                    tPrecedingTypes, gPrecedesCnts, gEventCnts);
        }
        return;
    } // /traverseTrace
}
