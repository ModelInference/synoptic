package synopticgwt.shared;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * An invariants object communicated between the Synoptic service and the GWT
 * client.
 */
public class GWTInvariantSet implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Maps an invariant type (e.g., "NFby") to a list of pairs of strings,
     * where each string is an event type. For example: [["a,"b"],["c","d"]]
     */
    public HashMap<String, List<GWTInvariant>> invs;

    public GWTInvariantSet() {
        invs = new HashMap<String, List<GWTInvariant>>();
    }

    /**
     * Add a new invariant type
     * 
     * @param invType
     */
    private void addInvType(String invType) {
        if (invs.containsKey(invType)) {
            return;
        }
        invs.put(invType, new LinkedList<GWTInvariant>());
    }

    /**
     * Add a new invariant
     * 
     * @param invType
     *            invariant type (e.g., "NFby")
     * @param invPair
     *            a pair of strings
     */
    public void addInv(String invType, GWTInvariant invPair) {
        addInvType(invType);
        invs.get(invType).add(invPair);
    }

    /**
     * Returns all the known invariant types.
     * 
     * @return
     */
    public Set<String> getInvTypes() {
        return invs.keySet();
    }

    /**
     * Returns all the invariant pairs of a particular invariant type
     * 
     * @param invType
     *            invariant type
     * @return
     */
    public List<GWTInvariant> getInvs(String invType) {
        return invs.get(invType);
    }

}
