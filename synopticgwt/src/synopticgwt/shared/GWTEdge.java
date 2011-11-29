package synopticgwt.shared;

import java.io.Serializable;

/**
 * Represents an edge between two GWTNodes in GWTGraph.
 */
public class GWTEdge implements Serializable {

    private static final long serialVersionUID = 1L;

    private GWTNode src = null;
    private GWTNode dst = null;

    // Edge weight indicates transition probability from source node to
    // destination node.
    double weight = -1;

    /**
     * A default constructor is necessary to avoid a SerializationException:
     * http://blog.holyjeez.com/2008/09/16/gwt-serialization-error-missing-
     * default-constructor/
     */
    public GWTEdge() {
        // Empty constructor to avoid SerializationException.
    }

    public GWTEdge(GWTNode src, GWTNode dst, double weight) {
        this.src = src;
        this.dst = dst;
        this.weight = weight;
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
}
