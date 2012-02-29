package synopticgwt.shared;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import synopticgwt.client.model.JSGraph;

/**
 * A graph object communicated between the Synoptic service and the GWT client.
 */
public class GWTGraph implements Serializable {

    private static final long serialVersionUID = 1L;

    public List<GWTNode> nodeSet;
    public List<GWTEdge> edges;
    
    private JSGraph jsGraph;
    
    public GWTGraph() {
        nodeSet = new LinkedList<GWTNode>();
        edges = new LinkedList<GWTEdge>();
    }
    
    public void addNode(GWTNode node) {
        nodeSet.add(node);
    }
    
    public void addEdge(GWTNode source, GWTNode target, double weight, int count) {
        edges.add(new GWTEdge(source, target, weight, count));
    }

    public List<GWTNode> getNodes() {
        return nodeSet;
    }

    public List<GWTEdge> getEdges() {
        return edges;
    }
}
