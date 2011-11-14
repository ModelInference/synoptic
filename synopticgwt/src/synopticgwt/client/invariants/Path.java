package synopticgwt.client.invariants;

import java.io.Serializable;

import com.google.gwt.core.client.JavaScriptObject;

/** 
 * Java wrapper for a Raphael path object 
 * @author timjv
 *
 */

public class Path implements Serializable {
   
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/** Wrapper for canvas this path is drawn on */
    private Paper paper;
    /** origin x coordinate */
    private double x1;
    /** origin y coordinate */
    private double y1;
    /** terminal x coordinate */
    private double x2;
    /** terminal y coordinate */
    private double y2;
    /** Raphael path */
    private JavaScriptObject path;

    /**
     * Creates a new path from (x1, y1) to (x2, y2)
     * 
     * @param x1 origin x coordinate
     * @param y1 origin y coordinate
     * @param x2 terminal x coordinate
     * @param y2 terminal y coordinate
     * @param paper Raphael canvas wrapper
     */
    public Path(double x1, double y1, double x2, double y2, Paper paper) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.paper = paper;
        this.path = constructPath(x1, y1, x2, y2, paper.getPaper());
    }

    /** 
     * Creates JS path object on paper from (x1, y1) to x2, y2)
     * @param x1 origin x coordinate
     * @param y1 origin y coordinate
     * @param x2 terminal x coordinate
     * @param y2 terminal y coordinate
     * @param paper unwrapped raphael canvas
     * @return unwrapped raphael path
     */
    private native JavaScriptObject constructPath(double x1, double y1, 
    		double x2, double y2, JavaScriptObject paper) /*-{
        var path = paper.path("M" + x1 + " " + y1 + "L" + x2 + " " + y2);
        return path;
    }-*/;

    /** 
     * Makes the path visible on paper 
     */
    public native void show() /*-{
		var path = this.@synopticgwt.client.invariants.Path::path;
        path.show();
    }-*/;

    /**  
     * Makes the path invisible on paper
     */
    public native void hide() /*-{
		var path = this.@synopticgwt.client.invariants.Path::path;
        path.hide();
    }-*/;

    /**
     * Changes the paths's color and stroke width to color and width
     * 
     * @param color
     * @param width
     */
    public native void setStroke(String color, int width) /*-{
		var path = this.@synopticgwt.client.invariants.Path::path;
        path.attr({
            stroke : color,
            'stroke-width' : width
        });
    }-*/;
}
