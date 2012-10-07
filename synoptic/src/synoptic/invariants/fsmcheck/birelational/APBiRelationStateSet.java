package synoptic.invariants.fsmcheck.birelational;

import java.util.List;
import java.util.Set;

import synoptic.invariants.BinaryInvariant;
import synoptic.invariants.fsmcheck.AFbyInvFsms;
import synoptic.invariants.fsmcheck.APInvFsms;
import synoptic.model.interfaces.INode;

public class APBiRelationStateSet<T extends INode<T>> extends FsmBiRelationalStateSet<T> {

    public APBiRelationStateSet(List<BinaryInvariant> invariants) {
        super(new APInvFsms<T>(invariants), invariants);
    }

}
