package synoptic.model.interfaces;

import java.util.Map;
import java.util.Set;

import synoptic.model.EventType;

public interface RelationPath {

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
    
}
