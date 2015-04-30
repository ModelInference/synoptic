package synoptic.invariants.miners;

import java.util.HashSet;
import java.util.Set;

import synoptic.invariants.AlwaysFollowedInvariant;
import synoptic.invariants.AlwaysPrecedesInvariant;
import synoptic.invariants.BinaryInvariant;
import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.InterruptedByInvariant;
import synoptic.invariants.NeverFollowedInvariant;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.invariants.constraints.IThresholdConstraint;
import synoptic.invariants.constraints.LowerBoundConstraint;
import synoptic.invariants.constraints.TempConstrainedInvariant;
import synoptic.invariants.constraints.UpperBoundConstraint;
import synoptic.main.AbstractMain;
import synoptic.model.ChainsTraceGraph;
import synoptic.model.EventNode;
import synoptic.model.Trace;
import synoptic.model.event.Event;
import synoptic.model.event.EventType;
import synoptic.model.interfaces.IRelationPath;
import synoptic.util.Pair;
import synoptic.util.resource.AbstractResource;

/**
 * <p>
 * Mines constrained invariants from totally ordered traces. Constrained
 * invariants are the standard set of invariants with constraints on the time
 * between events in an execution. New invariants are not created, the
 * unconstrained invariants are simply augmented with constraints.
 * </p>
 * <p>
 * The only three invariants that can be constrained are
 * AlwaysFollowedInvariant, AlwaysPrecedesInvariant and InterruptedByInvariant.
 * </p>
 * <p>
 * Uses other totally ordered invariant miners to first mine the unconstrained
 * invariants (if not given these explicitly). Mines constraints for these
 * unconstrained invariants by walking the trace directly. Walking the trace
 * consists of traversing the log for every constrained AFby, AP, and IntrBy
 * invariant. Each traversal finds a lower bound and upper bound constraint for
 * an unconstrained invariant. Two constrained invariants are then created (for
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
     * AlwaysFollowedInvariant, AlwaysPrecedesInvariant, and
     * InterruptedByInvariant. Returns a set of these constrained invariants.
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
                multipleRelations,
                AbstractMain.getInstance().options.outputSupportCount);
        return computeInvariants(g, multipleRelations, invs);
    }

    /**
     * Given a set of unconstrained invariants, walks the trace graph to compute
     * constraints for AFby, AP, and IntrBy invariants. Augments these existing
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

            if (!(inv instanceof AlwaysFollowedInvariant
                    || inv instanceof AlwaysPrecedesInvariant || inv instanceof InterruptedByInvariant)) {
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

        assert (inv instanceof AlwaysFollowedInvariant
                || inv instanceof AlwaysPrecedesInvariant || inv instanceof InterruptedByInvariant);

        EventType a = inv.getFirst();
        EventType b = inv.getSecond();

        // If invariant contains INITIAL node, we can't compute bound
        // constraints.
        if (a.isInitialEventType()) {
            return;
        }

        // constraints.left represents lower bound constraint.
        // constraints.right represents upper bound constraint.
        Pair<IThresholdConstraint, IThresholdConstraint> constraints;

        if (inv instanceof InterruptedByInvariant) {
            // IntrBy's constraints are between a&a, not a&b
            constraints = computeUpperLowerConstraints(a, a, true);
        } else {
            constraints = computeUpperLowerConstraints(a, b, false);
        }

        // Create two TempConstrainedInvariant objects using the lower bound and
        // upper bound computed.
        augmentInvariant(inv, constraints);
    }

    // Helper method for creating a lower and upper constrained invariant and
    // adding it into the constrainedInvs set.
    private <T extends BinaryInvariant> void augmentInvariant(T inv,
            Pair<IThresholdConstraint, IThresholdConstraint> constraints) {
        TempConstrainedInvariant<T> lowerConstrInv = new TempConstrainedInvariant<T>(
                inv, constraints.getLeft(),
                AbstractMain.getInstance().options.outputSupportCount);
        TempConstrainedInvariant<T> upperConstrInv = new TempConstrainedInvariant<T>(
                inv, constraints.getRight(),
                AbstractMain.getInstance().options.outputSupportCount);

        constrainedInvs.add(lowerConstrInv);
        constrainedInvs.add(upperConstrInv);
    }

    /**
     * Walks each relationPath and checks for nodes either of EventType a or b.
     * Uses ITime values of these nodes to update and compute a lower and upper
     * bound constraint for an invariant with predicates a and b. The algorithm
     * for computing the bounds is as follows: For every type 'a' in each path,
     * walk all subsequent nodes and retrieve the delta to this node (if 'a' or
     * 'b' pair). For lower bound, take the minimal value of all deltas, and the
     * max value for the upper bound. Returns the computed lower and upper bound
     * constraints as a pair. The left is the lower bound constraint and the
     * right is the upper bound constraint. This method provides an additional
     * param to the overloaded computeConstraints method: The IntrBy varies as
     * it must compute lower bounds between two a's. The upper bound is also
     * computed that way, but can reuse the original implementation. TODO:
     * refactor this at some point
     * 
     * @param a
     *            first invariant predicate
     * @param b
     *            second invariant predicate
     * @param betweenFirstAndFirstPredicate
     *            If the invariant's bounds are calculated between two instances
     *            of the first predicate (between a&a) instead of between an
     *            instance of the first predicate and an instance of the second
     *            predicate (between a&b). This currently only applies to
     *            IntrBy.
     * @return IThresholdConstraint pair where the left represents the lower
     *         bound constraint and the right represents the upper bound
     *         constraint
     */
    private Pair<IThresholdConstraint, IThresholdConstraint> computeUpperLowerConstraints(
            EventType a, EventType b, boolean betweenFirstAndFirstPredicate) {

        AbstractResource lowerBound = null;
        AbstractResource upperBound = null;

        // For each relationPath.
        // Go through each node in path.
        // Find nodes that match event types for invariant.
        // Find upperBound.
        // Find lowerBound
        // create ConstrainedInvariants.
        for (IRelationPath relationPath : relationPaths) {
            EventNode start = relationPath.getFirstNode();
            EventNode end = relationPath.getLastNode();

            EventNode curr = start;

            // Iterate over each node in path
            while (true) {
                // Reached ending node in path.
                if (curr.equals(end)) {
                    break;
                }

                // Current node we're at as we walk the trace.
                assert (curr.getAllTransitions().size() == 1);
                EventNode next = curr.getAllTransitions().get(0).getTarget();

                // for each node (of type a, else there will be no bound), walk
                // every subsequent node and check bounds
                while (curr.getEType().equals(a)) {
                    // Found (a,a) pair, compute constraints (IntrBy)
                    if (next.getEType().equals(a)
                            && betweenFirstAndFirstPredicate) {
                        AbstractResource delta = next.getTime().computeDelta(
                                curr.getTime());
                        if (lowerBound == null || delta.lessThan(lowerBound)) {
                            lowerBound = delta;
                        }

                        if (upperBound == null || upperBound.lessThan(delta)) {
                            upperBound = delta;
                        }
                    }

                    // Found (a,b) pair, compute constraints
                    if (next.getEType().equals(b)
                            && !betweenFirstAndFirstPredicate) {
                        AbstractResource delta = next.getTime().computeDelta(
                                curr.getTime());
                        if (lowerBound == null || delta.lessThan(lowerBound)) {
                            lowerBound = delta;
                        }

                        if (upperBound == null || upperBound.lessThan(delta)) {
                            upperBound = delta;
                        }
                    }

                    // Reached ending node in path.
                    if (next.equals(end)) {
                        break;
                    }

                    assert (next.getAllTransitions().size() == 1);
                    next = next.getAllTransitions().get(0).getTarget();
                }

                assert (curr.getAllTransitions().size() == 1);
                curr = curr.getAllTransitions().get(0).getTarget();
            }
        }

        IThresholdConstraint l = new LowerBoundConstraint(lowerBound);
        IThresholdConstraint u = new UpperBoundConstraint(upperBound);

        return new Pair<IThresholdConstraint, IThresholdConstraint>(l, u);
    }
}
