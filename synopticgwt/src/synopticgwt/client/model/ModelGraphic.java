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
    
    private JavaScriptObject draculaGraph;
    
    // The ModelTab that this graphic is associated with.
    private ModelTab modelTab;

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

		// Create the Dracula graph.
		var g = new $wnd.Graph();
		this.@synopticgwt.client.model.ModelGraphic::draculaGraph = g;
		g.edgeFactory.template.style.directed = true;

		// Add each node to the graph.
		for ( var i = 0; i < nodes.@java.util.List::size()(); i++) {
			var node = nodes.@java.util.List::get(I)(i);
			// Store graph state.
			var nodeHashCode = node.@synopticgwt.shared.GWTNode::getPartitionNodeHashCodeStr()();
			var nodeLabel = node.@synopticgwt.shared.GWTNode::toString()();
			$wnd.GRAPH_HANDLER.currentNodes[nodeHashCode] = nodeLabel;

			g.addNode(nodeHashCode, {
				label : nodeLabel,
				render : $wnd.GRAPH_HANDLER.render
			});
		}

		// Add each edge to graph.
		$wnd.GRAPH_HANDLER.currentEdges = [];
		for ( var i = 0; i < edges.@java.util.List::size()(); i++) {
			var edge = edges.@java.util.List::get(I)(i);
			this.@synopticgwt.client.model.ModelGraphic::addEdge(Lsynopticgwt/shared/GWTEdge;Lcom/google/gwt/core/client/JavaScriptObject;)(edge, g);
		}

		// Give stable layout to graph elements.
		var layouter = new $wnd.Graph.Layout.Stable(g, initial, terminal);

		// Render the graph.
		var renderer = new $wnd.Graph.Renderer.Raphael(canvasId, g, width,
				height);

		$wnd.GRAPH_HANDLER.initializeStableIDs(renderer, layouter, g);
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
		$wnd.clearSelectedNodes();

		// update graph and fetch array of new nodes
		var newNodes = this.@synopticgwt.client.model.ModelGraphic::updateRefinedGraph(Ljava/util/List;Ljava/util/List;I)(nodes,edges,refinedNode);

		// fetch the current layouter
		var layouter = $wnd.GRAPH_HANDLER.getLayouter();

		// update each graph element's position, re-assigning a position
		layouter.updateLayout($wnd.GRAPH_HANDLER.getGraph(), newNodes);

		// fetch the renderer
		var renderer = $wnd.GRAPH_HANDLER.getRenderer();

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

		var gh = $wnd.GRAPH_HANDLER;
		// Retrieve the refined node.
		var refinedNode = gh.graph.nodes[splitNodeID];

		// Remove the refined node and all of its edges from the graph.
		gh.graph.removeNode(splitNodeID);
		delete gh.currentNodes[splitNodeID];

		// Tracks which new nodes are added to update edges below.
		var newNodes = [];

		// Loop over all the given nodes, find and add new nodes to the graph
		for ( var i = 0; i < nodes.@java.util.List::size()(); i++) {
			var node = nodes.@java.util.List::get(I)(i);

			var nodeHashCode = node.@synopticgwt.shared.GWTNode::getPartitionNodeHashCodeStr()();
			var nodeLabel = node.@synopticgwt.shared.GWTNode::toString()();
			if (!gh.currentNodes[nodeHashCode]) {
				gh.currentNodes[nodeHashCode] = nodeLabel;
				newNodes[nodeHashCode] = true;
				gh.graph.addNode(nodeHashCode, {
					label : nodeLabel,
					render : gh.render,
					layoutPosX : refinedNode.layoutPosX,
					layoutPosY : refinedNode.layoutPosY
				});
			}
		}

		// re-draw the graph, adding new nodes to the canvas
		gh.rend.draw();

		// loop over all given edges, finding ones connected to the new
		// nodes that need to be added to the graph
		for ( var i = 0; i < edges.@java.util.List::size()(); i++) {
			var edge = edges.@java.util.List::get(I)(i);
			var sourceNode = edge.@synopticgwt.shared.GWTEdge::getSrc()();
			var sourceHash = sourceNode.@synopticgwt.shared.GWTNode::getPartitionNodeHashCodeStr()();
			var destNode = edge.@synopticgwt.shared.GWTEdge::getDst()();
			var destHash = destNode.@synopticgwt.shared.GWTNode::getPartitionNodeHashCodeStr()();

			if (newNodes[sourceHash] || newNodes[destHash]) {
				this.@synopticgwt.client.model.ModelGraphic::addEdge(Lsynopticgwt/shared/GWTEdge;Lcom/google/gwt/core/client/JavaScriptObject;)(edge, gh.graph);
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
		$wnd.GRAPH_HANDLER.currentEdges.push({
			"edge" : newEdge,
			"style" : style
		});
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
    public native void updateNodesBorder(String color) /*-{
		$wnd.setShiftClickNodesState(color);
    }-*/;

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
		var g = $wnd.GRAPH_HANDLER.getGraph();

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
		var g = $wnd.GRAPH_HANDLER.getGraph();

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
					modelEdges[i].connection.fg.attr({
						stroke : $wnd.HIGHLIGHT_COLOR,
						"stroke-width" : $wnd.SELECT_STROKE_WIDTH
					});
					break;
				}
			}
		}
    }-*/;

    // </JSNI methods>
    // //////////////////////////////////////////////////////////////////////////
}
