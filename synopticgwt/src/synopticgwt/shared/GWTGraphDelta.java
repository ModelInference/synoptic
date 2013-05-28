package synopticgwt.shared;

import java.io.Serializable;

/**
 * Represents a step of model refinement.
 */
public class GWTGraphDelta implements Serializable {

    private static final long serialVersionUID = 1L;

    private GWTGraph graph;

    /** ID of the node that was refined during this step. */
    private GWTNode refinedNode;

    /** Invariants that remain unsatisfied, after the delta is applied. */
    private GWTInvariantSet unsatInvs;

    public GWTGraphDelta() {
        // Empty constructor to avoid SerializationException.
    }

    public GWTGraphDelta(GWTGraph g, GWTNode refinedNode, GWTInvariantSet unsatInvs) {
        this.graph = g;
        this.refinedNode = refinedNode;
        this.unsatInvs = unsatInvs;
    }

    public GWTGraph getGraph() {
        return graph;
    }

    public GWTNode getRefinedNode() {
        return refinedNode;
    }

    public GWTInvariantSet getUnsatInvs() {
        return unsatInvs;
    }
}
