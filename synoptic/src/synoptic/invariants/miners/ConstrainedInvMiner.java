package synoptic.invariants.miners;

import java.util.LinkedHashSet;
import java.util.Set;

import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.model.ChainsTraceGraph;

/**
 *	Goal: mine invariants, then make constrained invariants
 */
public class ConstrainedInvMiner extends InvariantMiner implements TOInvariantMiner {
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
		TemporalInvariantSet invs = miner.computeInvariants(g, multipleRelations);
		TemporalInvariantSet constraintInvs = new TemporalInvariantSet();
		
		
		for (ITemporalInvariant i : invs.getSet()) {
		
		}
		return constraintInvs;
	}	
}
