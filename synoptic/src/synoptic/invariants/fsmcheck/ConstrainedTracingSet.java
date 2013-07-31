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
import synoptic.model.Partition;
import synoptic.model.event.EventType;
import synoptic.model.interfaces.INode;
import synoptic.model.interfaces.ITransition;
import synoptic.util.InternalSynopticException;
import synoptic.util.time.ITime;

public abstract class ConstrainedTracingSet<T extends INode<T>> extends
        TracingStateSet<T> {
    
    public static boolean TEMPDEBUG = true;

    /**
     * An extension of a HistoryNode which also records time deltas
     */
    public class ConstrainedHistoryNode extends HistoryNode {
        ITime tDelta;
        ConstrainedHistoryNode previousConst;

        public ConstrainedHistoryNode(T node, ConstrainedHistoryNode previous, int count,
                ITime tDelta) {
            super(node, previous, count);
            this.tDelta = tDelta;
            previousConst = previous;
        }

        /**
         * Converts this chain into a RelationPath list with time deltas
         */
        @Override
        public CExamplePath<T> toCounterexample(ITemporalInvariant inv) {

            List<T> path = new ArrayList<T>();
            List<ITime> tDeltas = new ArrayList<ITime>();
            ConstrainedHistoryNode cur = this;

            // TODO: why do we require isTerminal here?
            assert (cur.node).isTerminal();

            // Traverse the path of ConstrainedHistoryNodes recording T nodes
            // and time deltas in lists
            while (cur != null) {
                path.add(cur.node);
                tDeltas.add(cur.tDelta);
                cur = cur.previousConst;
            }
            Collections.reverse(path);

            // Constrained invariants only keep the shortest path to failure and
            // do not need to be shortened but do require storing of time deltas
            CExamplePath<T> rpath = new CExamplePath<T>(inv, path);

            return rpath;
        }
    }

    /**
     * Extends this node with another
     */
    public ConstrainedHistoryNode extend(T node, ConstrainedHistoryNode prior,
            ITime tDelta) {
        if (prior == null) {
            return null;
        }
        return new ConstrainedHistoryNode(node, prior, prior.count + 1, tDelta);
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
     * Time stored by the state machine from when t=0 state was first
     * encountered
     */
    List<ITime> t;

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
    List<ConstrainedHistoryNode> s;

    /**
     * The node (usually Partition) being transitioned _from_
     */
    T previous;

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

        s = new ArrayList<ConstrainedHistoryNode>(numStates);
        t = new ArrayList<ITime>(numStates);

        // Set up states and state times
        for (int i = 0; i < numStates; ++i) {
            s.add(null);
            t.add(tBound.getZeroTime());
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

        // Get all time deltas from the transition between "previous" and
        // "input" nodes
        List<ITime> times = null;
        if (previous != null) {

            // TODO: Find a reasonable way to get the true relation here.
            // Current best option seems to be
            // input.getAllTransitions().get(0).getRelation(), which is
            // extremely inefficient
            Set<String> relation = new HashSet<String>(1);
            relation.add("t");
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
        if (isUpper) {
            tMinMax = getMaxTime(times);
        } else {
            tMinMax = getMinTime(times);
        }

        if (TEMPDEBUG) {
            System.err.println(previous + " -> " + input + " : " + tMinMax);
        }

        // Whether current running time will be outside the time bound at each
        // state
        List<Boolean> outOfBound = new ArrayList<Boolean>(numStates);

        for (int i = 0; i < numStates; ++i) {
            outOfBound.add(null);
        }

        // Check for times outside time bound
        for (int i = 0; i < numStates; ++i) {

            // Increment running time and compare to time bound
            ITime newTime = t.get(i).incrBy(tMinMax);
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
        List<ConstrainedHistoryNode> sOld = s;

        // Final state nodes after the transition will be stored in s
        s = new ArrayList<ConstrainedHistoryNode>(numStates);
        for (int i = 0; i < numStates; ++i) {
            s.add(null);
        }

        // Call transition code specific to each invariant
        transition(input, isA, isB, outOfBound, sOld, tMinMax);

        if (TEMPDEBUG) {
            System.err.print("  [states] ");
            for (ConstrainedHistoryNode ss : s)
                System.err.print(ss != null ? "1" : "0");
            System.err.println();
        }

        // The node we just transitioned _to_ is our new previous node (for
        // future transitions)
        previous = input;
    }

    protected abstract void transition(T input, boolean isA, boolean isB,
            List<Boolean> outOfBound, List<ConstrainedHistoryNode> sOld, ITime tMinMax);

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
