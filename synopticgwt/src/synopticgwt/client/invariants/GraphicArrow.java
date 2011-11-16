package synopticgwt.client.invariants;

import java.io.Serializable;

import com.google.gwt.core.client.JavaScriptObject;

/** Java representation of a Javascript arrow on a Raphael canvas */
public class GraphicArrow implements Serializable {

	private static final long serialVersionUID = 1L;
    /** Length of the positive and negative arrowhead segments */
    public static final int HEAD_LENGTH = 10;
    /** Distance between terminal end of the arrow  and (x2, y2) */
    public static final int TARGET_BUFFER = 10;
	
	// Raphael paper object
    private JavaScriptObject paper;
    // Raphael path elements
    // Non-arrowhead part of the arrow
    private JavaScriptObject path;
    // Part of the arrowhead that has a positive angular offset from the body
    private JavaScriptObject positiveHead;
    // Part of the arrowhead that has a negative angular offset from the body
    private JavaScriptObject negativeHead;

    /** 
     * Draws an arrow from (x1, y1) to (x2, y2) on paper 
     * Paper is a Raphael paper object
     * */
    public GraphicArrow(int x1, int y1, int x2, int y2, JavaScriptObject paper) {
        this.paper = paper;
        constructArrow(x1, y1, x2 - TARGET_BUFFER, y2 - TARGET_BUFFER);
        setStroke(InvariantsGraph.DEFAULT_STROKE, 
            InvariantsGraph.DEFAULT_STROKE_WIDTH);
    }

    /** Draws and arrow from (x1, y1) to (x2, y2) */
    public void constructArrow(int x1, int y1, int x2, int y2) {
        /* 
         * I conceptually set (x2, y2) to (0, 0) and compute the relevant 
         * x1 and y1 values
         * */
        double xRelativeZero = x1 - x2;
        double yRelativeZero = y1 - y2;
        
        /* 
         * In cartesian coordinates, I am computing the angle that the line 
         * from (xRelativeZero, yRelativeZero) to the origin makes with 
         * some axis.
         *
         * In polar coordinates, I am computing theta given
         * x = xRelativeZero and y = yRelativeZero
         * */
        double theta = Math.atan2(yRelativeZero, xRelativeZero);
        double positiveTheta = theta + Math.PI / 4;
        double negativeTheta = theta - Math.PI / 4;

        /*
         * This computes the coodrinates for the part of the arrowhead that
         * is at a positive angular axis from the arrowbody 
         * */ 
        double relativePositiveHeadX = HEAD_LENGTH * Math.cos(positiveTheta);
        double relativePositiveHeadY = HEAD_LENGTH * Math.sin(positiveTheta);

        /*
         * This computes the coodrinates for the part of the arrowhead that
         * is at a negative angular axis from the arrowbody 
         * */ 
        double relativeNegativeHeadX = HEAD_LENGTH * Math.cos(negativeTheta);
        double relativeNegativeHeadY = HEAD_LENGTH * Math.sin(negativeTheta);

        /*
         * Shift relative positive arrowhead coordinates back to absolute
         * */  
        double positiveHeadX = relativePositiveHeadX + x2;
        double positiveHeadY = relativePositiveHeadY + y2;

        /*
         * Shift relative negative arrowhead coordinates back to absolute
         * */  
        double negativeHeadX = relativeNegativeHeadX + x2;
        double negativeHeadY = relativeNegativeHeadY + y2;

        this.path = constructPath(x1, y1, x2, y2);
        this.positiveHead = constructPath(x2, y2, positiveHeadX, 
            positiveHeadY);
        this.negativeHead = constructPath(x2, y2, negativeHeadX, 
            negativeHeadY);
    }
        
    // Creates JS path object on paper from (x1, y1, to x2, y2)
    private native JavaScriptObject constructPath(double x1, double y1, 
    		double x2, double y2) /*-{
		var paper = this.@synopticgwt.client.invariants.GraphicArrow::paper;
        var path = paper.path("M" + x1 + " " + y1 + "L" + x2 + " " + y2);
        return path;
    }-*/;

    /** Makes the arrow visible on paper */
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

    /** Makes the arrow invisible on paper */
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

    /** Changes the arrow's color and stroke width to color and width */
    public native void setStroke(String color, int width) /*-{
		var path = this.@synopticgwt.client.invariants.GraphicArrow::path;
		var positiveHead = 
            this.@synopticgwt.client.invariants.GraphicArrow::positiveHead;
		var negativeHead = 
            this.@synopticgwt.client.invariants.GraphicArrow::negativeHead;
        path.attr({
            stroke : color,
            'stroke-width' : width
        });
        positiveHead.attr({
            stroke : color,
            'stroke-width' : width
        });
        negativeHead.attr({
            stroke : color,
            'stroke-width' : width
        });
    }-*/;
}
