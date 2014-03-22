package synoptic.invariants.miners;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import synoptic.invariants.TemporalInvariantSet;
import synoptic.model.ChainsTraceGraph;
import synoptic.model.TransitiveRelationPath;
import synoptic.model.Trace;
import synoptic.model.event.Event;
import synoptic.model.event.EventType;
import synoptic.model.interfaces.IRelationPath;

/**
 * Implements a temporal invariant mining algorithm whose running time is linear
 * in the number events in the log, and whose space usage is quadratic in the
 * number of event types and running time also depends on partition sizes. A
 * more detailed complexity break-down is given below. <br/>
 * <br/>
 * This algorithm has lower space usage than the transitive-closure-based
 * algorithms.
 * 
 */
public class ChainWalkingTOInvMiner extends CountingInvariantMiner implements
        ITOInvariantMiner {
    
    public TemporalInvariantSet computeInvariants(ChainsTraceGraph g, boolean multipleRelations) {
        TemporalInvariantSet result = new TemporalInvariantSet();
        for (String r : g.getRelations()) {
            TemporalInvariantSet tmp = computeInvariants(g, r, multipleRelations);
            result.add(tmp);
        }
        return result;
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
     * number of times a b instance in a partition was preceded transitively by
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
     *            a chain trace graph of nodes of type LogEvent
     * @param relation
     *            the relation for which to mine invariants
     * @return the set of temporal invariants that g satisfies
     */
    public TemporalInvariantSet computeInvariants(ChainsTraceGraph g,
            String relation, boolean multipleRelations) {

        // TODO: we can set the initial capacity of the following HashMaps more
        // optimally, e.g. (N / 0.75) + 1 where N is the total number of event
        // types. See:
        // http://stackoverflow.com/questions/434989/hashmap-intialization-parameters-load-initialcapacity

        // Stores generated RelationPaths
        Set<IRelationPath> relationPaths = new HashSet<IRelationPath>();

        // Tracks event counts globally -- across all traces.
        Map<EventType, Integer> gEventCnts = new LinkedHashMap<EventType, Integer>();

        /*
         * Build the set of all event types in the RelationPaths. We will use
         * this set to pre-seed the various maps below. Also, since we're
         * iterating over all nodes, we might as well count up the total counts
         * of instances for each event type.
         */
        Set<EventType> eTypes = new LinkedHashSet<EventType>();
        for (Trace trace : g.getTraces()) {
            
            if (multipleRelations && !relation.equals(Event.defTimeRelationStr)) {
                IRelationPath relationPath = trace.getBiRelationalPath(relation, Event.defTimeRelationStr);
                relationPaths.add(relationPath);
            } else {
                Set<IRelationPath> subgraphs = trace.getSingleRelationPaths(relation);
                if (relation.equals(Event.defTimeRelationStr) && subgraphs.size() != 1) {
                    throw new IllegalStateException("Multiple relation subraphs for ordering relation graph");
                }
                relationPaths.addAll(subgraphs);
            }

            
        }
        
        for (IRelationPath relationPath : relationPaths) {
            eTypes.addAll(relationPath.getSeen());
            Map<EventType, Integer> relationPathEventCounts = relationPath
                    .getEventCounts();
            for (EventType eventType : relationPathEventCounts.keySet()) {
                int count = relationPathEventCounts.get(eventType);

                if (gEventCnts.containsKey(eventType)) {
                    count += gEventCnts.get(eventType);
                }

                gEventCnts.put(eventType, count);
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

        // Tracks which events were observed across all RelationPaths.
        Set<EventType> AlwaysFollowsINITIALSet = null;

        /*
         * Iterates over each RelationPath in the graph and aggregates the
         * individual Occurrences, Follows, and Precedes counts.
         */
        for (IRelationPath relationPath : relationPaths) {

            /*
             * Adds the Precedes count from the RelationPath into the graph
             * global count.
             */

            Map<EventType, Map<EventType, Integer>> relationPathPrecedesCounts = relationPath
                    .getPrecedesCounts();
            addCounts(relationPathPrecedesCounts, gPrecedesCnts);

            /*
             * Adds the FollowedBy count from the RelationPath into the graph
             * global count.
             */

            Map<EventType, Map<EventType, Integer>> relationPathFollowedByCounts = relationPath
                    .getFollowedByCounts();
            addCounts(relationPathFollowedByCounts, gFollowedByCnts);

            // Update the AlwaysFollowsINITIALSet set of events by
            // intersecting it with all events seen in this RelationPath.
            Set<EventType> relationPathSeen = relationPath.getSeen();
            if (AlwaysFollowsINITIALSet == null) {
                // This is the first RelationPath we are processing.
                AlwaysFollowsINITIALSet = new LinkedHashSet<EventType>(
                        relationPathSeen);
            } else {
                AlwaysFollowsINITIALSet.retainAll(relationPathSeen);
            }

            // At this point, we've completed all counts computation for the
            // current RelationPath.
        }

        return new TemporalInvariantSet(extractPathInvariantsFromWalkCounts(
                relation, gEventCnts, gFollowedByCnts, gPrecedesCnts, null,
                AlwaysFollowsINITIALSet, multipleRelations));
    }

    /**
     * Adds the values from src into dst where the input maps have the form
     * XCounts[a][b] = count
     * 
     * @param src
     * @param dst
     */
    private static void addCounts(Map<EventType, Map<EventType, Integer>> src,
            Map<EventType, Map<EventType, Integer>> dst) {

        for (EventType a : src.keySet()) {
            Map<EventType, Integer> srcBValues = src.get(a);
            Map<EventType, Integer> dstBValues = dst.get(a);
            for (EventType b : srcBValues.keySet()) {
                int count = srcBValues.get(b);
                if (dstBValues.containsKey(b)) {
                    count += dstBValues.get(b);
                }
                dstBValues.put(b, count);
            }
        }
    }
}
