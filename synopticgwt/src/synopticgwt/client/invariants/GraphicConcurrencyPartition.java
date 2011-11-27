package synopticgwt.client.invariants;

import java.util.HashSet;
import java.util.Set;

/** Represents a set of concurrent ACWith invariants */
public class GraphicConcurrencyPartition {
    /** Set of concurrent invariants */
    private Set<GraphicConcurrentInvariant> concurrentInvs;
    
    public GraphicConcurrencyPartition() {
        concurrentInvs = new HashSet<GraphicConcurrentInvariant>();
    }
    
    /** Adds gci to the partition if it's concurrent with the partition
     * 
     * @param gci
     * @return
     */
    public void add(GraphicConcurrentInvariant gci) {
        if (concurrentInvs.isEmpty() || isTransitive(gci)) {
            concurrentInvs.add(gci);
            GraphicEvent src = gci.getSrc();
            src.setACPartition(this);
            GraphicEvent dst = gci.getDst();
            dst.setACPartition(this);
        }
    }
    
    /** Determines whether or not otherGCInv is transitively concurrent
     * with this partition
     * @param gci
     * @return
     */
    public boolean isTransitive(GraphicConcurrentInvariant gci) {
        for (GraphicConcurrentInvariant gcInv : concurrentInvs) {
            if (gcInv.isTransitive(gci)) {
                return true;
            }
        }
        return false;
    }
    
    public void highlightOn() {
        for (GraphicConcurrentInvariant gci : concurrentInvs) {
            gci.highlightConcurrent();
        }
    }
    
    public void highlightOff() {
        for (GraphicConcurrentInvariant gci : concurrentInvs) {
            gci.highlightOff();
        }
    }

}
