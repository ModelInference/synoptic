package synoptic.invariants.fsmcheck.birelational.tracing;

import synoptic.invariants.BinaryInvariant;
import synoptic.invariants.fsmcheck.AFbyTracingSet;
import synoptic.model.event.EventType;
import synoptic.model.interfaces.INode;

public class AFBiTracingSet<T extends INode<T>> extends TracingBiRelationalStateSet<T> {

    public AFBiTracingSet(EventType a, EventType b) {
        super(new AFbyTracingSet<T>(a, b));
    }

    public AFBiTracingSet(BinaryInvariant inv) {
        super(new AFbyTracingSet<T>(inv));
    }

}
