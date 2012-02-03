package synopticgwt.client.invariants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import synopticgwt.client.util.MouseHover;
import synopticgwt.client.util.Paper;

/** 
 * Graphic model representing a logged event type
 * Also a java representation of a JavaScript text label on a Raphael canvas.
 * */
public class GraphicEvent implements Serializable, MouseHover {

    private static final long serialVersionUID = 1L;

    /** Wrapped Raphael label object */
    private Label label;
    /** Incident relations */
    private List<GraphicInvariant> invariants;

    /** 
     * Creates a graphic event 
     * @param x x coordinate of event
     * @param y y coordinate of event
     * @param fontSize size of graphic font
     * @param event text for event
     * @param paper Raphael canvas to create event on
     */

    public GraphicEvent(int x, int y, int fontSize, String event,
            Paper paper) {
        this.invariants = new ArrayList<GraphicInvariant>();
        this.label = new Label(paper, x, y, fontSize, 
            event, InvariantsGraph.DEFAULT_FILL);
        hide();
        label.setMouseover(this);
        label.setMouseout(this);
    }

    /** Return's the x coordinate of this event */
    public int getX() {
        return label.getX();
    }

    /** Return's the y coordinate of this event */
    public int getY() {
        return label.getY();
    }

    public void show() {
        label.show();
    }

    public void hide() {
        label.hide();
    }

    /** Adds gInv to the list of invariants incident to this event */
    public void addInvariant(GraphicInvariant gInv) {
    	if (invariants.size() == 0) {
    		show();
    	}
        invariants.add(gInv);
    }

    /** 
     * Highlights all of this event's incident invariants on mouseover
     */
    public void mouseover() {
        for (GraphicInvariant gi : invariants) {
    		gi.highlightOn();
        }
    }
    
    /** 
     * Removes highlighting from all of this event's incident invariants
     * on mouseout
     */
    public void mouseout() {
        for (GraphicInvariant gi : invariants) {
    		gi.highlightOff();
        }
    }

	public void setFill(String fill) {
		label.setFill(fill);
	}

}
