package synoptic.invariants.miners;

import synoptic.invariants.TemporalInvariantSet;
import synoptic.model.ChainsTraceGraph;

public interface TOInvariantMiner {
    /**
     * Computes and returns the temporal invariants that hold for the graph g.
     * 
     * @param g
     *            input graph over which to mine invariants
     * @return
     */
    public TemporalInvariantSet computeInvariants(ChainsTraceGraph g, boolean multipleRelations);
}
