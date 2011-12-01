package synopticgwt.client.invariants;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a set of concurrent ACWith invariants. Concurrency is defined as:
 * For any ACWith invariants i, j where each invariant has a src and a dst
 * event, i.src is always concurrent with j.src and j.dst, and i.dst is always
 * concurrent with j.src and j.dst. The invariant defined over this class is that
 * for any invariants k, l in the partition, k and l are concurrent.
 */
public class GraphicConcurrencyPartition {
    /** Set of concurrent invariants */
    private Set<GraphicConcurrentInvariant> concurrentInvs;

    public GraphicConcurrencyPartition() {
        concurrentInvs = new HashSet<GraphicConcurrentInvariant>();
    }

    /**
     * Adds gci to the partition if it's concurrent or empty
     * 
     * @param gci
     * @return
     */
    public void add(GraphicConcurrentInvariant gci) {
        if (concurrentInvs.isEmpty() || isTransitive(gci)) {
            concurrentInvs.add(gci);
            GraphicEvent src = gci.getA();
            src.setACPartition(this);
            GraphicEvent dst = gci.getB();
            dst.setACPartition(this);
        }
    }

    /**
     * Determines whether or not gci is concurrent with this partition.
     * 
     * @param gci
     * @return
     */
    public boolean isTransitive(GraphicConcurrentInvariant gci) {
        /*
         * Finding a single invariant in the partition that gci is concurrent
         * with implies gci is concurrent with every other invariant and
         * therefore the partition by transitivity.
         */
        for (GraphicConcurrentInvariant gcInv : concurrentInvs) {
            if (gcInv.isTransitive(gci)) {
                return true;
            }
        }
        return false;
    }

    public void highlightOn() {
        for (GraphicConcurrentInvariant gci : concurrentInvs) {
            gci.highlightOn();
        }
    }

    public void highlightOff() {
        for (GraphicConcurrentInvariant gci : concurrentInvs) {
            gci.highlightOff();
        }
    }

}
