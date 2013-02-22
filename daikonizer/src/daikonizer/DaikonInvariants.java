package daikonizer;

import java.util.Iterator;
import java.util.List;

import daikon.inv.Invariant;

/**
 * A wrapper class for a list of Daikon invariants.
 * 
 * These Daikon invariants will be associated with a transition in a Synoptic
 * model. We need this class because TransitionLabelType constructor expects
 * a Class instance.
 * 
 * @author rsukkerd
 *
 */
public class DaikonInvariants implements Iterable<Invariant> {
    private final List<Invariant> invariants;
    
    public DaikonInvariants(List<Invariant> invariants) {
        this.invariants = invariants;
    }

    @Override
    public Iterator<Invariant> iterator() {
        return invariants.iterator();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof DaikonInvariants)) {
            return false;
        }
        DaikonInvariants other = (DaikonInvariants) obj;
        return other.invariants.equals(invariants);
    }
    
    @Override
    public int hashCode() {
        return invariants.hashCode();
    }
    
    @Override
    public String toString() {
        String str = "";
        for (int i = 0; i < invariants.size() - 1; i++) {
            str += invariants.get(i).toString();
            str += "\n";
        }
        str += invariants.get(invariants.size() - 1).toString();
        return str;
    }
}
