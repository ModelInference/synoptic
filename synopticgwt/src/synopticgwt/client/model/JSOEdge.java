package synopticgwt.client.model;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * A class designed to interface directly with an instance of a dracula node.
 * 
 * @author andrew
 * 
 */
public class JSOEdge extends JavaScriptObject {

    // JSO types always have empty constructors.
    protected JSOEdge() {
    }

    /**
     * Sets the edge's label to the following
     * 
     * @param label
     *            The label to which the edge's label will be set.
     */
    public native final void setLabel(String label) /*-{
        this.connection.label.attr({
            "text" : label
        });
    }-*/;

    /**
     * Sets the style of the displayed edge
     * 
     * <p>
     * IMPORTANT NOTE: When changing the state of the edges in the Dracula Model
     * make sure to change the attributes using the "attr" command to change the
     * "connection.fg" field within each specific edge. This is because, when
     * changing the style attributes of the edge -- for example, edge.style.fill
     * = "#fff" -- when Dracula redraws the edge, more often than not, it
     * creates a new field (edge.connection.bg) to fill the color behind the
     * edge in question. This is important to note because all style changes
     * done outside of this method currently adhere to altering only the
     * edge.connection.fg field. So, if any changes are made to the edges where
     * the edge.connection.bg field is introduced, this WILL NOT clear those
     * changes from the edge's state, and may have to be appended to this code.
     * </p>
     * 
     * @param color
     *            The color to set the edge to.
     * @param strokeWidth
     *            The stroke width to set the edge to.
     */
    public native final void setStyle(String color, int strokeWidth) /*-{
        this.connection.fg.attr({
            "stroke" : color,
            "stroke-width" : strokeWidth
        });
    }-*/;
}
