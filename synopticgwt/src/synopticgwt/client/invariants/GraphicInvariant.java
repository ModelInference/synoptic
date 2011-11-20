package synopticgwt.client.invariants;

import java.io.Serializable;

import synopticgwt.client.util.MouseHover;
import synopticgwt.client.util.Paper;
import synopticgwt.shared.GWTInvariant;

/** 
 * Graphic model representing a GWTInvariant
 * Relates two GraphicEvents representing the source and destination of
 * the invariant arrow with the actual GraphicArrow 
 * */
public class GraphicInvariant implements Serializable, MouseHover {
	
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
    
    private boolean visible = true;

    /** Constructs a GraphicInvariant for GWTinv from src to dst on paper */
    public GraphicInvariant(GraphicEvent src, GraphicEvent dst,
        GWTInvariant GWTinv, Paper paper, InvariantGridLabel iGridLabel) {
        this.src = src;
        this.dst = dst;
        this.arrow = new GraphicArrow(src.getX(), src.getY(), dst.getX(),
            dst.getY(), paper);
        arrow.setMouseover(this);
        arrow.setMouseout(this);
        this.GWTinv = GWTinv;
        this.iGridLabel = iGridLabel;
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
    
    public boolean getVisible() {
    	return visible;
    }
    
    /** Highlights src, dst, and arrow based on arrow's transition type */
    // TODO: Remove invariant type hardcoding
    public void highlightOn() {
    	if (visible) {
	        src.setFill(InvariantsGraph.HIGHLIGHT_FILL);
	        dst.setFill(InvariantsGraph.HIGHLIGHT_FILL);
	        highlightOnArrow();
	        iGridLabel.highlightOn();
    	}
    }

    /** Removes highlightng from src, dst, and arrow */
    public void highlightOff() {
        src.setFill(InvariantsGraph.DEFAULT_FILL);
        dst.setFill(InvariantsGraph.DEFAULT_FILL);
        mouseout();
        iGridLabel.highlightOff();
    }
    
    /**
     * Highlights arrow based on arrow's transition type
     */
    public void highlightOnArrow() {
        if (visible) {
            String transitionType = GWTinv.getTransitionType();
            if (transitionType.equals("AP")) {
                arrow.setStroke(InvariantsGraph.AP_HIGHLIGHT_STROKE, 
                        InvariantsGraph.HIGHLIGHT_STROKE_WIDTH);
            } else if (transitionType.equals("AFby")) {
                arrow.setStroke(InvariantsGraph.AFBY_HIGHLIGHT_STROKE, 
                        InvariantsGraph.HIGHLIGHT_STROKE_WIDTH);
            } else if (transitionType.equals("NFby")) {
                arrow.setStroke(InvariantsGraph.NFBY_HIGHLIGHT_STROKE, 
                        InvariantsGraph.HIGHLIGHT_STROKE_WIDTH);
            } else {
                throw new IllegalStateException("Illegal type: " + transitionType);
            }    
        }
    }
    
    /** Removes highlightng from arrow */
    public void highlightOffArrow() {
        arrow.setStroke(InvariantsGraph.DEFAULT_STROKE, 
                InvariantsGraph.DEFAULT_STROKE_WIDTH); 
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
