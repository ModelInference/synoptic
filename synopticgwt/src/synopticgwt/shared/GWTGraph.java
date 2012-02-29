package synopticgwt.shared;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import synopticgwt.client.model.JSEdge;
import synopticgwt.client.model.JSGraph;
import synopticgwt.client.model.JSNode;

/**
 * A graph object communicated between the Synoptic service and the GWT client.
 */
public class GWTGraph implements Serializable {

    // Default color for nodes.
    private static String DEFAULT_COLOR = "#fa8";

    // Default stroke for border of node.
    private static int DEFAULT_STROKE_WIDTH = 2;

    // Default color for initial and terminal nodes.
    private static String INIT_TERM_COLOR = "#808080";

    // Color used when highlighting a node.
    private static String HIGHLIGHT_COLOR = "blue";

    // Border color for shift+click nodes after "View paths" clicked.
    // NOTE: Must also change same constant in ModelTab.java if modified.
    private static String SHIFT_CLICK_BORDER_COLOR = "blue";

    // Stroke width for border when node selected.
    private static int SELECT_STROKE_WIDTH = 4;

    // Label name that indicates initial node.
    private static String INITIAL = "INITIAL";

    // Label name that indicates terminal node.
    private static String TERMINAL = "TERMINAL";

    private static String DEFAULT_STROKE_COLOR = "black";

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

        this.initialized = true;
    }

    public void resize(int width, int height) {
        checkInit();
        this.jsGraph.reDraw(width, height);
    }

    public void clearNodeState() {
        checkInit();
        for (GWTNode node : this.nodeSet) {
            node.setStyle(DEFAULT_COLOR, DEFAULT_STROKE_COLOR,
                    DEFAULT_STROKE_WIDTH);
        }
    }

    public void clearEdgeState() {
        checkInit();
        setEdgeStyle(DEFAULT_STROKE_COLOR, 1, this.edges);
    }

    /**
     * Sets the style of a given collection of edges.
     * 
     * @param color
     *            The color to which the edges will be set.
     * @param strokeWidth
     *            The stroke width to which the edges will be set
     * @param edgeColl
     *            The collection of edges.
     */
    public static void setEdgeStyle(String color, int strokeWidth,
            Collection<GWTEdge> edgeColl) {
        for (GWTEdge edge : edgeColl) {
            edge.setStyle(color, strokeWidth);
        }
    }

    /**
     * Checks to see if the graph has been initialized.
     */
    private void checkInit() {
        if (!this.initialized)
            throw new IllegalStateException("Graphic has not bee initialized");
    }
}
