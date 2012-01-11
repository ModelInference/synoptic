package synopticgwt.client.invariants.model;

import java.io.Serializable;

import synopticgwt.client.invariants.InvariantGridLabel;
import synopticgwt.client.util.MouseHover;
import synopticgwt.client.util.Paper;
import synopticgwt.shared.GWTInvariant;

/**
 * Graphic model representing an ordered GWTInvariant Relates two Events
 * representing the source and destination of the invariant arrow with the
 * actual Arrow
 */
public class TOInvariant implements Serializable, MouseHover, Invariant {

    private static final long serialVersionUID = 1L;

    /** Event where arrow originates */
    private Event src;
    /** Event where arrow terminates */
    private Event dst;
    /** Arrow between src and dst */
    private Arrow arrow;
    /** GWTInvariant object that this represents */
    private GWTInvariant GWTinv;
    private InvariantGridLabel iGridLabel;

    private boolean visible;

    /**
     * Constructs a Invariant for GWTinv from src to dst on paper
     * 
     * @param labelOffset
     *            Used to offset arrow heads and bases from overlapping event
     *            labels
     */
    public TOInvariant(Event src, Event dst, GWTInvariant GWTinv, Paper paper,
            InvariantGridLabel iGridLabel, int labelOffset) {
        this.src = src;
        this.dst = dst;
        /*
         *  I use dst.getX() for srcX and src.getX() for dstX. This is a hack,
         *  but it seems to work reasonably well.
         */
        double srcX = src.getX();
        double dstX = dst.getX();
        this.arrow = new Arrow(srcX, src.getY(), dstX, dst.getY(), paper);
        arrow.setMouseover(this);
        arrow.setMouseout(this);
        this.GWTinv = GWTinv;
        this.iGridLabel = iGridLabel;
        visible = true;
    }

    /**
     * Makes the Invariant visible on the paper used to construct this
     */
    public void show() {
        visible = true;
        arrow.show();
    }

    /**
     * Makes the Invariant invisible on the paper used to construct this
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
    
    public double getHeight() {
        return arrow.getHeight();
    }
    
    public double getWidth() {
        return arrow.getWidth();
    }
    
    public double getSrcY() {
        return src.getY();
    }
    
    public double getDstY() {
        return dst.getY();
    }
    
    public double getX() {
        return arrow.getCenterX();
    }
    
    public double getY() {
        return arrow.getCenterY();
    }
    
    /**
     * 
     * @return Absolute difference between event y center coordinates
     */
    public double getEventHeightDifference() {
        return Math.abs(dst.getY() - src.getY());
    }
    
    public void translate(double dx, double dy) {
        arrow.translate(dx, dy);
    }
    
    public void scale(double sx, double sy) {
        arrow.scale(sx, sy);
    }
    
    public boolean arrowSrcIsTopLeft() {
        return arrow.arrowSrcIsTopLeft();
    }
    
    
}
