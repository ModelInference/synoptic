package synopticgwt.client.invariants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import synopticgwt.client.util.MouseHover;
import synopticgwt.client.util.Paper;

/**
 * Graphic model representing a logged event type Also a java representation of
 * a JavaScript text label on a Raphael canvas.
 */
public class GraphicEvent implements Serializable, MouseHover {

    private static final long serialVersionUID = 1L;

    /** Wrapped Raphael label object */
    private Label label;
    /** Incident OrderedInvariants and transitive ConcurrentInvariants */
    private List<GraphicInvariant> invariants;
    /** Invariant partition this event is concurrent with */
    private GraphicConcurrencyPartition ACpartition;
    /** Event partition this event is never concurrent with */
    private GraphicNonConcurrentPartition NCPartition;
    private String event;

    /**
     * Creates a graphic event
     * 
     * @param x
     *            x coordinate of event
     * @param y
     *            y coordinate of event
     * @param fontSize
     *            size of graphic font
     * @param event
     *            text for event
     * @param paper
     *            Raphael canvas to create event on
     */

    public GraphicEvent(int x, int y, int fontSize, String event, Paper paper) {
        this.invariants = new ArrayList<GraphicInvariant>();
        this.label = new Label(paper, x, y, fontSize, event,
                InvariantsGraph.DEFAULT_FILL);
        hide();
        this.event = event;
        label.setMouseover(this);
        label.setMouseout(this);
    }
    
    public void setACPartition(GraphicConcurrencyPartition ACPart) {
        this.ACpartition = ACPart;
    }
    
    public void setNCPartition(GraphicNonConcurrentPartition NCPart) {
        this.NCPartition = NCPart;
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
    public void addInvariant(GraphicOrderedInvariant gInv) {
        if (invariants.size() == 0) {
            show();
        }
        invariants.add(gInv);
    }

    public void highlightOrdered() {
        setFill(InvariantsGraph.ORDERED_FILL);
    }

    public void highlightDefault() {
        setFill(InvariantsGraph.DEFAULT_FILL);
    }

    public void highlightConcurrent() {
        setFill(InvariantsGraph.CONCURRENT_FILL);
    }

    public void highlightNeverConcurrent() {
        setFill(InvariantsGraph.NEVER_CONCURRENT_FILL);
    }

    /**
     * Highlights this event's incident OrderedInvariants, transitive
     * ConcurrentInvariants, and the event itself on mouseover
     */
    public void mouseover() {
        for (GraphicInvariant gi : invariants) {
            gi.highlightOn();
        }
        if (ACpartition != null)
            ACpartition.highlightOn();
        if (NCPartition != null)
            NCPartition.highlightOn();
        highlightOrdered();
    }

    /**
     * Removes highlighting from this event's incident OrderedInvariants,
     * transitive ConcurrentInvariants, and the event itself on mouseout
     */
    public void mouseout() {
        for (GraphicInvariant gi : invariants) {
            gi.highlightOff();
        }
        if (ACpartition != null)
            ACpartition.highlightOff();
        if (NCPartition != null)
            NCPartition.highlightOff();
        highlightDefault();
    }

    public void setFill(String fill) {
        label.setFill(fill);
    }
    
    public String getEvent() {
        return event;
    }
    
    /** Assumes this is only being compared with graphic events in the same
     * graphical column.
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof GraphicEvent) {
            GraphicEvent otherEvent = (GraphicEvent) o;
            return getEvent().equals(otherEvent.getEvent());
        }
        return false;
    }

}
