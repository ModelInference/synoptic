package synoptic.invariants.miners;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import synoptic.invariants.AlwaysFollowedInvariant;
import synoptic.invariants.AlwaysPrecedesInvariant;
import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.NeverFollowedInvariant;
import synoptic.invariants.birelational.AFBiRelationInvariant;
import synoptic.invariants.birelational.APBiRelationInvariant;
import synoptic.invariants.birelational.NFBiRelationInvariant;
import synoptic.invariants.concurrency.AlwaysConcurrentInvariant;
import synoptic.invariants.concurrency.NeverConcurrentInvariant;
import synoptic.model.event.DistEventType;
import synoptic.model.event.Event;
import synoptic.model.event.EventType;
import synoptic.model.event.StringEventType;
import synoptic.util.InternalSynopticException;

/**
 * Contains useful methods that can be used by invariant miners that collect
 * event pair counts to generate invariants.
 */
abstract public class CountingInvariantMiner extends InvariantMiner {

    /**
     * Builds a set of local invariants (those that hold between events at the
     * same host/process) based on the following observations:
     * 
     * <pre>
     * a AFby b <=> #F(a->b) == #a
     * a AP b   <=> #P(a->b) == #b
     * a NFby b <=> #F(a->b) == 0
     * INITIAL AFby a <=> a \in AlwaysFollowsINITIALSet
     * 
     * Where:
     * #(x)      = gEventCnts[a]
     * #F(a->b)  = gFollowedByCnts[a][b]
     * #P(a->b)  = gPrecedesCnts[b][a]
     * </pre>
     * 
     * @param relation
     * @param gEventCnts
     * @param gFollowedByCnts
     * @param gPrecedesCnts
     * @param AlwaysFollowsINITIALSet
     * @return
     */
    protected Set<ITemporalInvariant> extractPathInvariantsFromWalkCounts(
            String relation, Map<EventType, Integer> gEventCnts,
            Map<EventType, Map<EventType, Integer>> gFollowedByCnts,
            Map<EventType, Map<EventType, Integer>> gPrecedesCnts,
            Map<EventType, Set<EventType>> gEventCoOccurrences,
            Set<EventType> AlwaysFollowsINITIALSet,
            boolean multipleRelations) {

        Set<ITemporalInvariant> invariants = new LinkedHashSet<ITemporalInvariant>();

        for (EventType e1 : gEventCnts.keySet()) {
            for (EventType e2 : gEventCnts.keySet()) {

                if (neverFollowedBy(gFollowedByCnts, e1, e2)) {
                    // Online filtering of subsumed invariants:
                    if (gEventCoOccurrences == null
                            || !alwaysConcurrentWith(gFollowedByCnts,
                                    gEventCoOccurrences, e1, e2)) {
                        if (multipleRelations) {
                            invariants.add(new NFBiRelationInvariant(e1, e2, 
                                    relation, Event.defTimeRelationStr));
                        } else {
                            invariants.add(new NeverFollowedInvariant(e1, e2, 
                                    relation));
                        }
                    }
                }

                if (alwaysFollowedBy(gEventCnts, gFollowedByCnts, e1, e2)) {
                    if (multipleRelations) {
                        invariants.add(new AFBiRelationInvariant(e1, e2, 
                                relation, Event.defTimeRelationStr));
                    } else {
                        invariants.add(new AlwaysFollowedInvariant(e1, e2, 
                                relation));
                    }
                }

                if (alwaysPrecedes(gEventCnts, gPrecedesCnts, e1, e2)) {
                    if (multipleRelations) {
                        invariants.add(new APBiRelationInvariant(e1, e2, 
                                relation, Event.defTimeRelationStr));
                    } else {
                        invariants.add(new AlwaysPrecedesInvariant(e1, e2, 
                                relation));
                    }
                }
            }
        }

        // Determine all the INITIAL AFby x invariants to represent
        // "eventually x"
        for (EventType label : AlwaysFollowsINITIALSet) {
            if (multipleRelations) {
                invariants.add(new AFBiRelationInvariant(StringEventType
                        .newInitialStringEventType(), label, relation));
            } else {
                invariants.add(new AlwaysFollowedInvariant(StringEventType
                        .newInitialStringEventType(), label, relation));
            }
        }

        return invariants;
    }

    protected boolean alwaysPrecedes(Map<EventType, Integer> gEventCnts,
            Map<EventType, Map<EventType, Integer>> gPrecedesCnts,
            EventType e1, EventType e2) {
        if (gPrecedesCnts.get(e1).get(e2).equals(gEventCnts.get(e2))) {
            // #_P(label1->label2) == #label2 therefore label1
            // AP label2
            return true;
        }
        return false;
    }

    protected boolean neverFollowedBy(
            Map<EventType, Map<EventType, Integer>> gFollowedByCnts,
            EventType e1, EventType e2) {
        if (gFollowedByCnts.get(e1).get(e2) == 0) {
            // label1 was never followed by label2, therefore label1
            // NFby label2 (i.e. #_F(label1->label2) == 0)
            return true;
        }
        return false;
    }

    protected boolean alwaysFollowedBy(Map<EventType, Integer> gEventCnts,
            Map<EventType, Map<EventType, Integer>> gFollowedByCnts,
            EventType e1, EventType e2) {
        if (gFollowedByCnts.get(e1).get(e2).equals(gEventCnts.get(e1))) {
            // #_F(label1->label2) == #label1 therefore label1
            // AFby label2
            return true;
        }
        return false;
    }

