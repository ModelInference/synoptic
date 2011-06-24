package synoptic.invariants.miners;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;

import synoptic.invariants.AlwaysConcurrentInvariant;
import synoptic.invariants.AlwaysFollowedInvariant;
import synoptic.invariants.AlwaysPrecedesInvariant;
import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.NeverConcurrentInvariant;
import synoptic.invariants.NeverFollowedInvariant;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.model.DistEventType;
import synoptic.model.EventNode;
import synoptic.model.EventType;
import synoptic.model.StringEventType;
import synoptic.model.interfaces.IGraph;
import synoptic.model.interfaces.ITransition;
import synoptic.util.InternalSynopticException;

/**
 * Base class for all invariant miners.
 */
public abstract class InvariantMiner {
    protected static Logger logger = Logger.getLogger("TemporalInvSet Logger");

    /**
     * Computes and returns the temporal invariants that hold for the graph g.
     * 
     * @param g
     *            input graph over which to mine invariants
     * @return
     */
    public TemporalInvariantSet computeInvariants(IGraph<EventNode> g) {
        throw new InternalSynopticException(
                "computeInvariants must be overridden in a derived class.");
    }

    /**
     * Builds and returns a map of trace id to the set of initial nodes in the
     * trace. This is used for partially ordered traces, where it is not
     * possible to determine which initial nodes (pointed to from the synthetic
     * INITIAL node) are in the same trace.
     * 
     * @param initNode
     *            top level synthetic INITIAL node
     * @return a map of trace id to the set of initial nodes in the trace
     */
    protected LinkedHashMap<Integer, LinkedHashSet<EventNode>> buildTraceIdToInitNodesMap(
            EventNode initNode) {
        LinkedHashMap<Integer, LinkedHashSet<EventNode>> traceIdToInitNodes = new LinkedHashMap<Integer, LinkedHashSet<EventNode>>();
        // Build the trace id to initial nodes map by visiting all initial
        // nodes that are pointed to from the synthetic INITIAL node.
        for (ITransition<EventNode> initTrans : initNode.getTransitions()) {
            Integer tid = initTrans.getTarget().getTraceID();
            LinkedHashSet<EventNode> initTraceNodes;
            if (!traceIdToInitNodes.containsKey(tid)) {
                initTraceNodes = new LinkedHashSet<EventNode>();
                traceIdToInitNodes.put(tid, initTraceNodes);
            } else {
                initTraceNodes = traceIdToInitNodes.get(tid);
            }
            initTraceNodes.add(initTrans.getTarget());
        }
        return traceIdToInitNodes;
    }

