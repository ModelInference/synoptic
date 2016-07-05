package synoptic.model.export.types;

import java.util.Set;

import synoptic.model.EventNode;
import synoptic.model.event.EventType;
import synoptic.util.resource.AbstractResource;

/**
 * An edge in an event-based FSM version of a Synoptic model
 */
public class EvBasedEdge {
    public final int edgeID;
    public final EvBasedNode srcNode;
    public final EvBasedNode destNode;
    public final Set<EventNode> events;
    public final EventType eType;
    public final AbstractResource resMin;
    public final AbstractResource resMax;
    public final Double prob;

    public EvBasedEdge(int edgeID, EvBasedNode srcNode, EvBasedNode destNode,
            Set<EventNode> events, EventType eType, AbstractResource resMin,
            AbstractResource resMax, Double prob) {
        this.edgeID = edgeID;
        this.srcNode = srcNode;
        this.destNode = destNode;
        this.events = events;
        this.eType = eType;
        this.resMin = resMin;
        this.resMax = resMax;
        this.prob = prob;
    }
}
