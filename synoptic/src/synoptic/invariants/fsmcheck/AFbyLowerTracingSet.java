package synoptic.invariants.fsmcheck;

import java.util.List;

import synoptic.invariants.BinaryInvariant;
import synoptic.model.EventNode;
import synoptic.model.interfaces.INode;
import synoptic.model.interfaces.ITransition;
import synoptic.util.time.ITime;

/**
 * NFA state set for the AFbyLower constrained invariant which keeps the
 * shortest path justifying a given state being inhabited. <br />
 * <br />
 * State0 = states.get(0): A not seen <br />
 * State1 = states.get(1): A seen (reject state) <br />
 * State2 = states.get(2): A seen, then something other than B seen (reject
 * state) <br />
 * State3 = states.get(3): A seen, then B seen within time bound <br />
 * State4 = states.get(4): A seen, then B seen out of time bound (permanent
 * reject state)
 * 
 * @author Tony Ohmann (ohmann@cs.umass.edu)
 * @param <T>
 *            The node type, used as an input, and stored in path-history.
 */
public class AFbyLowerTracingSet<T extends INode<T>> extends
        ConstrainedTracingSet<T> {

    /**
     * Empty constructor for copy() and testing
     */
    public AFbyLowerTracingSet() {

    }

    public AFbyLowerTracingSet(BinaryInvariant inv) {
        super(inv, 5);
    }

    @Override
    protected void transition(T input,
            List<ITransition<EventNode>> transitions, boolean isA, boolean isB,
            List<Boolean> outOfBound, List<ConstrainedHistoryNode<T>> statesOld) {

        // State0 -> State0
        if (statesOld.get(0) != null && !isA) {
            states.set(0, statesOld.get(0));
        }

        // State0 -> State1
        if (statesOld.get(0) != null && isA) {
            states.set(1, statesOld.get(0));
        }

        // State2 -> State1
        if (statesOld.get(2) != null && isA) {
            states.set(1, preferMinTime(statesOld.get(2), states.get(1)));
        }

        // State3 -> State1
        if (statesOld.get(3) != null && isA) {
            states.set(1, preferMinTime(statesOld.get(3), states.get(1)));
        }

        // State1 -> State2
        if (statesOld.get(1) != null && !isB) {
            states.set(2, statesOld.get(1));
        }

        // State2 -> State2
        if (statesOld.get(2) != null && !isA && !isB) {
            states.set(2, preferMinTime(statesOld.get(2), states.get(2)));
        }

        // State1 -> State3
        if (statesOld.get(1) != null && isB && !outOfBound.get(1)) {
            states.set(3, statesOld.get(1));
        }

        // State2 -> State3
        if (statesOld.get(2) != null && isB && !outOfBound.get(2)) {
            states.set(3, preferMinTime(statesOld.get(2), states.get(3)));
        }

        // State3 -> State3
        if (statesOld.get(3) != null && !isA) {
            states.set(3, preferMinTime(statesOld.get(3), states.get(3)));
        }

        // State1 -> State4
        if (statesOld.get(1) != null && isB && outOfBound.get(1)) {
            states.set(4, statesOld.get(1));
        }

        // State2 -> State4
        if (statesOld.get(2) != null && isB && outOfBound.get(2)) {
            states.set(4, preferMinTime(statesOld.get(2), states.get(4)));
        }

        // State4 -> State4
        if (statesOld.get(4) != null) {
            states.set(4, preferMinTime(statesOld.get(4), states.get(4)));
        }

        // Retrieve the previously-found min time delta
        ITime tMin = transitions.get(0).getTimeDelta();
        if (tMin == null) {
            tMin = tBound.getZeroTime();
        }

        // Update the running time deltas of any states which require it. State0
        // disregards time. State1 sets time to 0, which is the default value.
        // State2,4 require updates.
        if (states.get(2) != null) {
            tRunning.set(2, tMin.incrBy(states.get(2).tDelta));
        }
        if (states.get(4) != null) {
            tRunning.set(4, tMin.incrBy(states.get(4).tDelta));
        }

        // Extend histories for each state
        for (int i = 0; i < states.size(); ++i) {
            states.set(i,
                    extend(input, states.get(i), transitions, tRunning.get(i)));
        }

        // The violation subpath whenever we reach State1
        if (states.get(1) != null) {
            states.get(1).startViolationHere();
        }

        // The violation subpath ended if we just reached State4
        if (states.get(4) != null && statesOld.get(4) == null) {
            states.get(4).endViolationHere();
        }
    }

    @Override
    public HistoryNode<T> failpath() {
        return states.get(4);
    }

    @Override
    public ConstrainedTracingSet<T> newOfThisType() {
        return new AFbyLowerTracingSet<T>();
    }
}
