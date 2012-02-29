package synopticgwt.client.model;

import java.io.Serializable;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * A class used to interface directly with an instance of a dracula graph edge
 * 
 * @author andrew
 */
public class JSNode extends JavaScriptObject implements Serializable {

    private static final long serialVersionUID = 1L;

    // Default JSO constructor
    protected JSNode() {
    }

    /**
     * Initializes the renderer function for the given node. This doesn't
     * necessarily draw the node right away (drawing this node is required in
     * order to instantiate the JSNode fields text and rect, however), but it
     * instantiates the function that allows the node to be drawn.
     */
    public native final void initRenderer() /*-{
        // The nested function lowers memory leaks (allegedly)
        // by passing the instance of this object to
        // the inner function and then returning said function with the 
        // instance inside of the closure.  This mainly makes it easier
        // to define the fields rect and txt within said instance.
        this.render = function(instance) {
            return function(canvas, node) {
                var rect;
                if (node.label == @synopticgwt.client.model.ModelGraphic::INITIAL
                        || node.label == @synopticgwt.client.model.ModelGraphic::TERMINAL) {
                    // creates the rectangle to be drawn
                    var rect = canvas
                            .rect(node.point[0] - 30, node.point[1] - 13, 122,
                                    46)
                            .attr(
                                    {
                                        "fill" : @synopticgwt.client.model.ModelGraphic::INIT_TERM_COLOR,
                                        "stroke-width" : @synopticgwt.client.model.ModelGraphic::DEFAULT_STROKE_WIDTH,
                                        r : "40px"
                                    });
                } else {
                    // creates the rectangle to be drawn
                    var rect = canvas
                            .rect(node.point[0] - 30, node.point[1] - 13, 122,
                                    46)
                            .attr(
                                    {
                                        "fill" : @synopticgwt.client.model.ModelGraphic::DEFAULT_COLOR,
                                        "stroke-width" : @synopticgwt.client.model.ModelGraphic::DEFAULT_STROKE_WIDTH,
                                        r : "9px"
                                    });
                    // associate label with rectangle object
                    rect.label = node.label;
                }

                text = canvas.text(node.point[0] + 30, node.point[1] + 10,
                        node.label).attr({
                    "font-size" : "16px",
                });

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
