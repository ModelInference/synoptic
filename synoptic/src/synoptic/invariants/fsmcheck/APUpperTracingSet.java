package synoptic.invariants.fsmcheck;

import java.util.ArrayList;
import java.util.List;

import synoptic.invariants.BinaryInvariant;
import synoptic.model.EventNode;
import synoptic.model.interfaces.INode;
import synoptic.model.interfaces.ITransition;
import synoptic.util.time.ITime;

/**
 * NFA state set for the APUpper constrained invariant which keeps the shortest
 * path justifying a given state being inhabited. <br />
 * <br />
 * states.get(0): Neither A nor B seen <br />
 * states.get(1): A seen <br />
 * states.get(2): A seen, then B seen within time bound or anything else seen <br />
 * states.get(3): B seen first or after A but out of time bound
 * 
 * @author Tony Ohmann (ohmann@cs.umass.edu)
 * @param <T>
 *            The node type, used as an input, and stored in path-history.
 */
public class APUpperTracingSet<T extends INode<T>> extends
        ConstrainedTracingSet<T> {

    /**
     * Empty constructor for copy()
     */
    private APUpperTracingSet() {

    }

    public APUpperTracingSet(BinaryInvariant inv) {
        super(inv, 4);
    }

    @Override
    public void setInitial(T input) {

        // Should only be called on INITIAL nodes
        assert (input.isInitial());

        ConstrainedHistoryNode newHistory = new ConstrainedHistoryNode(input,
                null, 1, null, null);

        // Always start on State0
        states.set(0, newHistory);

        // This node is our new previous node (for future transitions)
        previous = input;
    }

    @Override
    protected void transition(T input,
            List<ITransition<EventNode>> transitions, boolean isA, boolean isB,
            List<Boolean> outOfBound, List<ConstrainedHistoryNode> statesOld) {

        // s.get(0) -> s.get(0)
        if (statesOld.get(0) != null && !isA && !isB) {
            states.set(0, statesOld.get(0));
        }

        // s.get(0) -> s.get(1)
        if (statesOld.get(0) != null && isA) {
            states.set(1, statesOld.get(0));
        }

        // s.get(1) -> s.get(2)
        if (statesOld.get(1) != null
                && (isB && !outOfBound.get(1) || !isB && !isA)) {
            states.set(2, statesOld.get(1));
        }

        // s.get(2) -> s.get(2)
        if (statesOld.get(2) != null
                && (isB && !outOfBound.get(2) || !isB && !isA)) {
            states.set(2, preferMaxTime(statesOld.get(2), states.get(2)));
        }

        // s.get(0) -> s.get(3)
        if (statesOld.get(0) != null && isB) {
            states.set(3, statesOld.get(0));
        }

        // s.get(1) -> s.get(3)
        if (statesOld.get(1) != null && isB && outOfBound.get(1)) {
            states.set(3, preferMaxTime(statesOld.get(1), states.get(3)));
        }

        // s.get(2) -> s.get(3)
        if (statesOld.get(2) != null && isB && outOfBound.get(2)) {
            states.set(3, preferMaxTime(statesOld.get(2), states.get(3)));
        }

        // s.get(3) -> s.get(3)
        if (statesOld.get(3) != null) {
            states.set(3, preferMaxTime(statesOld.get(3), states.get(3)));
        }

        // Retrieve the previously-found max time delta
        ITime tMax = transitions.get(0).getTimeDelta();
        if (tMax == null) {
            tMax = tBound.getZeroTime();
        }

        // Update the running time deltas of any states which require it. State0
        // disregards time. State1 sets time to 0, which is the default value.
        // State2,3 require updates.
        if (states.get(2) != null) {
            tRunning.set(2, tMax.incrBy(states.get(2).tDelta));
        }
        if (states.get(3) != null) {
            tRunning.set(3, tMax.incrBy(states.get(3).tDelta));
        }

        // Extend histories for each state
        states.set(0,
                extend(input, states.get(0), transitions, tRunning.get(0)));
        states.set(1,
                extend(input, states.get(1), transitions, tRunning.get(1)));
        states.set(2,
                extend(input, states.get(2), transitions, tRunning.get(2)));
        // Do not extend permanent failure state State3 except (1) to add a
        // finishing terminal node or (2) if we just got to State3 for the first
        // time, i.e., from another state
        if (input.isTerminal() || states.get(3) != null
                && !states.get(3).equals(statesOld.get(3))) {
            states.set(3,
                    extend(input, states.get(3), transitions, tRunning.get(3)));
        }
    }

    @Override
    public HistoryNode failpath() {
        return states.get(3);
    }

    @Override
    public APUpperTracingSet<T> copy() {

        APUpperTracingSet<T> result = new APUpperTracingSet<T>();

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
    public void mergeWith(TracingStateSet<T> other) {
        APUpperTracingSet<T> casted = (APUpperTracingSet<T>) other;

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

    @Override
    public boolean isSubset(TracingStateSet<T> other) {
        // APUpperTracingSet<T> casted = (APUpperTracingSet<T>) other;
        // if (casted.s.get(0) == null && s.get(0) != null) {
        // return false;
        // } else if (casted.s.get(1) == null && s.get(1) != null) {
        // return false;
        // } else if (casted.s.get(2) == null && s.get(2) != null) {
        // return false;
        // } else if (casted.s.get(3) == null && s.get(3) != null) {
        // return false;
        // } else {
        // return true;
        // }

        // TODO: Find out what "subset" means for constrained tracing sets
        return false;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("APUpper: ");
        appendWNull(result, states.get(0));
        result.append(" | ");
        appendWNull(result, states.get(1));
        result.append(" | ");
        appendWNull(result, states.get(2));
        result.append(" | ");
        appendWNull(result, states.get(3));
        return result.toString();
    }
}
