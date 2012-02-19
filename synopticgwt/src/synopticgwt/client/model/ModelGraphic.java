package synopticgwt.client.model;

import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;

import synopticgwt.shared.GWTEdge;
import synopticgwt.shared.GWTNode;

/**
 * Used to create the graphic representing the Synoptic model.
 */
public class ModelGraphic {

    // Default color for nodes.
    public static String DEFAULT_COLOR = "#fa8";

    // The ModelTab that this graphic is associated with.
    private ModelTab modelTab;

    // Default stroke for border of node.
    public static int DEFAULT_STROKE_WIDTH = 2;

    // Default color for initial and terminal nodes.
    public static String INIT_TERM_COLOR = "#808080";

    // Color used when highlighting a node.
    public static String HIGHLIGHT_COLOR = "blue";

    // Border color for shift+click nodes after "View paths" clicked.
    // NOTE: Must also change same constant in ModelTab.java if modified.
    public static String SHIFT_CLICK_BORDER_COLOR = "blue";

    // Stroke width for border when node selected.
    public static int SELECT_STROKE_WIDTH = 4;

    // Label name that indicates initial node.
    public static String INITIAL = "INITIAL";

    // Label name that indicates terminal node.
    public static String TERMINAL = "TERMINAL";

    // TODO: Consider making some of these overlaying classes
    // as they may make some alterations of data easier.

    // The dracula graph. This contains all information (after being
    // instantiated)
    // for rendering and handling the graph directly on the window.
    public JavaScriptObject draculaGraph;

    // The set of all selected dracula nodes.
    public JavaScriptObject selectedDraculaNodes;

    // An array containing all rectangles objects.
    public JavaScriptObject allRects;

    // The selected node that has log lines displayed.
    private JavaScriptObject selectedNodeLog;

    // The JS object that determines and sets up the layour of the dracula graph
    // when being rendered.
    private JavaScriptObject draculaLayouter;

    // Gets the general renderer for the draculaGraph. This is different than
    // the extended renderer returned by calling the getCustomRenderer method,
    // as the method returns an extension this basic renderer.
    private JavaScriptObject draculaRenderer;

    public ModelGraphic(ModelTab modelTab) {
        this.modelTab = modelTab;
    }

    // //////////////////////////////////////////////////////////////////////////
    // JSNI methods -- JavaScript Native Interface methods. The method body of
    // these calls is pure JavaScript.

    /**
     * Updates the model edges to display transition probabilities.
     */
    public native void useProbEdgeLabels() /*-{
        var g = this.@synopticgwt.client.model.ModelGraphic::draculaGraph;
        var edges = g.edges;
        for (i = 0; i < edges.length; i++) {
            edges[i].connection.label.attr({
                text : edges[i].style.labelProb
            });
        }
    }-*/;

    /**
     * Updates the model edges to display transition counts.
     */
    public native void useCountEdgeLabels() /*-{
        var g = this.@synopticgwt.client.model.ModelGraphic::draculaGraph;
        var edges = g.edges;
        for (i = 0; i < edges.length; i++) {
            edges[i].connection.label.attr({
                text : edges[i].style.labelCount
            });
        }
    }-*/;

