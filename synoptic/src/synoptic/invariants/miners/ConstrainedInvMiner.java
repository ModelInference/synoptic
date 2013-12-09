package synoptic.invariants.miners;

import java.util.HashSet;
import java.util.Set;

import synoptic.invariants.AlwaysFollowedInvariant;
import synoptic.invariants.AlwaysPrecedesInvariant;
import synoptic.invariants.BinaryInvariant;
import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.NeverFollowedInvariant;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.invariants.constraints.IThresholdConstraint;
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
import synoptic.util.Pair;
import synoptic.util.time.ITime;

/**
 * <p>
 * Mines constrained invariants from totally ordered traces. Constrained
 * invariants are the standard set of invariants with constraints on the time
 * between events in an execution. New invariants are not created, the
 * unconstrained invariants are simply augmented with constraints.
 * </p>
 * <p>
 * The only two invariants that can be constrained are AlwaysFollowedInvariant
 * and AlwaysPrecedesInvariant.
 * </p>
 * <p>
 * Uses other totally ordered invariant miners to first mine the unconstrained
 * invariants (if not given these explicitly). Mines constraints for these
 * unconstrained invariants by walking the trace directly. Walking the trace
 * consists of traversing the log for every constrained AFby and AP invariant.
 * Each traversal finds a lower bound and upper bound constraint for an
 * unconstrained invariant. Two constrained invariants are then created (for
 * lower bound and upper bound) and added in the resulting constrained invariant
 * set.
 * </p>
 */
public class ConstrainedInvMiner extends InvariantMiner {

    // Stores generated RelationPaths
    private final Set<IRelationPath> relationPaths;
    // The set of constrained invariants that we will be returning.
    private TemporalInvariantSet constrainedInvs;

    public ConstrainedInvMiner() {
        this.relationPaths = new HashSet<IRelationPath>();
    }

    /**
     * Uses the miner passed into the constructor to first mine unconstrained
     * invariants. Then walks the trace to compute constraints for
     * AlwaysFollowedInvariant and AlwaysPrecedesInvariant. Returns a set of
     * these constrained invariants.
     * 
     * @param miner
     *            The miner to use for mining regular (unconstrained)
     *            invariants. private ITOInvariantMiner miner;
     * @param g
     *            a chain trace graph of nodes of type LogEvent
     * @param multipleRelations
     *            whether or not nodes have multiple relations
     * @return the set of constrained temporal invariants
     */
    public TemporalInvariantSet computeInvariants(ITOInvariantMiner miner,
            ChainsTraceGraph g, boolean multipleRelations) {

        TemporalInvariantSet invs = miner.computeInvariants(g,
                multipleRelations);
        return computeInvariants(g, multipleRelations, invs);
    }

    /**
     * Given a set of unconstrained invariants, walks the trace graph to compute
     * constraints for AFby and AP invariants. Augments these existing
     * invariants with constraints. Returns a set of these constrained
     * invariants.
     * 
     * @param g
     *            a chain trace graph of nodes of type LogEvent
     * @param multipleRelations
     *            whether or not nodes have multiple relations
     * @param invs
     *            set of unconstrained invariants returned from previous
     *            invariant miner
     * @return the set of constrained temporal invariants
     */
    public TemporalInvariantSet computeInvariants(ChainsTraceGraph g,
            boolean multipleRelations, TemporalInvariantSet invs) {

        // For each invocation we create a new invariant set to store the
        // generated constrained invariants.
        this.constrainedInvs = new TemporalInvariantSet();

        for (ITemporalInvariant inv : invs.getSet()) {
            String relation = inv.getRelation();
            boolean isTimeRelation = relation.equals(Event.defTimeRelationStr);

            // Loop through the traces.
            for (Trace trace : g.getTraces()) {

                if (multipleRelations && !isTimeRelation) {
                    IRelationPath relationPath = trace.getBiRelationalPath(
                            relation, Event.defTimeRelationStr);
                    relationPaths.add(relationPath);
                } else {
                    Set<IRelationPath> subgraphs = trace
                            .getSingleRelationPaths(relation);
                    if (isTimeRelation && subgraphs.size() != 1) {
                        throw new IllegalStateException(
                                "Multiple relation subraphs for ordering relation graph");
                    }
                    relationPaths.addAll(subgraphs);
                }
            }
        }

        for (ITemporalInvariant inv : invs.getSet()) {
            if (inv instanceof NeverFollowedInvariant) {
                constrainedInvs.add(inv);
            }

            if (!(inv instanceof AlwaysFollowedInvariant || inv instanceof AlwaysPrecedesInvariant)) {
                continue;
            }
            computeInvariants((BinaryInvariant) inv);
        }

        relationPaths.clear();
        return constrainedInvs;
    }

