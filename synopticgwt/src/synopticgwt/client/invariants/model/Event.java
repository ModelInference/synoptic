package synopticgwt.client.invariants.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import synopticgwt.client.invariants.InvariantsGraph;
import synopticgwt.client.invariants.Label;
import synopticgwt.client.util.MouseHover;
import synopticgwt.client.util.Paper;

/**
 * Graphic model representing a logged event type Also a java representation of
 * a JavaScript text label on a Raphael canvas.
 */
public class Event implements Serializable, MouseHover {

    private static final long serialVersionUID = 1L;

    /** Wrapped Raphael label object */
    private Label label;
    /** Incident OrderedInvariants and transitive ConcurrentInvariants */
    private List<Invariant> invariants;
    /**
     * Invariant partition this event is concurrent with. Null if this is not
     * part of an AC partition
     */
    private ACPartition ACpartition;
    /**
     * Event partition this event is never concurrent with. Null if this is not
     * part of a NC partition.
     */
    private NCPartition NCPartition;
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

    public Event(int x, int y, int fontSize, String event, Paper paper) {
        this.invariants = new ArrayList<Invariant>();
        this.label = new Label(paper, x, y, fontSize, event,
                InvariantsGraph.DEFAULT_FILL);
        hide();
        this.event = event;
        label.setMouseover(this);
        label.setMouseout(this);
    }

    public void setACPartition(ACPartition ACPart) {
        show();
        this.ACpartition = ACPart;
    }

    public void setNCPartition(NCPartition NCPart) {
        show();
        this.NCPartition = NCPart;
    }

    /** Return's the x coordinate of this event */
    public double getX() {
        return label.getX();
    }

    /** Return's the y coordinate of this event */
    public double getY() {
        return label.getY();
    }
    
    public double getBBoxX() {
        return label.getCenterX();
    }
    
    public double getBBoxY() {
        return label.getCenterY();
    }

    public void show() {
        label.show();
    }

    public void hide() {
        label.hide();
    }

    /** Adds gInv to the list of invariants incident to this event */
    public void addInvariant(TOInvariant gInv) {
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
        for (Invariant gi : invariants) {
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
        for (Invariant gi : invariants) {
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

    @Override
    public boolean equals(Object o) {
        /*
         * Assumes this is only being equated with graphic events in the same
         * graphical column which has no event duplicates.
         */
        if (o instanceof Event) {
            Event otherEvent = (Event) o;
            return getEvent().equals(otherEvent.getEvent());
        }
        return false;
    }

    @Override
    public int hashCode() {
        /*
         * Assumes this is only being hashed against graphic events in the same
         * graphical column which has no event duplicates.
         */
        return getEvent().hashCode();
    }
    
    public void translate(double dx, double dy) {
        label.translate(dx, dy);
    }
    
    public void scale(double sx, double sy) {
        label.scale(sx, sy);
    }
    
    public double getHeight() {
        return label.getHeight();
    }
    
    public double getWidth() {
        return label.getWidth();
    }

}
