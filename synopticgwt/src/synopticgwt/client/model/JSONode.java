package synopticgwt.client.model;

import com.google.gwt.core.client.JavaScriptObject;

import synopticgwt.client.util.MouseEventHandler;

/**
 * A class used to interface directly with an instance of a dracula graph edge
 * 
 * @author andrew
 */
public class JSONode extends JavaScriptObject {

    private static final int RECT_WIDTH = 122;

    // The minimum font size for the text graphic
    // TODO: Experiment to find a good value for this.
    private static final int MIN_FONT_SIZE = 11;

    // Default font size for the node text label.
    // NOTE: Whenever setting the JS font size, make sure
    // to add "px" to the end. This is an integer solely
    // to make comparisons simpler.
    private static final int DEFAULT_FONT_SIZE = 16;

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
    private native final void setMouseover(MouseEventHandler<JSONode> mOver) /*-{
        this.onmouseover = function(hoverable, obj) {
            return function(e) {
                hoverable.@synopticgwt.client.util.MouseEventHandler::mouseover(Ljava/lang/Object;)(obj);
            };
        }(mOver, this);
    }-*/;

    /**
     * Registers hover mouseout with the JSONode
     * 
     * @param hover
     *            object with java level mouseout function
     */
    private native final void setMouseout(MouseEventHandler<JSONode> mOut) /*-{
        this.onmouseout = function(hoverable, obj) {
            return function(e) {
                hoverable.@synopticgwt.client.util.MouseEventHandler::mouseout(Ljava/lang/Object;)(obj);
            };
        }(mOut, this);
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
     * Attaches the renderer function to given node. This doesn't necessarily
     * draw the node right away (drawing this node is required in order to
     * instantiate the JSONode fields text and rect, however), but it
     * instantiates the function that allows the node to be drawn.
     */
    public native final void attachRenderer() /*-{
        this.render = function(canvas, node) {
            var rect;

            if (this.label == @synopticgwt.client.model.JSGraph::INITIAL
                    || this.label == @synopticgwt.client.model.JSGraph::TERMINAL) {
                // creates the rectangle to be drawn
                rect = canvas
                        .rect(node.point[0] - 30, node.point[1] - 13, 122, 46)
                        .attr(
                                {
                                    "fill" : @synopticgwt.client.model.JSGraph::INIT_TERM_COLOR,
                                    "stroke-width" : @synopticgwt.client.model.JSGraph::DEFAULT_STROKE_WIDTH,
                                    r : "40px"
                                });
            } else {
                // creates the rectangle to be drawn
                rect = canvas
                        .rect(node.point[0] - 30, node.point[1] - 13, 122, 46)
                        .attr(
                                {
                                    "fill" : @synopticgwt.client.model.JSGraph::DEFAULT_COLOR,
                                    "stroke-width" : @synopticgwt.client.model.JSGraph::DEFAULT_STROKE_WIDTH,
                                    r : "9px"
                                });
            }

            var text = canvas.text(node.point[0] + 30, node.point[1] + 10,
                    node.label).attr({
                "font-size" : @synopticgwt.client.model.JSONode::DEFAULT_FONT_SIZE + "px",
            });

            // Scale the text if it doesn't fit well.
            if (text.getBBox().width > @synopticgwt.client.model.JSONode::RECT_WIDTH) {
                // Keep shrinking the text until it fits, then set the node's field to the size
                // and whether it should be shortened so this doesn't ever have to be run again.
                var maxWidth = @synopticgwt.client.model.JSONode::RECT_WIDTH;
                var fontSize = parseInt(text.attrs['font-size']);
                while (fontSize > @synopticgwt.client.model.JSONode::MIN_FONT_SIZE) {
                    fontSize -= 1;
                    text.attr({
                        'font-size' : fontSize + "px"
                    });
                    
                    // If the text is now small enough to fit, then exit the loop.
                    if (text.getBBox().width < maxWidth) {
                        break;
                    }
                }

                // If the text is at the min font size and it STILL doesn't fit, then
                // make the text small enough to fit within the rectangle, and set it to the default
                // font size again.
                if (text.getBBox().width > maxWidth) {
                    var newText = text.attrs['text'];
                    
                    // Set the text to be the first three character followed by ellipses, followed by the last three
                    // letters.
                    newText = newText.substring(0, 3) + " . . . " + newText.substring(newText.length - 3);
                    text.attr({
                        "text" : newText,
                        "font-size" : @synopticgwt.client.model.JSONode::DEFAULT_FONT_SIZE + "px"
                    });
                }
            }

            // For some strange reason, setting the rect and text object
            // only once solves any event-related issues when redrawing
            // the graph.  Make sure not to remove this unless you know
            // what you're doing!
            if (!this.rect && !this.text) {
                this.text = text;
                this.rect = rect;
            }

            // Attach the instances event handlers to the actual renderers.
            // may take up a little more memory as there is duplication
            // (one function in the instance, and one in the text/rect), but
            // it allows the event handlers to be attached before the node
            // is actually drawn.
            rect.node.onmouseout = text.node.onmouseout = this.onmouseout;
            rect.node.onmouseup = text.node.onmouseup = this.onmouseup;
            rect.node.onmouseover = text.node.onmouseover = this.onmouseover;

            // the Raphael set is obligatory, containing all you want to display
            // draws this node's label
            var set = canvas.set().push(rect).push(text);

            return set;
        };
    }-*/;
}
