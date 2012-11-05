package synoptic.invariants.fsmcheck.birelational.fsms;

import java.util.List;
import synoptic.invariants.BinaryInvariant;
import synoptic.invariants.fsmcheck.APInvFsms;
import synoptic.model.interfaces.INode;

public class APBiRelationStateSet<T extends INode<T>> extends FsmBiRelationalStateSet<T> {

    public APBiRelationStateSet(List<BinaryInvariant> invariants) {
        super(new APInvFsms<T>(invariants), invariants);
    }

}
