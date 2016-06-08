package synoptic.model.export.types;

import java.util.HashSet;
import java.util.Set;

/**
 * A node in an event-based FSM version of a Synoptic model
 */
public class EvBasedNode {
    int nodeID;
    Set<EvBasedEdge> outEdges = new HashSet<>();

    public EvBasedNode(int nodeID) {
        this.nodeID = nodeID;
    }

    public void addOutEdge(EvBasedEdge outEdge) {
        outEdges.add(outEdge);
    }
}
