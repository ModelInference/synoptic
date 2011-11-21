package synopticgwt.client.invariants;

import java.io.Serializable;

import synopticgwt.shared.GWTInvariant;

public class GraphicConcurrentInvariant implements Serializable,
        GraphicInvariant {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private GraphicEvent gEventA;
    private GraphicEvent gEventB;
    private GWTInvariant gwtInv;
    private InvariantGridLabel iGridLabel;
    private boolean visible;
    private GraphicConcurrencyPartition concurrencyParition;

    public GraphicConcurrentInvariant(GraphicEvent a, GraphicEvent b,
            GWTInvariant gwtInv, InvariantGridLabel iGridLabel) {
        this.gEventA = a;
        this.gEventB = b;
        this.gwtInv = gwtInv;
        this.iGridLabel = iGridLabel;
    }

    public void show() {
        visible = true;
    }

    public void hide() {
        visible = false;
    }

    public boolean isVisible() {
        return visible;
    }
    
    public void highlightConcurrent() {
        gEventA.highlightConcurrent();
        gEventB.highlightConcurrent();
    }
    
    public void highlightNeverConcurrent() {
        gEventA.highlightNeverConcurrent();
        gEventB.highlightNeverConcurrent();
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
        gEventA.highlightDefault();
        gEventB.highlightDefault();
    }
    
    public GWTInvariant getGWTInvariant() {
        return gwtInv;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof GraphicConcurrentInvariant) {
            GraphicConcurrentInvariant otherInv = (GraphicConcurrentInvariant) o;
            return getGWTInvariant().equals(otherInv.getGWTInvariant());
        }
        return false;
    }
    
    public GraphicEvent getEventA() {
        return gEventA;
    }
    
    public GraphicEvent getEventB() {
        return gEventB;
    }
    
    public boolean isTransitive(GraphicConcurrentInvariant gcInv) {
        GraphicEvent otherA = gcInv.getEventA();
        GraphicEvent otherB = gcInv.getEventB();
        boolean result = false;
        result = result || otherA.equals(getEventA());
        result = result || otherA.equals(getEventB());
        result = result || otherB.equals(getEventA());
        result = result || otherB.equals(getEventB());
        return result;
    }

}
