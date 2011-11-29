package synopticgwt.client.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;

import synopticgwt.client.util.JsniUtil;
import synopticgwt.shared.GWTEdge;
import synopticgwt.shared.GWTGraph;

public class DraculaGraph implements Serializable {
    
    // BIG TODO:  Most of this code is directly copied/pasted
    // from the graph_handler.js file and needs to be
    // obsoleted by this.
    //
    // Most (all?) of this code won't work yet, and needs
    // to use as little direct JSNI stuff as possible.
    // basically sketch code.
    
    private static final long serialVersionUID = 1L;
    
    public static final String DEFAULT_NODE_COLOR = "#808080";

    // The selected graph nodes, and Raphael
    // rectangle objects, respectively.
    private List<JavaScriptObject> selectedNodes;
    private List<JavaScriptObject> selectedRects;
    private List<JavaScriptObject> jsNodes;

    // The instance of the graph.
    private JavaScriptObject graph;

    // The object for designating the graph's
    // layout. Responsible for creating animations,
    // figuring out where nodes should be drawn, etc.
    private JavaScriptObject layouter;

    // This renders the nodes and the graph.
    private JavaScriptObject renderer;

    public DraculaGraph(ModelTab modelTab, GWTGraph gwtGraph, String canvasId) {
        selectedNodes = new ArrayList<JavaScriptObject>();
        selectedRects = new ArrayList<JavaScriptObject>();

        // Create the list of graph node labels and their Ids.
        HashMap<Integer, String> gwtNodes = gwtGraph.getNodes();
        JavaScriptObject nodes = getJSNodeArray(gwtNodes);
        JavaScriptObject edges = getJSEdgeArray(gwtGraph.getEdges());
        instantiateJSGraph(nodes, edges, canvasId);
    }

    private native void instantiateJSGraph(JavaScriptObject nodes,
            JavaScriptObject edges, String canvasId) /*-{
		
		// The TODO: to end all TODO:
		/////////  These methods need to be accessing Java objects
		// and pushing these things into the array.
		// need to be HashMaps or something.
		// 
		// Add object of some sort that is an Edge (like GWTEdge, which
		// might just need to be merged into this code to support 
		// JSNI methods).
		//
		
		//////// MAKE THIS A METHOD ////////
		
		var graph = this.@synopticgwt.client.model.DraculaGraph::graph;
		graph = $wnd.Graph();
		
		///////////////////////////////////

        
        /////////// MAKE THIS A METHOD ////
        
		// Add each node to graph.
		for ( var i = 0; i < nodes.length; i += 2) {
			graph.addNode(nodes[i], {
				label : nodes[i + 1],
				render : @synopticgwt.client.model.DraculaNodeRenderer::getRendererFunction()
			});
		}
        /////////////////////////////////////

        
        ///////////// AND THIS ////////////
		// Add each edge to graph.
		for ( var i = 0; i < edges.length; i += 3) {
			// edges[i]: source, edges[i+1]: target, edges[i+2]: weight for the label.
			g.addEdge(edges[i], edges[i + 1], {
				label : edges[i + 2],
			});
		}
        ///////////////////////////////////

        
        
        /////////////////////////////////////
        //      POSSIBLY MAKE THESE OBJECTS
		// Give stable layout to graph elements.
		var layouter = new $wnd.Graph.Layout.Stable(g, initial, terminal);

		// Render the graph.
		var renderer = new $wnd.Graph.Renderer.Raphael(canvasId, g, width,
				height);
        ////
        /////////////////////////////////////


        //////////////////////////////////////
        /// This probably doesn't need to be here.
		// Store graph state.
		$wnd.GRAPH_HANDLER.initializeStableIDs(nodes, edges, renderer,
				layouter, g);
		//
	    //////////////////////////////////////
	    
	    
    }-*/;

    /**
     * Creates and returns a JavaScriptObject array which contains all of the
     * key/value pairs in a HashMap TODO: Come up with a better implementation
     * than this.
     * 
     * @param gwtNodes
     * @return
     */
    private JavaScriptObject getJSNodeArray(HashMap<Integer, String> gwtNodes) {
        JavaScriptObject nodes = JavaScriptObject.createArray();
        for (Integer key : gwtNodes.keySet()) {
            JsniUtil.pushArray(nodes, key.toString());
            JsniUtil.pushArray(nodes, gwtNodes.get(key));
        }

        return nodes;
    }

    /**
     * Returns a JavaScriptObject array containing all of the values for the
     * given edges of a graph.
     * 
     * @param edges
     * @return
     */
    private JavaScriptObject getJSEdgeArray(Collection<GWTEdge> edges) {
        // Create the list of edges, where two consecutive node Ids is an edge.
        JavaScriptObject jsEdges = JavaScriptObject.createArray();
        for (GWTEdge edge : edges) {
            JsniUtil.pushArray(jsEdges, ((Integer) edge.getSrc()).toString());
            JsniUtil.pushArray(jsEdges, ((Integer) edge.getDst()).toString());

            // This contains the edge's weight.
            JsniUtil.pushArray(jsEdges, ((Double) edge.getWeight()).toString());
        }
        return jsEdges;
    }

    /**
     * This should clear all nodes that have been selected on the graph.
     */
    public void clearSelectedNodes() {
        // TODO: Make this clear the selected nodes and
        // the selected rects so that their styling and
        // such can be upgraded properly.
    }

    public native void render() /*-{
        // TODO: This should handle setting up rendering for the graph.
    }-*/;
}
