package synoptic.invariants.fsmcheck.birelational.tracing;

import synoptic.invariants.BinaryInvariant;
import synoptic.invariants.fsmcheck.NFbyTracingSet;
import synoptic.model.event.EventType;
import synoptic.model.interfaces.INode;

public class NFBiTracingSet<T extends INode<T>> extends TracingBiRelationalStateSet<T> {

    public NFBiTracingSet(EventType a, EventType b) {
        super(new NFbyTracingSet<T>(a, b));
    }

    public NFBiTracingSet(BinaryInvariant inv) {
        super(new NFbyTracingSet<T>(inv));
    }
}
