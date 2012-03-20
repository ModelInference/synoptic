package synoptic.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Maintains the set of all relation paths (a path corresponding to a particular
 * relation) part of a single input trace. If a relation exists in a trace, then
 * there is a single unique relation path corresponding to the relation.
 * 
 * @author timjv
 */
public class Trace {
    /*
     * A relation string and an EventNode uniquely specify a relation path
     * because we can simply walk the event nodes using their internal
     * transitions along edges labeled with the relation string.
     */

    /** Relation string -> Representative relation path. */
    private Map<String, RelationPath> paths;

    public Trace() {
        this.paths = new HashMap<String, RelationPath>();
    }

    /**
     * Adds the relation path specified by relation and eNode to this trace, if
     * there is no pre-existing relation path specified for the relation.
     * 
     * @param relation
     *            Type of relation for the path.
     * @param eNode
     *            First node in the relation path.
     */
    public void addRelationPath(String relation, EventNode eNode, boolean initialConnected) {
        if (paths.containsKey(relation)) {
            /*
             * A relation path has been specified for relation. Check that
             * relation paths are not being created when they already exist.
             */
            throw new IllegalArgumentException("Trace already contains path");
        }
        paths.put(relation, new RelationPath(eNode, relation,
                Event.defaultTimeRelationString, initialConnected));
    }

    public void markRelationPathFinalNode(String relation, EventNode eNode) {
        RelationPath path = paths.get(relation);
        path.setFinalNode(eNode);
    }

    public Set<EventType> getSeen(String relation) {
        if (!paths.containsKey(relation)) {
            throw new IllegalArgumentException("Trace doesn't contain the "
                    + relation + " relation");
        }
        return paths.get(relation).getSeen();
    }

    public Map<EventType, Integer> getEventCounts(String relation) {
        if (!paths.containsKey(relation)) {
            throw new IllegalArgumentException("Trace doesn't contain the "
                    + relation + " relation");
        }
        return paths.get(relation).getEventCounts();
    }

    // TODO: Make the return type deeply unmodifiable
    public Map<EventType, Map<EventType, Integer>> getFollowedByCounts(
            String relation) {
        if (!paths.containsKey(relation)) {
            throw new IllegalArgumentException("Trace doesn't contain the "
                    + relation + " relation");
        }
        return paths.get(relation).getFollowedByCounts();
    }

    // TODO: Make the return type deeply unmodifiable
    public Map<EventType, Map<EventType, Integer>> getPrecedesCounts(
            String relation) {
        if (!paths.containsKey(relation)) {
            throw new IllegalArgumentException("Trace doesn't contain the "
                    + relation + " relation");
        }
        return paths.get(relation).getPrecedesCounts();
    }

    public boolean hasRelation(String relation) {
        return paths.containsKey(relation);
    }

    public Set<String> getRelations() {
        return Collections.unmodifiableSet(paths.keySet());
    }

}
