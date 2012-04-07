package synoptic.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import synoptic.model.interfaces.ITransition;
import synoptic.util.InternalSynopticException;

/**
 * Provides access to all of the relation paths within this trace. Relations can
 * be associated with zero or more relation paths.
 * 
 * @author timjv
 */
public class Trace {

    /** Relations -> First non-INITIAL node for each relation in this trace */
    private Map<String, EventNode> relationToInitialNodes;
    // TODO: Is there a way to statically define this set?
    private Set<String> defaultTimeRelationStringSet;

    public Trace() {
        this.relationToInitialNodes = new HashMap<String, EventNode>();
        defaultTimeRelationStringSet = new HashSet<String>();
        defaultTimeRelationStringSet.add(Event.defaultTimeRelationString);
    }

    public void addInitialNode(String relation, EventNode eNode) {
        if (relationToInitialNodes.containsKey(relation)) {
            throw new IllegalArgumentException(
                    "Trace already contains initial node for " + relation);
        }

        relationToInitialNodes.put(relation, eNode);
    }

    /**
     * Returns zero or more RelationPaths for every subgraph of the relation
     * type.
     * 
     * @param relation
     * @return
     */
    public Set<RelationPath> getSingleRelationPaths(String relation) {
        /*
         * the initialTransitivelyConnected parameter is always false when
         * constructing relation paths in this method because the constructed
         * relation paths never have transitive connections. Each relation path
         * is either an island or the full trace.
         */

        // Relation paths only get added to results when they are terminated
        Set<RelationPath> results = new HashSet<RelationPath>();

        EventNode curNode;
        RelationPath pendingPath = null;

        Set<String> relationSet = new HashSet<String>();
        relationSet.add(relation);

        if (relationToInitialNodes.containsKey(relation)) {
            curNode = relationToInitialNodes.get(relation);
            pendingPath = new RelationPath(curNode, relationSet, false);
        } else {
            curNode = relationToInitialNodes
                    .get(Event.defaultTimeRelationString);
        }

        // Iterate through the trace chain and construct zero or more
        // RelationPaths
        while (!curNode.isTerminal()) {
            List<? extends ITransition<EventNode>> relationTransitions = curNode
                    .getTransitionsWithIntersectingRelations(relationSet);

            if (relationTransitions.size() > 1) {
                throw new InternalSynopticException(
                        "Multiple transitions exist for relation: " + relation);
            }

            // We are in the middle of construction a RelationPath
            if (pendingPath != null) {
                // curNode does not have an outgoing edge with the input
                // parameter relation type
                if (relationTransitions.isEmpty()) {
                    pendingPath.setFinalNode(curNode);
                    results.add(pendingPath);
                    pendingPath = null;
                }
            } else { // A RelationPath is not being constructed.
                /*
                 * This node has an outgoing edge containing the input parameter
                 * relation so begin constructing a relation path
                 */
                if (!relationTransitions.isEmpty()) {
                    pendingPath = new RelationPath(curNode, relationSet, false);
                }
            }

            /*
             * There are no transitions containing the input parameter relation,
             * so use the defaultTimeRelation transition to traverse the trace
             */
            if (relationTransitions.isEmpty()) {
                relationTransitions = curNode
                        .getTransitionsWithIntersectingRelations(defaultTimeRelationStringSet);
            }

            ITransition<EventNode> transition = relationTransitions.get(0);
            curNode = transition.getTarget();
        }

        return results;
    }

    /**
     * Returns a single RelationPath where each subgraph of the relation type is
     * transitively connected through the transitiveRelation. Returns null if no
     * such path exists.
     * 
     * @param relation
     * @param transitiveRelation
     * @return
     */
    public RelationPath getBiRelationalPath(String relation,
            String transitiveRelation) {
        EventNode firstNode;
        EventNode finalNode = null;
        boolean initialTransitivelyConnected;

        Set<String> relationSet = new HashSet<String>();
        relationSet.add(relation);
        relationSet.add(transitiveRelation);

        // birelational path is connected to the initial node by the primary
        // relation
        if (relationToInitialNodes.containsKey(relation)) {
            firstNode = relationToInitialNodes.get(relationSet);
            initialTransitivelyConnected = false;
        } else { // birelational path is connected to the initial node by the
                 // transitive relation
            firstNode = relationToInitialNodes
                    .get(Event.defaultTimeRelationString);
            initialTransitivelyConnected = true;
        }

        EventNode curNode = firstNode;

        while (!curNode.isTerminal()) {
            List<? extends ITransition<EventNode>> relationTransitions = curNode
                    .getTransitionsWithIntersectingRelations(relationSet);

            if (relationTransitions.size() > 1) {
                throw new InternalSynopticException(
                        "Multiple transitions exist for relation: " + relation);
            }

            // If a transition exists for the primary relation, mark the target
            // as the pending final node.
            if (!relationTransitions.isEmpty()) {
                ITransition<EventNode> transition = relationTransitions.get(0);
                finalNode = transition.getTarget();
            }

            /*
             * I have a redundant test here because what happens in the previous
             * block is conceptually separate from what happens in the next
             * block
             */
            // Begin the process of moving to the next node
            if (relationTransitions.isEmpty()) {
                relationTransitions = curNode
                        .getTransitionsWithIntersectingRelations(defaultTimeRelationStringSet);
            }

            ITransition<EventNode> transition = relationTransitions.get(0);
            curNode = transition.getTarget();
        }

        if (finalNode != null) {
            RelationPath result = new RelationPath(firstNode, relationSet,
                    initialTransitivelyConnected);
            result.setFinalNode(finalNode);
            return result;
        }

        return null;
    }
}
