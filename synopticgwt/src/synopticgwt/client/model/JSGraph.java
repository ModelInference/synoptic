package synopticgwt.client.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.Window;

import synopticgwt.client.util.MouseEventHandler;
import synopticgwt.shared.GWTEdge;
import synopticgwt.shared.GWTGraph;
import synopticgwt.shared.GWTNode;

/**
 * A class used to talk to GWTGraph for client side operations.
 */
public class JSGraph {

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

    // TODO: INITIAL_LABEL and TERMINAL_LABEL should not be hardcoded. Instead,
    // the EventType class should be ported to GWT, or a mirror type should be
    // created which would have the notion of initial/terminal based on
    // EventType.isInitialEventTyep and EventType.isTerminalEventType.

    // Label name that indicates initial node.
    private static String INITIAL = "INITIAL";

    // Label name that indicates terminal node.
    private static String TERMINAL = "TERMINAL";

    private static String DEFAULT_STROKE_COLOR = "black";

    // The set of selected nodes.
    private Set<JSONode> selectedNodes;

    private Map<GWTNode, JSONode> nodes;

    // Maps GWTEdge hash codes to edges.
    private Map<GWTEdge, JSOEdge> edges;

    // The JS Overlay class for talking to the JS implementation
    // of the graph.
    private JSOGraph jsoGraph;

    // The reference to the last clicked node (for
    // viewing log lines).
    private JSONode lastClicked;

    // Instance of the model tab (mainly for running RPCs to the
    // server).
    private final ModelTab modelTab;

    public JSGraph(ModelTab modelTab) {
        this.modelTab = modelTab;
    }

    public void create(GWTGraph graph, int width, int height, String canvasID) {
        this.selectedNodes = new HashSet<JSONode>();
        this.nodes = new HashMap<GWTNode, JSONode>();
        this.edges = new HashMap<GWTEdge, JSOEdge>();

        this.jsoGraph = JSOGraph.create();
        // For each node, add the node to the jsGraph,
        // and then add the reference to said node to this
        // graph's list of nodes.
        for (GWTNode node : graph.nodeSet) {
            JSONode jsoNode = this.jsoGraph.addNode(node);
            // Make sure to attach the custom renderer.
            jsoNode.attachRenderer();
            this.nodes.put(node, jsoNode);
        }

        // For each edge in the graph, add the corresponding edge to
        // the jsGraph, and then add the reference to said edge to
        // this graph.
        for (GWTEdge edge : graph.edges) {
            JSOEdge jsoEdge = this.jsoGraph.addEdge(edge);
            this.edges.put(edge, jsoEdge);
        }

        // Draw the graph after all the innards have been added.
        this.jsoGraph.draw(width, height, canvasID, INITIAL, TERMINAL);

        // MUST be set after the graph is drawn at least once in order to
        // ensure that each of the rect fields has been set properly.
        JSONodeEventHandler eventHandler = new JSONodeEventHandler();
        for (JSONode node : this.nodes.values()) {
            if (!node.getEventType().equals(INITIAL)
                    && !node.getEventType().equals(TERMINAL)) {
                node.setMouseover(eventHandler);
                node.setMouseout(eventHandler);
                node.setOnClick(eventHandler);
            }
        }
    }

    public void resize(int width, int height) {
        this.jsoGraph.reDraw(width, height);
    }

    /**
     * Clears all nodes of prior state, and sets any highlighted nodes back to
     * default display properties.
     */
    public void clearNodeState() {
        clearNodes(this.nodes.values());
        this.selectedNodes.clear();
    }

