package synoptic.invariants.fsmcheck.birelational.tracing;

import synoptic.invariants.BinaryInvariant;
import synoptic.invariants.fsmcheck.NFbyTracingSet;
import synoptic.invariants.fsmcheck.TracingStateSet;
import synoptic.model.event.EventType;
import synoptic.model.interfaces.INode;

public class NFBiTracingSet<T extends INode<T>> extends TracingBiRelationalStateSet<T> {

    public NFBiTracingSet(EventType a, EventType b) {
        super(new NFbyTracingSet<T>(a, b));
    }

    public NFBiTracingSet(BinaryInvariant inv) {
        super(new NFbyTracingSet<T>(inv));
    }

    public NFBiTracingSet(TracingStateSet<T> tracingSet) {
        super(tracingSet);
    }
    
    @Override
    public TracingBiRelationalStateSet<T> copy() {
        return new NFBiTracingSet<T>(tracingSet.copy());
    }
}
