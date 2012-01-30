package synopticgwt.shared;

import java.io.Serializable;

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
        assert count != 0;
        return count;
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
}
