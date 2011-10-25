package synopticgwt.client.invariants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;

/* Graphic model for a logged event type */
public class GraphicEvent implements Serializable {

	private static final long serialVersionUID = 1L;
	
	// Raphael paper object
    private GraphicPaper paper;
    // Raphael text objectj
    private JavaScriptObject text;
    // Incident arrows
    private List<GraphicArrow> arrows;

    public GraphicEvent(int x, int y, String event, GraphicPaper paper) {
        this.paper = paper;
        arrows = new ArrayList<GraphicArrow>();
        // TODO: construct text JSO
    }

    // If the GraphicEvent is not visible on the paper, make it visible
    public void show() {
        paper.showElement(text);
    }

    // If the GraphicEvent is visible on the InvariantsGraph, make it invisible
    public void hide() {
        paper.hideElement(text);
    }

    public void addArrow(GraphicArrow arrow) {
        arrows.add(arrow);
    }

    // Need to properly construct array of path objects to modify
    // and plug into var line in lines
    public native void updateMouseover() /*-{
		var text = this.@synopticgwt.client.invariants.GraphicEvent::text;
        
        // Function to execute when the tMiddle label is pointed-to.
        text.mouseover(function(y) {
            return function(e) {
                // y is text
                for ( var line in lines[y.attr('text')]) {
                    lines[y.attr('text')][line].attr({
                        'stroke-width' : '3'
                    });
                    lines[y.attr('text')][line].attr({
                        stroke : lines[y.attr('text')][line]
                                .attr('highlight')
                    });
                    lines[y.attr('text')][line].attr('dest').attr({
                        fill : "black"
                    });
                }
                y.attr({
                    fill : "black"
                });

            };
        }(text));
    }-*/;

    // Need to properly construct array of path objects to modify
    // and plug into var line in lines
    public native void updateMouseout() /*-{
		var text = this.@synopticgwt.client.invariants.GraphicEvent::text;

        // Function to execute when the tMiddle label is not pointed-to.
        text.mouseout(function(y) {
            return function(e) {
                for ( var line in lines[y.attr('text')]) {
                    lines[y.attr('text')][line].attr({
                        'stroke-width' : '1'
                    });
                    lines[y.attr('text')][line].attr({
                        stroke : "grey"
                    });
                    lines[y.attr('text')][line].attr('dest').attr({
                        fill : "grey"
                    });
                }
                y.attr({
                    fill : "grey"
                });
            };
        }(text));
    }-*/;
    
}
