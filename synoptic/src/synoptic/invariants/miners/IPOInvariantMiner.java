package synoptic.invariants.miners;

import synoptic.invariants.TemporalInvariantSet;
import synoptic.model.DAGsTraceGraph;

/**
 * Interface that all invariant miners that process partially ordered (PO) input
 * logs should support. Such logs are represented as DAGsTraceGraph instances.
 */
public interface IPOInvariantMiner {
    /**
     * Computes and returns the temporal invariants that hold for the graph g.
     * 
     * @param g
     *            input graph over which to mine invariants
     * @return
     */
    public TemporalInvariantSet computeInvariants(DAGsTraceGraph g);
}
