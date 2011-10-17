/* Graphic model for an invariant */
public class GraphicInvariant {
    GraphicEvent eFirst;
    GraphicEvent eLast;
    GraphicArrow arrow;
    GWTInvariant GWTinv;

    public class GraphicInvariant(GraphicEvent eFirst, GraphicEvent eLast,
        GraphicArrow arrow, GWTInvariant GWTinv) {
        this.eFirst = eFirst;
        this.eLast = eLast;
        this.arrow = arrow;
        setGWTInvariant(GWTinv);
    }

    public void setGWTInvariant(GWTInvariant GWTinv) {
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
