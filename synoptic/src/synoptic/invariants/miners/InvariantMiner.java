package synoptic.invariants.miners;

import java.util.logging.Logger;

/**
 * Base class for all invariant miners.
 */
public abstract class InvariantMiner {
    protected static Logger logger = Logger.getLogger("TemporalInvSet Logger");

    /**
     * Computes and returns the temporal invariants that hold for the graph g.
     * 
     * @param g
     *            input graph over which to mine invariants
     * @return
     */
    // public TemporalInvariantSet computeInvariants(TraceGraph g) {
    // throw new InternalSynopticException(
    // "computeInvariants must be overridden in a derived class.");
    // }

}