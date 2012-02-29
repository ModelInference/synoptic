package synopticgwt.client.model;

import java.io.Serializable;

import synopticgwt.shared.GWTEdge;
import synopticgwt.shared.GWTNode;

import com.google.gwt.core.client.JavaScriptObject;

public class JSGraph extends JavaScriptObject implements Serializable {

    private static final long serialVersionUID = 1L;

    // Default JSO Constructor
    protected JSGraph() {
    }

    /**
     * Creates and returns a JSGraph instance.
     */
    public static native JSGraph create() /*-{
        var g = new $wnd.Graph();
        g.edgeFactory.template.style.directed = true;
        return g;
    }-*/;

    /**
     * Returns a JSNode instance, else null if the given node is contained
     * within the graph.
     * 
     * @param nodeID
     *            The ID of the node in question.
     * @return A JSNode reference if found, null otherwise.
     */
    public native final JSNode getNode(int nodeID) /*-{
        return this[nodeID];
    }-*/;

    /**
     * Adds a GWTNode to the graph, and then returns a reference to said node.
     * 
     * TODO Add renderer function.
     */
    public native final JSNode addNode(GWTNode node) /*-{
        var nodeHashCode = node.@synopticgwt.shared.GWTNode::getPartitionNodeHashCodeStr()();
        var nodeLabel = node.@synopticgwt.shared.GWTNode::toString()();

        var jsNode = this.addNode(nodeHashCode, {
            label : nodeLabel,
        //  render : this.@synopticgwt.client.model.ModelGraphic::getNodeRenderer()()
        });
        return jsNode;
    }-*/;

    /**
     * Adds and returns an instance to the graph.
     * 
     * TODO Make some test as to whether to show counts or weights.
     */
    public native final JSEdge addEdge(GWTEdge edge) /*-{

        var sourceNode = edge.@synopticgwt.shared.GWTEdge::getSrc()();
        var source = sourceNode.@synopticgwt.shared.GWTNode::getPartitionNodeHashCodeStr()();
        var destNode = edge.@synopticgwt.shared.GWTEdge::getDst()();
        var dest = destNode.@synopticgwt.shared.GWTNode::getPartitionNodeHashCodeStr()();
        var transProb = edge.@synopticgwt.shared.GWTEdge::getWeightStr()();
        var transCount = edge.@synopticgwt.shared.GWTEdge::getCountStr()();
        //        var mTab = this.@synopticgwt.client.model.ModelGraphic::modelTab;
        //        var showCounts = mTab.@synopticgwt.client.model.ModelTab::getShowEdgeCounts()();
        //        if (showCounts) {
        //            labelVal = transCount;
        //        } else {
        //            labelVal = transProb;
        //        }

        style = {
            label : transProb,
            labelProb : transProb,
            labelCount : transCount
        };

        this.addEdge(source, dest, style);
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
        // Determinize Math.random() calls for deterministic graph layout. 
        // Relies on seedrandom.js
        $wnd.Math.seedrandom($wnd.randSeed);

        this.renderer.width = width;
        this.renderer.height = height;
        this.renderer.r.setSize(width, height);

        // This is redundant code, but unfortunatley, overlay
        // methods cannot be called from within overlay methods.
        this.layouter.updateLayout(this, this.nodes);
        this.renderer.draw();
    }-*/;

    /**
     * Draws the graph. Must be called after the nodes and edges have all been
     * added.
     */
    public native final void draw(int width, int height, String canvasId,
            String initial, String terminal) /*-{

        

        // Give stable layout to graph elements.
        var layouter = new $wnd.Graph.Layout.Stable(this, initial, terminal);
        this.layouter = layouter;

        // Render the graph.
        var renderer = new $wnd.Graph.Renderer.Raphael(canvasId, this, width,
                height);
        this.renderer = renderer;
    }-*/;
}
