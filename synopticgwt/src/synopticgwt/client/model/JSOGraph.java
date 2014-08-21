package synopticgwt.client.model;

import java.util.Collection;

import synopticgwt.shared.GWTEdge;
import synopticgwt.shared.GWTNode;

import com.google.gwt.core.client.JavaScriptObject;

public class JSOGraph extends JavaScriptObject {

    // Default JSO Constructor
    protected JSOGraph() {
    }

    /**
     * Removes the node with the corresponding ID from the graph (and does
     * nothing if it doesn't exist).
     * 
     * @param nodeID
     *            The id of the node to be removed
     */
    public native final void removeNode(GWTNode node) /*-{
        this
                .removeNode(node.@synopticgwt.shared.GWTNode::getPartitionNodeHashCode()());
    }-*/;

    /**
     * @param node
     *            The node to be looked up within the graph.
     * @return True if there is such a node, false otherwise.
     */
    public final boolean containsNode(GWTNode node) {
        return this.getNode(node) != null;
    }

    /**
     * Creates and returns a JSGraph instance.
     */
    public static native JSOGraph create() /*-{
        var g = new $wnd.Graph();
        g.edgeFactory.template.style.directed = true;
        return g;
    }-*/;

    /**
     * Returns a JSONode instance, else null if the given node is contained
     * within the graph.
     * 
     * @param node
     *            The the node in question.
     * @return A JSONode reference if found, null otherwise.
     */
    public native final JSONode getNode(GWTNode node) /*-{
        return this.nodes[node.@synopticgwt.shared.GWTNode::getPartitionNodeHashCode()()];
    }-*/;

    /**
     * Adds a GWTNode to the graph, and then returns a reference to said node.
     */
    public native final JSONode addNode(GWTNode node) /*-{
        var nodeHashCode = node.@synopticgwt.shared.GWTNode::getPartitionNodeHashCodeStr()();
        var nodeLabel = node.@synopticgwt.shared.GWTNode::toString()();

        var jsNode = this.addNode(nodeHashCode, {
            label : nodeLabel
        });
        return jsNode;
    }-*/;

    /**
     * Adds a GWTNode to the graph at the specified position.
     */
    public native final JSONode addNode(GWTNode node, double x, double y) /*-{
        var nodeHashCode = node.@synopticgwt.shared.GWTNode::getPartitionNodeHashCodeStr()();
        var nodeLabel = node.@synopticgwt.shared.GWTNode::toString()();

        var jsNode = this.addNode(nodeHashCode, {
            label : nodeLabel,
            layoutPosX : x,
            layoutPosY : y
        });
        return jsNode;
    }-*/;

    /**
     * Adds an edge to the graph and returns an instance of said edge.
     * 
     * @param edge
     *            The edge to be added to the graph.
     * 
     * @param edgeLabelType
     *            The type of label to use on this edge.
     */
    public native final JSOEdge addEdge(GWTEdge edge,
            JSGraph.EdgeLabelType labelType) /*-{
        var sourceNode = edge.@synopticgwt.shared.GWTEdge::getSrc()();
        var source = sourceNode.@synopticgwt.shared.GWTNode::getPartitionNodeHashCodeStr()();
        var destNode = edge.@synopticgwt.shared.GWTEdge::getDst()();
        var dest = destNode.@synopticgwt.shared.GWTNode::getPartitionNodeHashCodeStr()();
        var transProb = edge.@synopticgwt.shared.GWTEdge::getWeightStr()();
        var transCount = edge.@synopticgwt.shared.GWTEdge::getCountStr()();
        var label = @synopticgwt.client.model.JSGraph.EdgeLabelType::getEdgeLabelString(Lsynopticgwt/shared/GWTEdge;Lsynopticgwt/client/model/JSGraph$EdgeLabelType;)(edge, labelType)
        style = {
            "label" : label,
            labelProb : transProb,
            labelCount : transCount
        };

        var jsEdge = this.addEdge(source, dest, style);
        return jsEdge;
    }-*/;

    /**
     * Draws the graph after updating the parameters related to the graph's
     * size. Must be called after the nodes and edges have all been added, and
     * the new edges to be redrawn have been determined.
     */
    public final native void reDraw(Collection<GWTNode> newNodes) /*-{
        // Draw once where all the nodes are currently drawn, then
        // update the layout for the new nodes that have been added.
        this.renderer.draw();
        var nodes = [];
        var iterator = newNodes.@java.util.Collection::iterator()();
        while (iterator.@java.util.Iterator::hasNext()()) {
            var gwtNode = iterator.@java.util.Iterator::next()();
            nodes[gwtNode.@synopticgwt.shared.GWTNode::getPartitionNodeHashCode()()] = true;
        }

        this.layouter.updateLayout(this, nodes);
        this.renderer.draw();
    }-*/;

    /**
     * Draws the graph after updating the parameters related to the graph's
     * size. Must be called after the nodes and edges have all been added. It is
     * also recommended to only call this after changing the size of the canvas
     * on which this graph has been drawn.
     * 
     * @param width
     *            The width of the canvas
     * @param height
     *            The height of the canvas
     */
    public native final void reDraw(int width, int height) /*-{
        this.renderer.width = width;
        this.renderer.height = height;
        this.renderer.r.setSize(width, height);

        // This is redundant code, but unfortunatley, overlay
        // methods cannot be called from within overlay methods.
        this.layouter.updateLayout(this, this.nodes);
        this.renderer.draw();
    }-*/;

    /**
     * @return The height of the canvas on which this graph is drawn.
     */
    public native final int getCanvasHeight() /*-{
        return this.renderer.height;
    }-*/;

    /**
     * @return The width of the canvas on which this graph is drawn.
     */
    public native final int getCanvasWidth() /*-{
        return this.renderer.width;
    }-*/;

    /**
     * Draws the graph. Must be called after the nodes and edges have all been
     * added.
     */
    public native final void draw(int width, int height, String canvasId,
            String initial, String terminal) /*-{
        // Determinize Math.random() calls for deterministic graph layout. 
        // Relies on seedrandom.js
        $wnd.Math.seedrandom($wnd.randSeed);

        // Give stable layout to graph elements.
        var layouter = new $wnd.Graph.Layout.Stable(this, initial, terminal);
        this.layouter = layouter;

        // Render the graph.
        var renderer = new $wnd.Graph.Renderer.Raphael(canvasId, this, width,
                height);
        this.renderer = renderer;
    }-*/;
}