    /**
     * A JSNI method to create and display a graph.
     * 
     * @param modelTab
     *            A reference to the modelTab instance containing this graphic.
     * @param nodes
     *            An array of nodes, each consecutive pair is a <id,label>
     * @param edges
     *            An array of edges, each consecutive pair is <node id, node id>
     * @param width
     *            width of the graph
     * @param height
     *            height of the graph
     * @param canvasId
     *            the div id with which to associate the resulting graph
     */
    public native void createGraph(List<GWTNode> nodes, List<GWTEdge> edges,
            int width, int height, String canvasId, String initial,
            String terminal) /*-{

        // Determinize Math.random() calls for deterministic graph layout. Relies on seedrandom.js
        $wnd.Math.seedrandom($wnd.randSeed);

        var mTab = this.@synopticgwt.client.model.ModelGraphic::modelTab;
        var modelGraphic = this;

        // Export the handleLogRequest globally.
        $wnd.viewLogLines = function(id) {
            modelGraphic.@synopticgwt.client.model.ModelGraphic::clearEdgeState()();
            mTab.@synopticgwt.client.model.ModelTab::handleLogRequest(I)(id);
        };

        // Export global add/remove methods for selected nodes (moving 
        // nodes to model tab).
        $wnd.addSelectedNode = function(id) {
            mTab.@synopticgwt.client.model.ModelTab::addSelectedNode(I)(id);
        };

        $wnd.removeSelectedNode = function(id) {
            mTab.@synopticgwt.client.model.ModelTab::removeSelectedNode(I)(id);
        };

        // Create the Dracula graph and set the instance field to point to said
        // graph.
        var g = new $wnd.Graph();
        this.@synopticgwt.client.model.ModelGraphic::draculaGraph = g;
        g.edgeFactory.template.style.directed = true;

        // Add each node to the graph.
        for ( var i = 0; i < nodes.@java.util.List::size()(); i++) {
            var node = nodes.@java.util.List::get(I)(i);
            // Store graph state.
            var nodeHashCode = node.@synopticgwt.shared.GWTNode::getPartitionNodeHashCodeStr()();
            var nodeLabel = node.@synopticgwt.shared.GWTNode::toString()();

            g
                    .addNode(
                            nodeHashCode,
                            {
                                label : nodeLabel,
                                render : this.@synopticgwt.client.model.ModelGraphic::getCustomRenderer()()
                            });
        }

        for ( var i = 0; i < edges.@java.util.List::size()(); i++) {
            var edge = edges.@java.util.List::get(I)(i);
            this.@synopticgwt.client.model.ModelGraphic::addEdge(Lsynopticgwt/shared/GWTEdge;Lcom/google/gwt/core/client/JavaScriptObject;)(edge, g);
        }

        // Give stable layout to graph elements.
        var layouter = new $wnd.Graph.Layout.Stable(g, initial, terminal);

        // Render the graph.
        var renderer = new $wnd.Graph.Renderer.Raphael(canvasId, g, width,
                height);

        // Store the layouter.
        this.@synopticgwt.client.model.ModelGraphic::draculaLayouter = layouter;
    }-*/;

    /**
     * A JSNI method to update and display a refined graph, animating the
     * transition to a new layout.
     * 
     * @param nodes
     *            An array of nodes, each consecutive pair is a <id,label>
     * @param edges
     *            An array of edges, each consecutive pair is <node id, node id>
     * @param refinedNode
     *            the ID of the refined node
     * @param canvasId
     *            the div id with which to associate the resulting graph
     */
    public native void createChangingGraph(List<GWTNode> nodes,
            List<GWTEdge> edges, int refinedNode, String canvasId) /*-{

        // Determinize Math.random() calls for deterministic graph layout. Relies on seedrandom.js
        $wnd.Math.seedrandom($wnd.randSeed);

        // Clear the selected nodes from the graph's state.
        this.@synopticgwt.client.model.ModelGraphic::clearSelectedNodes();

        // update graph and fetch array of new nodes
        var newNodes = this.@synopticgwt.client.model.ModelGraphic::updateRefinedGraph(Ljava/util/List;Ljava/util/List;I)(nodes,edges,refinedNode);

        // fetch the current layouter
        var layouter = this.@synopticgwt.client.model.ModelGraphic::draculaLayouter;

        // update each graph element's position, re-assigning a position
        layouter.updateLayout(
                this.@synopticgwt.client.model.ModelGraphic::draculaGraph,
                newNodes);

        // fetch the renderer
        var renderer = this.@synopticgwt.client.model.ModelGraphic::draculaRenderer;

        // re-draw the graph, animating transitions from old to new position
        renderer.draw();
    }-*/;

