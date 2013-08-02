package synoptic.invariants.fsmcheck;

import java.util.ArrayList;
import java.util.List;

import synoptic.invariants.BinaryInvariant;
import synoptic.model.EventNode;
import synoptic.model.interfaces.INode;
import synoptic.util.time.ITime;

/**
 * NFA state set for the APUpper constrained invariant which keeps the shortest
 * path justifying a given state being inhabited. <br />
 * <br />
 * State0: Neither A nor B seen <br />
 * State1: A seen <br />
 * State2: A seen, then B seen within time bound <br />
 * State3: B seen first or after A but out of time bound
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
        s.set(0, newHistory);
    }

    @Override
    protected void transition(T input, EventNode event, boolean isA,
            boolean isB, List<Boolean> outOfBound,
            List<ConstrainedHistoryNode> sOld, ITime tMax) {

        // s.get(0) -> s.get(0)
        if (sOld.get(0) != null && !isA && !isB) {
            s.set(0, sOld.get(0));
        }

        // s.get(0) -> s.get(1)
        if (sOld.get(0) != null && isA) {
            s.set(1, sOld.get(0));
        }

        // s.get(1) -> s.get(2)
        if (sOld.get(1) != null && (isB && !outOfBound.get(1) || !isB && !isA)) {
            s.set(2, sOld.get(1));
        }

        // s.get(2) -> s.get(2)
        if (sOld.get(2) != null && (isB && !outOfBound.get(2) || !isB && !isA)) {
            s.set(2, preferMaxTime(sOld.get(2), s.get(2)));
        }

        // s.get(0) -> s.get(3)
        if (sOld.get(0) != null && isB) {
            s.set(3, sOld.get(0));
        }

        // s.get(1) -> s.get(3)
        if (sOld.get(1) != null && isB && outOfBound.get(1)) {
            s.set(3, preferMaxTime(sOld.get(1), s.get(3)));
        }

        // s.get(2) -> s.get(3)
        if (sOld.get(2) != null && isB && outOfBound.get(2)) {
            s.set(3, preferMaxTime(sOld.get(2), s.get(3)));
        }

        // s.get(3) -> s.get(3)
        if (sOld.get(3) != null) {
            s.set(3, preferMaxTime(sOld.get(3), s.get(3)));
        }

        // Update the running time deltas of any states which require it. State0
        // disregards time. State1 sets time to 0, which is the default value.
        // State2,3 require updates.
        if (s.get(2) != null) {
            t.set(2, tMax.incrBy(s.get(2).tDelta));
        }
        if (s.get(3) != null) {
            t.set(3, tMax.incrBy(s.get(3).tDelta));
        }

        // Extend histories for each state
        s.set(0, extend(input, event, s.get(0), t.get(0)));
        s.set(1, extend(input, event, s.get(1), t.get(1)));
        s.set(2, extend(input, event, s.get(2), t.get(2)));
        // Do not extend permanent failure state State3 except (1) to add a
        // finishing terminal node or (2) if we just got to State3 for the first
        // time, i.e., from another state
        if (input.isTerminal() || s.get(3) != null
                && !s.get(3).equals(sOld.get(3))) {
            s.set(3, extend(input, event, s.get(3), t.get(3)));
        }
    }

    @Override
    public HistoryNode failpath() {
        return s.get(3);
    }

    @Override
    public APUpperTracingSet<T> copy() {

        APUpperTracingSet<T> result = new APUpperTracingSet<T>();

        result.a = a;
        result.b = b;
        result.tBound = tBound;
        result.numStates = numStates;
        result.s = new ArrayList<ConstrainedHistoryNode>(s);
        result.t = new ArrayList<ITime>(t);
        result.previous = previous;

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
            s.set(i, preferMaxTime(s.get(i), casted.s.get(i)));
            if (s.get(i) != null) {
                t.set(i, s.get(i).tDelta);
            }
        }

        if (TEMPDEBUG) {
            System.err.print("  [times]  ");
            for (ITime tt : t)
                System.err.print(tt + ",");
            System.err.println();
        }
    }

    @Override
    public boolean isSubset(TracingStateSet<T> other) {
//        APUpperTracingSet<T> casted = (APUpperTracingSet<T>) other;
//        if (casted.s.get(0) == null && s.get(0) != null) {
//            return false;
//        } else if (casted.s.get(1) == null && s.get(1) != null) {
//            return false;
//        } else if (casted.s.get(2) == null && s.get(2) != null) {
//            return false;
//        } else if (casted.s.get(3) == null && s.get(3) != null) {
//            return false;
//        } else {
//            return true;
//        }

        // TODO: Find out what "subset" means for constrained tracing sets
        return false;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("APUpper: ");
        appendWNull(result, s.get(3)); // Failure case first.
        result.append(" | ");
        appendWNull(result, s.get(2));
        result.append(" | ");
        appendWNull(result, s.get(1));
        result.append(" | ");
        appendWNull(result, s.get(0));
        return result.toString();
    }
}
