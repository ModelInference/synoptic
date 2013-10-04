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
 * State0 = states.get(0): Neither A nor B seen <br />
 * State1 = states.get(1): A seen <br />
 * State2 = states.get(2): A seen, then B seen within time bound or anything
 * else seen <br />
 * State3 = states.get(3): B seen first or after A but out of time bound
 * (permanent reject state)
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
    protected void transition(T input,
            List<ITransition<EventNode>> transitions, boolean isA, boolean isB,
            List<Boolean> outOfBound, List<ConstrainedHistoryNode> statesOld) {

        // State0 -> State0
        if (statesOld.get(0) != null && !isA && !isB) {
            states.set(0, statesOld.get(0));
        }

        // State0 -> State1
        if (statesOld.get(0) != null && isA) {
            states.set(1, statesOld.get(0));
        }

        // State1 -> State2
        if (statesOld.get(1) != null
                && (isB && !outOfBound.get(1) || !isB && !isA)) {
            states.set(2, statesOld.get(1));
        }

        // State2 -> State2
        if (statesOld.get(2) != null
                && (isB && !outOfBound.get(2) || !isB && !isA)) {
            states.set(2, preferMaxTime(statesOld.get(2), states.get(2)));
        }

        // State0 -> State3
        if (statesOld.get(0) != null && isB) {
            states.set(3, statesOld.get(0));
        }

        // State1 -> State3
        if (statesOld.get(1) != null && isB && outOfBound.get(1)) {
            states.set(3, preferMaxTime(statesOld.get(1), states.get(3)));
        }

        // State2 -> State3
        if (statesOld.get(2) != null && isB && outOfBound.get(2)) {
            states.set(3, preferMaxTime(statesOld.get(2), states.get(3)));
        }

        // State3 -> State3
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
        for (int i = 0; i < states.size(); ++i) {
            states.set(i,
                    extend(input, states.get(i), transitions, tRunning.get(i)));
        }

        // The violation subpath started if we just reached State1
        if (states.get(1) != null && statesOld.get(1) == null) {
            states.get(1).startViolationHere();
        }

        // The violation subpath ended if we just reached State3
        if (states.get(3) != null && statesOld.get(3) == null) {
            states.get(3).endViolationHere();
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
}
