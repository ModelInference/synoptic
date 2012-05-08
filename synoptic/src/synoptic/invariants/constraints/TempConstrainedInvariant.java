package synoptic.invariants.constraints;

import java.util.List;

import synoptic.invariants.BinaryInvariant;
import synoptic.model.interfaces.INode;

/**
 * A temporally constrained binary invariant. A composition of a binary
 * invariant instance and a temporal threshold constraint on the latencies
 * between the two events in the binary invariant.
 *  
 * A temporally constrained invariant is defined as one of following:
 * <pre>
 *  Using these three conditions: 
 *  	1. a_i is followed by at least one c_i before TERMINAL.
 *
 *  	2. The first c_i, c_j, follows a_i in at most n time.
 *
 *  	3. The first c_i, c_j, follows a_i in at least n time.
 *  
 *  An "Always Followed by in less than 'n' units" invariant (an addition to
 *  {@link synoptic.invariants.AlwaysFollowedInvariant}) is defined
 *  as satisfying conditions 1 and 2.
 *
 *  An "Always Followed by in greater than 'n' units" invariant (an addition to
 *  {@link synoptic.invariants.AlwaysFollowedInvariant}) is defined
 *  as satisfying conditions 1 and 3.
 *
 * </pre>
 *
 */
public class TempConstrainedInvariant<BInv extends BinaryInvariant> extends
        BinaryInvariant {
    protected BInv inv;
    protected IThresholdConstraint constr;

    public TempConstrainedInvariant(BInv inv, IThresholdConstraint constr) {
        super(inv);
        this.inv = inv;
        this.constr = constr;
    }

    @Override
    public String getLTLString() {
        return inv.getLTLString();
    }

    @Override
    public <T extends INode<T>> List<T> shorten(List<T> path) {
        return inv.shorten(path);
    }

    @Override
    public String getShortName() {
        return inv.getShortName();
    }

    @Override
    public String getLongName() {
        return inv.getLongName();
    }

    @Override
    public String getRegex(char firstC, char secondC) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return "Binary inv:" + inv.toString() + ", Constraint: "
                + constr.toString();
    }
}
