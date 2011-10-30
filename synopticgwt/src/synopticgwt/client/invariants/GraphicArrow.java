package synopticgwt.client.invariants;

import java.io.Serializable;

import com.google.gwt.core.client.JavaScriptObject;

/* Java object representing an arrow on the iGraph */
public class GraphicArrow implements Serializable {

	private static final long serialVersionUID = 1L;
    public static final int HEAD_LENGTH = 10;
    public static final int TARGET_BUFFER = 10;
	
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
        constrctArrow(x1, y1, x2 - TARGET_BUFFER, y2 - TARGET_BUFFER);
    }

    public void constructArrow(int x1, int y1, int x2, int y2) {
        double xRelativeZero = x1 - x2;
        double yRelativeZero = y1 - y2;
        double r = Math.sqrt(Math.pow(xRelativeZero, 2) 
            + Math.pow(yRelativeZero, 2));
        
        double theta = Math.atan(yRelativeZero, xRelativeZero);
        double positiveTheta = theta + Math.PI / 4;
        double negativeTheta = theta - Math.PI / 4;

        double relativePositiveHeadX = HEAD_LENGTH * Math.cos(positiveTheta);
        double relativePositiveHeadY = HEAD_LENGTH * Math.sin(positiveTheta);

        double relativeNegativeHeadX = HEAD_LENGTH * Math.cos(negativeTheta);
        double relativeNegativeHeadY = HEAD_LENGTH * Math.sin(negativeTheta);

        double positiveHeadX = relativePositiveHeadX + x2;
        double positiveHeadY = relativePositiveHeadY + y2;

        double negativeHeadX = relativeNegativeHeadX + x2;
        double negativeHeadY = relativeNegativeHeadY + y2;

        this.path = constructPath(x1, y1, x2, y2);
        this.positiveHead = constructPath(x2, y2, positiveHeadX, 
            positiveHeadY);
        this.negativeHead = constructPath(x2, y2, negativeHeadX, 
            negativeHeadY);
    }
        

    public native JavaScriptObject constructPath(int x1, int y1, int x2, 
            int y2) /*-{
		var paper = this.@synopticgwt.client.invariants.GraphicEvent::paper;
        var path = paper.path("M" + x1 + " " + y1 + "L" + x2 + " " y2);
        return path;
    }-*/;

    // If the arrow is not visible on the paper, make it visible
    public native void show() /*-{
		var path = this.@synopticgwt.client.invariants.GraphicArrow::path;
		var positiveHead = 
            this.@synopticgwt.client.invariants.GraphicArrow::positiveHead;
		var negativeHead = 
            this.@synopticgwt.client.invariants.GraphicArrow::negativeHead;
        path.show();
        positiveHead.show();
        negativeHead.show();
    }-*/;

    // If the arrow is visible on the paper, make it invisible
    public native void hide() /*-{
		var path = this.@synopticgwt.client.invariants.GraphicArrow::path;
		var positiveHead = 
            this.@synopticgwt.client.invariants.GraphicArrow::positiveHead;
		var negativeHead = 
            this.@synopticgwt.client.invariants.GraphicArrow::negativeHead;
        path.hide();
        positiveHead.hide();
        negativeHead.hide();
    }-*/;

    public native void setStroke(String color, int width) /*-{
		var path = this.@synopticgwt.client.invariants.GraphicArrow::path;
		var positiveHead = 
            this.@synopticgwt.client.invariants.GraphicArrow::positiveHead;
		var negativeHead = 
            this.@synopticgwt.client.invariants.GraphicArrow::negativeHead;
        path.attr({
            stroke : color,
            stroke-width : width
        });
    }-*/;
}
