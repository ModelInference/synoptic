package synoptic.invariants.fsmcheck.birelational.fsms;

import java.util.List;
import synoptic.invariants.BinaryInvariant;
import synoptic.invariants.fsmcheck.AFbyInvFsms;
import synoptic.model.interfaces.INode;

public class AFBiRelationStateSet<T extends INode<T>> extends FsmBiRelationalStateSet<T> {
    
    public AFBiRelationStateSet(List<BinaryInvariant> invariants) {
        super(new AFbyInvFsms<T>(invariants), invariants);
    }
    
}
