package synoptic.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import synoptic.model.event.Event;
import synoptic.model.event.EventType;
import synoptic.model.interfaces.ITransition;
import synoptic.model.interfaces.IRelationPath;
import synoptic.util.InternalSynopticException;

/**
 * Represents a connected subgraph over a relation through a trace. Supports
 * operations to count event Occurrences, Follows, and Precedes over the
 * specified relation within the trace.
 * 
 * @author timjv
 */
public class TransitiveRelationPath implements IRelationPath {

    /*
     * The representation for a relation path is the first non initial node in a
     * trace that is part of the connected subgraph of the relation. Using the
     * second node in the path, the rest of the nodes can be implicitly accessed
     * through a combination of relation and transitive relation edges.
     * 
     * We can get away with not having the initial node in the relation path
     * because of a dependency on how counting is done for the initial node. The
     * initial node appears once in each trace and is never preceded by any
     * event. The followedBy counts for an initial node is equivalent to the
     * event types seen in the trace.
     */

    /** First non-INITIAL node in this relation path */
    private EventNode eNode;
    /** Final non-TERMINAL node in this relation path */
    private EventNode eFinal;
    /** Relation this path is over */
    private String relation;
    /**
     * The relation this path uses for ordered traversal, defaults to
     * Event.defaultTimeRelationString, or "t"
     */
    private String orderingRelation = Event.defTimeRelationStr;
    /**
     * Caching indicator -- whether or not the various counts have already been
     * computed.
     */
    private boolean counted;

    /**
     * Whether or not INITIAL is directly or transitively connected to the
     * relation subgraph
     */
    private boolean initialTransitivelyConnected;

    /** The set of nodes seen prior to some point in the trace. */
    private Set<EventType> seen;
    /** Maintains the current event count in the path. */
    private Map<EventType, Integer> eventCounts;
    /**
     * Maintains the current FollowedBy count for the path.
     * followedByCounts[a][b] = count iff the number of a's that appeared before
     * this b is count.
     */
    private Map<EventType, Map<EventType, Integer>> followedByCounts;
    /**
     * Maintains the current precedes count for the path. precedesCounts[a][b] =
     * count iff the number of b's that appeared after this a is count.
     */
    private Map<EventType, Map<EventType, Integer>> precedesCounts;

    /**
     * @param eNode
     *            First non-INITIAL node in this relation path
     * @param relation
     *            Relation this path is over
     * @param initialTransitivelyConnected
     *            Whether INITIAL is directly or transitively connected to the
     *            relation subgraph
     */
    public TransitiveRelationPath(EventNode eNode, EventNode eFinal, 
            String relation, String transitiveRelation,
            boolean initialTransitivelyConnected) {
        this.eNode = eNode;
        this.eFinal = eFinal;
        this.relation = relation;
        this.counted = false;
        this.seen = new LinkedHashSet<EventType>();
        this.eventCounts = new LinkedHashMap<EventType, Integer>();
        this.followedByCounts = new LinkedHashMap<EventType, Map<EventType, Integer>>();
        this.precedesCounts = new LinkedHashMap<EventType, Map<EventType, Integer>>();
        this.initialTransitivelyConnected = initialTransitivelyConnected;
    }

