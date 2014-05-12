package synoptic.invariants.fsmcheck;

import java.util.BitSet;
import java.util.List;

import synoptic.invariants.BinaryInvariant;
import synoptic.model.interfaces.INode;

/**
 * FSM for a set of invariants of the form "A never followed by B". The FSM
 * enters a new state when A is encountered. If we are in this state when a B is
 * encountered any time after, then we enter into a permanent failure state.
 * NOTE: ensure this documentation stays consistent with NFbyTracingSet.
 * 
 * @see NFbyTracingSet
 * @see FsmStateSet
 */
public class NFbyInvFsms<T extends INode<T>> extends FsmStateSet<T> {
    /**
     * <pre>
     * State 1: Accept state (no A seen, maybe some Bs seen)
     * State 2: Accept state (A seen)
     * State 3: Permanent failed state (B after A seen)
     * 
     * (non-a/b preserves state) 1 -a-> 2, 1 -b-> 1, 2 -a-> 2, 2 -b-> 3
     * </pre>
     */

    public NFbyInvFsms(List<BinaryInvariant> invs) {
        super(invs, 3);
    }

    @Override
    public boolean isFail() {
        return !sets.get(2).isEmpty();
    }

    @Override
    public BitSet whichFail() {
        return (BitSet) sets.get(2).clone();
    }

    @Override
    public BitSet whichPermanentFail() {
        return (BitSet) sets.get(2).clone();
    }

    @Override
    public void setInitial(T input) {
        BitSet isA = getInputCopy(0, input);
        sets.set(1, (BitSet) isA.clone());
        isA.flip(0, count);
        sets.set(0, isA);
    }

    @Override
    public void transition(T input) {
        /*
         * NOTE: unlike the other synoptic.invariants, isA and isB can be
         * simultaneously 1 (simultaneous assignment - order not significant) s1
         * = s1 & !isA s2 = (s1 & isA) | (s2 & !isB) s3 = s3 | (s2 & isB)
         */

        // isA is cloned so that it can be mutated.
        BitSet isA = getInputInvariantsDependencies(0, input);
        BitSet isB = getInputInvariantsDependencies(1, input);
        BitSet s1 = sets.get(0);
        BitSet s2 = sets.get(1);
        BitSet s3 = sets.get(2);

        // var = expression in terms of original values

        BitSet t = (BitSet) s2.clone();
        t.and(isB); // t = s2 & isB
        s3.or(t); // s3 = s3 | (s2 & isB)

        t = (BitSet) s1.clone();
        t.and(isA); // t = s1 & isA
        s2.andNot(isB); // s2 = s2 & !isB
        s2.or(t); // s2 = (s1 & isA) | (s2 & !isB)

        s1.andNot(isA); // s1 = s1 & !isA
    }
}
