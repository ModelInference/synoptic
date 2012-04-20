package synoptic.invariants.miners;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

import synoptic.invariants.AlwaysConcurrentInvariant;
import synoptic.invariants.AlwaysFollowedInvariant;
import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.TempConstrainedInvariant;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.model.ChainsTraceGraph;
import synoptic.model.EventType;

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
			// Get times and see if satisfies constraint
			
			EventType first = i.getPredicates().iterator().next();
			EventType second = i.getPredicates().iterator().next();
			
			
		}
		return constraintInvs;
	}	
}
