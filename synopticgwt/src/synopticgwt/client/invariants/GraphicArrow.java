/* Java object representing an arrow on the iGraph */
public class GraphicArrow {
    // Raphael path elements
    JavaScriptObject body;
    // Part of the arrowhead that has a positive angular offset from the body
    JavaScriptObject positiveHead;
    // Part of the arrowhead that has a negative angular offset from the body
    JavaScriptObject negativeHead;
    InvariantsGraph iGraph;

    public GraphicArrow(JavaScriptObject body, JavaScriptObject positiveHead,
        JavaScriptObject negativeHead, InvariantsGraph iGraph) {
        this.body = body;
        this.positiveHead = positiveHead;
        this.negativeHead = negativeHead;
        this.iGraph = iGraph;
    }


    /* If the arrow is not visible on the InvariantsGraph, make it
     * make it visible
     */  
    public void show() {
        iGraph.showElement(label);
    }

    /* If the arrow is visible on the InvariantsGraph, make it
     * make it invisible
     */  
    public void hide() {
        iGraph.hideElement(label);
    }
