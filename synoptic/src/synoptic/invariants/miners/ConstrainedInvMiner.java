package synoptic.invariants.miners;

import java.util.HashSet;
import java.util.Set;

import synoptic.invariants.AlwaysFollowedInvariant;
import synoptic.invariants.AlwaysPrecedesInvariant;
import synoptic.invariants.BinaryInvariant;
import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.invariants.constraints.TempConstrainedInvariant;
import synoptic.invariants.constraints.UpperBoundConstraint;
import synoptic.model.ChainsTraceGraph;
import synoptic.model.EventNode;
import synoptic.model.Trace;
import synoptic.model.event.Event;
import synoptic.model.event.EventType;
import synoptic.model.interfaces.IRelationPath;
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
	
	@Override
    public TemporalInvariantSet computeInvariants(ChainsTraceGraph g,
            boolean multipleRelations) {
    	
    	TemporalInvariantSet invs = miner.computeInvariants(g, multipleRelations);  	
    	
    	return computeInvariants(g, multipleRelations, invs);    
    }
    
    public static TemporalInvariantSet computeInvariants(ChainsTraceGraph g,
    		boolean multipleRelations, TemporalInvariantSet invs) {
    		
    	TemporalInvariantSet result = new TemporalInvariantSet();
    	for (String r : g.getRelations()) {
            TemporalInvariantSet tmp = computeInvariants(g, multipleRelations, r, invs);
            result.add(tmp);
        }
        return result;
	}
    	
    public static TemporalInvariantSet computeInvariants(ChainsTraceGraph g, 
    		boolean multipleRelations, String relation, TemporalInvariantSet invs) {
    	
    	TemporalInvariantSet constrainedInvs = new TemporalInvariantSet();

    	// Stores generated RelationPaths
        Set<IRelationPath> relationPaths = new HashSet<IRelationPath>();
    	
        // Loop through the traces.
    	for (Trace trace : g.getTraces()) {
            IRelationPath relationPath = null;
            
            if (multipleRelations && !relation.equals(Event.defaultTimeRelationString)) {
                relationPath = trace.getBiRelationalPath(relation, Event.defaultTimeRelationString);
            } else {
                Set<IRelationPath> single = trace.getSingleRelationPaths(relation);
                if (relation.equals(Event.defaultTimeRelationString) && single.size() != 1) {
                    throw new IllegalStateException("Multiple relation subraphs for single relation graph");
                }
                relationPath = single.toArray(new IRelationPath[1])[0];
            }
      
            if (relationPath == null) {
                continue;
            }
            relationPaths.add(relationPath);
    	}
    
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
        		
        		
        		for (IRelationPath path : relationPaths) {
        			//TODO
        			// 1) Loop through each relationPath
        			// 2) Retrieve upper and lower bound for invariant like
        			//    commented out loop below.
        			// 3) Keep track of each of these lower and upper bounds.
        			// 4) Get the min of the lower and the max of the upper
        			// 5) Set the threshold for particular invariant using min and max in 4). 
            	}
        		
        		// Iterate all nodes in graph and check for nodes having
        		// transitions containing a and b EventType above.
//        		for (EventNode node : g.getNodes()) {
//        			if (node.getEType().equals(a)) {
//        				recentA = node;
//        				if (first == null) {
//        					first = node.getTime();
//        				}
//        			}
//        			if (node.getEType().equals(b)) {
//        				if (recentA != null) {
//        					ITime delta = node.getTime().computeDelta(recentA.getTime());
//            				lowerBound = delta;
//            				// Found new lowerBound.
//            				if (delta.lessThan(lowerBound)) {
//            					lowerBound = delta;
//            				}
//        				}
//        				last = node.getTime();
//        			}
//        		}
        		
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

