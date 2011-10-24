package synopticgwt.client.invariants;

import com.google.gwt.core.client.JavaScriptObject;

/* Java object representing an arrow on the iGraph */
public class GraphicArrow {
    // Raphael paper object
    private GraphicPaper paper;
    // Raphael path elements
    // Non-arrowhead part of the arrow
    private JavaScriptObject path;
    // Part of the arrowhead that has a positive angular offset from the body
    private JavaScriptObject positiveHead;
    // Part of the arrowhead that has a negative angular offset from the body
    private JavaScriptObject negativeHead;

    public GraphicArrow(int x1, int y1, int x2, int y2, GraphicPaper paper) {
        this.paper = paper;
        // TODO: construct arrow JSOs
    }

    // If the arrow is not visible on the paper, make it visible
    public void show() {
        paper.showElement(path);
        paper.showElement(positiveHead);
        paper.showElement(negativeHead);
    }

    // If the arrow is visible on the paper, make it invisible
    public void hide() {
        paper.hideElement(path);
        paper.hideElement(positiveHead);
        paper.hideElement(negativeHead);
    }
}
