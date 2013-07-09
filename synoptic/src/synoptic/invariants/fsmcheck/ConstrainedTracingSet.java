package synoptic.invariants.fsmcheck;

import java.util.List;

import synoptic.invariants.BinaryInvariant;
import synoptic.invariants.constraints.TempConstrainedInvariant;
import synoptic.model.event.EventType;
import synoptic.model.interfaces.INode;
import synoptic.model.interfaces.ITransition;
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
     * Yields the shorter (smaller time delta) or longer (larger time delta)
     * path of the two passed in
     * 
     * @param first
     *            First history node
     * @param second
     *            Second history node
     * @param findLonger
     *            If TRUE, find longer path. If FALSE, find shorter path.
     * @return The shorter or longer path, whichever was requested
     */
    public ConstrainedHistoryNode preferShorterOrLonger(
            ConstrainedHistoryNode first, ConstrainedHistoryNode second,
            boolean findLonger) {

        // If either node is null, return the other
        if (second == null) {
            return first;
        }
        if (first == null) {
            return second;
        }

        // Return the node with the smaller or larger time delta, whichever was
        // requested
        if (first.tDelta.lessThan(second.tDelta)) {
            if (findLonger) {
                return second;
            }
            return first;
        }

        // Else if first is longer...
        if (findLonger) {
            return first;
        }
        return second;
    }

    /**
     * Running time stored by the state machine since t=0 state
     */
    ITime t;

    /**
     * Upper- or lower-bound time constraint
     */
    ITime tBound;
    EventType a, b;

    /**
     * Create a new time-constrained tracing state set
     * 
     * @param a
     * @param b
     * @param tBound
     */
    public ConstrainedTracingSet(EventType a, EventType b, ITime tBound) {
        this.a = a;
        this.b = b;
        this.tBound = tBound;
        t = getZeroTime();
    }

    /**
     * Create a new time-constrained tracing state set from a constrained
     * invariant
     * 
     * @param inv
     *            Must be a constrained BinaryInvariant
     */
    @SuppressWarnings("unchecked")
    public ConstrainedTracingSet(BinaryInvariant inv) {

        // Constrained tracing state sets can only be used with constrained
        // invariants
        assert inv instanceof TempConstrainedInvariant<?>;
        TempConstrainedInvariant<BinaryInvariant> constInv = (TempConstrainedInvariant<BinaryInvariant>) inv;

        a = inv.getFirst();
        b = inv.getSecond();
        tBound = constInv.getConstraint().getThreshold();
        t = getZeroTime();
    }
    
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
     * Get the smallest or largest time delta out of all time delta series of
     * one Partition's Transitions in a list
     * 
     * @param transitions
     *            A list of one Partition's Transitions with time delta series
     * @param findMax
     *            If TRUE, find max time delta. If FALSE, find min.
     * @return Smallest or largest ITime time delta
     */
    protected <Node extends INode<Node>> ITime getMinMaxTimeDelta(
            List<? extends ITransition<Node>> transitions, boolean findMax) {

        ITime minMaxTime = null;

        // Find and store the min or max time delta
        for (ITransition<Node> trans : transitions) {
            for (ITime delta : trans.getDeltaSeries().getAllDeltas()) {
                if (minMaxTime == null || findMax && minMaxTime.lessThan(delta)
                        || !findMax && delta.lessThan(minMaxTime)) {
                    minMaxTime = delta;
                }
            }
        }

        if (minMaxTime == null)
            return getZeroTime();
        return minMaxTime;
    }
}
