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
    // Incident relations
    private List<GraphicInvariant> invariants;

    public GraphicEvent(int x, int y, String event, GraphicPaper paper) {
        this.paper = paper;
        invariants = new ArrayList<GraphicInvariant>();
        text = constructText(x, y, event);
        setMouseover();
        setMouseout();
    }

    public native JavaScriptObject constructText(int x, int y, 
            String text) /*-{
		var paper = this.@synopticgwt.client.invariants.GraphicEvent::paper;
        var text = paper.text(x, y, text);
        return text;
    }-*/;

    // If the GraphicEvent is not visible on the paper, make it visible
    public native void show() /*-{
		var text = this.@synopticgwt.client.invariants.GraphicEvent::text;
        text.show();
    }-*/;

    // If the GraphicEvent is visible on the InvariantsGraph, make it invisible
    public void hide() /*-{
		var text = this.@synopticgwt.client.invariants.GraphicEvent::text;
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
		var text = this.@synopticgwt.client.invariants.GraphicEvent::text;
        
        text.mouseover(
            $entry(this.@synopticgwt.client.invariants.GraphicEvent::
                highlightOnIncidentInvariants());
        );
    }-*/;


    // Function to execute when the label is not pointed-to.
    public native void setMouseout() /*-{
		var text = this.@synopticgwt.client.invariants.GraphicEvent::text;

        text.mouseout(
            $entry(this.@synopticgwt.client.invariants.GraphicEvent::
                highlightOffIncidentInvariants());
        );
    }-*/;

    public native void setFill(String color) /*-{
		var text = this.@synopticgwt.client.invariants.GraphicEvent::text;
        text.attr({
            fill : color
        });
    }-*/;
    
}
