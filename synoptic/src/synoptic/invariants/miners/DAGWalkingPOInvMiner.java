package synoptic.invariants.miners;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import synoptic.invariants.TemporalInvariantSet;
import synoptic.main.TraceParser;
import synoptic.model.EventNode;
import synoptic.model.EventType;
import synoptic.model.interfaces.IGraph;
import synoptic.model.interfaces.ITransition;
import synoptic.util.InternalSynopticException;
import synoptic.util.NotImplementedException;

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
        LinkedHashSet<EventType> tSeen = new LinkedHashSet<EventType>();
        // Maintains the current event count in the trace.
        LinkedHashMap<EventType, Integer> tEventCnts = new LinkedHashMap<EventType, Integer>();
        // Maintains the current FollowedBy count for the trace.
        // tFollowedByCnts[a][b] = cnt iff the number of a's that appeared
        // before
        // this b is cnt.
        LinkedHashMap<EventType, LinkedHashMap<EventType, Integer>> tFollowedByCnts = new LinkedHashMap<EventType, LinkedHashMap<EventType, Integer>>();

        // Iterate through all the traces -- each transition from the INITIAL
        // node connects\holds a single trace.
        for (ITransition<EventNode> initTrans : initNode.getTransitions()) {
            EventNode curNode = initTrans.getTarget();

        }

        throw new NotImplementedException();

        // return extractInvariantsFromWalkCounts(relation, gEventCnts,
        // gFollowedByCnts, gPrecedesCnts, AlwaysFollowsINITIALSet);
    }

}
