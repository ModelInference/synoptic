package synoptic.invariants.miners;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;

import synoptic.invariants.AlwaysFollowedInvariant;
import synoptic.invariants.AlwaysPrecedesInvariant;
import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.NeverFollowedInvariant;
import synoptic.invariants.TemporalInvariantSet;
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
}