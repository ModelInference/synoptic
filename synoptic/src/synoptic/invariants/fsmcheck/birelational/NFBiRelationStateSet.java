package synoptic.invariants.fsmcheck.birelational;

import java.util.List;
import java.util.Set;

import synoptic.invariants.BinaryInvariant;
import synoptic.invariants.fsmcheck.APInvFsms;
import synoptic.invariants.fsmcheck.NFbyInvFsms;
import synoptic.model.interfaces.INode;

public class NFBiRelationStateSet<T extends INode<T>> extends FsmBiRelationalStateSet<T> {

    public NFBiRelationStateSet(List<BinaryInvariant> invariants) {
        super(new NFbyInvFsms<T>(invariants), invariants);
    }

}
