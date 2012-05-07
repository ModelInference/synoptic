package synoptic.invariants.miners;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import synoptic.invariants.AlwaysFollowedInvariant;
import synoptic.invariants.AlwaysPrecedesInvariant;
import synoptic.invariants.BinaryInvariant;
import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.invariants.constraints.LowerBoundConstraint;
import synoptic.invariants.constraints.TempConstrainedInvariant;
import synoptic.invariants.constraints.UpperBoundConstraint;
import synoptic.model.ChainsTraceGraph;
import synoptic.model.EventNode;
import synoptic.model.Trace;
import synoptic.model.Transition;
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
    	
    	// The set of constrained invariants that we will be returning.
    	TemporalInvariantSet constrainedInvs = new TemporalInvariantSet();

    	// Stores generated RelationPaths
        Set<IRelationPath> relationPaths = new HashSet<IRelationPath>();
    	
        // Loop through the traces.
    	for (Trace trace : g.getTraces()) {
            
            if (multipleRelations && !relation.equals(Event.defTimeRelationStr)) {
                IRelationPath relationPath = trace.getBiRelationalPath(relation, Event.defTimeRelationStr);
                relationPaths.add(relationPath);
            } else {
                Set<IRelationPath> subgraphs = trace.getSingleRelationPaths(relation);
                if (relation.equals(Event.defTimeRelationStr) && subgraphs.size() != 1) {
                    throw new IllegalStateException("Multiple relation subraphs for ordering relation graph");
                }
                relationPaths.addAll(subgraphs);
            }
            
    	}
    	
    	// For each AFby or AP invariant.
    	// 		For each relationPath.
    	//			Go through each node in path.
    	//			Find nodes that match event types for invariant.
    	//			Find upperBound  
    	// 			Find and lowerBound to create ConstrainedInvariants.
    	for (ITemporalInvariant i : invs.getSet()) {
    		if (i instanceof AlwaysFollowedInvariant || i instanceof AlwaysPrecedesInvariant) {
    			EventType a = ((BinaryInvariant) i).getFirst();
        		EventType b = ((BinaryInvariant) i).getSecond();
        		
        		ITime lowerBound = null;
        		ITime upperBound = null;
        		 
        		for (IRelationPath relationPath : relationPaths) {
        			// First occurrence of a and last occurrence of b.
            		// last - first = upper bound
            		ITime first = null;
            		ITime last = null;
            		
            		// Track nodes of event type a for computing lowerBound.
            		EventNode recentA = null;
        			
            		EventNode curr = relationPath.getFirstNode();
            		EventNode end = relationPath.getLastNode();
            		Transition<EventNode> trans;
            		            		   
            		while (true) {
        				if (curr.getEType().equals(a)) {
        					recentA = curr;
        					if (first == null) {
        						first = curr.getTime();
        					}
        				}
        				
        				if (curr.getEType().equals(b)) {
        					// If node of event type a is found already, then we can obtain
        					// a delta value since we now found node of event type b.
        					if (recentA != null) {	
        						ITime delta = curr.getTime().computeDelta(recentA.getTime());
        						if (lowerBound == null || delta.lessThan(lowerBound)) {
        							lowerBound = delta;
        						}
        					}
        					last = curr.getTime();
        				}	
            			        				
        				// Dealing with single relation, so only one transition.
        				trans = curr.getAllTransitions().get(0);
        				
        				// Reached ending node in path.
        				if (curr.equals(end)) {
        					break;
        				} else {
        					curr = trans.getTarget();
        				}
            		}
            		
            		// relationPath contains the invariant.
            		// Note: this will exclude invariants with an INITIAL node, since that
            		// will yield a null lowerbound and upperbound.
            		if (first != null && last != null) {
            			ITime delta = last.computeDelta(first);
            			if (upperBound == null || upperBound.lessThan(delta)) {
            				upperBound = delta;
            			}       			
            		} 		
        		}
        		
        		// TODO used for testing purposes, remove when done.
        		logger.info("Eventtype a = " + a + ", b = " + b + ", upperbound = " + upperBound + ", lowerbound = " + lowerBound);	
    		
        		if (i instanceof AlwaysFollowedInvariant) {
        			TempConstrainedInvariant<AlwaysFollowedInvariant> lowerConstrInv =
        				new TempConstrainedInvariant<AlwaysFollowedInvariant>(
        					(AlwaysFollowedInvariant) i, new LowerBoundConstraint(lowerBound));
        			TempConstrainedInvariant<AlwaysFollowedInvariant> upperConstrInv =
        				new TempConstrainedInvariant<AlwaysFollowedInvariant>(
        					(AlwaysFollowedInvariant) i, new UpperBoundConstraint(upperBound));
        			
        			constrainedInvs.add(lowerConstrInv);
        			constrainedInvs.add(upperConstrInv);
        		} else { // AlwaysPrecedesInvariant
        			TempConstrainedInvariant<AlwaysPrecedesInvariant> lowerConstrInv =
        				new TempConstrainedInvariant<AlwaysPrecedesInvariant>(
        					(AlwaysPrecedesInvariant) i, new LowerBoundConstraint(lowerBound));
        			TempConstrainedInvariant<AlwaysPrecedesInvariant> upperConstrInv =
        				new TempConstrainedInvariant<AlwaysPrecedesInvariant>(
        					(AlwaysPrecedesInvariant) i, new UpperBoundConstraint(upperBound));
        			
        			constrainedInvs.add(lowerConstrInv);
        			constrainedInvs.add(upperConstrInv);
        		}
    		}
    	}
    	return constrainedInvs;
        //return invs;
    }
}