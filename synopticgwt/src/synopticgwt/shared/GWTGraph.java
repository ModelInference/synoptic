package synopticgwt.shared;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import java.util.HashSet;

/**
 * A graph object communicated between the Synoptic service and the GWT client.
 */
public class GWTGraph implements Serializable {

    private static final long serialVersionUID = 1L;

    public HashSet<GWTNode> nodeIdToLabel;

    // The first int is the sourceID then the targetID, and then the edge
    // weight.
    public List<GWTEdge> edges;

    public GWTGraph() {
        nodeIdToLabel = new HashSet<GWTNode>();
        edges = new LinkedList<GWTEdge>();
    }

    public void addNode(GWTNode node) {
        nodeIdToLabel.add(node);
    }

    public void addEdge(GWTNode source, GWTNode target, double weight) {
        edges.add(new GWTEdge(source, target, weight));
    }

    public HashSet<GWTNode> getNodes() {
        return nodeIdToLabel;
    }

    public List<GWTEdge> getEdges() {
        return edges;
    }
}
