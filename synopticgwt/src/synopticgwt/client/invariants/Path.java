package synopticgwt.client.invariants;

import java.io.Serializable;

import com.google.gwt.core.client.JavaScriptObject;

import synopticgwt.client.util.MouseHover;
import synopticgwt.client.util.Paper;

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
     * @param originX origin x coordinate
     * @param originY origin y coordinate
     * @param terminalX terminal x coordinate
     * @param terminalY terminal y coordinate
     * @param canvas unwrapped raphael canvas
     * @return unwrapped raphael path
     */
    private native JavaScriptObject constructPath(double originX, double originY, 
    		double terminalX, double terminalY, JavaScriptObject canvas) /*-{
        var path = canvas.path("M" + originX + " " + originY +
         		"L" + terminalX + " " + terminalY);
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
    
    /** 
     * Registers hover mouseover with the Raphael path
     * @param hover object with java level mouseover function
     */
    public native void setMouseover(MouseHover hover) /*-{
        var path = this.@synopticgwt.client.invariants.Path::path;
        path.mouseover(
            function(hoverable) {
                return function(e) {
                    hoverable.@synopticgwt.client.util.MouseHover::mouseover()();
                };
            } (hover));
    }-*/;
    
    /** 
     * Registers hover mouseout with the Raphael path
     * @param hover object with java level mouseout function
     */
    public native void setMouseout(MouseHover hover) /*-{
        var path = this.@synopticgwt.client.invariants.Path::path;
        path.mouseout(
            function(hoverable) {
                return function(e) {
                    hoverable.@synopticgwt.client.util.MouseHover::mouseout()();
                };
            } (hover));
    }-*/;
}
