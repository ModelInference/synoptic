package synoptic.invariants.fsmcheck;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import synoptic.invariants.BinaryInvariant;
import synoptic.invariants.constraints.TempConstrainedInvariant;
import synoptic.model.Partition;
import synoptic.model.event.EventType;
import synoptic.model.interfaces.INode;
import synoptic.util.InternalSynopticException;
import synoptic.util.time.DTotalTime;
import synoptic.util.time.FTotalTime;
import synoptic.util.time.ITime;
import synoptic.util.time.ITotalTime;

public abstract class ConstrainedTracingSet<T extends INode<T>> extends
        TracingStateSet<T> {

    /**
     * An extension of a HistoryNode which also records time deltas
     */
    public class ConstrainedHistoryNode extends HistoryNode {
        ITime tDelta;
        
        public ConstrainedHistoryNode(T node, HistoryNode previous, int count,
                ITime tDelta) {
            super(node, previous, count);
            this.tDelta = tDelta;
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
     * Time stored by the state machine from when t=0 state was first
     * encountered
     */
    List<ITime> t;

    /**
     * Upper- or lower-bound time constraint
     */
    ITime tBound;
    EventType a, b;
    
    /**
     * Number of states in the state machine
     */
    int numStates;
    
    /**
     * A path for each state in the appropriate state machine
     */
    List<ConstrainedHistoryNode> s;

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
    @SuppressWarnings("unchecked")
    public ConstrainedTracingSet(BinaryInvariant inv, int numStates) {

        // Constrained tracing state sets can only be used with constrained
        // invariants
        assert inv instanceof TempConstrainedInvariant<?>;
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
            t.add(getZeroTime());
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
        
        ITime tMax = null;
        ITime tMin;
        Set<ITime> times = ((Partition)input).getAllTimes();
        
        // Get min (if lower constraint) or max (if upper constraint) time delta
        // of all events in input Partition
        if (isUpper) {
            tMax = getMaxTime(times);
            tMin = getMinTime(times);
        } else {
            tMin = getMinTime(times);
        }
        
        // Whether current running time will be outside the time bound at each
        // state
        List<Boolean> outOfBound = new ArrayList<Boolean>(numStates);
        
        for (int i = 0; i < numStates; ++i) {
            outOfBound.add(null);
        }
        
        // Check for times outside time bound
        for (int i = 0; i < numStates; ++i) {
            
            // TODO: Fix. This is specific to upper bounds now.
            // Negative time delta
            if (!t.get(i).lessThan(tMax)) {
                outOfBound.set(i, false);
            }

            // Check if positive time delta is within time bound
            else {
                
                // Compare new running time to time bound
                int tComparison;
                if (isUpper) {
                    tComparison = tMax.computeDelta(t.get(i)).compareTo(tBound);
                } else {
                    tComparison = tMin.computeDelta(t.get(i)).compareTo(tBound);
                }
                
                // Within bound if upper and <= bound or if lower >= bound
                if (isUpper && tComparison <= 0 || !isUpper && tComparison >= 0) {
                    outOfBound.set(i, false);
                }
                
                // Outside of bound
                else {
                    outOfBound.set(i, true);
                }
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
        transition(input, isA, isB, outOfBound, sOld, tMin, tMax);
    }
    
    protected abstract void transition(T input, boolean isA, boolean isB,
            List<Boolean> outOfBound, List<ConstrainedHistoryNode> sOld,
            ITime tMin, ITime tMax);
    
    /**
     * Get a new zero ITime of the appropriate type, the same type as the
     * constrained invariant's upper or lower time bound
     * 
     * @return
     *          A new zero ITime
     */
    protected ITime getZeroTime() {
        
        // Integer time
        if (tBound instanceof ITotalTime) {
            return new ITotalTime(0);
            
        // Floating-point time
        } else if (tBound instanceof FTotalTime) {
            return new FTotalTime(0.0f);
            
        // Double-precision floating-point time
        } else if (tBound instanceof DTotalTime) {
            return new DTotalTime(0.0);
            
        } else {
            return null;
        }
    }

    /**
     * Get the smallest time in a time series
     * 
     * @param times
     *            The time series
     * @return Smallest time
     */
    private ITime getMinTime(Set<ITime> times) {
        return getMinMaxTime(times, false);
    }
    
    /**
     * Get the largest time in a time series
     * 
     * @param times
     *            The time series
     * @return Largest time
     */
    private ITime getMaxTime(Set<ITime> times) {
        return getMinMaxTime(times, true);
    }
    
    /**
     * Get smallest or largest time
     * 
     * @param times
     *            The time series
     * @param findMax
     *            If TRUE, find max time. If FALSE, find min.
     */
    private ITime getMinMaxTime(Set<ITime> times, boolean findMax) {

        ITime minMaxTime = null;

        // Find and store the min or max time
        for (ITime time : times) {
            if (minMaxTime == null || findMax && minMaxTime.lessThan(time)
                    || !findMax && time.lessThan(minMaxTime)) {
                minMaxTime = time;
            }
        }

        if (minMaxTime == null)
            return getZeroTime();
        return minMaxTime;
    }
}