    /**
     * Clears all nodes of prior state, and sets any highlighted nodes back to
     * default display properties.
     */
    public void clearSelectedNodes() {
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
    private static void clearNodes(Collection<JSONode> nodes) {
        for (JSONode node : nodes) {
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
        for (JSOEdge edge : this.edges.values()) {
            edge.setStyle(DEFAULT_STROKE_COLOR, 1);
        }
    }

    /**
     * Highlights the edges within the graph according to the specified path.
     * 
     * @param path
     *            The list of edges to be highlighted.
     */
    public void highlightEdges(Collection<GWTEdge> path) {
        this.clearEdgeState();
        // for (GWTEdge edge : path) {
        // this.edges.get(edge).setStyle(HIGHLIGHT_COLOR, SELECT_STROKE_WIDTH);
        // }

        // TODO The commented code above _should_ work, but edges.get(edge)
        // keeps
        // returning null, suggesting there might be so sort of discrepancy that
        // needs to be sorted out. For now there's this horribly inefficient
        // nested loop.
        for (GWTEdge edge : path) {
            for (GWTEdge key : this.edges.keySet())
                if (key.getSrc().getPartitionNodeHashCode() == edge.getSrc()
                        .getPartitionNodeHashCode()
                        && key.getDst().getPartitionNodeHashCode() == edge
                                .getDst().getPartitionNodeHashCode()) {

                    this.edges.get(key).setStyle(HIGHLIGHT_COLOR,
                            SELECT_STROKE_WIDTH);
                    break;
                }
        }

    }

    /**
     * Changes the nodes that have been selected to the highlighted border
     * color.
     */
    public void setPathHighlightViewState() {
        // Clear the node whose log lines are being shown (if they've been
        // selected).
        if (this.lastClicked != null) {
            this.lastClicked.setStyle(DEFAULT_STROKE_COLOR,
                    DEFAULT_STROKE_WIDTH);
            this.lastClicked = null;
        } else {
            // Clears any highlighted edges if there are no log lines being
            // viewed
            // (which means the graph is showing paths traces and must be
            // cleared).
            this.clearEdgeState();
        }

        // Set all the borders to the highlight color.
        for (JSONode selectedNode : this.selectedNodes) {
            selectedNode.setStyle(DEFAULT_COLOR, HIGHLIGHT_COLOR,
                    SELECT_STROKE_WIDTH);
        }
    }

    /**
     * Event handler for JSONodes. This handles any event that occurs over the
     * graphical representation of the JSONode in question. As of now, when
     * hovering over a node, the node (and any other nodes of of the same event
     * type) are highlighted. When the mouse is taken away, the nodes are all
     * returned to their default color.
     * 
     * When a node is clicked, it is highlighted and the corresponding log lines
     * that relate to that specific event are shown.
     */
    private class JSONodeEventHandler implements MouseEventHandler<JSONode> {

        // Empty constructor to please the compiler.
        public JSONodeEventHandler() {
        }

        @SuppressWarnings("synthetic-access")
        @Override
        public void mouseover(JSONode eventNode) {
            // Highlight all nodes that have the same event type.
            for (JSONode node : JSGraph.this.nodes.values()) {
                if (node.getEventType().equals(eventNode.getEventType())) {
                    node.setStyle(JSGraph.HIGHLIGHT_COLOR);
                }
            }
        }

        @SuppressWarnings("synthetic-access")
        @Override
        public void mouseout(JSONode t) {
            // Set all but the initial and terminal nodes to the default color.
            for (JSONode node : JSGraph.this.nodes.values()) {
                if (!node.getEventType().equals(JSGraph.INITIAL)
                        && !node.getEventType().equals(JSGraph.TERMINAL)) {
                    if (!JSGraph.this.selectedNodes.contains(node))
                        node.setStyle(JSGraph.DEFAULT_COLOR);
                }
            }
        }

        @SuppressWarnings("synthetic-access")
        @Override
        public void onclick(JSONode clickedNode, boolean shiftKey) {
            if (!shiftKey) {
                if (JSGraph.this.lastClicked == null
                        || !JSGraph.this.lastClicked.equals(clickedNode)) {
                    // Clear the selected nodes and display the log lines in the
                    // model panel.
                    // the line for clearing selected nodes could possibly be
                    // put outside of the
                    // if statement.
                    JSGraph.this.clearSelectedNodes();

                    // TODO Handle this error better.
                    try {
                        JSGraph.this.modelTab.handleLogRequest(clickedNode
                                .getNodeID());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // If the last selected node (for log lines) is not null
                    // set it to default colors before changing the current node
                    // that has
                    // been clicked.
                    if (JSGraph.this.lastClicked != null) {
                        JSGraph.this.lastClicked.setStyle(DEFAULT_COLOR,
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
                        JSGraph.this.clearEdgeState();
                    }

                    // The variable has to be set by accessing the instance
                    // object. It WILL NOT
                    // write to the modelGraphic instance variable if
                    // "selectedNodeLog" is rewritten (for whatever reason).
                    // be wary of this.
                    JSGraph.this.lastClicked = clickedNode;
                    clickedNode.setStyle(DEFAULT_COLOR, "red",
                            SELECT_STROKE_WIDTH);
                }
            } else {
                // If the node clicked (with shift held) is not equal to this
                // one, clear
                boolean nodeNotSelected = !JSGraph.this.selectedNodes
                        .contains(clickedNode);
                if (nodeNotSelected) {
                    // Node associated with log lines listed is
                    // surrounded by red and thick border.
                    clickedNode.setStyle(HIGHLIGHT_COLOR);
                    JSGraph.this.selectedNodes.add(clickedNode);
                    JSGraph.this.modelTab.addSelectedNode(clickedNode
                            .getNodeID());

                    // If the node clicked has been selected, remove the
                    // highlight and also remove it from modelGraphic
                } else {
                    // Set the stroke and stroke width back to normal as well,
                    // as they are
                    // still technically selected when viewing the model in edge
                    // highlight mode.
                    clickedNode.setStyle(DEFAULT_COLOR, DEFAULT_STROKE_COLOR,
                            DEFAULT_STROKE_WIDTH);
                    JSGraph.this.selectedNodes.remove(clickedNode);
                    JSGraph.this.modelTab.removeSelectedNode(clickedNode
                            .getNodeID());
                }
            }
        }
    }
}
