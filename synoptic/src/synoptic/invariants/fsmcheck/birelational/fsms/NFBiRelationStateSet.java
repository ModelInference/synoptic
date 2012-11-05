package synoptic.invariants.fsmcheck.birelational.fsms;

import java.util.List;
import synoptic.invariants.BinaryInvariant;
import synoptic.invariants.fsmcheck.NFbyInvFsms;
import synoptic.model.interfaces.INode;

public class NFBiRelationStateSet<T extends INode<T>> extends FsmBiRelationalStateSet<T> {

    public NFBiRelationStateSet(List<BinaryInvariant> invariants) {
        super(new NFbyInvFsms<T>(invariants), invariants);
    }

}
