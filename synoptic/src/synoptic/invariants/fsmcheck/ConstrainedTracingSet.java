package synoptic.invariants.fsmcheck;

import java.util.List;

import synoptic.invariants.BinaryInvariant;
import synoptic.invariants.constraints.TempConstrainedInvariant;
import synoptic.model.event.EventType;
import synoptic.model.interfaces.INode;
import synoptic.model.interfaces.ITransition;
import synoptic.util.time.ITime;

public abstract class ConstrainedTracingSet<T extends INode<T>> extends
        TracingStateSet<T> {

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
    }

    /**
     * Get the largest time delta of all transitions in a list
     * 
     * @param transitions
     *            A list of transitions with time deltas
     * @return Largest ITime time delta
     */
    protected <Node extends INode<Node>> ITime getMaxTimeDelta(
            List<? extends ITransition<Node>> transitions) {

        assert !transitions.isEmpty();

        // Get first transition's time delta
        ITime maxTime = transitions.get(0).getTimeDelta();

        // Find and store the max time delta
        for (ITransition<Node> trans : transitions) {
            if (maxTime.lessThan(trans.getTimeDelta())) {
                maxTime = trans.getTimeDelta();
            }
        }

        return maxTime;
    }

    /**
     * Get the smallest time delta of all transitions in a list
     * 
     * @param transitions
     *            A list of transitions with time deltas
     * @return Smallest ITime time delta
     */
    protected <Node extends INode<Node>> ITime getMinTimeDelta(
            List<? extends ITransition<Node>> transitions) {

        assert !transitions.isEmpty();

        // Get first transition's time delta
        ITime minTime = transitions.get(0).getTimeDelta();

        // Find and store the max time delta
        for (ITransition<Node> trans : transitions) {
            if (trans.getTimeDelta().lessThan(minTime)) {
                minTime = trans.getTimeDelta();
            }
        }

        return minTime;
    }
}
