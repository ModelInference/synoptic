package synopticgwt.client.invariants;

import java.util.HashSet;
import java.util.Set;

/** What's this for? Why not use GraphicConcurrencyPartition?
 * Events that are always concurrent with each other have a nice transitive
 * relationship where if a AC b and b AC c, then a AC c. Events that are never
 * concurrent don't. For never concurrent events, a NC b and b NC c does not
 * imply a NC c
 * 
 * @author t101jv
 *
 */
public class GraphicNonConcurrentPartition {
    /** Graphic event to root the NC relationship from */
    private GraphicEvent baseEvent;
    private Set<GraphicConcurrentInvariant> NCInvs;
    
    public GraphicNonConcurrentPartition(GraphicEvent baseEvent) {
        this.baseEvent = baseEvent;
        this.NCInvs = new HashSet<GraphicConcurrentInvariant>();
        baseEvent.setNCPartition(this);
    }
    
    public boolean add(GraphicConcurrentInvariant gci) {
        boolean result = isNeverConcurrent(gci);
        if (result) {
            NCInvs.add(gci);
        } 
        return result;  
    }
    
    /** Returns whether or not gci has an event which is never
     * concurrent with baseEvent. Or, equivalently, whether or not
     * gci contains baseEvent.
     * 
     * @param gci
     * @return
     */
    public boolean isNeverConcurrent(GraphicConcurrentInvariant gci) {
        boolean equalsSrc = gci.getSrc().equals(baseEvent);
        boolean equalsDst = gci.getDst().equals(baseEvent);
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
