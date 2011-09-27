package synoptic.invariants.miners;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import synoptic.invariants.TemporalInvariantSet;
import synoptic.main.TraceParser;
import synoptic.model.ChainsTraceGraph;
import synoptic.model.EventNode;
import synoptic.model.EventType;
import synoptic.model.interfaces.ITransition;
import synoptic.util.InternalSynopticException;

/**
 * Implements a temporal invariant mining algorithm whose running time is linear
 * in the number events in the log, and whose space usage is quadratic in the
 * number of event types and running time also depends on partition sizes. A
 * more detailed complexity break-down is given below. <br/>
 * <br/>
 * This algorithm has lower space usage than the transitive-closure-based
 * algorithms.
 * 
 * @author ivan
 */
public class ChainWalkingTOInvMiner extends CountingInvariantMiner implements
        TOInvariantMiner {

    public TemporalInvariantSet computeInvariants(ChainsTraceGraph g) {
        return computeInvariants(g, TraceParser.defaultRelation);
    }

    /**
     * Compute invariants of a graph g by mining invariants directly from the
     * partitions associated with the graph. /**
     * <p>
     * Mines AFby, AP, NFby invariants from a list of partitions -- each of
     * which is a list of LogEvents. It does this by leveraging the following
     * three observations:
     * </p>
     * <p>
     * (1) To check a AFby b it is sufficient to count the number of times a
     * transitive a->b was seen across all partitions and check if it equals the
     * number of a's seen across all partitions. If the two are equal then AFby
     * is true.
     * </p>
     * <p>
     * (2) To check a NFby b it is sufficient to count the number of times a
     * transitive a->b was seen across all partitions and declare a NFby b true
     * iff the count is 0.
     * </p>
     * <p>
     * (3) To check a AP b it is sufficient to count across all partitions the
     * number of times an b instance in a partition was preceded transitively by
     * an a in the same partition, and declare a AP b true iff this count equals
     * the number of b's seen across all partitions.
     * </p>
     * </p>
     * <p>
     * NOTE1: This code only works for events that are totally ordered in each
     * partition -- they have to be sorted according to some relation
     * beforehand!
     * </p>
     * <p>
     * NOTE2: This code also mines invariants of the form "INITIAL AFby x", i.e.
     * "eventually x" invariants.
     * </p>
     * 
     * @param g
     *            the graph of nodes of type LogEvent
     * @return the set of temporal invariants that g satisfies
     */
    public TemporalInvariantSet computeInvariants(ChainsTraceGraph g,
            String relation) {

        // TODO: we can set the initial capacity of the following HashMaps more
        // optimally, e.g. (N / 0.75) + 1 where N is the total number of event
        // types. See:
        // http://stackoverflow.com/questions/434989/hashmap-intialization-parameters-load-initialcapacity

        // Tracks event counts globally -- across all traces.
        Map<EventType, Integer> gEventCnts = new LinkedHashMap<EventType, Integer>();

        // Build the set of all event types in the graph. We will use this set
        // to pre-seed the various maps below. Also, since we're iterating over
        // all nodes, we might as well count up the total counts of instances
        // for each event type.
        Set<EventType> eTypes = new LinkedHashSet<EventType>();
        for (EventNode node : g.getNodes()) {
            EventType e = node.getEType();
            // TODO: we currently only ignore initial nodes, be we also need to
            // ignore terminal nodes. However, this would require a change to
            // traversal below (instead of starting reverse traversal at
            // terminal, we would need to start one node higher).

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

        // Tracks followed-by counts.
        Map<EventType, Map<EventType, Integer>> gFollowedByCnts = new LinkedHashMap<EventType, Map<EventType, Integer>>();
        // Tracks precedence counts.
        Map<EventType, Map<EventType, Integer>> gPrecedesCnts = new LinkedHashMap<EventType, Map<EventType, Integer>>();

        // Initialize the event-type contents of the maps that persist
        // across traces (global counts maps).
        for (EventType e : eTypes) {
            Map<EventType, Integer> mapF = new LinkedHashMap<EventType, Integer>();
            Map<EventType, Integer> mapP = new LinkedHashMap<EventType, Integer>();
            gFollowedByCnts.put(e, mapF);
            gPrecedesCnts.put(e, mapP);
            for (EventType e2 : eTypes) {
                mapF.put(e2, 0);
                mapP.put(e2, 0);
            }
        }

        // Tracks which events were observed across all traces.
        Set<EventType> AlwaysFollowsINITIALSet = null;
        EventNode initNode = g.getDummyInitialNode(relation);

        // The set of nodes seen prior to some point in the trace.
        Set<EventType> tSeen = new LinkedHashSet<EventType>();
        // Maintains the current event count in the trace.
        Map<EventType, Integer> tEventCnts = new LinkedHashMap<EventType, Integer>();
        // Maintains the current FollowedBy count for the trace.
        // tFollowedByCnts[a][b] = cnt iff the number of a's that appeared
        // before this b is cnt.
        Map<EventType, Map<EventType, Integer>> tFollowedByCnts = new LinkedHashMap<EventType, Map<EventType, Integer>>();

        // Iterate through all the traces -- each transition from the INITIAL
        // node connects\holds a single trace.
        for (ITransition<EventNode> initTrans : initNode.getTransitions()) {
            EventNode curNode = initTrans.getTarget();

            while (curNode.getTransitions().size() != 0) {
                // NOTE: this invariant miner only works for totally ordered
                // traces, so each node must have no more than 1 out-going
                // transition.
                if (curNode.getTransitions().size() != 1) {
                    throw new InternalSynopticException(
                            "SpecializedInvariantMiner does not work on partially ordered traces.");
                }

                // The current event is 'b', and all prior events are 'a' --
                // this notation indicates that an 'a' always occur prior to a
                // 'b' in the trace.
                EventType b = curNode.getEType();

                // Update the global precedes counts based on the a events that
                // preceded the current b event in this trace.
                for (EventType a : tSeen) {
                    gPrecedesCnts.get(a)
                            .put(b, gPrecedesCnts.get(a).get(b) + 1);
                }

                // Update the followed by counts for this trace: the number of a
                // FollowedBy b at this point in this trace is exactly the
                // number of a's that we've seen so far.
                for (EventType a : tSeen) {
                    Map<EventType, Integer> tmp;
                    if (!tFollowedByCnts.containsKey(a)) {
                        tmp = new LinkedHashMap<EventType, Integer>();
                        tFollowedByCnts.put(a, tmp);
                    } else {
                        tmp = tFollowedByCnts.get(a);
                    }

                    tmp.put(b, tEventCnts.get(a));
                }
                tSeen.add(b);

                // Update the trace event counts.
                if (!tEventCnts.containsKey(b)) {
                    tEventCnts.put(b, 1);
                } else {
                    tEventCnts.put(b, tEventCnts.get(b) + 1);
                }

                // Move on to the next node in the trace.
                curNode = curNode.getTransitions().get(0).getTarget();
            }

            // Update the global event followed by counts based on followed by
            // counts collected in this trace. We merge the counts with
            // addition.
            for (EventType a : tFollowedByCnts.keySet()) {
                for (EventType b : tFollowedByCnts.get(a).keySet()) {
                    gFollowedByCnts.get(a).put(
                            b,
                            gFollowedByCnts.get(a).get(b)
                                    + tFollowedByCnts.get(a).get(b));
                }
            }

            // Update the AlwaysFollowsINITIALSet set of events by
            // intersecting it with all events seen in this partition.
            if (AlwaysFollowsINITIALSet == null) {
                // This is the first trace we've processed.
                AlwaysFollowsINITIALSet = new LinkedHashSet<EventType>(tSeen);
            } else {
                AlwaysFollowsINITIALSet.retainAll(tSeen);
            }

            // Clear all the per-trace structures to prepare for the next trace.
            tSeen.clear();
            tEventCnts.clear();
            tFollowedByCnts.clear();

            // At this point, we've completed all counts computation for the
            // current trace.
        }

        return new TemporalInvariantSet(extractPathInvariantsFromWalkCounts(
                relation, gEventCnts, gFollowedByCnts, gPrecedesCnts, null,
                AlwaysFollowsINITIALSet));
    }
}
