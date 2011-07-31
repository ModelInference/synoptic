package synopticgwt.shared;

import java.io.Serializable;

/**
 * Represents an edge between two GWTNodes in GWTGraph.
 */
public class GWTEdge implements Serializable {

    private static final long serialVersionUID = 1L;

    // Nodes are identified by their hashcodes at the moment.
    int src = -1;
    int dst = -1;

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

    public GWTEdge(int src, int dst, double weight) {
        this.src = src;
        this.dst = dst;
        this.weight = weight;
    }

    public int getSrc() {
        assert src != -1;
        return src;
    }

    public int getDst() {
        assert dst != -1;
        return dst;
    }

    public double getWeight() {
        assert weight != -1;
        return weight;
    }
}
