package synoptic.invariants.constraints;

import java.util.List;

import synoptic.invariants.BinaryInvariant;
import synoptic.model.interfaces.INode;

/**
 * A temporally constrained binary invariant. A composition of a binary
 * invariant instance and a temporal threshold constraint on the latencies
 * between the two events in the binary invariant.
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
