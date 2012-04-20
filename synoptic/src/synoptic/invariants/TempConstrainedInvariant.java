package synoptic.invariants;

import java.util.List;
import java.util.Set;

import synoptic.model.EventType;
import synoptic.model.interfaces.INode;

public abstract class TempConstrainedInvariant<B extends BinaryInvariant> {
	protected B inv;
	protected IInequality constr;
	
	public TempConstrainedInvariant(B inv, IInequality constr) {
		this.inv = inv;
		this.constr = constr;
    }
}
