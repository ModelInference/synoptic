/* Graphic model for an invariant event */
public class GraphicEvent {
    private String event;
    // Corresponding Raphael Text Element in iGraph
    private JavaScriptObject label;
    // For access to show/hide element methods
    InvariantsGraph iGraph;

    public GraphicEvent(String event, JavaScriptObject label, InvariantsGraph
        iGraph) {
        this.event = event;
        this.label = label;
        this.iGraph = iGraph;
    }

    public String getEvent() {
        return event;
    }

    /* If the GraphicEvent is not visible on the InvariantsGraph, make it
     * make it visible
     */  
    public void show() {
        iGraph.showElement(label);
    }

    /* If the GraphicEvent is visible on the InvariantsGraph, make it
     * make it invisible
     */  
    public void hide() {
        iGraph.hideElement(label);
    }
}
