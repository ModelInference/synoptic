package synopticgwt.shared;

import java.io.Serializable;

import synopticgwt.client.model.JSEdge;

import com.google.gwt.i18n.client.NumberFormat;

/**
 * Represents an edge between two GWTNodes in GWTGraph.
 */
public class GWTEdge implements Serializable {

    // TODO: Add a field containing a JSNI object and
    // a method that can be used for changing the style
    // of said object to allow this object to interact
    // with Dracula Graph edges.

    private static final long serialVersionUID = 1L;

    private GWTNode src = null;
    private GWTNode dst = null;

    private JSEdge jsEdge;

    // Edge weight indicates transition probability from source node to
    // destination node.
    double weight = -1;

    // Edge count indicates the total number of transitions from source node to
    // destination node.
    int count = 0;

    /**
     * A default constructor is necessary to avoid a SerializationException:
     * http://blog.holyjeez.com/2008/09/16/gwt-serialization-error-missing-
     * default-constructor/
     */
    public GWTEdge() {
        // Empty constructor to avoid SerializationException.
    }

    public GWTEdge(GWTNode src, GWTNode dst, double weight, int count) {
        this.src = src;
        this.dst = dst;
        this.weight = weight;
        this.count = count;
    }

    public GWTNode getSrc() {
        assert src != null;
        return src;
    }

    public GWTNode getDst() {
        assert dst != null;
        return dst;
    }

    public double getWeight() {
        assert weight != -1;
        return weight;
    }

    public int getCount() {
        // TODO: remove this comment when
        // errors have been addressed.
        // assert count != 0;
        return count;
    }

    /**
     * <pre>
     * NOTE: This method is a copy of
     * synoptic.model.export.GraphExportFormatter.probToString()
     * 
     * Unfortunately, there is no way to unify these two methods without passing
     * probabilities as both doubles and strings from the server, or as strings
     * and then converting them to doubles. Both of alternatives are ugly enough
     * to make this duplication ok in this case.
     * </pre>
     */
    private static String probToString(double prob) {
        return NumberFormat.getFormat("0.00").format(
                Math.round(prob * 100.0) / 100.0);
    }

    public String getWeightStr() {
        return probToString(this.getWeight());
    }

    public String getCountStr() {
        return ((Integer) this.getCount()).toString();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (!(other instanceof GWTEdge))
            return false;
        GWTEdge o = (GWTEdge) other;
        return this.src.equals(o.getSrc()) && this.dst.equals(o.getDst())
                && weight == o.getWeight() && count == o.getCount();
    }

    @Override
    public int hashCode() {
        int result = 23;
        result = 37 * result + this.src.hashCode();
        result = 41 * result + this.dst.hashCode();
        result = 47 * result + (new Double(weight)).hashCode();
        result = 51 * result + (new Integer(count)).hashCode();
        return result;
    }

    /**
     * Sets the value of the internal javascript object reference.
     */
    public void setJSEdge(JSEdge edge) {
        this.jsEdge = edge;
    }

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
    public native void setStyle(String color, int strokeWidth) /*-{
        var edge = this.@synopticgwt.shared.GWTEdge::jsEdge;
        edge.connection.fg.attr({
            stroke : color,
            "stroke-width" : strokeWidth
        });
    }-*/;
}
