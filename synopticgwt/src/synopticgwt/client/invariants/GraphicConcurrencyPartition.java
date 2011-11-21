package synopticgwt.client.invariants;

import java.util.HashSet;
import java.util.Set;

public class GraphicConcurrencyPartition {
    private Set<GraphicConcurrentInvariant> concurrentInvs;
    
    public GraphicConcurrencyPartition() {
        concurrentInvs = new HashSet<GraphicConcurrentInvariant>();
    }
    
    public boolean add(GraphicConcurrentInvariant otherGCInv) {
        boolean transitive = isTransitive(otherGCInv);
        if (transitive) {
            concurrentInvs.add(otherGCInv);
        }
        return transitive;
    }
    
    public boolean isTransitive(GraphicConcurrentInvariant otherGCInv) {
        for (GraphicConcurrentInvariant gcInv : concurrentInvs) {
            if (gcInv.isTransitive(otherGCInv)) {
                return true;
            }
        }
        return false;
    }

}
