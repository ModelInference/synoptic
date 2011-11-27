package synopticgwt.client.invariants;

import java.io.Serializable;

import synopticgwt.client.util.MouseHover;
import synopticgwt.client.util.Paper;
import synopticgwt.shared.GWTInvariant;

/**
 * Graphic model representing an ordered GWTInvariant Relates two GraphicEvents
 * representing the source and destination of the invariant arrow with the
 * actual GraphicArrow
 */
public class GraphicOrderedInvariant implements Serializable, MouseHover,
        GraphicInvariant {

    private static final long serialVersionUID = 1L;

    /** GraphicEvent where arrow originates */
    private GraphicEvent src;
    /** GraphicEvent where arrow terminates */
    private GraphicEvent dst;
    /** GraphicArrow between src and dst */
    private GraphicArrow arrow;
    /** GWTInvariant object that this represents */
    private GWTInvariant GWTinv;
    private InvariantGridLabel iGridLabel;

    private boolean visible;

    /** Constructs a GraphicInvariant for GWTinv from src to dst on paper */
    public GraphicOrderedInvariant(GraphicEvent src, GraphicEvent dst,
            GWTInvariant GWTinv, Paper paper, InvariantGridLabel iGridLabel) {
        this.src = src;
        this.dst = dst;
        this.arrow = new GraphicArrow(src.getX(), src.getY(), dst.getX(),
                dst.getY(), paper);
        arrow.setMouseover(this);
        arrow.setMouseout(this);
        this.GWTinv = GWTinv;
        this.iGridLabel = iGridLabel;
        visible = true;
    }

    /**
     * Makes the GraphicInvariant visible on the paper used to construct this
     */
    public void show() {
        visible = true;
        arrow.show();
    }

    /**
     * Makes the GraphicInvariant invisible on the paper used to construct this
     */
    public void hide() {
        visible = false;
        arrow.hide();
    }

    public boolean isVisible() {
        return visible;
    }

    /** Highlights src, dst, and arrow based on arrow's transition type */
    public void highlightOn() {
        if (isVisible()) {
            src.highlightOrdered();
            dst.highlightOrdered();
            highlightOnArrow();
        }
    }

    /** Removes highlightng from src, dst, and arrow */
    public void highlightOff() {
        src.highlightDefault();
        dst.highlightDefault();
        highlightOffArrow();
    }

    /**
     * Highlights arrow based on arrow's transition type
     */
    // TODO: Remove invariant type hardcoding
    public void highlightOnArrow() {
        if (isVisible()) {
            String transitionType = GWTinv.getTransitionType();
            if (transitionType.equals("AP")) {
                arrow.highlightAP();
            } else if (transitionType.equals("AFby")) {
                arrow.highlightAFby();
            } else if (transitionType.equals("NFby")) {
                arrow.highlightNFby();
            } else {
                throw new IllegalStateException("Illegal type: "
                        + transitionType);
            }
            iGridLabel.highlightOn();
        }
    }

    /** Removes highlightng from arrow */
    public void highlightOffArrow() {
        arrow.highlightDefault();
        iGridLabel.highlightOff();
    }

    /**
     * Sets arrow stroke to highlight state
     */
    @Override
    public void mouseover() {
        highlightOnArrow();
    }

    /**
     * Sets arrow stroke to default state
     */
    @Override
    public void mouseout() {
        highlightOffArrow();
    }
}
