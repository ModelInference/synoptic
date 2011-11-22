package model;

import java.util.ArrayList;
import java.util.List;


public class InvsModel extends EncodedAutomaton {
	private List<InvModel> invariants;
	
	public InvsModel() {
		invariants = new ArrayList<InvModel>();
	}
	
	public void intersectWith(List<InvModel> invs) {
		for (InvModel inv : invs) {
			this.intersectWith(inv);
		}
	}
	
	public void intersectWith(InvModel inv) {
		invariants.add(inv);
		super.intersectWith(inv);
	}
}