    // updates the graph by removing the node with the splitNodeID and adding
    // (plus drawing)
    // all newly refined nodes at the position of the removed node. returns an
    // array of the
    // new nodes
    public native JavaScriptObject updateRefinedGraph(List<GWTNode> nodes,
            List<GWTEdge> edges, int splitNodeID) /*-{

        var dGraph = this.@synopticgwt.client.model.ModelGraphic::draculaGraph;
        // Retrieve the refined node.
        var refinedNode = dGraph.nodes[splitNodeID];

        // Remove the refined node and all of its edges from the graph.
        dGraph.removeNode(splitNodeID);

        // Tracks which new nodes are added to update edges below.
        var newNodes = [];

        // Loop over all the given nodes, find and add new nodes to the graph
        for ( var i = 0; i < nodes.@java.util.List::size()(); i++) {
            var node = nodes.@java.util.List::get(I)(i);

            var nodeHashCode = node.@synopticgwt.shared.GWTNode::getPartitionNodeHashCodeStr()();
            var nodeLabel = node.@synopticgwt.shared.GWTNode::toString()();
            if (!dGraph.nodes[nodeHashCode]) {
                newNodes[nodeHashCode] = true;
                dGraph
                        .addNode(
                                nodeHashCode,
                                {
                                    label : nodeLabel,
                                    render : this.@synopticgwt.client.model.ModelGraphic::getCustomRenderer()(),
                                    layoutPosX : refinedNode.layoutPosX,
                                    layoutPosY : refinedNode.layoutPosY
                                });
            }
        }

        // re-draw the graph, adding new nodes to the canvas
        this.@synopticgwt.client.model.ModelGraphic::draculaRenderer.draw();

        // loop over all given edges, finding ones connected to the new
        // nodes that need to be added to the graph
        for ( var i = 0; i < edges.@java.util.List::size()(); i++) {
            var edge = edges.@java.util.List::get(I)(i);
            var sourceNode = edge.@synopticgwt.shared.GWTEdge::getSrc()();
            var sourceHash = sourceNode.@synopticgwt.shared.GWTNode::getPartitionNodeHashCodeStr()();
            var destNode = edge.@synopticgwt.shared.GWTEdge::getDst()();
            var destHash = destNode.@synopticgwt.shared.GWTNode::getPartitionNodeHashCodeStr()();

            if (newNodes[sourceHash] || newNodes[destHash]) {
                this.@synopticgwt.client.model.ModelGraphic::addEdge(Lsynopticgwt/shared/GWTEdge;Lcom/google/gwt/core/client/JavaScriptObject;)(edge, dGraph);
            }
        }

        // return the set of new nodes
        return newNodes;
    }-*/;

    private native void addEdge(GWTEdge edge, JavaScriptObject graph) /*-{

        var sourceNode = edge.@synopticgwt.shared.GWTEdge::getSrc()();
        var source = sourceNode.@synopticgwt.shared.GWTNode::getPartitionNodeHashCodeStr()();
        var destNode = edge.@synopticgwt.shared.GWTEdge::getDst()();
        var dest = destNode.@synopticgwt.shared.GWTNode::getPartitionNodeHashCodeStr()();

        // var transProb = @synopticgwt.client.model.ModelGraphic::probToString(D)(edge.@synopticgwt.shared.GWTEdge::getWeight()());
        var transProb = edge.@synopticgwt.shared.GWTEdge::getWeightStr()();
        var transCount = edge.@synopticgwt.shared.GWTEdge::getCountStr()();
        var mTab = this.@synopticgwt.client.model.ModelGraphic::modelTab;
        var showCounts = mTab.@synopticgwt.client.model.ModelTab::getShowEdgeCounts()();

        if (showCounts) {
            labelVal = transCount;
        } else {
            labelVal = transProb;
        }
        style = {
            label : labelVal,
            labelProb : transProb,
            labelCount : transCount
        };
        newEdge = graph.addEdge(source, dest, style);
    }-*/;

    /**
     * A JSNI method for updating the graph. This is supposed to be called upon
     * resizing the graph, as the graph is assumed not to have changed at all
     * when calling this method. Changes the size of the Raphael canvas and the
     * model div to the width and height of the parameters.
     * 
     * @param width
     *            The new width of the graph's canvas.
     * @param height
     *            The new height of the graph's canvas.
     */
    public native void resizeGraph(int width, int height) /*-{
        // Determinize Math.random() calls for deterministic graph layout. Relies on seedrandom.js
        $wnd.Math.seedrandom($wnd.randSeed);

        // Get the current layout so it can be updated.
        var layouter = this.@synopticgwt.client.model.ModelGraphic::draculaLayouter;
        var dGraph = this.@synopticgwt.client.model.ModelGraphic::draculaGraph;

        // Update the layout for all nodes.
        layouter.updateLayout(dGraph, dGraph.nodes);

        // Grab a pointer to the current renderer.
        var rend = this.@synopticgwt.client.model.ModelGraphic::draculaRenderer;

        // Change the appropriate height/width of the div.
        rend.width = width;
        rend.height = height;

        // Change the width/height of the Raphael canvas.
        rend.r.setSize(width, height);

        // Draw the new graph with all of the repositioned nodes.
        rend.draw();
    }-*/;