    /**
     * Walks each relation path to compute a lower and upper bound constraint
     * for the given invariant. Augments the given invariant with the two
     * constraints and returns them within a TemporalInvariantSet.
     * 
     * @param inv
     *            the invariant that is being augmented with constraints
     */
    public void computeInvariants(BinaryInvariant inv) {

        assert (inv instanceof AlwaysFollowedInvariant || inv instanceof AlwaysPrecedesInvariant);

        EventType a = inv.getFirst();
        EventType b = inv.getSecond();

        // If invariant contains INITIAL node, we can't compute bound
        // constraints.
        if (a.isInitialEventType()) {
            return;
        }

        // Return pair.left represents lower bound constraint.
        // Return pair.right represents upper bound constraint.
        Pair<IThresholdConstraint, IThresholdConstraint> constraints = computeConstraints(
                a, b);

        // Create two TempConstrainedInvariant objects using the lower bound and
        // upper bound computed.
        augmentInvariant(inv, constraints);
    }

    // Helper method for creating a lower and upper constrained invariant and
    // adding it into the constrainedInvs set.
    private <T extends BinaryInvariant> void augmentInvariant(T inv,
            Pair<IThresholdConstraint, IThresholdConstraint> constraints) {
        TempConstrainedInvariant<T> lowerConstrInv = new TempConstrainedInvariant<T>(
                inv, constraints.getLeft());
        TempConstrainedInvariant<T> upperConstrInv = new TempConstrainedInvariant<T>(
                inv, constraints.getRight());

        constrainedInvs.add(lowerConstrInv);
        constrainedInvs.add(upperConstrInv);
    }

    /**
     * Walks each relationPath and checks for nodes either of EventType a or b.
     * Uses ITime values of these nodes to update and compute a lower and upper
     * bound constraint for an invariant with predicates a and b. The algorithm
     * to compute an lower bound is as follows: For each relationPath Node
     * recentA Walking down the trace If see a node of EventType a set recentA
     * to this node If see a node of EventType b && recentA set obtain delta
     * (difference between time of this node and recentA) The lower bound is the
     * min delta value out of all the deltas The algorithm to compute an upper
     * bound is as follows: For each relationPath Walking down the trace Find
     * the first node of EventType a Find the last node of EventType b Obtain a
     * delta value (difference between time of first and last) for relationPath
     * The upper bound is the max delta value out of all the deltas Returns the
     * computed lower and upper bound constraints as a pair. The left is the
     * lower bound constraint and the right is the upper bound constraint.
     * 
     * @param relationPaths
     *            set of relationPaths to walk
     * @param a
     *            first invariant predicate
     * @param b
     *            second invariant predicate
     * @return IThresholdConstraint pair where the left represents the lower
     *         bound constraint and the right represents the upper bound
     *         constraint
     */
    private Pair<IThresholdConstraint, IThresholdConstraint> computeConstraints(
            EventType a, EventType b) {

        ITime lowerBound = null;
        ITime upperBound = null;

        // For each relationPath.
        // Go through each node in path.
        // Find nodes that match event types for invariant.
        // Find upperBound.
        // Find and lowerBound to create ConstrainedInvariants.
        for (IRelationPath relationPath : relationPaths) {
            EventNode start = relationPath.getFirstNode();
            EventNode end = relationPath.getLastNode();

            // First occurrence of a and last occurrence of b.
            // last - first = upperBound
            ITime firstA = null;
            ITime lastB = null;

            // Track nodes of event type a for computing lowerBound.
            EventNode recentA = null;

            Transition<EventNode> trans;

            // Current node we're at as we walk the trace.
            EventNode curr = start;

            while (true) {
                if (curr.getEType().equals(a)) {
                    recentA = curr;
                    if (firstA == null) {
                        firstA = curr.getTime();
                    }
                }

                if (curr.getEType().equals(b)) {
                    // If node of event type a is found already, then we can
                    // obtain a delta value since we now found node of event
                    // type b.
                    if (recentA != null) {
                        ITime delta = curr.getTime().computeDelta(
                                recentA.getTime());
                        if (lowerBound == null || delta.lessThan(lowerBound)) {
                            lowerBound = delta;
                        }
                    }
                    lastB = curr.getTime();
                }

                // Dealing with a TO log, so only one transition available to
                // use.
                assert (curr.getAllTransitions().size() == 1);
                trans = curr.getAllTransitions().get(0);

                // Reached ending node in path.
                if (curr.equals(end)) {
                    break;
                }
                curr = trans.getTarget();
            }

            // relationPath contains the invariant.
            // Note: this will exclude invariants with an INITIAL node, since
            // that will yield a null lower-bound and upper-bound.
            if (firstA != null && lastB != null) {
                ITime delta = lastB.computeDelta(firstA);
                if (upperBound == null || upperBound.lessThan(delta)) {
                    upperBound = delta;
                }
            }
        }

        IThresholdConstraint l = new LowerBoundConstraint(lowerBound);
        IThresholdConstraint u = new UpperBoundConstraint(upperBound);

        return new Pair<IThresholdConstraint, IThresholdConstraint>(l, u);
    }
}
