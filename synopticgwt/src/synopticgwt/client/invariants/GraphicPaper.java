package synopticgwt.client.invariants;

import com.google.gwt.core.client.JavaScriptObject;

public class GraphicPaper {
    // Raphael paper object
    private JavaScriptObject paper;
    
    public GraphicPaper(int width, int height, String canvasId) {
        paper = constructPaper(width, height, canvasId);
    }

    public native JavaScriptObject constructPaper(int width, int height, 
            String canvasId) /*-{
		var paper = $wnd.Raphael($doc.getElementById(canvasId), width, height);

		// Attribute to track the target node pointed to from the middle 
		// text-element.
		paper.customAttributes.dest = function(textElem) {
			return {
				dest : textElem
			};
		};

		// Attribute to track the highlighted color of the lines connected 
		// to the selected middle text-element.
		paper.customAttributes.highlight = function(color) {
			return {
				highlight : color
			};
		};

        return paper;
    }-*/;

    public native JavaScriptObject drawPath(int x1, int y1, int x2, int y2) /*-{
		var paper = this.@synopticgwt.client.invariants.GraphicPaper::paper;
    }-*/;

    public native JavaScriptObject drawText(int x, int y, String text) /*-{
        TODO: implement
    }-*/;

    public native void showElement(JavaScriptObject element) /*-{
        TODO: implement
    }-*/;

    public native void hideElement(JavaScriptObject element) /*-{
        TODO: implement
    }-*/;
}
