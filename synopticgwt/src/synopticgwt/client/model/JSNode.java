package synopticgwt.client.model;

import java.io.Serializable;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * A class used to interface directly with an instance of a dracula graph edge
 * 
 * @author andrew
 * 
 */
public class JSNode extends JavaScriptObject implements Serializable {

    private static final long serialVersionUID = 1L;

    // Default JSO constructor
    protected JSNode() {
    }

    public native final void initRenderer() /*-{
        this.render = function(canvas, node) {
            var rect;
            if (node.label == @synopticgwt.client.model.ModelGraphic::INITIAL
                    || node.label == @synopticgwt.client.model.ModelGraphic::TERMINAL) {
                // creates the rectangle to be drawn
                var rect = canvas
                        .rect(node.point[0] - 30, node.point[1] - 13, 122, 46)
                        .attr(
                                {
                                    "fill" : @synopticgwt.client.model.ModelGraphic::INIT_TERM_COLOR,
                                    "stroke-width" : @synopticgwt.client.model.ModelGraphic::DEFAULT_STROKE_WIDTH,
                                    r : "40px"
                                });
            } else {
                // creates the rectangle to be drawn
                var rect = canvas
                        .rect(node.point[0] - 30, node.point[1] - 13, 122, 46)
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

            // the Raphael set is obligatory, containing all you want to display
            // draws this node's label
            var set = canvas.set().push(rect).push(text);

            return set;
        }
    }-*/;
}