    /**
     * For all selected nodes in model, change their border to given color.
     * 
     * @param color
     *            The color to which the nodes will be set.
     */
    public void updateNodesBorder(String color) {
        setShiftClickNodesState(color);
    }

    /**
     * Clears the state of the edges in the graph, but does not redraw the
     * graph. this has to be done after this method is called (and any
     * subsequent alterations to the graph that may have occurred thenceforth).
     * <p>
     * IMPORTANT NOTE: When changing the state of the edges in the Dracula Model
     * make sure to change the attributes using the "attr" command to change the
     * "connection.fg" field within each specific edge. This is because, when
     * changing the style attributes of the edge -- for example, edge.style.fill
     * = "#fff" -- when Dracula redraws the edge, more often than not, it
     * creates a new field (edge.connection.bg) to fill the color behind the
     * edge in question. This is important to note because all style changes
     * done outside of this method currently adhere to altering only the
     * edge.connection.fg field. So, if any changes are made to the edges where
     * the edge.connection.bg field is introduced, this WILL NOT clear those
     * changes from the edge's state, and may have to be appended to this code.
     * </p>
     */
    public native void clearEdgeState() /*-{
        var g = this.@synopticgwt.client.model.ModelGraphic::draculaGraph;

        var edges = g.edges;
        for (i = 0; i < edges.length; i++) {
            // Set the edge color back to black,
            // and set the width back to normal.
            edges[i].connection.fg.attr({
                stroke : "#000",
                "stroke-width" : 1
            });
        }
    }-*/;

    /**
     * Highlights a path through the model based on array of edges passed.
     * Changes the edges' styles as to be reversible by the
     * {@code clearEdgeState} method //
     */
    public native void highlightEdges(List<GWTEdge> edges) /*-{
        var g = this.@synopticgwt.client.model.ModelGraphic::draculaGraph;

        // TODO: Refactor this inefficient n^2 loop to loop through just the edges,
        // and use to access the Dracula graph edge instance associated with the
        // GWTEdge directly.

        this.@synopticgwt.client.model.ModelGraphic::clearEdgeState()();
        var modelEdges = g.edges;
        for ( var i = 0; i < modelEdges.length; i++) {
            for ( var j = 0; j < edges.@java.util.List::size()(); j++) {
                var edge = edges.@java.util.List::get(I)(j);
                var sourceNode = edge.@synopticgwt.shared.GWTEdge::getSrc()();
                var sourceHash = sourceNode.@synopticgwt.shared.GWTNode::getPartitionNodeHashCodeStr()();
                var destNode = edge.@synopticgwt.shared.GWTEdge::getDst()();
                var destHash = destNode.@synopticgwt.shared.GWTNode::getPartitionNodeHashCodeStr()();

                // If this edges matches one of the ones that needs to be highlighted,
                // then highlight it by changing it's style attributes.
                if (modelEdges[i].source.id == sourceHash
                        && modelEdges[i].target.id == destHash) {
                    modelEdges[i].connection.fg
                            .attr({
                                stroke : @synopticgwt.client.model.ModelGraphic::HIGHLIGHT_COLOR,
                                "stroke-width" : @synopticgwt.client.model.ModelGraphic::SELECT_STROKE_WIDTH
                            });
                    break;
                }
            }
        }
    }-*/;

    // TODO The following three functions have not been changed to access the
    // appropriate methods and, as such, need to be changed to do so.

    /*
     * A function for clearing the state of the selected nodes. Each node is set
     * back to the default color, border color, and stroke width, and then
     * removed from the set of selected nodes.
     */
    public native void clearSelectedNodes() /*-{
        var selectedDraculaNodes = this.@synopticgwt.client.model.ModelGraphic::selectedDraculaNodes;

        for ( var i in selectedDraculaNodes) {
            selectedDraculaNodes[i]
                    .attr({
                        "fill" : @synopticgwt.client.model.ModelGraphic::DEFAULT_COLOR,
                        "stroke" : "black",
                        "stroke-width" : @synopticgwt.client.model.ModelGraphic::DEFAULT_STROKE_WIDTH
                    });
            removeSelectedNode(parseInt(i));
            delete selectedDraculaNodes[i];
        }
    }-*/;