    protected boolean alwaysConcurrentWith(
            Map<EventType, Map<EventType, Integer>> gFollowedByCnts,
            Map<EventType, Set<EventType>> gEventCoOccurrences, EventType e1,
            EventType e2) {
        int e1_fby_e2 = gFollowedByCnts.get(e1).get(e2);
        int e2_fby_e1 = gFollowedByCnts.get(e2).get(e1);

        if (e1_fby_e2 == 0 && e2_fby_e1 == 0) {
            // That is, e1 NFby e2 && e2 NFby e1 means that e1 and e2
            // are concurrent if they _ever_ co-appeared in the same
            // trace.
            if ((gEventCoOccurrences.containsKey(e1) && gEventCoOccurrences
                    .get(e1).contains(e2))
                    || (gEventCoOccurrences.containsKey(e2) && gEventCoOccurrences
                            .get(e2).contains(e1))) {
                return true;
            }
        }
        return false;
    }

    protected boolean neverConcurrentWith(Map<EventType, Integer> gEventCnts,
            Map<EventType, Map<EventType, Integer>> gPrecedesCnts,
            Map<EventType, Map<EventType, Integer>> gFollowedByCnts,
            Map<EventType, Set<EventType>> gEventCoOccurrences,
            Map<EventType, Map<EventType, Integer>> gEventTypesOrderedBalances,
            EventType e1, EventType e2) {
        int e1_fby_e2 = gFollowedByCnts.get(e1).get(e2);
        int e2_fby_e1 = gFollowedByCnts.get(e2).get(e1);

        if (e1_fby_e2 != 0 || e2_fby_e1 != 0) {
            // e1 was ordered with e2 or e2 was ordered with e1 at
            // least once. Now we need to check whether they were _always_
            // ordered w.r.t each other whenever they co-appeared in the
            // same trace.

            if (gEventTypesOrderedBalances.get(e1).get(e2) == 0) {
                // Assert: the invariant is commutative, so should
                // hold true for (e2,e1):
                assert (gEventTypesOrderedBalances.get(e2).get(e1) == 0);
                return true;
            }
        }
        return false;
    }

    /**
     * Extracts concurrency/distributed invariants -- AlwaysConcurrentWith and
     * NeverConcurrentWith. These invariants are only useful for events at
     * different hosts (i.e., for x,y (totally ordered) events at the same host
     * AlwaysConcurrentWith(x,y) is trivially false, and
     * NeverConcurrentWith(x,y) is trivially true).
     * 
     * @param mineNeverConcurrentWith
     * @param relation
     * @param gEventCnts
     * @param gFollowedByCnts
     * @param gPrecedesCnts
     * @param gEventCoOccurrences
     * @return
     */
    protected Set<ITemporalInvariant> extractConcurrencyInvariantsFromWalkCounts(
            boolean mineNeverConcurrentWith, String relation,
            Map<EventType, Integer> gEventCnts,
            Map<EventType, Map<EventType, Integer>> gPrecedesCnts,
            Map<EventType, Map<EventType, Integer>> gFollowedByCnts,
            Map<EventType, Set<EventType>> gEventCoOccurrences,
            Map<EventType, Map<EventType, Integer>> gEventTypesOrderedBalances) {

        Set<ITemporalInvariant> invariants = new LinkedHashSet<ITemporalInvariant>();

        Set<EventType> toVisitETypes = new LinkedHashSet<EventType>();
        toVisitETypes.addAll(gEventCnts.keySet());

        // logger.info("gFollowedByCnts: " + gFollowedByCnts.toString());
        // logger.info("gEventCoOccurrences: " +
        // gEventCoOccurrences.toString());
        // logger.info("gEventTypesOrderedBalances is "
        // + gEventTypesOrderedBalances.toString());

        for (EventType e1 : gEventCnts.keySet()) {
            // We don't consider (e1, e1) as these would only generate local
            // invariants, and we do not consider (e1,e2) if we've already
            // considered (e2,e1) as distributed invariants are symmetric.
            toVisitETypes.remove(e1);

            for (EventType e2 : toVisitETypes) {
                if (!(e1 instanceof DistEventType)
                        || !(e2 instanceof DistEventType)) {
                    throw new InternalSynopticException(
                            "Cannot compute concurrency invariants on non-distributed event types.");
                }

                if (((DistEventType) e1).getProcessName().equals(
                        ((DistEventType) e2).getProcessName())) {
                    // See comment at top of function about local versions of
                    // concurrency invariants -- we ignore them.
                    continue;
                }

                if (alwaysConcurrentWith(gFollowedByCnts, gEventCoOccurrences,
                        e1, e2)) {
                    invariants.add(new AlwaysConcurrentInvariant(
                            (DistEventType) e1, (DistEventType) e2, relation));

                } else if (mineNeverConcurrentWith) {
                    if (neverConcurrentWith(gEventCnts, gPrecedesCnts,
                            gFollowedByCnts, gEventCoOccurrences,
                            gEventTypesOrderedBalances, e1, e2)) {

                        // Online filtering of subsumed invariants:
                        // Only include "e1 NeverConcurrent e2" if not
                        // "e1/e2 AlwaysPrecedes e2/e1" and not
                        // "e1/e2 AlwaysFollowed e2/e1"
                        if (!alwaysPrecedes(gEventCnts, gPrecedesCnts, e1, e2)
                                && !alwaysPrecedes(gEventCnts, gPrecedesCnts,
                                        e2, e1)
                                && !alwaysFollowedBy(gEventCnts,
                                        gFollowedByCnts, e1, e2)
                                && !alwaysFollowedBy(gEventCnts,
                                        gFollowedByCnts, e2, e1)) {
                            invariants.add(new NeverConcurrentInvariant(
                                    (DistEventType) e1, (DistEventType) e2,
                                    relation));
                        }
                    }
                }
            }
        }
        return invariants;
    }
}
