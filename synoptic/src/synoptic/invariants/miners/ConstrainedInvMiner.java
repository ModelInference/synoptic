package synoptic.invariants.miners;

import synoptic.invariants.AlwaysFollowedInvariant;
import synoptic.invariants.AlwaysPrecedesInvariant;
import synoptic.invariants.BinaryInvariant;
import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.invariants.constraints.TempConstrainedInvariant;
import synoptic.invariants.constraints.UpperBoundConstraint;
import synoptic.model.ChainsTraceGraph;
import synoptic.model.EventNode;
import synoptic.model.event.EventType;
import synoptic.util.time.ITime;
import synoptic.util.time.ITotalTime;

/**
 * <b>Work in progress.</b> Mines constrained invariants from totally ordered
 * traces. Uses other totally ordered invariant miners to first mine the
 * unconstrained invariants (if not given these explicitly), and then mines the
 * constrains on these invariants by walking the trace directly.
 */
public class ConstrainedInvMiner extends InvariantMiner implements
        ITOInvariantMiner {
	private ITOInvariantMiner miner;

	public ConstrainedInvMiner(ITOInvariantMiner miner) {
		this.miner = miner;
    }
	
    public TemporalInvariantSet computeInvariants(ChainsTraceGraph g,
            boolean multipleRelations) {
    	TemporalInvariantSet invs = miner.computeInvariants(g, multipleRelations);
    	return computeInvariants(g, multipleRelations, invs);    
    }
    
    public static TemporalInvariantSet computeInvariants(ChainsTraceGraph g,
    		boolean multipleRelations, TemporalInvariantSet invs) {
    	
    	TemporalInvariantSet constrainedInvs = new TemporalInvariantSet();

		// Iterate through all invariants.
        for (ITemporalInvariant i : invs.getSet()) {
        	// Found invariants that can be constrained.
        	if (i instanceof AlwaysFollowedInvariant ||
        		i instanceof AlwaysPrecedesInvariant) {
        		        		
        		EventType a = ((BinaryInvariant) i).getFirst();
        		EventType b = ((BinaryInvariant) i).getSecond();
        		
        		// First occurrence of a.
        		ITime first = null;
        		
        		// Last occurrence of b.
        		ITime last = null;
        		
        		ITime lowerBound = null;
        		
        		// Track nodes of event a for computing lowerBound;
        		EventNode recentA = null;
        		
        		// Iterate all nodes in graph and check for nodes having
        		// transitions containing a and b EventType above.
        		for (EventNode node : g.getNodes()) {
        			if (node.getEType().equals(a)) {
        				recentA = node;
        				if (first == null) {
        					first = node.getTime();
        				}
        			}
        			if (node.getEType().equals(b)) {
        				if (recentA != null) {
        					ITime delta = node.getTime().computeDelta(recentA.getTime());
            				lowerBound = delta;
            				// Found new lowerBound.
            				if (delta.lessThan(lowerBound)) {
            					lowerBound = delta;
            				}
        				}
        				last = node.getTime();
        			}
        		}
        		
        		//TODO 
        		//Unsure of what to do for this case. For now, 
        		//for an INITIAL node, set min value to 0 to not
        		//break code.
        		if (last == null) {
        			last = new ITotalTime(0);
        		}
        		
        		ITime upperBound = last.computeDelta(first);
        		
        		if (i instanceof AlwaysFollowedInvariant) {
        			TempConstrainedInvariant<AlwaysFollowedInvariant> lowerConstrInv =
            			new TempConstrainedInvariant<AlwaysFollowedInvariant>(
            					(AlwaysFollowedInvariant) i, new UpperBoundConstraint(lowerBound));
        			TempConstrainedInvariant<AlwaysFollowedInvariant> upperConstrInv =
            			new TempConstrainedInvariant<AlwaysFollowedInvariant>(
            					(AlwaysFollowedInvariant) i, new UpperBoundConstraint(upperBound));
        			constrainedInvs.add(lowerConstrInv);
        			constrainedInvs.add(upperConstrInv);
        		} else { // AlwaysPrecedesInvariant
        			TempConstrainedInvariant<AlwaysPrecedesInvariant> lowerConstrInv =
            			new TempConstrainedInvariant<AlwaysPrecedesInvariant>(
            					(AlwaysPrecedesInvariant) i, new UpperBoundConstraint(lowerBound));
        			TempConstrainedInvariant<AlwaysPrecedesInvariant> upperConstrInv =
            			new TempConstrainedInvariant<AlwaysPrecedesInvariant>(
            					(AlwaysPrecedesInvariant) i, new UpperBoundConstraint(upperBound));
        			constrainedInvs.add(lowerConstrInv);
        			constrainedInvs.add(upperConstrInv);
        		}
        	}
        }
        //return constrainedInvs;
        return invs;
	}
}

