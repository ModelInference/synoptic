package synoptic.invariants.fsmcheck;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import synoptic.invariants.BinaryInvariant;
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
 * @param <T>
 *            The node type, used as an input, and stored in path-history.
 */
public abstract class ConstrainedTracingSet<T extends INode<T>> extends
        TracingStateSet<T> {

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
     * A path for each state in the appropriate state machine. An explanation
     * for each state should be included in the documentation for any
     * subclasses. States are stored this way due to some constrained FSM states
     * lacking concise and descriptive names.
     */
    List<ConstrainedHistoryNode<T>> states;

    /**
     * The node (usually Partition) being transitioned _from_
     */
    T previous;

    /**
     * The (single) relation of the invariant
     */
    Set<String> relation;

    /**
     * Extends this node with another
     */
    public ConstrainedHistoryNode<T> extend(T node,
            ConstrainedHistoryNode<T> prior,
            List<ITransition<EventNode>> transitions, ITime tDelta) {

        if (prior == null) {
            return null;
        }

        // Make new node extended from prior
        ConstrainedHistoryNode<T> extendedNode = new ConstrainedHistoryNode<T>(
                node, prior, prior.count + 1, transitions, tDelta);
        extendedNode.violationStart = prior.violationStart;
        extendedNode.violationEnd = prior.violationEnd;

        return extendedNode;
    }

    /**
     * Return the non-null path with the smaller running time delta
     */
    public ConstrainedHistoryNode<T> preferMinTime(
            ConstrainedHistoryNode<T> first, ConstrainedHistoryNode<T> second) {
        return preferMinMaxTime(first, second, false);
    }

    /**
     * Return the non-null path with the larger running time delta
     */
    public ConstrainedHistoryNode<T> preferMaxTime(
            ConstrainedHistoryNode<T> first, ConstrainedHistoryNode<T> second) {
        return preferMinMaxTime(first, second, true);
    }

    /**
     * Return the non-null path with the smaller or larger (whichever is
     * requested) running time delta
     * 
     * @param findMax
     *            If TRUE, find path with larger time delta. If FALSE, smaller.
     */
    private ConstrainedHistoryNode<T> preferMinMaxTime(
            ConstrainedHistoryNode<T> first, ConstrainedHistoryNode<T> second,
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
     * Set the states inhabited by this tracing state set. Should generally be
     * used only for testing
     * 
     * @param states
     *            The states to inhabit in the FSM representation of this
     *            tracing state set
     */
    public void setStates(List<ConstrainedHistoryNode<T>> states) {
        this.states = states;
        numStates = states.size();
    }

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

        states = new ArrayList<ConstrainedHistoryNode<T>>(numStates);
        tRunning = new ArrayList<ITime>(numStates);

        // Set up states and state times
        for (int i = 0; i < numStates; ++i) {
            states.add(null);
            tRunning.add(tBound.getZeroTime());
        }
    }

    @Override
    public void setInitial(T input) {

        // Should only be called on INITIAL nodes
        assert (input.isInitial());

        ConstrainedHistoryNode<T> newHistory = new ConstrainedHistoryNode<T>(
                input, tBound.getZeroTime());

        // Always start on State0
        states.set(0, newHistory);

        // This node is our new previous node (for future transitions)
        previous = input;
    }

    /**
     * Returns true if this ConstrainedTracingSet is of an upper-bound time
     * constraint type, false otherwise
     */
    private boolean isUpperBoundType() {
        if (this instanceof APUpperTracingSet
                || this instanceof AFbyUpperTracingSet) {
            return true;
        } else if (this instanceof APLowerTracingSet
                || this instanceof AFbyLowerTracingSet) {
            return false;
        } else {
            throw new InternalSynopticException(
                    "ConstrainedTracingSet not updated to handle this subtype: "
                            + this.getClass());
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
        Set<ITransition<EventNode>> evTransitions = null;
        if (previous != null) {
            // Get transitions
            evTransitions = ((Partition) previous)
                    .getEventTransitionsWithExactRelations((Partition) input,
                            relation);

            // Transitions should not be empty or null
            if (evTransitions == null || evTransitions.size() == 0) {
                throw new InternalSynopticException(
                        "Model-checker transitioned along non-existent edge");
            }
        }

        boolean isUpper = isUpperBoundType();

        // Get transition(s) with min (if lower constraint) or max (if upper
        // constraint) time delta
        List<ITransition<EventNode>> minMaxTrans = null;
        if (isUpper) {
            minMaxTrans = getMaxTransitions(evTransitions);
        } else {
            minMaxTrans = getMinTransitions(evTransitions);
        }

        // Store min/max time for convenience
        ITime minMaxTime = minMaxTrans.get(0).getTimeDelta();

        // Whether current running time will be outside the time bound at each
        // state
        List<Boolean> outOfBound = new ArrayList<Boolean>(numStates);

        for (int i = 0; i < numStates; ++i) {
            outOfBound.add(null);
        }

        // Check for times outside time bound
        for (int i = 0; i < numStates; ++i) {

            // Increment running time and compare to time bound
            ITime newTime = tRunning.get(i).incrBy(minMaxTime);
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
        List<ConstrainedHistoryNode<T>> statesOld = states;

        // Final state nodes after the transition will be stored in s
        states = new ArrayList<ConstrainedHistoryNode<T>>(numStates);
        for (int i = 0; i < numStates; ++i) {
            states.add(null);
        }

        // Call transition code specific to each invariant
        transition(input, minMaxTrans, isA, isB, outOfBound, statesOld);

        // The node we just transitioned _to_ is our new previous node (for
        // future transitions)
        previous = input;
    }

    protected abstract void transition(T input,
            List<ITransition<EventNode>> transitions, boolean isA, boolean isB,
            List<Boolean> outOfBound, List<ConstrainedHistoryNode<T>> sOld);

    /**
     * Get the event transition(s) with the smallest time delta
     * 
     * @param transitions
     *            The event transitions
     * @return Smallest time delta transitions
     */
    private List<ITransition<EventNode>> getMinTransitions(
            Set<ITransition<EventNode>> transitions) {
        return getMinMaxTransitions(transitions, false);
    }

    /**
     * Get the event transition(s) with the largest time delta
     * 
     * @param transitions
     *            The event transitions
     * @return Largest time delta transitions
     */
    private List<ITransition<EventNode>> getMaxTransitions(
            Set<ITransition<EventNode>> transitions) {
        return getMinMaxTransitions(transitions, true);
    }

    /**
     * Get transition(s) with min or max time delta
     */
    private List<ITransition<EventNode>> getMinMaxTransitions(
            Set<ITransition<EventNode>> transitions, boolean findMax) {

        // Min/max transitions to be returned
        List<ITransition<EventNode>> minMaxTransitions = new ArrayList<ITransition<EventNode>>();

        // Find and store transitions with min or max time delta
        for (ITransition<EventNode> curTrans : transitions) {

            // Check if there is no min/max yet
            if (minMaxTransitions.isEmpty()
                    || minMaxTransitions.get(0).getTimeDelta() == null) {

                minMaxTransitions.clear();
                minMaxTransitions.add(curTrans);

            } else {

                // Compare current transition's time with min/max time
                ITime curTime = curTrans.getTimeDelta();
                ITime minMaxTime = minMaxTransitions.get(0).getTimeDelta();
                int timeComparison = curTime.compareTo(minMaxTime);

                // Finding MAX time delta
                if (findMax) {
                    // Current time is less than max: ignore
                    if (timeComparison < 0) {
                        continue;
                    }
                    // Current time ties the max: add to list
                    else if (timeComparison == 0) {
                        minMaxTransitions.add(curTrans);
                    }
                    // Current time is more than max: replace list
                    else {
                        minMaxTransitions.clear();
                        minMaxTransitions.add(curTrans);
                    }
                }

                // Finding MIN time delta
                else {
                    // Current time is less than min: replace list
                    if (timeComparison < 0) {
                        minMaxTransitions.clear();
                        minMaxTransitions.add(curTrans);
                    }
                    // Current time ties the min: add to list
                    else if (timeComparison == 0) {
                        minMaxTransitions.add(curTrans);
                    }
                    // Current time is more than min: ignore
                    else {
                        continue;
                    }
                }
            }
        }

        return minMaxTransitions;
    }

    @Override
    public void mergeWith(TracingStateSet<T> other) {
        // Cast the parameter tracing set, and record if this is a tracing set
        // for an upper-bound constrained invariant
        ConstrainedTracingSet<T> casted = (ConstrainedTracingSet<T>) other;
        boolean isUpper = isUpperBoundType();

        // If there is no previous, set one
        if (previous == null) {
            previous = casted.previous;
        }

        for (int i = 0; i < numStates; ++i) {
            // For upper-bound types, keep the state with the higher time
            if (isUpper) {
                states.set(i,
                        preferMaxTime(states.get(i), casted.states.get(i)));
            }

            // For lower-bound types, keep the state with the lower time
            else {
                states.set(i,
                        preferMinTime(states.get(i), casted.states.get(i)));
            }

            // Update the running time at this state
            if (states.get(i) != null) {
                tRunning.set(i, states.get(i).tDelta);
            }
        }
    }

    /**
     * Return a new, empty ConstrainedTracingSet object of the same subtype as
     * the current object
     */
    public abstract ConstrainedTracingSet<T> newOfThisType();

    @Override
    public ConstrainedTracingSet<T> copy() {
        ConstrainedTracingSet<T> result = newOfThisType();

        result.a = a;
        result.b = b;
        result.tBound = tBound;
        result.numStates = numStates;
        result.states = new ArrayList<ConstrainedHistoryNode<T>>(states);
        result.tRunning = new ArrayList<ITime>(tRunning);
        result.previous = previous;
        result.relation = relation;

        return result;
    }

    @Override
    public boolean isSubset(TracingStateSet<T> o) {
        // Cast so that we can access FSM states
        ConstrainedTracingSet<T> other = (ConstrainedTracingSet<T>) o;

        // Interate over all of this tracing set's states
        for (int i = 0; i < numStates; ++i) {
            ConstrainedHistoryNode<T> thisState = states.get(i);

            // Check if this state is inhabited
            if (thisState != null) {
                ConstrainedHistoryNode<T> otherState = other.states.get(i);

                if (otherState == null) {
                    // This tracing set inhabits a state 'other' doesn't and
                    // therefore is NOT a subset of 'other'
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        // Tracing set type (minus "TracingSet" at the end)
        result.append(this.getClass().getSimpleName()
                .replaceFirst("TracingSet", "")
                + ": ");

        // Print all current FSM states
        for (int i = 0; i < states.size(); ++i) {
            appendWNull(result, states.get(i));

            // Print separator after all but the last state
            if (i < states.size() - 1) {
                result.append(" | ");
            }
        }

        return result.toString();
    }
}
