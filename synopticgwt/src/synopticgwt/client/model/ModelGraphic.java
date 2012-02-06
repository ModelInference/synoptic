package synopticgwt.client.model;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Used to create the graphic representing the Synoptic model.
 */
public class ModelGraphic {
    // //////////////////////////////////////////////////////////////////////////
    // JSNI methods -- JavaScript Native Interface methods. The method body of
    // these calls is pure JavaScript.

    public static native void useProbEdgeLabels() /*-{
        // TODO
    }-*/;

    public static native void useCountEdgeLabels() /*-{
        // TODO: work in progress.
        edges = $wnd.GRAPH_HANDLER.getCurrentEdges();
        for ( var i = 0; i < edges.length; i += 1) {
            edges[i].style.label = edges[i].style.labelCount;
            $wnd.jQuery.extend(edges[i].edge.style, edges[i].style);
        }
        $wnd.GRAPH_HANDLER.getLayouter().layout()
        $wnd.GRAPH_HANDLER.getRenderer().draw();
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
    public static native void createGraph(ModelTab modelTab,
            JavaScriptObject nodes, JavaScriptObject edges, int width,
            int height, String canvasId, String initial, String terminal) /*-{

        // Define all global functions.
        @synopticgwt.client.model.ModelGraphic::defineGlobalFunctions(Lsynopticgwt/client/model/ModelTab;)(modelTab);

        // Create the graph.
        var g = new $wnd.Graph();
        g.edgeFactory.template.style.directed = true;

        // Add each node to graph.
        for ( var i = 0; i < nodes.length; i += 2) {
            g.addNode(nodes[i], {
                label : nodes[i + 1],
                render : $wnd.GRAPH_HANDLER.render
            });
        }

        // Add each edge to graph.
        $wnd.GRAPH_HANDLER.currentEdges = [];
        for ( var i = 0; i < edges.length; i += 4) {
            // edges[i]: source, edges[i+1]: target, edges[i+2]: weight for the label.
            style = {
                label : edges[i + 2],
                labelProb : edges[i + 2],
                labelCount : edges[i + 3],
            };
            edge = g.addEdge(edges[i], edges[i + 1], style);
            $wnd.GRAPH_HANDLER.currentEdges.push({
                "edge" : edge,
                "style" : style
            });
        }
        // Give stable layout to graph elements.
        var layouter = new $wnd.Graph.Layout.Stable(g, initial, terminal);

        // Render the graph.
        var renderer = new $wnd.Graph.Renderer.Raphael(canvasId, g, width,
                height);

        // Store graph state.
        $wnd.GRAPH_HANDLER.initializeStableIDs(nodes, edges, renderer,
                layouter, g);
    }-*/;

    private static native void defineGlobalFunctions(ModelTab modelTab) /*-{
        // Determinize Math.random() calls for deterministic graph layout. Relies on seedrandom.js
        $wnd.Math.seedrandom($wnd.randSeed);

        // Export the handleLogRequest globally.
        $wnd.viewLogLines = function(id) {
            modelTab.@synopticgwt.client.model.ModelTab::handleLogRequest(I)(id);
        };

        // Determines if the infoPanel's paths table is visible.
        $wnd.infoPanelPathsVisible = function() {
            return modelTab.@synopticgwt.client.model.ModelTab::pathsTableIsVisible()();
        }

        // Export global add/remove methods for selected nodes (moving 
        // nodes to model tab).
        $wnd.addSelectedNode = function(id) {
            modelTab.@synopticgwt.client.model.ModelTab::addSelectedNode(I)(id);
        };

        $wnd.removeSelectedNode = function(id) {
            modelTab.@synopticgwt.client.model.ModelTab::removeSelectedNode(I)(id);
        };
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
    public static native void createChangingGraph(JavaScriptObject nodes,
            JavaScriptObject edges, int refinedNode, String canvasId) /*-{

        // Determinize Math.random() calls for deterministic graph layout. Relies on seedrandom.js
        $wnd.Math.seedrandom($wnd.randSeed);

        // Clear the selected nodes from the graph's state.
        $wnd.clearSelectedNodes();

        // update graph and fetch array of new nodes
        var newNodes = $wnd.GRAPH_HANDLER.updateRefinedGraph(nodes, edges,
                refinedNode);

        // fetch the current layouter
        var layouter = $wnd.GRAPH_HANDLER.getLayouter();

        // update each graph element's position, re-assigning a position
        layouter.updateLayout($wnd.GRAPH_HANDLER.getGraph(), newNodes);

        // fetch the renderer
        var renderer = $wnd.GRAPH_HANDLER.getRenderer();

        // re-draw the graph, animating transitions from old to new position
        renderer.draw();
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
    public static native void resizeGraph(int width, int height) /*-{
        // Determinize Math.random() calls for deterministic graph layout. Relies on seedrandom.js
        $wnd.Math.seedrandom($wnd.randSeed);

        // Get the current layout so it can be updated.
        var layouter = $wnd.GRAPH_HANDLER.getLayouter();

        // Update the layout for all nodes.
        layouter.updateLayout($wnd.GRAPH_HANDLER.getGraph(), $wnd.GRAPH_HANDLER
                .getCurrentNodes());

        // Grab a pointer to the current renderer.
        var rend = $wnd.GRAPH_HANDLER.getRenderer();

        // Change the appropriate height/width of the div.
        rend.width = width;
        rend.height = height;

        // Change the width/height of the Raphael canvas.
        rend.r.setSize(width, height);

        // Draw the new graph with all of the repositioned nodes.
        rend.draw();
    }-*/;

    // For all selected nodes in model, change their border to given color.
    public static native void updateNodesBorder(String color) /*-{
        $wnd.setShiftClickNodesState(color);
    }-*/;

    /**
     * Clears the state of the edges in the graph, but does not redraw the
     * graph. this has to be done after this method is called (and any
     * subsequent alterations to the graph that may have occurred thenceforth).
     */
    public static native void clearEdgeState() /*-{
        var g = $wnd.GRAPH_HANDLER.getGraph();

        var edges = g.edges;
        for (i = 0; i < edges.length; i++) {
            // Set the fill to none so it cannot be
            // seen.
            $wnd.console.log(edges[i]);
            edges[i].connection.fg.attr({
                stroke : "#000"
            });
        }
    }-*/;

    /**
     * Highlights a path through the model based on array of edges passed TODO
     * Clear the previous state of the model before highlighting more edges.
     */
    public static native void highlightEdges(JavaScriptObject edges) /*-{
        var g = $wnd.GRAPH_HANDLER.getGraph();

        @synopticgwt.client.model.ModelGraphic::clearEdgeState()();
        var modelEdges = g.edges;
        for ( var i = 0; i < modelEdges.length; i++) {
            for ( var j = 0; j < edges.length; j += 4) {
                // If this edges matches one of the ones that needs to be highlighted,
                // then replace it with the new edge.
                if (modelEdges[i].source.id == edges[j]
                        && modelEdges[i].target.id == edges[j + 1]) {
                    modelEdges[i].connection.fg.attr({
                        stroke : "#56f"
                    });
                    break;
                }
            }
        }
    }-*/;

    // </JSNI methods>
    // //////////////////////////////////////////////////////////////////////////
}
