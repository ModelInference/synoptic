package synopticgwt.client.invariants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;

/* Graphic model for a logged event type */
public class GraphicEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    // Raphael paper object
    private JavaScriptObject paper;
    // Raphael text object
    private JavaScriptObject labelText;
    // Incident relations
    private List<GraphicInvariant> invariants;

    private int labelXCoord;
    private int labelYCoord;

    public GraphicEvent(int x, int y, int fontSize, String event,
            JavaScriptObject paper) {
        this.paper = paper;
        this.labelXCoord = x;
        this.labelYCoord = y;
        invariants = new ArrayList<GraphicInvariant>();
        labelText = constructText(x, y, fontSize, InvariantsGraph.DEFAULT_FILL,
                event);
        setMouseover();
        setMouseout();
    }

    public native JavaScriptObject constructText(int x, int y, int fontSize,
            String fillColor, String text) /*-{
		var paper = this.@synopticgwt.client.invariants.GraphicEvent::paper;
		var text = paper.text(x, y, text);
		text.attr({
			'font-size' : fontSize + "px",
			fill : fillColor
		});
		return text;
    }-*/;

    public int getX() {
        return labelXCoord;
    }

    public int getY() {
        return labelYCoord;
    }

    // If the GraphicEvent is not visible on the paper, make it visible
    public native void show() /*-{
		var text = this.@synopticgwt.client.invariants.GraphicEvent::labelText;
		text.show();
    }-*/;

    // If the GraphicEvent is visible on the InvariantsGraph, make it invisible
    public native void hide() /*-{
		var text = this.@synopticgwt.client.invariants.GraphicEvent::labelText;
		text.hide();
    }-*/;

    public void addInvariant(GraphicInvariant gInv) {
        invariants.add(gInv);
    }

    public void hightlightOnIncidentInvariants() {

        for (GraphicInvariant gi : invariants) {
            gi.highlightOn();
        }
    }

    public void hightlightOffIncidentInvariants() {
        for (GraphicInvariant gi : invariants) {
            gi.highlightOff();
        }
    }

    // Function to execute when the label is pointed-to.
    public native void setMouseover() /*-{
		this.@synopticgwt.client.invariants.GraphicEvent::labelText
				.mouseover(function(thisGraphicEvent) {
					return function(e) {
						thisGraphicEvent
								.@synopticgwt.client.invariants.GraphicEvent::hightlightOnIncidentInvariants()
								();
					};
				}(this));
    }-*/;

    // Function to execute when the label is not pointed-to.
    public native void setMouseout() /*-{
		this.@synopticgwt.client.invariants.GraphicEvent::labelText
				.mouseout(function(thisGraphicEvent) {
					return function(e) {
						thisGraphicEvent
								.@synopticgwt.client.invariants.GraphicEvent::hightlightOffIncidentInvariants()
								();
					};
				}(this));
    }-*/;

    public native void setFill(String color) /*-{
		var text = this.@synopticgwt.client.invariants.GraphicEvent::labelText;
		text.attr({
			fill : color
		});
    }-*/;

}
