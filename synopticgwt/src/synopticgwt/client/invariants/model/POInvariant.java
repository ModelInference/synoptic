package synopticgwt.client.invariants.model;

import java.io.Serializable;

import synopticgwt.client.invariants.InvariantGridLabel;
import synopticgwt.shared.GWTInvariant;

/**
 * A graphic model (representing a GWTInvariant) that relates two concurrent
 * events. This represents a PO invariant, as opposed to a TO invariant. This is
 * used by the InvariantsGraph. A set of ACwith invariants are represented by
 * ACPartition. A set of NCwith invariants are represented by NCPartition
 */
public class POInvariant implements Serializable, Invariant {

    private static final long serialVersionUID = 1L;

    /** The two events that are related by this invariant. */
    private Event a;
    private Event b;

    /** GWTInvariant object that this represents */
    private GWTInvariant gwtInv;

    private InvariantGridLabel iGridLabel;
    private boolean visible;

    /** Constructs an Invariant for GWTinv over a and b on paper */
    public POInvariant(Event a, Event b, GWTInvariant gwtInv,
            InvariantGridLabel iGridLabel) {
        this.a = a;
        this.b = b;
        this.gwtInv = gwtInv;
        this.iGridLabel = iGridLabel;
        this.visible = true;
    }

    /**
     * Allows this invariant to be highlighted when involved in a mouseover
     * event.
     */
    public void show() {
        visible = true;
    }

    /**
     * Prevents this invariant from being highlighted when involved in a
     * mouseover event
     */
    public void hide() {
        visible = false;
    }

    public boolean isVisible() {
        return visible;
    }

    public void highlightConcurrent() {
        a.highlightConcurrent();
        b.highlightConcurrent();
    }

    public void highlightNeverConcurrent() {
        a.highlightNeverConcurrent();
        b.highlightNeverConcurrent();
    }

    @Override
    public void highlightOn() {
        if (isVisible()) {
            String transitionType = gwtInv.getTransitionType();
            if (transitionType.equals("ACwith")) {
                highlightConcurrent();
            } else if (transitionType.equals("NCwith")) {
                highlightNeverConcurrent();
            } else {
                throw new IllegalStateException("Illegal type: "
                        + transitionType);
            }
            iGridLabel.highlightOn();
        }
    }

    @Override
    public void highlightOff() {
        a.highlightDefault();
        b.highlightDefault();
        iGridLabel.highlightOff();
    }

    public GWTInvariant getGWTInvariant() {
        return gwtInv;
    }

    @Override
    public boolean equals(Object o) {
        // Equal if the underlying GWTInvariant is equal.
        if (o instanceof POInvariant) {
            POInvariant otherInv = (POInvariant) o;
            return getGWTInvariant().equals(otherInv.getGWTInvariant());
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return gwtInv.hashCode();
    }

    public Event getA() {
        return a;
    }

    public Event getB() {
        return b;
    }

    /**
     * Return whether or not there is a transitive implication between this and
     * gcInv. For example if this = a AC b and gcInv = c AC b, then isTransitive
     * would return true since this and gcInv are transitively related through
     * b. Otherwise, returns false.
     * 
     * @param gcInv
     * @return
     */
    public boolean isTransitive(POInvariant gcInv) {
        Event otherA = gcInv.getA();
        Event otherB = gcInv.getB();
        boolean result = false;
        result = result || otherA.equals(getA());
        result = result || otherA.equals(getB());
        result = result || otherB.equals(getA());
        result = result || otherB.equals(getB());
        return result;
    }

}
