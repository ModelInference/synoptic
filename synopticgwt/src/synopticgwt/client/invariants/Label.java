package synopticgwt.client.invariants;

import java.io.Serializable;

import com.google.gwt.core.client.JavaScriptObject;

import synopticgwt.client.util.MouseHover;
import synopticgwt.client.util.Paper;

/**
 * Java wrapper for a raphael text
 * @author timjv
 *
 */
public class Label implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/** X coordinate of label on paper */
    private int x;
    /** Y coordinate of label on paper */
    private int y;
    /** Raphael paper object label is drawn on */
    private Paper paper;
    /** Label's font size */
    private int fontSize;
    /** Text presented on Label */
    private String labelText;
    /** Raphael label object */
    private JavaScriptObject label;

    /**
     * Creates a label
     * @param paper Wrapped raphael paper
     * @param x x coodrinate for label
     * @param y y coordinate for label
     * @param fontSize size of font
     * @param labelText string to present on label
     * @param color color of label text
     */
    public Label(Paper paper, int x, int y, int fontSize, String labelText, 
            String color) {
    	
        this.paper = paper;
        this.x = x;
        this.y = y;
        this.fontSize = fontSize;
        this.labelText = labelText;
        this.label = constructLabel(x, y, fontSize, 
            InvariantsGraph.DEFAULT_FILL, labelText, paper.getPaper());
    }

    /**
     * Creates a raphael label
     * @param xCoord x coodrinate for label
     * @param yCoord y coordinate for label
     * @param font size of font
     * @param fillColor color of label text
     * @param text string to present on label
     * @param canvas unwrapped raphael paper
     * @return unwrapped raphael text
     */
    private native JavaScriptObject constructLabel(int xCoord, int yCoord, int font,
            String fillColor, String text, JavaScriptObject canvas) /*-{
		var rLabel = canvas.text(xCoord, yCoord, text);
		rLabel.attr({
			'font-size' : font + "px",
			fill : fillColor
		});
		return rLabel;
    }-*/;

    /** 
     * Makes label visible on paper
     */
    public native void show() /*-{
		var label = this.@synopticgwt.client.invariants.Label::label;
		label.show();
    }-*/;

    /** 
     * Makes label invisible on paper
     */
    public native void hide() /*-{
		var label = this.@synopticgwt.client.invariants.Label::label;
		label.hide();
    }-*/;
    
    /** 
     * Removes label from paper
     */
    public native void remove() /*-{
        var label = this.@synopticgwt.client.invariants.Label::label;
        label.remove();
    }-*/;

    /** 
     * Sets the fill of the JS labelText object to color
     * @param color
     */
    public native void setFill(String color) /*-{
		var label = this.@synopticgwt.client.invariants.Label::label;
		label.attr({
			fill : color
		});
    }-*/;

    /** 
     * 
     * @return Wrapped paper label is drawn on
     */
    public Paper getPaper() {
        return this.paper;
    }
    
    /** 
     * 
     * @return x coordinate of label 
     */
    public int getX() {
        return this.x;
    }
        
    /** 
     * 
     * @return y coordinate of label
     */
    public int getY() {
        return this.y;
    }
    
    /**
     * Returns element height, 0 if element is not rendered
     */
    public native float getHeight() /*-{
        var label = this.@synopticgwt.client.invariants.Label::label;
        var labelBBox = label.getBBox();
        return labelBBox.height;
    }-*/;

    /**
     * Returns element width, 0 if element is not rendered
     */
    public native float getWidth() /*-{
        var label = this.@synopticgwt.client.invariants.Label::label;
        var labelBBox = label.getBBox();
        return labelBBox.width;
    }-*/;
    
    /**
     * 
     * @return unwrapped raphael text
     */
    public JavaScriptObject getLabel() {
        return label;
    }

    /** 
     * Registers hover mouseover with the Raphael text
     * @param hover object with java level mouseover function
     */
    public native void setMouseover(MouseHover hover) /*-{
        var label = this.@synopticgwt.client.invariants.Label::label;
        label.mouseover(
        	function(hoverable) {
	            return function(e) {
	                hoverable.@synopticgwt.client.util.MouseHover::mouseover()();
            	};
        	} (hover));
    }-*/;
    
    /** 
     * Registers hover mouseout with the Raphael text
     * @param hover object with java level mouseout function
     */
    public native void setMouseout(MouseHover hover) /*-{
        var label = this.@synopticgwt.client.invariants.Label::label;
        label.mouseout(
        	function(hoverable) {
	            return function(e) {
	                hoverable.@synopticgwt.client.util.MouseHover::mouseout()();
	            };
        	} (hover));
    }-*/;

}
