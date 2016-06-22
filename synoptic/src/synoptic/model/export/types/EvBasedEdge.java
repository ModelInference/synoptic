package synoptic.model.export.types;

import java.util.HashSet;
import java.util.Set;

import synoptic.model.EventNode;
import synoptic.model.event.EventType;
import synoptic.util.resource.AbstractResource;

/**
 * An edge in an event-based FSM version of a Synoptic model
 */
public class EvBasedEdge {
    int edgeID;
    EvBasedNode srcNode;
    EvBasedNode destNode;
    Set<EventNode> events = new HashSet<EventNode>();
    EventType eType;
    AbstractResource resMin;
    AbstractResource resMax;
    Double prob;

    public EvBasedEdge(int edgeID, EvBasedNode srcNode, EvBasedNode destNode, Set<EventNode> events, EventType eType,
            AbstractResource resMin, AbstractResource resMax, Double prob) {
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
