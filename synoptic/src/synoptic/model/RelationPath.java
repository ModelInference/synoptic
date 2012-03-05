package synoptic.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import synoptic.util.InternalSynopticException;

/**
 * Represents a single relation path through a trace.
 * 
 * @author timjv
 *
 */
public class RelationPath {
	
	/** First non-INITIAL node in this relation path */
	private EventNode eNode;
	/** Relation this path is over */
	private String relation;
	/** Relation this path uses for transitivity */
	private String transitiveRelation;
	private boolean counted;
	
	private Set<EventType> seen;
    private Map<EventType, Integer> eventCounts;
    private Map<EventType, Map<EventType, Integer>> followedByCounts;
    private Map<EventType, Map<EventType, Integer>> precedesCounts;

	public RelationPath(EventNode eNode, String relation, String transitiveRelation) {
		this.eNode = eNode;
		this.relation = relation;
		this.transitiveRelation = transitiveRelation;
		this.counted = false;
		this.seen = new LinkedHashSet<EventType>();
		this.eventCounts = new LinkedHashMap<EventType, Integer>();
		this.followedByCounts = new LinkedHashMap<EventType, Map<EventType, Integer>>();
		this.precedesCounts = new LinkedHashMap<EventType, Map<EventType, Integer>>();
	}
	
	public void count() {
		EventNode curNode = eNode;
		List<Transition<EventNode>> transitions = curNode.getTransitions();
		
		while (!transitions.isEmpty()) {
			
            if (transitions.size() != 1) {
                throw new InternalSynopticException(
                        "SpecializedInvariantMiner does not work on partially ordered traces.");
            }
			
            // The current event is 'b', and all prior events are 'a' --
            // this notation indicates that an 'a' always occur prior to a
            // 'b' in the path.
			EventType b = curNode.getEType();
			
            // Update the precedes counts based on the a events that
            // preceded the current b event in this path.
            for (EventType a : seen) {
            	Map<EventType, Integer> bValues;
            	if (!precedesCounts.containsKey(a)) {
            		precedesCounts.put(a, new LinkedHashMap<EventType, Integer>());
            	}
            	
            	bValues = precedesCounts.get(a);
            	
            	if (!bValues.containsKey(b)) {
            		bValues.put(b, 0);
            	}
            	
            	bValues.put(b, bValues.get(b) + 1);

            }
            
            // Update the followed by counts for this path: the number of a
            // FollowedBy b at this point in this trace is exactly the
            // number of a's that we've seen so far.
            for (EventType a : seen) {
                Map<EventType, Integer> bValues;
                
                if (!followedByCounts.containsKey(a)) {
                    followedByCounts.put(a, new LinkedHashMap<EventType, Integer>());
                }
                    
                bValues = followedByCounts.get(a);

                bValues.put(b, eventCounts.get(a));
            }
            seen.add(b);
            
            // Update the trace event counts.
            if (!eventCounts.containsKey(b)) {
            	eventCounts.put(b, 1);
            } else {
            	eventCounts.put(b, eventCounts.get(b) + 1);
            }
            
            // Move on to the next node in the trace.
            List<Transition<EventNode>> searchTransitions = curNode.getTransitions(relation);
            
            if (searchTransitions.isEmpty()) {
            	searchTransitions = curNode.getTransitions(transitiveRelation);
            } 
        	
            curNode = searchTransitions.get(0).getTarget();
            transitions = curNode.getTransitions();
		}
		
		counted = true;
	}
	
	public Set<EventType> getSeen() {
		if (!counted) {
			count();
		}
		return Collections.unmodifiableSet(seen);
	}
	
	public Map<EventType, Integer> getEventCounts() {
		if (!counted) {
			count();
		}
		return Collections.unmodifiableMap(eventCounts);
	}
	
	// TODO: Make the return type deeply unmodifiable
	public Map<EventType, Map<EventType, Integer>> getFollowedByCounts() {
		if (!counted) {
			count();
		}
		return Collections.unmodifiableMap(followedByCounts);
	}
	
	// TODO: Make the return type deeply unmodifiable
    public Map<EventType, Map<EventType, Integer>> getPrecedesCounts() {
		if (!counted) {
			count();
		}
    	return Collections.unmodifiableMap(precedesCounts);
    }
    
    public String getRelation() {
    	return relation;
    }
}