    private native void setShiftClickNodesState(String color) /*-{
        // Whether or not the node displaying log line is in
        // shift+click set.
        var clickNodeInSet = false;
        var selectedNodeLog = this.@synopticgwt.client.model.ModelGraphic::selectedNodeLog;
        var selectedDraculaNodes = this.@synopticgwt.client.model.ModelGraphic::selectedDraculaNodes;
        var selectedNodeLog = this.@synopticgwt.client.model.ModelGraphic::selectedNodeLog;

        for ( var i in selectedDraculaNodes) {
            if (selectedNodeLog == selectedDraculaNodes[i]) {
                clickNodeInSet = true;
            }
            selectedDraculaNodes[i]
                    .attr({
                        "fill" : @synopticgwt.client.model.ModelGraphic::DEFAULT_COLOR,
                        "stroke" : color,
                        "stroke-width" : @synopticgwt.client.model.ModelGraphic::SELECT_STROKE_WIDTH
                    });
        }
        // Set node to default styling.
        if (!clickNodeInSet) {
            selectedNodeLog
                    .attr({
                        "stroke" : "black",
                        "stroke-width" : @synopticgwt.client.model.ModelGraphic::DEFAULT_STROKE_WIDTH
                    });
        }
        selectedNodeLog = undefined;
    }-*/;

    /*
     * A function that returns true if the rectangle object being passed is
     * currently selected. Returns false if rectangle object is not selected.
     */
    public native boolean isSelectedNode(JavaScriptObject rect) /*-{
        var selectedDraculaNodes = this.@synopticgwt.client.model.ModelGraphic::selectedDraculaNodes;
        for ( var i in selectedDraculaNodes) {
            if (selectedDraculaNodes[i] == rect) {
                return true;
            }
        }
        return false;
    }-*/;

    // TODO Comment and format this monster. Possibly create a separate class,
    // and make sure to cut out ANYTHING that is even remotely unnecessary.

