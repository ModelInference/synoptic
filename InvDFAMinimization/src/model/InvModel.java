package model;

import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.TOInitialTerminalInvariant;
import synoptic.main.TraceParser;
import synoptic.model.EventType;

public class InvModel extends EncodedAutomaton {
	
	private ITemporalInvariant inv;
	
	public InvModel(ITemporalInvariant invariant) {
		this.inv = invariant;
		char first = super.getEncoding(inv.getFirst().toString());
		char second = super.getEncoding(inv.getSecond().toString());
		String re = inv.getRegex(first, second);
		super.intersectWithRE(re);
	}
	
	public static InvModel initialTerminalInv(EventType initial, EventType terminal) {
		return new InvModel(new TOInitialTerminalInvariant(initial, 
				terminal, TraceParser.defaultRelation));
	}
}
