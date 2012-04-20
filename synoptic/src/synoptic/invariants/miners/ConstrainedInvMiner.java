package synoptic.invariants.miners;

import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.model.ChainsTraceGraph;
import synoptic.model.event.EventType;

/**
 * Mines constrained invariants from totally ordered traces. Uses other totally
 * ordered invariant miners to first mine the unconstrained invariants (if not
 * given these explicitly), and then mines the constrains on these invariants by
 * walking the trace directly.
 */
public class ConstrainedInvMiner extends InvariantMiner implements
        TOInvariantMiner {
    private TOInvariantMiner miner;

    public ConstrainedInvMiner(boolean useTransitiveClosureMining) {
        if (useTransitiveClosureMining) {
            miner = new TransitiveClosureInvMiner();
        } else {
            miner = new ChainWalkingTOInvMiner();
        }
    }

    @Override
    public TemporalInvariantSet computeInvariants(ChainsTraceGraph g,
            boolean multipleRelations) {

        // TODO: add an invariant set argument -- used when the unconstrained
        // invariants were already mined, and the purpose of this code would be
        // to augment this set with constraints (generating a new set
        // of constrained invariants as the final result).

        TemporalInvariantSet invs = miner.computeInvariants(g,
                multipleRelations);
        TemporalInvariantSet constraintInvs = new TemporalInvariantSet();

        for (ITemporalInvariant i : invs.getSet()) {
            // Get times and see if satisfies constraint

            EventType first = i.getPredicates().iterator().next();
            EventType second = i.getPredicates().iterator().next();

        }
        return constraintInvs;
    }
}