    /**
     * Returns the function necessary for rendering the graph. This currently
     * also contains any sort of event-driven aspects of the graph, including,
     * but not limited to, clicking on nodes, and mousing over nodes, as well as
     * styling code.
     * 
     * @return
     */
    public native JavaScriptObject getCustomRenderer() /*-{
        // provides instructions for how to render a node. accepts the canvas to be
        // drawn on
        // and the node to draw. returns the set of drawn shapes for the node
        // (rectangle and
        // label)

        var renderer = function(canvas, node) {
            var rect;
            
            var initialNodeLabel = @synopticgwt.client.model.ModelGraphic::INITIAL;
            var terminalNodeLabel = @synopticgwt.client.model.ModelGraphic::TERMINAL;
            var allRects = this.@synopticgwt.client.model.ModelGraphic::allRects;
            var selectedNodeLog = this.@synopticgwt.client.model.ModelGraphic::selectedNodeLog;
            var selectedDraculaNodes = this.@synopticgwt.client.model.ModelGraphic::selectedDraculaNodes;
            var mTab = this.@synopticgwt.client.model.ModelGraphic::modelTab;
            
            if (node.label == initialNodeLabel || node.label == terminalNodeLabel) {
                // creates the rectangle to be drawn
                var rect = canvas.rect(node.point[0] - 30, node.point[1] - 13,
                        122, 46).attr({
                    "fill" : @synopticgwt.client.model.ModelGraphic::INIT_TERM_COLOR,
                    "stroke-width" : @synopticgwt.client.model.ModelGraphic::DEFAULT_STROKE_WIDTH,
                    r : "40px"
                });
            } else {
                // creates the rectangle to be drawn
                var rect = canvas.rect(node.point[0] - 30, node.point[1] - 13,
                        122, 46).attr({
                    "fill" : @synopticgwt.client.model.ModelGraphic::DEFAULT_COLOR,
                    "stroke-width" : @synopticgwt.client.model.ModelGraphic::DEFAULT_STROKE_WIDTH,
                    r : "9px"
                });
                // associate label with rectangle object
                rect.label = node.label;
                allRects[allRects.length] = rect;
            }

            // Adds a function to the given rectangle so that, when clicked,
            // the associated event node is "selected" (shown as blue when clicked)
            // and then the log lines associated with the event are shown in the
            // the model tab (grabbed via a RPC).
            //
            // When clicking the same node again, the node stays selected. When
            // clicking
            // a different node, the previous node is deselected, and the new node
            // is
            // selected.
            //
            // The function will also detect shift events, and toggle
            // more than one node if the shift key is being
            // held down. If the node has been clicked without
            // the shift key being held down all nodes except for the node clicked
            // will be deselected. Holding shift and clicking a selected node
            // will deselect it.
            rect.node.onmouseup = function(event) {
                if (node.label != initialNodeLabel && node.label != terminalNodeLabel) {

                    if (!event.shiftKey && selectedNodeLog != rect) {
                        this.@synopticgwt.client.model.ModelGraphic::clearSelectedNodes()();
                        mtab.@synopticgwt.client.model.ModelTab::handleLogRequest(I)(parseInt(node.id));
                    }

                    if (selectedDraculaNodes[node.id] == undefined) {
                        // Node associated with log lines listed is
                        // surrounded by red and thick border.
                        if (event.shiftKey) {
                            rect.attr("fill", @synopticgwt.client.model.ModelGraphic::HIGHLIGHT_COLOR);
                            selectedDraculaNodes[node.id] = rect;
                            mTab.@synopticgwt.client.model.ModelTab::addSelectedNode(I)(parseInt(node.id));
                        } else {
                            // Remove red border from previous node displaying log lines.
                            if (selectedNodeLog != undefined) {
                                selectedNodeLog.attr({
                                    "stroke" : "black",
                                    "stroke-width" : @synopticgwt.client.model.ModelGraphic::DEFAULT_STROKE_WIDTH
                                });
                            }
                            selectedNodeLog = rect;
                            rect.attr({
                                "fill" : @synopticgwt.client.model.ModelGraphic::DEFAULT_COLOR,
                                "stroke" : "red",
                                "stroke-width" : @synopticgwt.client.model.ModelGraphic::SELECT_STROKE_WIDTH
                            });
                        }

                    } else {
                        if (selectedNodeLog == rect) {
                            rect.attr({
                                "fill" : @synopticgwt.client.model.ModelGraphic::DEFAULT_COLOR
                            });
                        } else { // All nodes except for one displaying log lines.
                            rect.attr({
                                "fill" : @synopticgwt.client.model.ModelGraphic::DEFAULT_COLOR,
                                "stroke" : "black",
                                "stroke-width" : @synopticgwt.client.model.ModelGraphic::DEFAULT_STROKE_WIDTH
                            });
                        }
                        delete selectedDraculaNodes[node.id];
                        removeSelectedNode(parseInt(node.id));
                    }
                }
            };

            // On a mouse hover, highlight that node and other nodes
            // that are of the same type.
            rect.node.onmouseover = function(event) {
                if (node.label != initialNodeLabel && node.label != terminalNodeLabel) {
                    for ( var i = 0; i < allRects.length; i++) {
                        var currRect = allRects[i];
                        if (currRect.label == node.label) {
                            currRect.attr("fill", @synopticgwt.client.model.ModelGraphic::HIGHLIGHT_COLOR);
                        }
                    }
                }
            };

            // On a mouse hovering out, un-highlight that node and 
            // other nodes that are of the same type.
            rect.node.onmouseout = function(event) {
                if (node.label != initialNodeLabel && node.label != terminalNodeLabel) {
                    for ( var i = 0; i < allRects.length; i++) {
                        var currRect = allRects[i];
                        // Return to default color if the rectangle is
                        // not currently selected. Highlight if node has
                        // colored border after "View paths".
                        if (!this.@synopticgwt.client.model.ModelGraphic::isSelectedNode(Lcom/google/gwt/core/client/JavaScriptObject;)(currRect)
                                || currRect.attr("stroke") == @synopticgwt.client.model.ModelGraphic::SHIFT_CLICK_BORDER_COLOR) {
                            currRect.attr("fill", @synopticgwt.client.model.ModelGraphic::DEFAULT_COLOR);
                        }
                    }
                }
            };

            text = canvas.text(node.point[0] + 30, node.point[1] + 10,
                    node.label).attr({
                "font-size" : "16px",
            });

            // the Raphael set is obligatory, containing all you want to display
            // draws this node's label
            var set = canvas.set().push(rect).push(text);

            // The text, when clicked should behave as if the rectangle was clicked.
            text.node.onmouseup = rect.node.onmouseup;

            // The text, when hovering over and hovering out should behave the same
            // as the rectangle.
            text.node.onmouseout = rect.node.onmouseout;
            text.node.onmouseover = rect.node.onmouseover;
            return set;

        };

        return renderer;
    }-*/;

    // </JSNI methods>
    // //////////////////////////////////////////////////////////////////////////
}
