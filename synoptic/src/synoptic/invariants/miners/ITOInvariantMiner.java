package synoptic.invariants.miners;

import synoptic.invariants.TemporalInvariantSet;
import synoptic.model.ChainsTraceGraph;

/**
 * Interface that all invariant miners that process totally ordered (TO) input
 * logs should support. Such logs are represented as ChainsTraceGraph instances.
 */
public interface ITOInvariantMiner {
    /**
     * Computes and returns the temporal invariants that hold for the graph g.
     * 
     * @param g
     *            input graph over which to mine invariants
     * @return
     */
    public TemporalInvariantSet computeInvariants(ChainsTraceGraph g,
            boolean multipleRelations);
}
