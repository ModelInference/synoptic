package synoptic.invariants.miners;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

/**
 * <b>Work in progress.</b> Mines constrained invariants from totally ordered
 * traces. Constrained invariants are the standard set of invariants with time 
 * constraints on the time between events in an execution. Invariants that
 * can be constrained are AlwaysFollowedInvariant and AlwaysPrecedesInvariant.
 * 
 * Uses other totally ordered invariant miners to first mine the unconstrained 
 * invariants (if not given these explicitly). Mines constraints for these
 * unconstrained invariants by walking the trace directly. New invariants are not
 * created, the unconstrained invariants are just augmented with constraints.
 */
public class ConstrainedInvMiner extends InvariantMiner implements
        ITOInvariantMiner {
	private ITOInvariantMiner miner;
	
	public ConstrainedInvMiner(ITOInvariantMiner miner) {
		this.miner = miner;
    }
	
	/**
	 * Uses the miner passed into the constructor to first mine unconstrained
	 * invariants. Then walks the trace to compute constraints for
	 * AlwaysFollowedInvariant and AlwaysPrecedesInvariant. Returns a set of
	 * these constrained invariants.
	 * @param g
     *            a chain trace graph of nodes of type LogEvent
     * @param multipleRelations
     *            whether or not nodes have multiple relations
     * @return the set of constrained temporal invariants
	 */
	@Override
    public TemporalInvariantSet computeInvariants(ChainsTraceGraph g,
            boolean multipleRelations) {
    	
		TemporalInvariantSet invs = miner.computeInvariants(g, multipleRelations);  	
    	return computeInvariants(g, multipleRelations, invs);    
    }
    
	/**
	 * Given a set of unconstrained invariants, walks the trace graph to
	 * compute constraints for AFby and AP invariants. Augments these
	 * existing invariants with constraints. Returns a set of these constrained
	 * invariants.
	 * @param g
	 * 				a chain trace graph of nodes of type LogEvent
	 * @param multipleRelations
	 * 				whether or not nodes have multiple relations
	 * @param invs
	 * 				set of unconstrained invariants returned from previous invariant miner
	 * @return the set of constrained temporal invariants
	 */
    public static TemporalInvariantSet computeInvariants(ChainsTraceGraph g,
    		boolean multipleRelations, TemporalInvariantSet invs) {
    		
    	TemporalInvariantSet result = new TemporalInvariantSet();
    	
    	// Stores generated RelationPaths
        Set<IRelationPath> relationPaths = new HashSet<IRelationPath>();
    	
        for (ITemporalInvariant i : invs.getSet()) {
        	String relation = i.getRelation();
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
        }
    	
    	for (ITemporalInvariant i : invs.getSet()) {
    		if (!(i instanceof AlwaysFollowedInvariant || i instanceof AlwaysPrecedesInvariant)) {
    			continue; 
    		}
			computeInvariants(g, multipleRelations, relationPaths, i);
    	}

    	//return result;
    	return invs;
    }
    	
    public static TemporalInvariantSet computeInvariants(ChainsTraceGraph g, 
    		boolean multipleRelations, Set<IRelationPath> relationPaths, ITemporalInvariant i) {
    	
    	// The set of constrained invariants that we will be returning.
    	TemporalInvariantSet constrainedInvs = new TemporalInvariantSet();
    	
		EventType a = ((BinaryInvariant) i).getFirst();
		EventType b = ((BinaryInvariant) i).getSecond();
		
		// TODO hackish implementation right now, where 0 index = lowerbound, 1 index = upperbound
		// make a container class holding two ITimes?
		List<ITime> bounds = new ArrayList<ITime>(2);
		bounds.add(null);
		bounds.add(null);
		
		// 		For each relationPath.
    	//			Go through each node in path.
    	//			Find nodes that match event types for invariant.
    	//			Find upperBound  
    	// 			Find and lowerBound to create ConstrainedInvariants.
		for (IRelationPath relationPath : relationPaths) {
			EventNode curr = relationPath.getFirstNode();
    		EventNode end = relationPath.getLastNode();
    		
			bounds = computeBounds(a, b, curr, end, bounds);
		}
		
		// TODO used for testing purposes, remove when done.
		logger.info("Eventtype a = " + a + ", b = " + b + ", lowerbound = " + bounds.get(0) + ", upperbound = " + bounds.get(1));	
	
		if (i instanceof AlwaysFollowedInvariant) {
			TempConstrainedInvariant<AlwaysFollowedInvariant> lowerConstrInv =
				new TempConstrainedInvariant<AlwaysFollowedInvariant>(
					(AlwaysFollowedInvariant) i, new LowerBoundConstraint(bounds.get(0)));
			TempConstrainedInvariant<AlwaysFollowedInvariant> upperConstrInv =
				new TempConstrainedInvariant<AlwaysFollowedInvariant>(
					(AlwaysFollowedInvariant) i, new UpperBoundConstraint(bounds.get(1)));
			
			constrainedInvs.add(lowerConstrInv);
			constrainedInvs.add(upperConstrInv);
		} else if (i instanceof AlwaysPrecedesInvariant) {
			TempConstrainedInvariant<AlwaysPrecedesInvariant> lowerConstrInv =
				new TempConstrainedInvariant<AlwaysPrecedesInvariant>(
					(AlwaysPrecedesInvariant) i, new LowerBoundConstraint(bounds.get(0)));
			TempConstrainedInvariant<AlwaysPrecedesInvariant> upperConstrInv =
				new TempConstrainedInvariant<AlwaysPrecedesInvariant>(
					(AlwaysPrecedesInvariant) i, new UpperBoundConstraint(bounds.get(1)));
			
			constrainedInvs.add(lowerConstrInv);
			constrainedInvs.add(upperConstrInv);
		}
		return constrainedInvs;
    }
    
    /**
     * Computes and returns lower and upper bound values for a relationPath.
     * @param a
     * @param b
     * @param start 
     * @param end
     * @param bounds
     * @return
     */
    private static List<ITime> computeBounds(EventType a, EventType b, EventNode start, EventNode end,
			List<ITime> bounds) {
    	// First occurrence of a and last occurrence of b.
    	// last - first = upperBound
    	ITime first = null;
    	ITime last = null;

    	// Track nodes of event type a for computing lowerBound.
    	EventNode recentA = null;

    	Transition<EventNode> trans;

    	List<ITime> result = new ArrayList<ITime>(2);
    	result.add(bounds.get(0));
    	result.add(bounds.get(1));

    	while (true) {
    		if (start.getEType().equals(a)) {
    			recentA = start;
    			if (first == null) {
    				first = start.getTime();
    			}
    		}

    		if (start.getEType().equals(b)) {
    			// If node of event type a is found already, then we can obtain
    			// a delta value since we now found node of event type b.
    			if (recentA != null) {	
    				ITime delta = start.getTime().computeDelta(recentA.getTime());
    				if (result.get(0) == null || delta.lessThan(result.get(0))) {
    					result.add(0, delta);
    				}
    			}
    			last = start.getTime();
    		}	
	
    		// Dealing with a TO log, so only one transition available to use.
    		assert(start.getAllTransitions().size() == 1);
    		trans = start.getAllTransitions().get(0);

    		// Reached ending node in path.
    		if (start.equals(end)) {
    			break;
    		} else {
    			start = trans.getTarget();
    		}
    	}

    	// relationPath contains the invariant.
    	// Note: this will exclude invariants with an INITIAL node, since that
    	// will yield a null lowerbound and upperbound.
    	if (first != null && last != null) {
    		ITime delta = last.computeDelta(first);
    		if (result.get(1) == null || result.get(1).lessThan(delta)) {
    			result.add(1, delta);
    		}       			
    	} 
    	return result;
    }    
}