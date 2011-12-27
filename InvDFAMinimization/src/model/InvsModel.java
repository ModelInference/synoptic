package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Extends the EncodedAutomaton class to encode the intersection of multiple
 * InvModel Automatons.
 * 
 * @author Jenny
 *
 */
public class InvsModel extends EncodedAutomaton {
	private List<InvModel> invariants;
	
	/**
	 * Constructs a new InvsModel that accepts all strings.
	 */
	public InvsModel() {
		invariants = new ArrayList<InvModel>();
	}

	/**
	 * Intersects this InvsModel with all of the given InvModels and adds each to this model's
	 * list of invariants.
	 */
	public void intersectWith(List<InvModel> invs) {
		for (InvModel inv : invs) {
			this.intersectWith(inv);
		}
	}
	
	/**
	 * Intersects this InvsModel with the given InvModel and adds the invariant to this model's
	 * list of invariants.
	 */
	public void intersectWith(InvModel inv) {
		invariants.add(inv);
		super.intersectWith(inv);
	}
}
