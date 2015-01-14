package synoptic.invariants.miners;

import java.util.Set;

import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.model.ChainsTraceGraph;

/**
 * Interface that all invariant miners that process totally ordered (TO) input
 * logs should support. Such logs are represented as ChainsTraceGraph instances.
 */
public interface ITOInvariantMiner {

    /**
     * Returns a set of classes of the invariants which will be mined by this
     * miner.
     * 
     * @return a set of classes of the invariants which will be mined by this
     *         miner
     */
    public Set<Class<? extends ITemporalInvariant>> getMinedInvariants();

    /**
     * Returns a set of classes of the invariants which will NOT be mined by
     * this miner.
     * 
     * @return a set of classes of the invariants which will NOT be mined by
     *         this miner
     */
    public Set<Class<? extends ITemporalInvariant>> getIgnoredInvariants();

    /**
     * Computes and returns the temporal invariants that hold for the graph g.
     * 
     * @param g
     *            input graph over which to mine invariants
     * @return
     */
    public TemporalInvariantSet computeInvariants(ChainsTraceGraph g,
            boolean multipleRelations, boolean supportCount);
}
