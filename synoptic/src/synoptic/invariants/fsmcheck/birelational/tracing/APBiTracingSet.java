package synoptic.invariants.fsmcheck.birelational.tracing;

import synoptic.invariants.BinaryInvariant;
import synoptic.invariants.fsmcheck.APTracingSet;
import synoptic.invariants.fsmcheck.TracingStateSet;
import synoptic.model.event.EventType;
import synoptic.model.interfaces.INode;

public class APBiTracingSet<T extends INode<T>> extends TracingBiRelationalStateSet<T> {

    public APBiTracingSet(EventType a, EventType b) {
        super(new APTracingSet<T>(a, b));
    }

    public APBiTracingSet(BinaryInvariant inv) {
        super(new APTracingSet<T>(inv));
    }

    public APBiTracingSet(TracingStateSet<T> tracingSet) {
        super(tracingSet);
    }
    
    @Override
    public TracingBiRelationalStateSet<T> copy() {
        return new APBiTracingSet<T>(tracingSet.copy());
    }
}
