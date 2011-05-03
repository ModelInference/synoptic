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
public class GWTInvariants implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public HashMap<String, List<String>> invs;

    public GWTInvariants() {
        invs = new HashMap<String, List<String>>();
    }

    private void addInvType(String invType) {
        List<String> val;
        if (invs.containsKey(invType)) {
            val = invs.get(invType);
        } else {
            val = new LinkedList<String>();
            invs.put(invType, val);
        }
    }

    public void addInv(String invType, String inv) {
        addInvType(invType);
        invs.get(invType).add(inv);
    }

    public Set<String> getInvTypes() {
        return invs.keySet();
    }

    public List<String> getInvs(String invType) {
        return invs.get(invType);
    }

}
