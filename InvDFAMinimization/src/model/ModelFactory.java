package model;

import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.TemporalInvariantSet;

/**
 * Factory class for constructing EncodedAutomatons.
 * @author Jenny
 *
 */
public class ModelFactory {

	/**
	 * Constructs an InvsModel by intersecting InvModels for each of the given temporal invariants.
	 * @param invariants a set of TemporalInvariants
	 * @return the intersected InvsModel
	 */
	public static InvsModel getModel(TemporalInvariantSet invariants) {

		// Initial model will accept all Strings.
		InvsModel model = new InvsModel();
		
		// Intersect provided invariants.
		for (ITemporalInvariant invariant : invariants) {
			InvModel current = new InvModel(invariant);
			model.intersectWith(current);
		}
		
		// Optimize by minimizing the model.
		model.minimize();
		
		return model;
	}
}
