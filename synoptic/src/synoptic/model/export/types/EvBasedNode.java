package synoptic.model.export.types;

import java.util.HashSet;
import java.util.Set;

/**
 * A node in an event-based FSM version of a Synoptic model
 */
public class EvBasedNode {
    public final int nodeID;
    public final Set<EvBasedEdge> outEdges = new HashSet<>();

    public EvBasedNode(int nodeID) {
        this.nodeID = nodeID;
    }

    public void addOutEdge(EvBasedEdge outEdge) {
        outEdges.add(outEdge);
    }
}
