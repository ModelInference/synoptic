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
    public List<GWTPair<Integer, Integer>> edges;

    public GWTGraph() {
        nodeIdToLabel = new HashMap<Integer, String>();
        edges = new LinkedList<GWTPair<Integer, Integer>>();
    }

    public void addNode(Integer nodeID, String label) {
        nodeIdToLabel.put(nodeID, label);
    }

    public void addEdge(Integer nodeID1, Integer nodeID2) {
        edges.add(new GWTPair<Integer, Integer>(nodeID1, nodeID2, 0));
    }

    public HashMap<Integer, String> getNodes() {
        return nodeIdToLabel;
    }

    public List<GWTPair<Integer, Integer>> getEdges() {
        return edges;
    }

}