    /**
     * Assumes tracegraph is already constructed. Walks over the tracegraph that
     * eNode is part of to populate seen, eventcounts, followedByCounts, and
     * precedesCounts. Throws an error if a node has multiple transitions for a
     * single relation (i.e., not a totally ordered relation path).
     */
    private void count() {
        if (counted) {
            return;
        }
        
        Set<String> orderingRelationSet = new HashSet<String>();
        orderingRelationSet.add(orderingRelation);
        Set<String> relationSet = new HashSet<String>();
        relationSet.add(relation);

        EventNode curNode = eNode;

        boolean hasImmediateIncomingRelation = !initialTransitivelyConnected;
        List<? extends ITransition<EventNode>> transitions = curNode
                .getTransitionsWithExactRelations(relationSet);
        // .getTransitions(relation);

        if (transitions.isEmpty()) {
            transitions = curNode
                    .getTransitionsWithIntersectingRelations(orderingRelationSet);
        }

        // TODO: Refactor this loop -- there is a lot of redundancy in acquiring
        // transitions, checking if the transitions set is empty, etc.

        while (!transitions.isEmpty()) {

            // TODO: Refactor this well formed transition test into Trace
            // Each node we traverse must have exactly one transition with the
            // ordering relation.
            if (curNode.getTransitionsWithIntersectingRelations(
                    orderingRelationSet).size() != 1) {
                throw new InternalSynopticException(
                        "There should be exactly one transition with an ordering relation.");
            }

            // Each node we traverse must have at most 1 transition with
            // a relation.
            if (curNode.getTransitionsWithExactRelations(relationSet).size() > 1) {
                throw new InternalSynopticException(
                        "There should be not be more than one transition with the " +
                        relation + " relation.");
            }

            boolean hasImmediateOutgoingRelation = curNode
                    .getTransitionsWithIntersectingRelations(relationSet).size() == 1;

            if (!hasImmediateOutgoingRelation && !hasImmediateIncomingRelation) {
                // Move on to the next node in the trace.
                if (curNode.equals(eFinal)) {
                    break;
                }

                curNode = curNode
                        .getTransitionsWithIntersectingRelations(
                                orderingRelationSet).get(0).getTarget();

                transitions = curNode
                        .getTransitionsWithExactRelations(relationSet);

                if (transitions.isEmpty()) {
                    transitions = curNode
                            .getTransitionsWithIntersectingRelations(orderingRelationSet);
                }

                hasImmediateIncomingRelation = false;
                continue;
            }

            hasImmediateIncomingRelation = hasImmediateOutgoingRelation;

            // The current event is 'b', and all prior events are 'a' --
            // this notation indicates that an 'a' always occur prior to a
            // 'b' in the path.
            EventType b = curNode.getEType();

            // Update the precedes counts based on the a events that
            // preceded the current b event in this path.
            for (EventType a : seen) {
                Map<EventType, Integer> bValues;
                if (!precedesCounts.containsKey(a)) {
                    precedesCounts.put(a,
                            new LinkedHashMap<EventType, Integer>());
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
                    followedByCounts.put(a,
                            new LinkedHashMap<EventType, Integer>());
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
            List<? extends ITransition<EventNode>> searchTransitions = curNode
                    .getTransitionsWithExactRelations(relationSet);

            if (searchTransitions.isEmpty()) {
                searchTransitions = curNode
                        .getTransitionsWithIntersectingRelations(orderingRelationSet);
            }

            if (curNode.equals(eFinal)) {
                break;
            }

            curNode = searchTransitions.get(0).getTarget();

            transitions = curNode.getTransitionsWithExactRelations(relationSet);

            if (transitions.isEmpty()) {
                transitions = curNode
                        .getTransitionsWithIntersectingRelations(orderingRelationSet);
            }
        }

        counted = true;
    }

    public Set<EventType> getSeen() {
        count();
        return Collections.unmodifiableSet(seen);
    }

    public Map<EventType, Integer> getEventCounts() {
        count();
        return Collections.unmodifiableMap(eventCounts);
    }

    /**
     * Map<a, Map<b, count>> iff the number of a's that appeared before this b
     * is count.
     */
    public Map<EventType, Map<EventType, Integer>> getFollowedByCounts() {
        count();
        // TODO: Make the return type deeply unmodifiable
        return Collections.unmodifiableMap(followedByCounts);
    }

    /**
     * Map<a, Map<b, count>> iff the number of b's that appeared after this a is
     * count.
     */
    public Map<EventType, Map<EventType, Integer>> getPrecedesCounts() {
        count();
        // TODO: Make the return type deeply unmodifiable
        return Collections.unmodifiableMap(precedesCounts);
    }
    
    public EventNode getFirstNode() {
    	return this.eNode;
    }
    
    public EventNode getLastNode() {
    	return this.eFinal;
    }
    
    public String getRelation() {
    	return this.relation;
    }
}
