package synoptic.invariants.miners;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import synoptic.invariants.NeverImmediatelyFollowedInvariant;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.model.ChainsTraceGraph;
import synoptic.model.EventNode;
import synoptic.model.event.Event;
import synoptic.model.event.EventType;
import synoptic.model.interfaces.ITransition;

/**
 * Mines the NeverImmediatelyFollowedyBy invariants, which are the basis for the
 * initial PartitionGraph that Synoptic starts with, and which are a form of
 * implicit invariants in Synoptic. This miner is used by InvariMint.
 */
public class ImmediateInvariantMiner {

    private final ChainsTraceGraph g;

    private final Set<EventType> allEvents;

    private final TemporalInvariantSet nifByInvariants;

    public ImmediateInvariantMiner(ChainsTraceGraph g) {
        this.g = g;
        Map<EventType, Set<EventType>> cifByMap = getCIFbyMap();

        allEvents = cifByMap.keySet();

        nifByInvariants = computeInvariants(cifByMap);
    }

    private Map<EventType, Set<EventType>> getCIFbyMap() {
        EventNode initNode = g.getDummyInitialNode();

        // Maps each EventType to the set of EventTypes that immediately follow
        // it.
        Map<EventType, Set<EventType>> canFollow = new HashMap<EventType, Set<EventType>>();

        // Iterate through all the traces -- each transition from the
        // INITIAL node connects\holds a single trace.
        for (ITransition<EventNode> initTrans : initNode.getAllTransitions()) {

            EventNode cur = initTrans.getTarget();
            EventType first = initNode.getEType();
            EventType second = cur.getEType();

            while (true) {
                if (!canFollow.containsKey(first)) {
                    canFollow.put(first, new HashSet<EventType>());
                }
                canFollow.get(first).add(second);
                if (cur.getAllTransitions().size() == 0) {

                    // Add terminal event to the canFollow set
                    if (!canFollow.containsKey(second)) {
                        canFollow.put(second, new HashSet<EventType>());
                    }
                    break;
                }
                cur = cur.getAllTransitions().get(0).getTarget();
                first = second;
                second = cur.getEType();
            }
        }

        return canFollow;
    }

    private TemporalInvariantSet computeInvariants(
            Map<EventType, Set<EventType>> canFollow) {
        // Create invariants
        TemporalInvariantSet neverIFbyInvariants = new TemporalInvariantSet();

        for (Entry<EventType, Set<EventType>> entry : canFollow.entrySet()) {
            EventType source = entry.getKey();
            Set<EventType> followedBy = entry.getValue();
            for (EventType target : allEvents) {
                if (!followedBy.contains(target)) {
                    neverIFbyInvariants
                            .add(new NeverImmediatelyFollowedInvariant(source,
                                    target, Event.defTimeRelationStr));
                }
            }
        }
        return neverIFbyInvariants;
    }

    /**
     * Returns the set of all NIFby invariants parsed from the TraceGraph by
     * this miner.
     */
    public TemporalInvariantSet getNIFbyInvariants() {
        return nifByInvariants;
    }

    /**
     * Returns a set of all EventTypes in the TraceGraph parsed by this miner.
     */
    public Set<EventType> getEventTypes() {
        return allEvents;
    }
}
