package synoptic.invariants.fsmcheck;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import synoptic.invariants.BinaryInvariant;
import synoptic.invariants.CExamplePath;
import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.constraints.TempConstrainedInvariant;
import synoptic.model.EventNode;
import synoptic.model.Partition;
import synoptic.model.event.EventType;
import synoptic.model.interfaces.INode;
import synoptic.model.interfaces.ITransition;
import synoptic.util.InternalSynopticException;
import synoptic.util.time.ITime;

/**
 * NFA state set superclass for all time-constrained invariants. It keeps the
 * shortest path justifying a given state being inhabited.
 * 
 * @author Tony Ohmann (ohmann@cs.umass.edu)
 * @param <T>
 *            The node type, used as an input, and stored in path-history.
 */
public abstract class ConstrainedTracingSet<T extends INode<T>> extends
        TracingStateSet<T> {

    /**
     * An extension of a HistoryNode which also records time deltas
     */
    public class ConstrainedHistoryNode extends HistoryNode {
        /**
         * Concrete edge used to arrive at this node
         */
        ITransition<EventNode> transition;
        ITime tDelta;
        ConstrainedHistoryNode previousConst;

        public ConstrainedHistoryNode(T node, ConstrainedHistoryNode previous,
                int count, ITransition<EventNode> transition, ITime tDelta) {
            super(node, previous, count);
            this.transition = transition;
            this.tDelta = (tDelta != null ? tDelta : tBound.getZeroTime());
            previousConst = previous;
        }

        /**
         * Converts this chain into a RelationPath list with time deltas
         */
        @Override
        public CExamplePath<T> toCounterexample(ITemporalInvariant inv) {

            List<T> path = new ArrayList<T>();
            List<ITransition<EventNode>> transitions = new ArrayList<ITransition<EventNode>>();
            List<ITime> tDeltas = new ArrayList<ITime>();
            ConstrainedHistoryNode cur = this;

            // Traverse the path of ConstrainedHistoryNodes recording T nodes,
            // transitions, and running time deltas in lists
            while (cur != null) {
                path.add(cur.node);
                transitions.add(cur.transition);
                tDeltas.add(cur.tDelta);
                cur = cur.previousConst;
            }
            Collections.reverse(path);
            Collections.reverse(transitions);
            Collections.reverse(tDeltas);

            // Constrained invariants only keep the shortest path to failure and
            // do not need to be shortened but do require transitions and
            // running time deltas
            CExamplePath<T> rpath = new CExamplePath<T>(inv, path, transitions,
                    tDeltas);

            return rpath;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            ConstrainedHistoryNode cur = this;
            while (cur != null) {
                sb.append(cur.node.getEType());
                sb.append("(");
                if (cur.transition != null
                        && cur.transition.getTimeDelta() != null) {
                    sb.append(cur.transition.getTimeDelta());
                }
                sb.append(")<-");
                cur = cur.previousConst;
            }
            return sb.toString();
        }
    }

    /**
     * Extends this node with another
     */
    public ConstrainedHistoryNode extend(T node, ConstrainedHistoryNode prior,
            ITransition<EventNode> transition, ITime tDelta) {
        if (prior == null) {
            return null;
        }
        return new ConstrainedHistoryNode(node, prior, prior.count + 1,
                transition, tDelta);
    }

    /**
     * Return the non-null path with the smaller running time delta
     */
    public ConstrainedHistoryNode preferMinTime(ConstrainedHistoryNode first,
            ConstrainedHistoryNode second) {
        return preferMinMaxTime(first, second, false);
    }

    /**
     * Return the non-null path with the larger running time delta
     */
    public ConstrainedHistoryNode preferMaxTime(ConstrainedHistoryNode first,
            ConstrainedHistoryNode second) {
        return preferMinMaxTime(first, second, true);
    }

    /**
     * Return the non-null path with the smaller or larger (whichever is
     * requested) running time delta
     * 
     * @param findMax
     *            If TRUE, find path with larger time delta. If FALSE, smaller.
     */
    private ConstrainedHistoryNode preferMinMaxTime(
            ConstrainedHistoryNode first, ConstrainedHistoryNode second,
            boolean findMax) {

        // If one path is null, return the other
        if (second == null) {
            return first;
        }
        if (first == null) {
            return second;
        }

        // Return the path with higher/max or lower/min running time, whichever
        // was requested

        // "Second" running time is greater
        if (first.tDelta.lessThan(second.tDelta)) {
            if (findMax) {
                return second;
            }
            return first;
        }

        // "First" running time is greater
        {
            if (findMax) {
                return first;
            }
            return second;
        }
    }

    /**
     * Running time stored by the state machine from when t=0 state was first
     * encountered
     */
    List<ITime> tRunning;

    /**
     * Upper- or lower-bound time constraint
     */
    ITime tBound;
    public EventType a, b;

    /**
     * Number of states in the state machine
     */
    int numStates;

    /**
     * A path for each state in the appropriate state machine
     */
    List<ConstrainedHistoryNode> states;

    /**
     * The node (usually Partition) being transitioned _from_
     */
    T previous;

    /**
     * The (single) relation of the invariant
     */
    Set<String> relation;

    /**
     * Empty constructor for copy()
     */
    protected ConstrainedTracingSet() {

    }

    /**
     * Create a new time-constrained tracing state set from a constrained
     * invariant
     * 
     * @param inv
     *            Must be a constrained BinaryInvariant
     * @param numStates
     *            Number of states in the state machine
     */
    public ConstrainedTracingSet(BinaryInvariant inv, int numStates) {

        // Constrained tracing state sets can only be used with constrained
        // invariants
        assert inv instanceof TempConstrainedInvariant<?>;
        @SuppressWarnings("unchecked")
        TempConstrainedInvariant<BinaryInvariant> constInv = (TempConstrainedInvariant<BinaryInvariant>) inv;

        a = inv.getFirst();
        b = inv.getSecond();
        tBound = constInv.getConstraint().getThreshold();
        this.numStates = numStates;

        // Get the invariant's relation
        relation = new HashSet<String>(1);
        relation.add(inv.getRelation());

        states = new ArrayList<ConstrainedHistoryNode>(numStates);
        tRunning = new ArrayList<ITime>(numStates);

        // Set up states and state times
        for (int i = 0; i < numStates; ++i) {
            states.add(null);
            tRunning.add(tBound.getZeroTime());
        }
    }

    @Override
    public void transition(T input) {

        EventType name = input.getEType();

        // Whether this event is the A or B of this invariant
        boolean isA = false;
        boolean isB = false;

        // Precompute whether this event is the A or B of this invariant
        if (a.equals(name)) {
            isA = true;
        } else if (b.equals(name)) {
            isB = true;
        }

        // Get and all time deltas from the transition between "previous" and
        // "input" nodes
        List<ITime> times = null;
        if (previous != null) {
            ITransition<Partition> trans = ((Partition) previous)
                    .getTransitionWithExactRelation((Partition) input, relation);
            times = trans.getDeltaSeries().getAllDeltas();
        }

        // TODO: Add checks for the other 3 constrained invariants when they're
        // implemented
        boolean isUpper;
        if (this instanceof APUpperTracingSet) {
            isUpper = true;
        } else {
            throw new InternalSynopticException(
                    "ConstrainedTracingSet not updated to handle this subtype: "
                            + this.getClass());
        }

        ITime tMinMax = null;

        // Get min (if lower constraint) or max (if upper constraint) time delta
        // TODO: After adding something like Partition.getAllEventTransitions(),
        // do this at the level of event transitions rather than only times
        if (isUpper) {
            tMinMax = getMaxTime(times);
        } else {
            tMinMax = getMinTime(times);
        }

        // Whether current running time will be outside the time bound at each
        // state
        List<Boolean> outOfBound = new ArrayList<Boolean>(numStates);

        for (int i = 0; i < numStates; ++i) {
            outOfBound.add(null);
        }

        // Check for times outside time bound
        for (int i = 0; i < numStates; ++i) {
            // TODO: Make this process correct for lower-bound invariants

            // Increment running time and compare to time bound
            ITime newTime = tRunning.get(i).incrBy(tMinMax);
            int tComparison = newTime.compareTo(tBound);

            // Within bound if upper and <= bound or if lower >= bound
            if (isUpper && tComparison <= 0 || !isUpper && tComparison >= 0) {
                outOfBound.set(i, false);
            }

            // Outside of bound
            else {
                outOfBound.set(i, true);
            }
        }

        // Keep old paths before this transition
        List<ConstrainedHistoryNode> statesOld = states;

        // Final state nodes after the transition will be stored in s
        states = new ArrayList<ConstrainedHistoryNode>(numStates);
        for (int i = 0; i < numStates; ++i) {
            states.add(null);
        }

        // Find the specific event corresponding to the min/max time found
        ITransition<EventNode> transition = findMinMaxTransition(tMinMax);

        // Call transition code specific to each invariant
        transition(input, transition, isA, isB, outOfBound, statesOld);

        // The node we just transitioned _to_ is our new previous node (for
        // future transitions)
        previous = input;
    }

    private ITransition<EventNode> findMinMaxTransition(ITime tMinMax) {
        if (previous instanceof Partition) {
            // Look at all events in previous
            for (EventNode prevEv : ((Partition) previous).getEventNodes()) {

                // Find the single transition out of this event
                ITransition<EventNode> prevToInput = prevEv
                        .getTransitionsWithExactRelations(relation).get(0);

                // Check if this was the min/max time found earlier
                if (prevToInput != null) {
                    if (tMinMax.equals(prevToInput.getTimeDelta())
                            || prevToInput.getTarget().isTerminal()
                            && tMinMax.equals(tMinMax.getZeroTime())) {
                        return prevToInput;
                    }
                }
            }
        }
        return null;
    }

    protected abstract void transition(T input,
            ITransition<EventNode> transition, boolean isA, boolean isB,
            List<Boolean> outOfBound, List<ConstrainedHistoryNode> sOld);

    /**
     * Get the smallest time in a time delta series
     * 
     * @param times
     *            The time delta series
     * @return Smallest time delta or a zero-time ITime if times is empty or
     *         null
     */
    private ITime getMinTime(List<ITime> times) {
        return getMinMaxTime(times, false);
    }

    /**
     * Get the largest time in a time delta series
     * 
     * @param times
     *            The time delta series
     * @return Largest time delta or a zero-time ITime if times is empty or null
     */
    private ITime getMaxTime(List<ITime> times) {
        return getMinMaxTime(times, true);
    }

    /**
     * Get smallest or largest time delta
     * 
     * @param times
     *            The time delta series
     * @param findMax
     *            If TRUE, find max time. If FALSE, find min.
     */
    private ITime getMinMaxTime(List<ITime> times, boolean findMax) {

        ITime minMaxTime = null;

        // Return zero-time if times is empty or null
        if (times == null || times.size() == 0) {
            return tBound.getZeroTime();
        }

        // Find and store the min or max time
        for (ITime time : times) {
            if (minMaxTime == null || findMax && minMaxTime.lessThan(time)
                    || !findMax && time.lessThan(minMaxTime)) {
                minMaxTime = time;
            }
        }

        return minMaxTime;
    }
}
