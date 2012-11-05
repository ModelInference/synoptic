package synoptic.invariants.fsmcheck.birelational.tracing;

import synoptic.invariants.BinaryInvariant;
import synoptic.invariants.fsmcheck.APTracingSet;
import synoptic.model.event.EventType;
import synoptic.model.interfaces.INode;

public class APBiTracingSet<T extends INode<T>> extends TracingBiRelationalStateSet<T> {

    public APBiTracingSet(EventType a, EventType b) {
        super(new APTracingSet<T>(a, b));
    }

    public APBiTracingSet(BinaryInvariant inv) {
        super(new APTracingSet<T>(inv));
    }
}
