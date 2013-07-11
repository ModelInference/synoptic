package synoptic.model.interfaces;

import java.util.Map;
import java.util.Set;

import synoptic.model.EventNode;
import synoptic.model.event.EventType;

/**
 * Represents a path through the TraceGraph for some set of relations. This
 * handles both multi-relational and uni-relational inputs logs.
 */
public interface IRelationPath {

    public Set<EventType> getSeen();

    public Map<EventType, Integer> getEventCounts();

    /**
     * Map<a, Map<b, count>> iff the number of a's that appeared before this b
     * is count.
     */
    public Map<EventType, Map<EventType, Integer>> getFollowedByCounts();

    /**
     * Map<a, Map<b, count>> iff the number of b's that appeared after this a is
     * count.
     */
    public Map<EventType, Map<EventType, Integer>> getPrecedesCounts();
    
    /**
     * @return first non-INITIAL node in this relation path
     */
    public EventNode getFirstNode();
    
    /**
     * @return final non-TERMINAL node in this relation path
     */
    public EventNode getLastNode();
    
    /**
     * @return relation that path is over
     */
    public String getRelation();
}
