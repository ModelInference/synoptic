package synopticgwt.client.model;

import synopticgwt.client.util.MouseEventHandler;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * A class used to interface directly with an instance of a dracula graph edge
 * 
 * @author andrew
 * 
 */
public class JSONode extends JavaScriptObject {

    // Default JSO constructor
    protected JSONode() {
    }

    public native final int getNodeID() /*-{
        return parseInt(this.id);
    }-*/;

    public native final String getEventType() /*-{
        return this.label;
    }-*/;

    public native final double getLayoutPosX() /*-{
        return this.layoutPosX;
    }-*/;

    public native final double getLayoutPosY() /*-{
        return this.layoutPosY;
    }-*/;

    /**
     * Sets the style of the node to the following
     * 
     * @param fillColor
     *            The color with which the node will be filled.
     * @param borderColor
     *            The color to which the border will be set.
     * @param borderWidth
     *            The width of the border.
     */
    public native final void setStyle(String fillColor, String borderColor,
            int borderWidth) /*-{
        this.rect.attr({
            "stroke" : borderColor,
            "stroke-width" : borderWidth,
            "fill" : fillColor
        });
    }-*/;

    /**
     * Sets the style of the node to the following
     * 
     * @param borderColor
     *            The color to which the border will be set.
     * @param borderWidth
     *            The width of the border.
     */
    public native final void setStyle(String borderColor, int borderWidth) /*-{
        this.rect.attr({
            "stroke" : borderColor,
            "stroke-width" : borderWidth,
        });
    }-*/;

    /**
     * Sets the fill color of the node to the following
     * 
     * @param fillColor
     *            The color with which the node will be filled.
     */
    public native final void setStyle(String fillColor) /*-{
        this.rect.attr({
            "fill" : fillColor
        });
    }-*/;

    /**
     * Attaches an event handler to the graphical representation of this object,
     * which fires when the object is moused over, clicked on, or when the mouse
     * exits.
     * 
     * @param handler
     *            The event handler.
     */
    public final void attachEventHandler(MouseEventHandler<JSONode> handler) {
        this.setMouseout(handler);
        this.setMouseover(handler);
        this.setOnClick(handler);
    }

    /**
     * Registers hover mouseover with the JSONode
     * 
     * @param hover
     *            object with java level mouseover function
     */
    private native final void setMouseover(MouseEventHandler<JSONode> hover) /*-{
        this.onmouseover = function(hoverable, obj) {
            return function(e) {
                hoverable.@synopticgwt.client.util.MouseEventHandler::mouseover(Ljava/lang/Object;)(obj);
            };
        }(hover, this);
    }-*/;

    /**
     * Registers hover mouseout with the JSONode
     * 
     * @param hover
     *            object with java level mouseout function
     */
    private native final void setMouseout(MouseEventHandler<JSONode> hover) /*-{
        this.onmouseout = function(hoverable, obj) {
            return function(e) {
                hoverable.@synopticgwt.client.util.MouseEventHandler::mouseout(Ljava/lang/Object;)(obj);
            };
        }(hover, this);
    }-*/;

    /**
     * Registers a click event with the JSONode.
     * 
     * @param click
     *            object with java level onclick function
     */
    private native final void setOnClick(MouseEventHandler<JSONode> click) /*-{
        this.onmouseup = function(clickable, obj) {
            return function(e) {
                clickable.@synopticgwt.client.util.MouseEventHandler::onclick(Ljava/lang/Object;Z)(obj, e.shiftKey);
            };
        }(click, this);
    }-*/;

    /**
     * Attaches the renderer function for to given node. This doesn't
     * necessarily draw the node right away (drawing this node is required in
     * order to instantiate the JSONode fields text and rect, however), but it
     * instantiates the function that allows the node to be drawn.
     */
    public native final void attachRenderer() /*-{
        this.render = function(instance) {
            return function(canvas, node) {
                var rect;
                if (node.label == @synopticgwt.client.model.JSGraph::INITIAL
                        || node.label == @synopticgwt.client.model.JSGraph::TERMINAL) {
                    // creates the rectangle to be drawn
                    var rect = canvas
                            .rect(node.point[0] - 30, node.point[1] - 13, 122,
                                    46)
                            .attr(
                                    {
                                        "fill" : @synopticgwt.client.model.JSGraph::INIT_TERM_COLOR,
                                        "stroke-width" : @synopticgwt.client.model.JSGraph::DEFAULT_STROKE_WIDTH,
                                        r : "40px"
                                    });
                } else {
                    // creates the rectangle to be drawn
                    var rect = canvas
                            .rect(node.point[0] - 30, node.point[1] - 13, 122,
                                    46)
                            .attr(
                                    {
                                        "fill" : @synopticgwt.client.model.JSGraph::DEFAULT_COLOR,
                                        "stroke-width" : @synopticgwt.client.model.JSGraph::DEFAULT_STROKE_WIDTH,
                                        r : "9px"
                                    });
                    // associate label with rectangle object
                    rect.label = node.label;
                }

                text = canvas.text(node.point[0] + 30, node.point[1] + 10,
                        node.label).attr({
                    "font-size" : "16px",
                });

                // Attach the instances event handlers to the actual renderers.
                // may take up a little more memory as there is duplication
                // (one function in the instance, and one in the text/rect), but
                // it allows the event handlers to be attached before the node
                // is actually drawn.
                rect.node.onmouseup = text.node.onmouseup = instance.onmouseup;
                rect.node.onmouseover = text.node.onmouseover = instance.onmouseover;
                rect.node.onmouseout = text.node.onmouseout = instance.onmouseout;

                instance.rect = rect;
                instance.text = text;

                // the Raphael set is obligatory, containing all you want to display
                // draws this node's label
                var set = canvas.set().push(rect).push(text);

                return set;
            }
        }(this);
    }-*/;
}
