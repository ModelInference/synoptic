package daikonizer;

import java.util.ArrayList;
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
public class DaikonInvariants implements Iterable<Invariant>, 
        Comparable<DaikonInvariants> {
    private final List<Invariant> invariants;
    // We maintain a list of string representations of the invariants
    // for comparison purposes (e.g., use in equals, hashCode etc.)
    // since Invariant and its inherited classes do not implement
    // equals and hashCode methods, but Invariant implements toString.
    private final List<String> invariantStr;
    
    public DaikonInvariants(List<Invariant> invariants) {
        this.invariants = invariants;
        invariantStr = new ArrayList<String>();
        for (Invariant inv : invariants) {
            invariantStr.add(inv.toString());
        }
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
        return other.invariantStr.equals(invariantStr);
    }
    
    @Override
    public int hashCode() {
        return invariantStr.hashCode();
    }
    
    @Override
    public String toString() {
        String str = "";
        for (int i = 0; i < invariantStr.size() - 1; i++) {
            str += invariantStr.get(i);
            str += "\n";
        }
        str += invariantStr.get(invariantStr.size() - 1);
        return str;
    }

    @Override
    public int compareTo(DaikonInvariants daikonInvariants) {
        if (hashCode() < daikonInvariants.hashCode()) {
            return -1;
        }
        if (hashCode() > daikonInvariants.hashCode()) {
            return 1;
        }
        return 0;
    }
}
