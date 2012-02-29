package synopticgwt.shared;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import synopticgwt.client.model.JSEdge;
import synopticgwt.client.model.JSGraph;
import synopticgwt.client.model.JSNode;

/**
 * A graph object communicated between the Synoptic service and the GWT client.
 */
public class GWTGraph implements Serializable {

    private static final long serialVersionUID = 1L;

    public List<GWTNode> nodeSet;
    public List<GWTEdge> edges;

    // Shows whether the graph has been initialized
    // and drawn. Must be set to true in order for
    // some operations to complete successfully.
    private boolean initialized = false;

    // A reference to the dracula graph used by this object
    // the field also contains references central to the dracula
    // graph, including things like the layouter and renderer.
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

    public void create(int width, int height, String canvasID, String initial,
            String terminal) {

        this.jsGraph = JSGraph.create();

        // For each node, add the node to the jsGraph,
        // and then add the reference to said node to the
        // corresponding GWTNode so subsequent alterations
        // can be controlled through the node, and not necessarily through
        // the graph.
        for (GWTNode node : this.nodeSet) {
            JSNode jsNode = this.jsGraph.addNode(node);
            node.setJSNode(jsNode);
        }

        // For each edge in the graph, add the corresponding edge to
        // the jsGraph, and then set the reference to said edge to be
        // inside of the corresponding GWTEdge, so further alterations
        // can be set through the
        for (GWTEdge edge : this.edges) {
            JSEdge jsEdge = this.jsGraph.addEdge(edge);
            edge.setJSEdge(jsEdge);
        }

        // Draw the graph after all the innards have been added.
        this.jsGraph.draw(width, height, canvasID, initial, terminal);
    }
}
