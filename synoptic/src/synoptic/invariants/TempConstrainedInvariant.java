package synoptic.invariants;

import java.util.List;
import java.util.Set;

import synoptic.model.EventType;
import synoptic.model.interfaces.INode;

public abstract class TempConstrainedInvariant<B extends BinaryInvariant> extends BinaryInvariant {
	protected B inv;
	protected IInequality constr;
	
	public TempConstrainedInvariant(B inv, IInequality constr) {
		super(inv.first, inv.second, inv.relations);
		this.inv = inv;
		this.constr = constr;
    }
}
