package synopticgwt.client.invariants;

import java.io.Serializable;

import com.google.gwt.core.client.JavaScriptObject;

import synopticgwt.shared.GWTInvariant;

/* Graphic model for an invariant */
public class GraphicInvariant implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	GraphicEvent src;
    GraphicEvent dst;
    GraphicArrow arrow;
    GWTInvariant GWTinv;

    public GraphicInvariant(GraphicEvent src, GraphicEvent dst,
        GWTInvariant GWTinv, JavaScriptObject paper) {
        this.src = src;
        this.dst = dst;
        this.arrow = new GraphicArrow(src.getX(), src.getY(), dst.getX(),
            dst.getY(), paper);
        this.GWTinv = GWTinv;
    }

    /* If the GraphicInvariant is not visible on the InvariantsGraph, make it
     * make it visible
     */  
    public void show() {
        arrow.show();
    }

    /* If the GraphicInvariant is visible on the InvariantsGraph, make it
     * make it invisible
     */  
    public void hide() {
        arrow.hide();
    }

    public void highlightOn() {
        src.setFill(InvariantsGraph.HIGHLIGHT_FILL);
        dst.setFill(InvariantsGraph.HIGHLIGHT_FILL);

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

    public void highlightOff() {
        src.setFill(InvariantsGraph.DEFAULT_FILL);
        dst.setFill(InvariantsGraph.DEFAULT_FILL);
        arrow.setStroke(InvariantsGraph.DEFAULT_STROKE, 
        		InvariantsGraph.DEFAULT_STROKE_WIDTH);
    }
}
