package synopticgwt.client.invariants;

import synopticgwt.shared.GWTInvariant;

/* Graphic model for an invariant */
public class GraphicInvariant {
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
}
