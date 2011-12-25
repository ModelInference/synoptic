package synopticgwt.client.model;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Used to create the graphic representing the Synoptic model.
 */
public class ModelGraphic {
    // //////////////////////////////////////////////////////////////////////////
    // JSNI methods -- JavaScript Native Interface methods. The method body of
    // these calls is pure JavaScript.

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

        // This keeps track of the dracula nodes so that selected nodes
        // can be monitored.
        $wnd.graphNodes = [];

        // Export the handleLogRequest globally.
        $wnd.viewLogLines = function(id) {
            modelTab.@synopticgwt.client.model.ModelTab::handleLogRequest(I)(id);
        };

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
        for ( var i = 0; i < edges.length; i += 3) {
            // edges[i]: source, edges[i+1]: target, edges[i+2]: weight for the label.
            g.addEdge(edges[i], edges[i + 1], {
                label : edges[i + 2],
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

        // Clear the graph nodes.
        $wnd.graphNodes = [];

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

    // </JSNI methods>
    // //////////////////////////////////////////////////////////////////////////
}
