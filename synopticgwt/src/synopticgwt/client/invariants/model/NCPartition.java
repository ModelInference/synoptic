package synopticgwt.client.invariants.model;

import java.util.HashSet;
import java.util.Set;

/**
 * This class contains the set of all NCwith invariants that implicate a
 * particular baseEvent. This set of invariants is used to compactly present
 * this set of relationships to the user. For example, the set of invariant is
 * highlighted whenever the user mouses-over the baseEvent in the
 * InvariantsGraph. Unlike ACPartition, NCwith invariants, the set of which are
 * represented by this class, do _not_ have a transitive property.
 * 
 * @author t101jv
 */
public class NCPartition {

    private Event baseEvent;
    private Set<POInvariant> NCInvs;

    public NCPartition(Event baseEvent) {
        this.baseEvent = baseEvent;
        this.NCInvs = new HashSet<POInvariant>();
        baseEvent.setNCPartition(this);
    }

    public boolean add(POInvariant gci) {
        boolean result = isInvariantOverBaseEvent(gci);
        if (result) {
            NCInvs.add(gci);
        }
        return result;
    }

    /**
     * Returns whether or not gci contains baseEvent.
     * 
     * @param gci
     * @return
     */
    public boolean isInvariantOverBaseEvent(POInvariant gci) {
        boolean equalsSrc = gci.getA().equals(baseEvent);
        boolean equalsDst = gci.getB().equals(baseEvent);
        return equalsSrc || equalsDst;
    }

    public void highlightOn() {
        for (POInvariant gci : NCInvs) {
            gci.highlightOn();
        }
    }

    public void highlightOff() {
        for (POInvariant gci : NCInvs) {
            gci.highlightOff();
        }
    }
}
