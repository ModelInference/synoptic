package synoptic.invariants.miners;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import synoptic.invariants.TemporalInvariantSet;
import synoptic.main.TraceParser;
import synoptic.model.EventNode;
import synoptic.model.EventType;
import synoptic.model.interfaces.IGraph;
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
public class ChainWalkingTOInvMiner extends InvariantMiner {

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
    @Override
    public TemporalInvariantSet computeInvariants(IGraph<EventNode> g) {
        String relation = TraceParser.defaultRelation;

        // TODO: we can set the initial capacity of the following HashMaps more
        // optimally, e.g. (N / 0.75) + 1 where N is the total number of event
        // types. See:
        // http://stackoverflow.com/questions/434989/hashmap-intialization-parameters-load-initialcapacity

        // Tracks event counts globally -- across all traces.
        Map<EventType, Integer> gEventCnts = new LinkedHashMap<EventType, Integer>();
        // Tracks followed-by counts.
        Map<EventType, Map<EventType, Integer>> gFollowedByCnts = new LinkedHashMap<EventType, Map<EventType, Integer>>();
        // Tracks precedence counts.
        Map<EventType, Map<EventType, Integer>> gPrecedesCnts = new LinkedHashMap<EventType, Map<EventType, Integer>>();
        // Tracks which events were observed across all traces.
        Set<EventType> AlwaysFollowsINITIALSet = null;

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
                    Map<EventType, Integer> precedingLabelCnts;
                    if (!gPrecedesCnts.containsKey(a)) {
                        precedingLabelCnts = new LinkedHashMap<EventType, Integer>();
                        gPrecedesCnts.put(a, precedingLabelCnts);
                    } else {
                        precedingLabelCnts = gPrecedesCnts.get(a);
                    }
                    if (!precedingLabelCnts.containsKey(b)) {
                        precedingLabelCnts.put(b, 1);
                    } else {
                        precedingLabelCnts
                                .put(b, precedingLabelCnts.get(b) + 1);
                    }
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

                // Update the global event counts.
                if (!gEventCnts.containsKey(b)) {
                    gEventCnts.put(b, 1);
                } else {
                    gEventCnts.put(b, gEventCnts.get(b) + 1);
                }

                // Move on to the next node in the trace.
                curNode = curNode.getTransitions().get(0).getTarget();
            }

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
                relation, gEventCnts, gFollowedByCnts, gPrecedesCnts,
                AlwaysFollowsINITIALSet));
    }
}
