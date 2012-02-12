package synopticgwt.shared;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * A graph object communicated between the Synoptic service and the GWT client.
 */
public class GWTGraph implements Serializable {

    private static final long serialVersionUID = 1L;

    public HashSet<GWTNode> nodeSet;
    public List<GWTEdge> edges;

    public GWTGraph() {
        nodeSet = new HashSet<GWTNode>();
        edges = new LinkedList<GWTEdge>();
    }

    public void addNode(GWTNode node) {
        nodeSet.add(node);
    }

    public void addEdge(GWTNode source, GWTNode target, double weight, int count) {
        edges.add(new GWTEdge(source, target, weight, count));
    }

    public HashSet<GWTNode> getNodes() {
        return nodeSet;
    }

    public List<GWTEdge> getEdges() {
        return edges;
    }
}
