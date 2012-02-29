package synopticgwt.shared;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import synopticgwt.client.model.JSEdge;
import synopticgwt.client.model.JSGraph;
import synopticgwt.client.model.JSNode;
import synopticgwt.client.model.ModelTab;
import synopticgwt.client.util.MouseEventHandler;

/**
 * A graph object communicated between the Synoptic service and the GWT client.
 */
public class GWTGraph implements Serializable, MouseEventHandler<GWTNode> {

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
    private Set<GWTNode> selectedNodes;

    // Shows whether the graph has been initialized
    // and drawn. Must be set to true in order for
    // some operations to complete successfully.
    private boolean initialized = false;

    private GWTNode lastClicked = null;

    // private ModelTab modelTab;

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

    public void create(int width, int height, String canvasID, ModelTab modelTab) {

        this.selectedNodes = new HashSet<GWTNode>();
        // this.modelTab = modelTab;

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
        this.jsGraph.draw(width, height, canvasID, INITIAL, TERMINAL);

        // MUST be set after the graph is drawn at least once in order to
        // ensure that each of the rect fields has been set properly.
        for (GWTNode node : this.nodeSet) {
            if (!node.getEventType().equals(INITIAL)
                    && !node.getEventType().equals(TERMINAL)) {
                node.setMouseover(this);
                node.setMouseout(this);
                node.setOnClick(this);
            }
        }

        this.initialized = true;
    }

    public void resize(int width, int height) {
        checkInit();
        this.jsGraph.reDraw(width, height);
    }

    /**
     * Clears all nodes of prior state, and sets any highlighted nodes back to
     * default display properties.
     */
    public void clearNodeState() {
        checkInit();
        clearNodes(this.nodeSet);
        this.selectedNodes.clear();
    }

    /**
     * Clears the selected nodes, as well as setting any highlighted nodes back
     * to the default display properties.
     */
    public void clearSelectedNodes() {
        checkInit();
        clearNodes(this.selectedNodes);
        this.selectedNodes.clear();
    }

    /**
     * A helper method that clears the specified collection of nodes.
     * 
     * @param nodes
     *            The collection of nodes that are to be set to the default
     *            state.
     */
    private static void clearNodes(Collection<GWTNode> nodes) {
        for (GWTNode node : nodes) {
            if (!node.getEventType().equals(INITIAL)
                    && !node.getEventType().equals(TERMINAL))
                node.setStyle(DEFAULT_COLOR, DEFAULT_STROKE_COLOR,
                        DEFAULT_STROKE_WIDTH);
        }
    }

    /**
     * Clears the display state of the edges, i.e. sets them all back to
     * default.
     */
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
     * Checks to see if the graph has been initialized, and throws an exception
     * if it has not.
     */
    private void checkInit() {
        if (!this.initialized)
            throw new IllegalStateException("Graphic has not bee initialized");
    }

    /**
     * Mouseover handler that fires when the given node is moused over. The node
     * then sends itself through this method.
     */
    @Override
    public void mouseover(GWTNode hoveredNode) {
        // Highlight all nodes that have the same event type.
        for (GWTNode node : this.nodeSet) {
            if (node.getEventType().equals(hoveredNode.getEventType())) {
                node.setStyle(HIGHLIGHT_COLOR);
            }
        }
    }

    /**
     * Mouseout handler that fires when the given node is moused out. The node
     * sends itself through this method.
     */
    @Override
    public void mouseout(GWTNode hoveredNode) {
        // Set all but the initial and terminal nodes to the default color.
        for (GWTNode node : this.nodeSet) {
            if (!node.getEventType().equals(INITIAL)
                    && !node.getEventType().equals(TERMINAL)) {
                if (!this.selectedNodes.contains(node))
                    node.setStyle(DEFAULT_COLOR);
            }
        }
    }

    /**
     * Click handler that fires when a given node is clicked.
     */
    @Override
    public void onclick(GWTNode clickedNode, boolean shiftKey) {

        if (!shiftKey) {
            if (this.lastClicked == null
                    || !this.lastClicked.equals(clickedNode)) {
                // Clear the selected nodes and display the log lines in the
                // model panel.
                // the line for clearing selected nodes could possibly be
                // put outside of the
                // if statement.
                this.clearSelectedNodes();

                // TODO Handle this error better.
                // try {
                // this.modelTab.handleLogRequest(clickedNode.getPartitionNodeHashCode());
                // } catch (Exception e) {
                // e.printStackTrace();
                // }

                // If the last selected node (for log lines) is not null
                // set it to default colors before changing the current node
                // that has
                // been clicked.
                if (this.lastClicked != null) {
                    this.lastClicked.setStyle(DEFAULT_COLOR,
                            DEFAULT_STROKE_COLOR, DEFAULT_STROKE_WIDTH);
                } else {
                    // If the selectedNodeLog is null, that means there may
                    // be edges
                    // highlighted. If not, then there must be a node
                    // already selected
                    // to view log lines, so the state of the graph must be
                    // where
                    // the highlighted edges will have to have been cleared
                    // already.
                    // So, this will only run when a.) the graph has just
                    // been made, b.)
                    // when clicking a node without intending to "select"
                    // it, and at no
                    // other times.
                    this.clearEdgeState();
                }

                // The variable has to be set by accessing the instance
                // object. It WILL NOT
                // write to the modelGraphic instance variable if
                // "selectedNodeLog" is rewritten (for whatever reason).
                // be wary of this.
                this.lastClicked = clickedNode;
                clickedNode.setStyle(DEFAULT_COLOR, "red", SELECT_STROKE_WIDTH);
            }
        } else {
            // If the node clicked (with shift held) is not equal to this
            // one, clear
            boolean nodeNotSelected = !this.selectedNodes.contains(clickedNode);
            if (nodeNotSelected) {
                // Node associated with log lines listed is
                // surrounded by red and thick border.
                clickedNode.setStyle(HIGHLIGHT_COLOR);
                this.selectedNodes.add(clickedNode);

                // If the node clicked has been selected, remove the
                // highlight and also remove it from modelGraphic
            } else {
                // Set the stroke and stroke width back to normal as well,
                // as they are
                // still technically selected when viewing the model in edge
                // highlight mode.
                clickedNode.setStyle(DEFAULT_COLOR, DEFAULT_STROKE_COLOR,
                        DEFAULT_STROKE_WIDTH);
                this.selectedNodes.remove(clickedNode);
            }
        }
    }
}
