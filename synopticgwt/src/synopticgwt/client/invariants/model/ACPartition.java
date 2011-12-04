package synopticgwt.client.invariants.model;

import java.util.HashSet;
import java.util.Set;


/**
 * Represents a set of ACWith invariants, closed under transitive closure. That
 * is, if a and b are in the set, and b ACWith c, then c is also in the set.
 * That is, a ACwith c.
 */
public class ACPartition {
    /** Set of concurrent invariants */
    private Set<POInvariant> concurrentInvs;

    public ACPartition() {
        concurrentInvs = new HashSet<POInvariant>();
    }

    /**
     * Adds a gci to the partition if one of the event types is (transitively)
     * concurrent with at least one of the event types in this partition. If
     * not, then this is a noop.
     * 
     * @param gci
     * @return
     */
    public void add(POInvariant gci) {
        if (concurrentInvs.isEmpty() || isTransitive(gci)) {
            concurrentInvs.add(gci);
            Event src = gci.getA();
            src.setACPartition(this);
            Event dst = gci.getB();
            dst.setACPartition(this);
        }
    }

    /**
     * Determines whether or not an event type in the gci is (transitively)
     * concurrent with at least one of the event types in this partition.
     * 
     * @param gci
     * @return
     */
    public boolean isTransitive(POInvariant gci) {
        // Loop through all invariants, trying to find one that matches the
        // event type of this gci.
        for (POInvariant gcInv : concurrentInvs) {
            if (gcInv.isTransitive(gci)) {
                return true;
            }
        }
        return false;
    }

    public void highlightOn() {
        for (POInvariant gci : concurrentInvs) {
            gci.highlightOn();
        }
    }

    public void highlightOff() {
        for (POInvariant gci : concurrentInvs) {
            gci.highlightOff();
        }
    }

}
