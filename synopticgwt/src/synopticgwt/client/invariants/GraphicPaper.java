package synopticgwt.client.invariants;

import java.io.Serializable;

import com.google.gwt.core.client.JavaScriptObject;

public class GraphicPaper implements Serializable {

	private static final long serialVersionUID = 1L;

    private static String DEFAULT_STROKE = "grey";
    private static String AP_HIGHLIGHT_STROKE = "blue";
    private static String AFBY_HIGHLIGHT_STROKE = "blue";
    private static String NFBY_HIGHLIGHT_STROKE = "red";

    private static int DEFAULT_STROKE_WIDTH = 1;
    private static int HIGHLIGHT_STROKE_WIDTH = 3;

    private static String DEFAULT_FILL = "grey";
    private static String HIGHLIGHT_FILL = "black";
	
	// Raphael paper object
    private JavaScriptObject paper;
    
    public GraphicPaper(int width, int height, String canvasId) {
        paper = constructPaper(width, height, canvasId);
    }

    public native JavaScriptObject constructPaper(int width, int height, 
            String canvasId) /*-{
		var paper = $wnd.Raphael($doc.getElementById(canvasId), width, height);
        return paper;
    }-*/;

}
