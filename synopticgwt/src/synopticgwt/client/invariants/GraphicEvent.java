package synopticgwt.client.invariants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;

/** 
 * Graphic model representing a logged event type
 * Also a java representation of a JavaScript text label on a Raphael canvas.
 * */
public class GraphicEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    // Raphael canvas paper object
    private JavaScriptObject paper;
    // Raphael text object
    private JavaScriptObject labelText;
    // Incident relations
    private List<GraphicInvariant> invariants;

    // Label's x coordinate on paper
    private int labelXCoord;
    // Label's y coordinate on paper
    private int labelYCoord;

    /** 
     * Creates a graphic event with text event, font size fontSize, and 
     * positioned at (x, y) on paper.
     * */
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

    private native JavaScriptObject constructText(int x, int y, int fontSize,
            String fillColor, String text) /*-{
		var paper = this.@synopticgwt.client.invariants.GraphicEvent::paper;
		var text = paper.text(x, y, text);
		text.attr({
			'font-size' : fontSize + "px",
			fill : fillColor
		});
		return text;
    }-*/;

    /** Return's the x coordinate of this event */
    public int getX() {
        return labelXCoord;
    }

    /** Return's the y coordinate of this event */
    public int getY() {
        return labelYCoord;
    }

    /** Makes this visible on paper */
    public native void show() /*-{
		var text = this.@synopticgwt.client.invariants.GraphicEvent::labelText;
		text.show();
    }-*/;

    /** Makes this invisible on paper */
    public native void hide() /*-{
		var text = this.@synopticgwt.client.invariants.GraphicEvent::labelText;
		text.hide();
    }-*/;

    /** Adds gInv to the list of invariants incident to this event */
    public void addInvariant(GraphicInvariant gInv) {
        invariants.add(gInv);
    }

    /** Highlights all of the invariants incident to this event */
    public void hightlightOnIncidentInvariants() {
        for (GraphicInvariant gi : invariants) {
            gi.highlightOn();
        }
    }

    /** Removes highlighting from all of the invariants incident to this 
     * event 
     * */
    public void hightlightOffIncidentInvariants() {
        for (GraphicInvariant gi : invariants) {
            gi.highlightOff();
        }
    }

    /** Registers highlightOnIncidentInvariants with the JS labelText object */
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

    /** Registers highlightOffIncidentInvariants with the JS labelText object */
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

    /** Sets the fill of the JS labelText object to color */
    public native void setFill(String color) /*-{
		var text = this.@synopticgwt.client.invariants.GraphicEvent::labelText;
		text.attr({
			fill : color
		});
    }-*/;

}
