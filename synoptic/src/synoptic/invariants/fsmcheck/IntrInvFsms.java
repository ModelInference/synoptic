package synoptic.invariants.fsmcheck;

import java.util.BitSet;
import java.util.List;

import synoptic.invariants.BinaryInvariant;
import synoptic.model.interfaces.INode;

public class IntrInvFsms<T extends INode<T>> extends FsmStateSet<T> {
    /**
     * <pre>
     * State 1: Accept state (no A seen, maybe some Bs seen)
     * State 2: Accept state (one A seen)
     * State 3: Permanent failed state (two As seen and no B in between)
     * 
     * </pre>
     */

    public IntrInvFsms(List<BinaryInvariant> invs) {
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
        // TODO: check what this does
        BitSet isA = getInputCopy(0, input);
        sets.set(1, (BitSet) isA.clone());
        isA.flip(0, count);
        sets.set(0, isA);
    }

    @Override
    public void transition(T input) {
        // TODO: check if these transitions are accurate. Seems as if there are
        // some transitions missing. Suggestion:
        // s1 = (s1 & !isA) | (s2 & isB)
        // s2 = (s1 & isA) | (s2 & !(isA | isB))
        // s3 = s3 | (s2 & isA)

        /*
         * s1 = (s1 | s2) & isB s2 = s1 & isA s3 = s3 | (s2 & isA)
         */

        BitSet isA = getInputInvariantsDependencies(0, input);
        BitSet isB = getInputInvariantsDependencies(1, input);
        BitSet s1 = sets.get(0);
        BitSet s2 = sets.get(1);
        BitSet s3 = sets.get(2);

        s1.or(s2);
        s1.and(isB);

        s2 = s1;
        s2.and(isA);

        BitSet t = (BitSet) s2.clone();
        t.and(isA);
        s3.or(t);
    }
}