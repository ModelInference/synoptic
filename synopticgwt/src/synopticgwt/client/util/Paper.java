package synopticgwt.client.util;

import java.io.Serializable;

import com.google.gwt.core.client.JavaScriptObject;

/** 
 * Java wrapper for a Raphael canvas
 *
 */
public class Paper implements Serializable {

	private static final long serialVersionUID = 1L;
	/** Raphael paper object */
    private JavaScriptObject paper;
    /** width of canvas */
    private double width;
    /** height of canvas */
    private double height;
    /** Identifier for tag that canvas is injected into */
    private String canvasID;

    /**
     * Creates a new Raphael canvas
     * @param width Width of canvas
     * @param height Height of canvas
     * @param canvasID Identifier for tag that canvas is injected into
     */
    public Paper(double width, double height, String canvasID) {
        this.width = width;
        this.height = height;
        this.canvasID = canvasID;
        paper = constructPaper(width, height, canvasID);
    }

    /**
     * 
     * @return Unwrapped canvas object
     */
    public JavaScriptObject getPaper() {
        return this.paper;
    }
    
    /**
     * 
     * @return Width of canvas
     */
    public double getWidth() {
        return this.width;
    }

    /**
     * 
     * @return Height of canvas
     */
    public double getHeight() {
        return this.height;
    }

    /**
     * 
     * @return Canvas's tag
     */
    public String getCanvasID() {
        return this.canvasID;
    }
    
    /** 
     * Removes all elements from the paper
     */
    public native void clear() /*-{
        var paper = this.@synopticgwt.client.util.Paper::paper;
        paper.clear();
    }-*/;
    
    /** 
     * Resizes paper element
     */
    public native void setSize(double width, double height) /*-{
        var paper = this.@synopticgwt.client.util.Paper::paper;
        paper.setSize(width, height);
    }-*/;

    /**
     * Creates a new Raphael canvas object
     * 
     * @param canvasWidth of the canvas
     * @param canvasHeight height of the canvas
     * @param canvasId ID for document element canvas is injected into
     * @return Unwrapped canvas object
     */
    private native JavaScriptObject constructPaper(double canvasWidth, double canvasHeight, 
            String canvasId) /*-{
		var paper = $wnd.Raphael($doc.getElementById(canvasId), canvasWidth, canvasHeight);
        return paper;
    }-*/;
    
}
