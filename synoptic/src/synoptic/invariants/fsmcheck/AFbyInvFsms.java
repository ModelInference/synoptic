package synoptic.invariants.fsmcheck;

import java.util.BitSet;
import java.util.List;

import synoptic.invariants.BinaryInvariant;
import synoptic.model.interfaces.INode;

/**
 * FSM for a set of invariants of the form "A always followed by B". The FSM
 * enters a failure state when A is encountered, and then enters a success state
 * when B is encountered. This means that the current state upon encountering a
 * final node indicates which, of A and B, was last encountered. NOTE: ensure
 * this documentation stays consistent with AFbyTracingSet.
 * 
 * @see AFbyTracingSet
 * @see FsmStateSet
 */
public class AFbyInvFsms<T extends INode<T>> extends FsmStateSet<T> {
    /**
     * <pre>
     * State 1: Accept state (no A or B seen)
     * State 2: Failed state (saw A before any B)
     * State 3: Accept state (saw B after an A)
     * 
     * (non-a/b preserves state) 1 -a-> 2, 1 -b-> 1, 2 -a-> 2, 2 -b-> 1
     * </pre>
     */

    public AFbyInvFsms(List<BinaryInvariant> invs) {
        super(invs, 2);
    }

    @Override
    public boolean isFail() {
        return !sets.get(1).isEmpty();
    }

    @Override
    public BitSet whichFail() {
        return (BitSet) sets.get(1).clone();
    }

    @Override
    public BitSet whichPermanentFail() {
        return new BitSet();
    }

    @Override
    public void setInitial(T input) {
        BitSet isA = getInputCopy(0, input);

        sets.set(1, (BitSet) isA.clone());
        // Modify isA to be the complement of itself.
        isA.flip(0, count);
        sets.set(0, isA);
    }

    @Override
    public void transition(T input) {
        BitSet isA = getInputInvariantsDependencies(0, input);
        BitSet isB = getInputInvariantsDependencies(1, input);
        BitSet neither = nor(isA, isB, count);
        BitSet s1 = sets.get(0);
        BitSet s2 = sets.get(1);

        /*
         * neither = !(isA | isB) (simultaneous assignment - order not
         * significant) s1 = (s1 & neither) | isB s2 = (s2 & neither) | isA
         */

        s1.and(neither);
        s1.or(isB);

        s2.and(neither);
        s2.or(isA);
    }
}
