package synopticgwt.shared;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * A graph object communicated between the Synoptic service and the GWT client.
 */
public class GWTGraph implements Serializable {

    private static final long serialVersionUID = 1L;

    public HashMap<Integer, String> nodeIdToLabel;

    // The first int is the sourceID then the targetID, and then the edge
    // weight.
    public List<GWTEdge> edges;

    public GWTGraph() {
        nodeIdToLabel = new HashMap<Integer, String>();
        edges = new LinkedList<GWTEdge>();
    }

    public void addNode(Integer nodeID, String label) {
        nodeIdToLabel.put(nodeID, label);
    }

    public void addEdge(int nodeID1, int nodeID2, double weight) {
        edges.add(new GWTEdge(nodeID1, nodeID2, weight));
    }

    public HashMap<Integer, String> getNodes() {
        return nodeIdToLabel;
    }

    public List<GWTEdge> getEdges() {
        return edges;
    }

}
