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
     * An extension of a HistoryNode which also records time deltas
     */
    public class ConstrainedHistoryNode extends HistoryNode {
        /**
         * Concrete edge(s) used to arrive at this node
         */
        List<ITransition<EventNode>> transitions;
        ITime tDelta;
        ConstrainedHistoryNode previousConst;
        int violationStart;
        int violationEnd;

        public ConstrainedHistoryNode(T node, ConstrainedHistoryNode previous,
                int count, List<ITransition<EventNode>> transitions,
                ITime tDelta) {
            super(node, previous, count);
            this.transitions = transitions;
            this.tDelta = (tDelta != null ? tDelta : tBound.getZeroTime());
            previousConst = previous;
        }

        /**
         * Set the start of the violation subpath to this node. This node's
         * label should always be the invariant's first predicate.
         */
        public void startViolationHere() {
            violationStart = count;
        }

        /**
         * Set the end of the violation subpath to this node. This node's label
         * should always be the invariant's second predicate.
         */
        public void endViolationHere() {
            violationEnd = count;
        }

        /**
         * Converts this chain into a RelationPath list with time deltas
         */
        @Override
        public CExamplePath<T> toCounterexample(ITemporalInvariant inv) {

            List<T> path = new ArrayList<T>();
            List<List<ITransition<EventNode>>> transitionsList = new ArrayList<List<ITransition<EventNode>>>();
            List<ITime> tDeltas = new ArrayList<ITime>();
            ConstrainedHistoryNode cur = this;

            // Traverse the path of ConstrainedHistoryNodes recording T nodes,
            // transitions, and running time deltas in lists
            while (cur != null) {
                path.add(cur.node);
                transitionsList.add(cur.transitions);
                tDeltas.add(cur.tDelta);
                cur = cur.previousConst;
            }
            Collections.reverse(path);
            Collections.reverse(transitionsList);
            Collections.reverse(tDeltas);

            // Constrained invariants maintain the violation subpath and do not
            // need to be shortened but do require transitions and running time
            // deltas
            CExamplePath<T> rpath = new CExamplePath<T>(inv, path,
                    transitionsList, tDeltas, violationStart, violationEnd);

            return rpath;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            ConstrainedHistoryNode cur = this;
            while (cur != null) {
                sb.append(cur.node.getEType());
                sb.append("(");

                // Include time if it's available
                if (cur.transitions != null && cur.transitions.get(0) != null) {
                    ITransition<EventNode> trans = cur.transitions.get(0);
                    if (trans.getTimeDelta() != null
                            && !trans.getSource().isInitial()
                            && !trans.getSource().isTerminal()
                            && !trans.getTarget().isInitial()
                            && !trans.getTarget().isTerminal()) {
                        sb.append(cur.transitions.get(0).getTimeDelta());
                    }
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
            List<ITransition<EventNode>> transitions, ITime tDelta) {

        if (prior == null) {
            return null;
        }

        // Make new node extended from prior
        ConstrainedHistoryNode extendedNode = new ConstrainedHistoryNode(node,
                prior, prior.count + 1, transitions, tDelta);
        extendedNode.violationStart = prior.violationStart;
        extendedNode.violationEnd = prior.violationEnd;

        return extendedNode;
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
    public void setInitial(T input) {

        // Should only be called on INITIAL nodes
        assert (input.isInitial());

        ConstrainedHistoryNode newHistory = new ConstrainedHistoryNode(input,
                null, 0, null, null);

        // Always start on State0
        states.set(0, newHistory);

        // This node is our new previous node (for future transitions)
        previous = input;
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

        boolean isUpper;
        if (this instanceof APUpperTracingSet
                || this instanceof AFbyUpperTracingSet) {
            isUpper = true;
        } else if (this instanceof APLowerTracingSet
                || this instanceof AFbyLowerTracingSet) {
            isUpper = false;
        } else {
            throw new InternalSynopticException(
                    "ConstrainedTracingSet not updated to handle this subtype: "
                            + this.getClass());
        }

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
        List<ConstrainedHistoryNode> statesOld = states;

        // Final state nodes after the transition will be stored in s
        states = new ArrayList<ConstrainedHistoryNode>(numStates);
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
            List<Boolean> outOfBound, List<ConstrainedHistoryNode> sOld);

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
        ConstrainedTracingSet<T> casted = (ConstrainedTracingSet<T>) other;

        if (previous == null) {
            previous = casted.previous;
        }

        // For each state, keep the one with the higher running time
        for (int i = 0; i < numStates; ++i) {
            states.set(i, preferMaxTime(states.get(i), casted.states.get(i)));
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
        result.states = new ArrayList<ConstrainedHistoryNode>(states);
        result.tRunning = new ArrayList<ITime>(tRunning);
        result.previous = previous;
        result.relation = relation;

        return result;
    }

    @Override
    public boolean isSubset(TracingStateSet<T> other) {
        ConstrainedTracingSet<T> casted = (ConstrainedTracingSet<T>) other;

        for (int i = 0; i < numStates; ++i) {
            ConstrainedHistoryNode thisNode = states.get(i);
            if (thisNode != null) {
                ConstrainedHistoryNode otherNode = casted.states.get(i);
                if (otherNode == null) {
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
