package synoptic.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import synoptic.model.event.Event;
import synoptic.model.interfaces.IRelationPath;
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

    public Trace() {
        this.relationToInitialNodes = new HashMap<String, EventNode>();
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
    public Set<IRelationPath> getSingleRelationPaths(String relation) {
        /*
         * the initialTransitivelyConnected parameter is always false when
         * constructing relation paths in this method because the constructed
         * relation paths never have transitive connections. Each relation path
         * is either an island or the full trace.
         */

        // Relation paths only get added to results when they are terminated
        Set<IRelationPath> results = new HashSet<IRelationPath>();

        EventNode pendingInitial = null;
        EventNode curNode;

        Set<String> relationSet = new HashSet<String>();
        relationSet.add(relation);

        if (relationToInitialNodes.containsKey(relation)) {
            curNode = relationToInitialNodes.get(relation);
            pendingInitial = curNode;
        } else {
            curNode = relationToInitialNodes
                    .get(Event.defTimeRelationStr);
        }

        EventNode prevNode = curNode;

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
            if (pendingInitial != null) {
                // curNode does not have an outgoing edge with the input
                // parameter relation type
                if (relationTransitions.isEmpty()) {
                    IRelationPath relationPath = new ChainRelationPath(
                            pendingInitial, curNode, relation);
                    results.add(relationPath);
                    pendingInitial = null;
                }
            } else { // A RelationPath is not being constructed.
                /*
                 * This node has an outgoing edge containing the input parameter
                 * relation so begin constructing a relation path
                 */
                if (!relationTransitions.isEmpty()) {
                    pendingInitial = curNode;
                }
            }

            /*
             * There are no transitions containing the input parameter relation,
             * so use the defaultTimeRelation transition to traverse the trace
             */
            if (relationTransitions.isEmpty()) {
                relationTransitions = curNode
                        .getTransitionsWithIntersectingRelations(Event.defTimeRelationSet);
            }

            prevNode = curNode;

            ITransition<EventNode> transition = relationTransitions.get(0);
            curNode = transition.getTarget();
        }

        if (pendingInitial != null) {
            IRelationPath relationPath = new ChainRelationPath(pendingInitial,
                    prevNode, relation);
            results.add(relationPath);
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
    public IRelationPath getBiRelationalPath(String relation,
            String transitiveRelation) {

        EventNode firstNode;
        EventNode finalNode = null;
        boolean initialTransitivelyConnected;

        Set<String> relationSet = new HashSet<String>();
        relationSet.add(relation);

        // birelational path is connected to the initial node by the primary
        // relation
        if (relationToInitialNodes.containsKey(relation)) {
            firstNode = relationToInitialNodes.get(relation);
            initialTransitivelyConnected = false;
            finalNode = firstNode;
        } else { // birelational path is connected to the initial node by the
                 // transitive relation
            firstNode = relationToInitialNodes
                    .get(Event.defTimeRelationStr);
            initialTransitivelyConnected = true;
        }

        EventNode curNode = firstNode;

        while (!curNode.isTerminal()) {
            List<? extends ITransition<EventNode>> relationTransitions = curNode
                    .getTransitionsWithIntersectingRelations(relationSet);

            if (relationTransitions.size() > relationSet.size()) {
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
                        .getTransitionsWithIntersectingRelations(Event.defTimeRelationSet);
            }

            ITransition<EventNode> transition = relationTransitions.get(0);
            curNode = transition.getTarget();
        }

        if (finalNode != null) {
            return new TransitiveRelationPath(firstNode, finalNode, relation,
                    transitiveRelation, initialTransitivelyConnected);
        }

        return null;
    }

    public boolean containsRelation(String relation) {
        return relationToInitialNodes.containsKey(relation);
    }
}
