package synopticgwt.client.invariants;

import java.util.HashSet;
import java.util.Set;

/**
 * This class contains the set of all NCwith invariants that implicate a
 * particular baseEvent. This set of invariants is used to compactly present
 * this set of relationships to the user. For example, the set of invariant is
 * highlighted whenever the user mouses-over the baseEvent in the
 * InvariantsGraph. Unlike GraphicConcurrencyPartition, NCwith invariants, the
 * set of which are represented by this class, do _not_ have a transitive
 * property.
 * 
 * @author t101jv
 */
public class GraphicNonConcurrentPartition {

    private GraphicEvent baseEvent;
    private Set<GraphicConcurrentInvariant> NCInvs;

    public GraphicNonConcurrentPartition(GraphicEvent baseEvent) {
        this.baseEvent = baseEvent;
        this.NCInvs = new HashSet<GraphicConcurrentInvariant>();
        baseEvent.setNCPartition(this);
    }

    public boolean add(GraphicConcurrentInvariant gci) {
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
    public boolean isInvariantOverBaseEvent(GraphicConcurrentInvariant gci) {
        boolean equalsSrc = gci.getA().equals(baseEvent);
        boolean equalsDst = gci.getB().equals(baseEvent);
        return equalsSrc || equalsDst;
    }

    public void highlightOn() {
        for (GraphicConcurrentInvariant gci : NCInvs) {
            gci.highlightOn();
        }
    }

    public void highlightOff() {
        for (GraphicConcurrentInvariant gci : NCInvs) {
            gci.highlightOff();
        }
    }
}