    /**
     * Builds an invariants set based on the following observations:
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
    protected TemporalInvariantSet extractInvariantsFromWalkCounts(
            String relation,
            LinkedHashMap<EventType, Integer> gEventCnts,
            LinkedHashMap<EventType, LinkedHashMap<EventType, Integer>> gFollowedByCnts,
            LinkedHashMap<EventType, LinkedHashMap<EventType, Integer>> gPrecedesCnts,
            LinkedHashSet<EventType> AlwaysFollowsINITIALSet) {

        Set<ITemporalInvariant> invariants = new LinkedHashSet<ITemporalInvariant>();

        for (EventType label1 : gEventCnts.keySet()) {
            for (EventType label2 : gEventCnts.keySet()) {

                if (!gFollowedByCnts.containsKey(label1)) {
                    // label1 appeared only as the last event, therefore
                    // nothing can follow it, therefore label1 NFby label2

                    invariants.add(new NeverFollowedInvariant(label1, label2,
                            relation));

                } else {
                    if (!gFollowedByCnts.get(label1).containsKey(label2)) {
                        // label1 was never followed by label2, therefore label1
                        // NFby label2 (i.e. #_F(label1->label2) == 0)
                        invariants.add(new NeverFollowedInvariant(label1,
                                label2, relation));
                    } else {
                        // label1 was sometimes followed by label2
                        if (gFollowedByCnts.get(label1).get(label2)
                                .equals(gEventCnts.get(label1))) {
                            // #_F(label1->label2) == #label1 therefore label1
                            // AFby label2
                            invariants.add(new AlwaysFollowedInvariant(label1,
                                    label2, relation));
                        }
                    }
                }

                if (!gPrecedesCnts.containsKey(label1)) {
                    // label1 only appeared as the last event, therefore
                    // it cannot precede any other event
                } else {
                    if (gPrecedesCnts.get(label1).containsKey(label2)) {

                        // label1 sometimes preceded label2
                        if (gPrecedesCnts.get(label1).get(label2)
                                .equals(gEventCnts.get(label2))) {
                            // #_P(label1->label2) == #label2 therefore label1
                            // AP label2
                            invariants.add(new AlwaysPrecedesInvariant(label1,
                                    label2, relation));
                        }
                    }
                }
            }
        }

        // Determine all the INITIAL AFby x invariants to represent
        // "eventually x"
        for (EventType label : AlwaysFollowsINITIALSet) {
            invariants.add(new AlwaysFollowedInvariant(StringEventType
                    .NewInitialStringEventType(), label, relation));
        }

        return new TemporalInvariantSet(invariants);
    }

    /**
     * Used to extract concurrency invariants -- AlwaysConcurrentWith and
     * NeverConcurrentWith. These invariants are only useful for events at
     * different hosts (i.e., for x,y (totally ordered) events at the same host
     * AlwaysConcurrentWith(x,y) is trivially false, and
     * NeverConcurrentWith(x,y) is trivially true).
     * 
     * @param relation
     * @param gEventCnts
     * @param gFollowedByCnts
     * @param gPrecedesCnts
     * @return
     */
    protected TemporalInvariantSet extractConcurrencyInvariantsFromWalkCounts(
            String relation,
            LinkedHashMap<EventType, Integer> gEventCnts,
            LinkedHashMap<EventType, LinkedHashMap<EventType, Integer>> gFollowedByCnts,
            LinkedHashMap<EventType, LinkedHashMap<EventType, Integer>> gPrecedesCnts,
            LinkedHashMap<EventType, LinkedHashMap<EventType, Integer>> traceCoOccurrenceCnts) {

        Set<ITemporalInvariant> invariants = new LinkedHashSet<ITemporalInvariant>();

        LinkedHashSet<LinkedHashSet<EventType>> processedPairs = new LinkedHashSet<LinkedHashSet<EventType>>();

        // TODO: concurrent invariants for (x,y) is the same as for (y,x) so we
        // need to filter out the duplicates.
        for (EventType e1 : gEventCnts.keySet()) {
            for (EventType e2 : gEventCnts.keySet()) {
                if (!(e1 instanceof DistEventType)
                        || !(e2 instanceof DistEventType)) {
                    // TODO: specialize the exception
                    // TODO: print error message
                    throw new InternalSynopticException(
                            "Cannot compute concurrency invariants on non-distributed event types.");
                }

                if (((DistEventType) e1).getPID().equals(
                        ((DistEventType) e2).getPID())) {
                    // See comment at top of function about local versions of
                    // concurrency invariants -- we ignore them.
                    continue;
                }

                LinkedHashSet<EventType> s = new LinkedHashSet<EventType>();
                s.add(e1);
                s.add(e2);
                if (processedPairs.contains(s)) {
                    continue;
                }
                processedPairs.add(s);

                int e1_fby_e2 = 0;
                int e1_p_e2 = 0;
                int e2_fby_e1 = 0;
                int e2_p_e1 = 0;

                if (gFollowedByCnts.containsKey(e1)
                        && gFollowedByCnts.get(e1).containsKey(e2)) {
                    e1_fby_e2 = gFollowedByCnts.get(e1).get(e2);
                }
                if (gFollowedByCnts.containsKey(e2)
                        && gFollowedByCnts.get(e2).containsKey(e1)) {
                    e2_fby_e1 = gFollowedByCnts.get(e2).get(e1);
                }

                if (gPrecedesCnts.containsKey(e1)
                        && gPrecedesCnts.get(e1).containsKey(e2)) {
                    e1_p_e2 = gPrecedesCnts.get(e1).get(e2);
                }

                if (gPrecedesCnts.containsKey(e2)
                        && gPrecedesCnts.get(e2).containsKey(e1)) {
                    e2_p_e1 = gPrecedesCnts.get(e2).get(e1);
                }

                if (e1_fby_e2 == 0 && e2_fby_e1 == 0) {
                    // Potential concurrency if e1 and e2 _ever_ co-appeared in
                    // the same trace.
                    if ((traceCoOccurrenceCnts.containsKey(e1) && traceCoOccurrenceCnts
                            .get(e1).containsKey(e2))
                            || (traceCoOccurrenceCnts.containsKey(e2) && traceCoOccurrenceCnts
                                    .get(e2).containsKey(e1))) {
                        invariants.add(new AlwaysConcurrentInvariant(
                                (DistEventType) e1, (DistEventType) e2,
                                relation));
                    }
                } else if (e1_fby_e2 == e1_p_e2 && e2_fby_e1 == e2_p_e1) {
                    // Potential lack of concurrency if e1 and e2 _always_
                    // co-appeared in the same trace.
                    int e1_cooccur_e2 = 0;
                    if (traceCoOccurrenceCnts.containsKey(e1)
                            && traceCoOccurrenceCnts.get(e1).containsKey(e2)) {
                        e1_cooccur_e2 = traceCoOccurrenceCnts.get(e1).get(e2);
                    } else if (traceCoOccurrenceCnts.containsKey(e2)
                            && traceCoOccurrenceCnts.get(e2).containsKey(e1)) {
                        e1_cooccur_e2 = traceCoOccurrenceCnts.get(e2).get(e1);
                    }
                    if (e1_cooccur_e2 == (e1_fby_e2 + e2_fby_e1)) {
                        // Definite lack of concurrency.
                        invariants.add(new NeverConcurrentInvariant(
                                (DistEventType) e1, (DistEventType) e2,
                                relation));
                    }
                }
            }
        }

        return new TemporalInvariantSet(invariants);
    }
}