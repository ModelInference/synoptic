package synopticgwt.shared;

import java.io.Serializable;

import synopticgwt.client.model.JSNode;
import synopticgwt.client.util.MouseEventHandler;

/**
 * A representation of a graph node for GWT. Overall, this is a representation
 * of a partition node which acts as a bridge between Synoptic's server and the
 * front end.
 * 
 * @author a.w.davies.vio
 */
public class GWTNode implements Serializable {

    // TODO: Create a JSNI field that holds a reference
    // to a Dracula Graph node so that updates can be sent
    // to the node via this object rather than in JS
    // file.

    private static final long serialVersionUID = 1L;

    // The event type of the partition.
    private String eventType = null;

    // The hashCode of the corresponding pNode.
    private int pNodeHash;

    private JSNode jsNode;

    public GWTNode() {
        // Default constructor to avoid serialization errors.
    }

    /**
     * Constructs a GWTNode object, which identifies itself via its event type.
     * The event type is the String representation of the event type to which
     * this node must correspond.
     * 
     * @param eType
     *            The String of the eType of the corresponding partition node.
     * @param hashCode
     *            The hashCode of the corresponding partition node.
     */
    public GWTNode(String eType, int hashCode) {
        assert eType != null;
        this.eventType = eType;
        this.pNodeHash = hashCode;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (!(other instanceof GWTNode))
            return false;
        GWTNode o = (GWTNode) other;
        return o.toString().equals(this.toString())
                && o.pNodeHash == this.pNodeHash;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + eventType.hashCode();
        result = 31 * result + pNodeHash;
        return result;
    }

    public String getEventType() {
        assert this.eventType != null;
        return eventType;
    }

    @Override
    public String toString() {
        return getEventType();
    }

    /**
     * @return The hash code for the Partition Node object that this object
     *         represents.
     */
    public int getPartitionNodeHashCode() {
        return pNodeHash;
    }

    /**
     * @return The hash code for the Partition Node object that this object
     *         represents.
     */
    public String getPartitionNodeHashCodeStr() {
        return ((Integer) pNodeHash).toString();
    }

    /**
     * Sets the instance of the JSNode.
     * 
     * @param node
     */
    public void setJSNode(JSNode node) {
        this.jsNode = node;
        this.jsNode.initRenderer();
    }

    /**
     * Sets the style of the node to the following
     * 
     * @param fillColor
     *            The color with which the node will be filled.
     * @param borderColor
     *            The color to which the border will be set.
     * @param borderWidth
     *            The width of the border.
     */
    public native void setStyle(String fillColor, String borderColor,
            int borderWidth) /*-{
        var node = this.@synopticgwt.shared.GWTNode::jsNode;
        node.rect.attr({
            "stroke" : borderColor,
            "stroke-width" : borderWidth,
            "fill" : fillColor
        });
    }-*/;
    
    /**
     * Sets the style of the node to the following
     * 
     * @param borderColor
     *            The color to which the border will be set.
     * @param borderWidth
     *            The width of the border.
     */
    public native void setStyle(String borderColor, int borderWidth) /*-{
        var node = this.@synopticgwt.shared.GWTNode::jsNode;
        node.rect.attr({
            "stroke" : borderColor,
            "stroke-width" : borderWidth,
        });
    }-*/;

    /**
     * Sets the fill color of the node to the following
     * 
     * @param fillColor
     *            The color with which the node will be filled.
     */
    public native void setStyle(String fillColor) /*-{
        var node = this.@synopticgwt.shared.GWTNode::jsNode;
        node.rect.attr({
            "fill" : fillColor
        });
    }-*/;

    /**
     * Registers hover mouseover with the GWTNode
     * 
     * @param hover
     *            object with java level mouseover function
     */
    public native void setMouseover(MouseEventHandler<GWTNode> hover) /*-{
        var node = this.@synopticgwt.shared.GWTNode::jsNode;
        node.rect.node.onmouseover = node.text.node.onmouseover = function(
                hoverable, obj) {
            return function(e) {
                hoverable.@synopticgwt.client.util.MouseEventHandler::mouseover(Ljava/lang/Object;)(obj);
            };
        }(hover, this);
    }-*/;

    /**
     * Registers hover mouseout with the GWTNode
     * 
     * @param hover
     *            object with java level mouseout function
     */
    public native void setMouseout(MouseEventHandler<GWTNode> hover) /*-{
        var node = this.@synopticgwt.shared.GWTNode::jsNode;
        node.rect.node.onmouseout = node.text.node.onmouseout = function(
                hoverable, obj) {
            return function(e) {
                hoverable.@synopticgwt.client.util.MouseEventHandler::mouseout(Ljava/lang/Object;)(obj);
            };
        }(hover, this);
    }-*/;

    /**
     * Registers a click event with the GWTNode.
     * 
     * @param click
     *            object with java level onclick function
     */
    public native void setOnClick(MouseEventHandler<GWTNode> click) /*-{
        var node = this.@synopticgwt.shared.GWTNode::jsNode;
        node.rect.node.onmouseup = node.text.node.onmouseup = function(
                clickable, obj) {
            return function(e) {
                clickable.@synopticgwt.client.util.MouseEventHandler::onclick(Ljava/lang/Object;Z)(obj, e.shiftKey);
            };
        }(click, this);
    }-*/;
}