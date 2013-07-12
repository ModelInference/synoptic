package synoptic.invariants.fsmcheck;

import java.util.ArrayList;
import java.util.List;

import synoptic.invariants.BinaryInvariant;
import synoptic.model.interfaces.INode;
import synoptic.util.time.ITime;

/**
 * NFA state set for the APUpper constrained invariant which keeps the shortest
 * path justifying a given state being inhabited. <br /><br />
 * 
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
     * State0: Neither A nor B seen
     * State1: A seen
     * State2: A seen, then B seen within time bound
     * State3: B seen first or after A but out of time bound
     */

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
        assert(input.isInitial());

        ConstrainedHistoryNode newHistory = new ConstrainedHistoryNode(input, null, 1, null);
        
        // Always start on State0
        s.set(0, newHistory);
    }

    @Override
    protected void transition(T input, boolean isA, boolean isB, List<Boolean> outOfBound, List<ConstrainedHistoryNode> sOld, ITime tNew) {

        // s.get(0) -> s.get(0)
        if (sOld.get(0) != null && !isA && !isB) {
            s.set(0, sOld.get(0));
        }

        // s.get(0) -> s.get(1)
        if (sOld.get(0) != null && isA) {
            s.set(1, sOld.get(0));
            t.set(1, tNew);
        }

        // s.get(1) -> s.get(2)
        if (sOld.get(1) != null && (isB && !outOfBound.get(1) || !isB && !isA)) {
            s.set(2, sOld.get(1));
        }

        // s.get(2) -> s.get(2)
        if (sOld.get(2) != null && (isB && !outOfBound.get(2) || !isB && !isA)) {
            s.set(2, preferShorterOrLonger(sOld.get(2), s.get(2), false));
        }

        // s.get(0) -> s.get(3)
        if (sOld.get(0) != null && isB) {
            s.set(3, sOld.get(0));
        }

        // s.get(1) -> s.get(3)
        if (sOld.get(1) != null && isB && outOfBound.get(1)) {
            s.set(3, preferShorterOrLonger(sOld.get(1), s.get(3), false));
        }

        // s.get(2) -> s.get(3)
        if (sOld.get(2) != null && isB && outOfBound.get(2)) {
            s.set(3, preferShorterOrLonger(sOld.get(2), s.get(3), false));
        }

        // s.get(3) -> s.get(3)
        if (sOld.get(3) != null) {
            s.set(3, preferShorterOrLonger(sOld.get(3), s.get(3), false));
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
        
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void mergeWith(TracingStateSet<T> other) {
        APUpperTracingSet<T> casted = (APUpperTracingSet<T>) other;
        s.set(0, (ConstrainedHistoryNode) preferShorter(s.get(0), casted.s.get(0)));
        s.set(1, (ConstrainedHistoryNode) preferShorter(s.get(1), casted.s.get(1)));
        s.set(2, (ConstrainedHistoryNode) preferShorter(s.get(2), casted.s.get(2)));
        s.set(3, (ConstrainedHistoryNode) preferShorter(s.get(3), casted.s.get(3)));
        
        // Keep the lowest initial t for each state
        for (int i = 0; i < numStates; ++i) {
            if (casted.t.get(i).lessThan(t.get(i))) {
                t = casted.t;
            }
        }
    }

    @Override
    public boolean isSubset(TracingStateSet<T> other) {
        APUpperTracingSet<T> casted = (APUpperTracingSet<T>) other;
        if (casted.s.get(0) == null && s.get(0) != null) {
            return false;
        } else if (casted.s.get(1) == null && s.get(1) != null) {
            return false;
        } else if (casted.s.get(2) == null && s.get(2) != null) {
            return false;
        } else if (casted.s.get(3) == null && s.get(3) != null) {
            return false;
        } else {
            return true;
        }
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
