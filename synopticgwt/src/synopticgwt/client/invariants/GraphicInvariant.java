package synopticgwt.client.invariants;

import java.io.Serializable;

import synopticgwt.shared.GWTInvariant;

/* Graphic model for an invariant */
public class GraphicInvariant implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	GraphicEvent src;
    GraphicEvent dst;
    GraphicArrow arrow;
    GWTInvariant GWTinv;

    public GraphicInvariant(GraphicEvent src, GraphicEvent dst,
        GraphicArrow arrow, GWTInvariant GWTinv) {
        this.src = src;
        this.dst = dst;
        this.arrow = arrow;
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

    public highlightOn() {
        src.setFill(GraphicPaper.HIGHLIGHT_FILL);
        dst.setFill(GraphicPaper.HIGHLIGHT_FILL);

        String transitionType = GWTinv.getTransitionType();
        if (transitionType.equals("AP")) {
            arrow.setStroke(GraphicPaper.AP_HIGHLIGHT_STROKE, 
                HIGHTLIGHT_STROKE_WIDTH);
        } else if (transitionType.equals("AFby")) {
            arrow.setStroke(GraphicPaper.AFBY_HIGHLIGHT_STROKE, 
                HIGHTLIGHT_STROKE_WIDTH);
        } else if (transitionType.equals("NFby")) {
            arrow.setStroke(GraphicPaper.NFBY_HIGHLIGHT_STROKE, 
                HIGHTLIGHT_STROKE_WIDTH);
        } else {
            throw new IllegalStateExeption("Illegal type: " + transitionType);
        }
    }

    public highlightOff() {
        src.setFill(GraphicPaper.DEFAULT_FILL);
        dst.setFill(GraphicPaper.DEFAULT_FILL);
        arrow.setStroke(GraphicPaper.DEFAULT_STROKE, DEFAULT_STROKE_WIDTH);
    }
}
