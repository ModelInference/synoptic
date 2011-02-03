package synoptic.invariants.fsmcheck;

import java.util.BitSet;
import java.util.List;

import synoptic.invariants.BinaryInvariant;
import synoptic.model.interfaces.INode;

/**
 * Represents a set of "A always precedes B" synoptic.invariants to simulate.
 * This finite state machine enters a permanent success state upon encountering
 * A, and enters a permanent failure state upon encountering B. This reflects
 * the fact that the first of the two events encountered is the only thing
 * relevant to the failure state of the invariant.
 * 
 * @author Michael Sloan (mgsloan@gmail.com)
 * @see APTracingSet
 * @see FsmStateSet
 */
public class APInvFsms<T extends INode<T>> extends FsmStateSet<T> {
    public APInvFsms(int size) {
        super(size, 3);
    }

    public APInvFsms(List<BinaryInvariant> invs) {
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

    /*
     * State 1: Accept state (no A or B seen) State 2: Permanent accept state (A
     * seen first) State 3: Permanent fail state (B seen first) (non-a/b
     * preserves state) 1 -a-> 2 1 -b-> 3
     */

    public void setInitial(T input) {
        BitSet isA = getInput(0, input), isB = getInput(1, input), neither = nor(
                isA, isB, count);
        sets.set(0, neither);
        sets.set(1, isA);
        sets.set(2, isB);
    }

    @Override
    public void transition(T input) {
        // inputs cloned so that they can be mutated.
        BitSet isA = getInputCopy(0, input), isB = getInputCopy(1, input), neither = nor(
                isA, isB, count), s1 = sets.get(0), s2 = sets.get(1), s3 = sets
                .get(2);

        /*
         * n = !(isA | isB) (simultaneous assignment - order not significant) s1
         * = s1 & n s2 = s2 | (s1 & isA) s3 = s3 | (s1 & isB)
         */

        s1.and(neither);

        isA.and(s1); // isA = s1 & isA
        s2.or(isA); // s2 = s2 | (s1 & isA)

        isB.and(s1); // isB = s1 & isA
        s3.or(isB); // s3 = s3 | (s1 & isB)
    }
}