package synopticgwt.client.invariants;

import java.io.Serializable;

import synopticgwt.shared.GWTInvariant;

/**
 * Graphic model representing a concurrent GWTInvariant, relates two 
 * GraphicEvents representing the source and destination of the invariant
 */
public class GraphicConcurrentInvariant implements Serializable,
        GraphicInvariant {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    /** Events are labeled src and dst for simplicity, as opposed to a and b 
     * but this relationship doesn't really exist since concurrency is
     * commutative.
     */
    private GraphicEvent src;
    private GraphicEvent dst;
    /** GWTInvariant object that this represents */
    private GWTInvariant gwtInv;
    private InvariantGridLabel iGridLabel;
    private boolean visible;

    /** Constructs a GraphicInvariant for GWTinv over src and dst on paper */
    public GraphicConcurrentInvariant(GraphicEvent src, GraphicEvent dst,
            GWTInvariant gwtInv, InvariantGridLabel iGridLabel) {
        this.src = src;
        this.dst = dst;
        this.gwtInv = gwtInv;
        this.iGridLabel = iGridLabel;
    }

    /**
     * Allows this invariant to be highlighted when involved in a mouseover 
     * event
     */
    public void show() {
        visible = true;
    }

    /**
     * Prevents this invariant from being highlighted when involved in a mouseover 
     * event
     */
    public void hide() {
        visible = false;
    }

    public boolean isVisible() {
        return visible;
    }
    
    public void highlightConcurrent() {
        src.highlightConcurrent();
        dst.highlightConcurrent();
    }
    
    public void highlightNeverConcurrent() {
        src.highlightNeverConcurrent();
        dst.highlightNeverConcurrent();
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
        src.highlightDefault();
        dst.highlightDefault();
    }
    
    public GWTInvariant getGWTInvariant() {
        return gwtInv;
    }
    
    /** Equal if the underlying GWTInvariant is equal */
    @Override
    public boolean equals(Object o) {
        if (o instanceof GraphicConcurrentInvariant) {
            GraphicConcurrentInvariant otherInv = (GraphicConcurrentInvariant) o;
            return getGWTInvariant().equals(otherInv.getGWTInvariant());
        }
        return false;
    }
    
    public GraphicEvent getSrc() {
        return src;
    }
    
    public GraphicEvent getDst() {
        return dst;
    }
    
    /** Returns whether or not the two invariants share a mutual graphic 
     * event 
     */
    public boolean isTransitive(GraphicConcurrentInvariant gcInv) {
        GraphicEvent otherA = gcInv.getSrc();
        GraphicEvent otherB = gcInv.getDst();
        boolean result = false;
        result = result || otherA.equals(getSrc());
        result = result || otherA.equals(getDst());
        result = result || otherB.equals(getSrc());
        result = result || otherB.equals(getDst());
        return result;
    }

}
