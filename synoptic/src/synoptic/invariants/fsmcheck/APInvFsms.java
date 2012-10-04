package synoptic.invariants.fsmcheck;

import java.util.BitSet;
import java.util.List;

import synoptic.invariants.BinaryInvariant;
import synoptic.model.event.Event;
import synoptic.model.interfaces.INode;

/**
 * FSM for a set of invariants of the form "A always precedes B". The FSM enters
 * a permanent success state upon encountering an A, and enters a permanent
 * failure state upon encountering a B. This reflects the fact that the first of
 * the two events encountered is the only thing relevant to the failure state of
 * the invariant. NOTE: ensure this documentation stays consistent with
 * APTracingSet.
 * 
 * @author Michael Sloan (mgsloan@gmail.com)
 * @see APTracingSet
 * @see FsmStateSet
 */
public class APInvFsms<T extends INode<T>> extends FsmStateSet<T> {
    /**
     * <pre>
     * State 1: Accept state (no A or B seen)
     * State 2: Permanent accept state (A seen first)
     * State 3: Permanent fail state (B seen first)
     * 
     * (non-a/b preserves state) 1 -a-> 2, 1 -b-> 3
     * </pre>
     */

    private static int STATE_ONE = 0;
    private static int STATE_TWO = 1;
    private static int STATE_THREE = 2;

    public APInvFsms(List<BinaryInvariant> invs) {
        // 3 states
        super(invs, 3);
    }

    @Override
    public boolean isFail() {
        return !sets.get(STATE_THREE).isEmpty();
    }

    @Override
    public BitSet whichFail() {
        return (BitSet) sets.get(STATE_THREE).clone();
    }

    @Override
    public BitSet whichPermanentFail() {
        return (BitSet) sets.get(STATE_THREE).clone();
    }

    @Override
    public void setInitial(T input) {
        BitSet isA = getInputInvariantsDependencies(0, input);
        BitSet isB = getInputInvariantsDependencies(1, input); 
        BitSet neither = nor(isA, isB, invariantsCount);
        sets.set(STATE_ONE, neither);
        sets.set(STATE_TWO, isA);
        sets.set(STATE_THREE, isB);
    }

    @Override
    public void transition(T input) {
        // Inputs cloned so that they can be mutated.
        BitSet isA = getInputCopy(0, input);
        BitSet isB = getInputCopy(1, input);
        BitSet neither = nor(isA, isB, invariantsCount);
        BitSet s1 = sets.get(STATE_ONE);
        BitSet s2 = sets.get(STATE_TWO);
        BitSet s3 = sets.get(STATE_THREE);

        /*
         * n = !(isA | isB) (simultaneous assignment - order not significant) s1
         * = s1 & n s2 = s2 | (s1 & isA) s3 = s3 | (s1 & isB)
         */

        // Transition to s2 if in s1 and input is A
        isA.and(s1); // isA = s1 & isA
        s2.or(isA); // s2 = s2 | (s1 & isA)

        // Transition to s3 if in s1 and input is B
        isB.and(s1); // isB = s1 & isA
        s3.or(isB); // s3 = s3 | (s1 & isB)

        // Stay at s1 if input is neither A or B
        s1.and(neither);
    }

    @Override
    public void transition(T input, String relation) {
        super.transition(input, relation);
        
    }
}
