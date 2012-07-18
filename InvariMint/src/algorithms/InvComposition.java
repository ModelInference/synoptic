package algorithms;

import java.io.IOException;

import model.InvModel;
import model.InvsModel;

import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.TemporalInvariantSet;

public class InvComposition {

    /**
     * Constructs an InvsModel by intersecting InvModels for each of the given
     * temporal invariants.
     * 
     * @param invariants
     *            a set of TemporalInvariants
     * @param minimize
     *            whether or not to minimize the model before returning.
     * @return the intersected InvsModel
     * @throws IOException
     */
    public static InvsModel intersectModelWithInvs(
            TemporalInvariantSet invariants, boolean minimizeDFAIntersections,
            InvsModel model) throws IOException {

        // Intersect invariants into model.
        for (ITemporalInvariant invariant : invariants) {
            InvModel invDFA = new InvModel(invariant, model.getEventEncodings());
            model.intersectWith(invDFA);

            if (minimizeDFAIntersections) {
                // Optimize by minimizing the model.
                model.minimize();
            }
        }

        if (minimizeDFAIntersections) {
            // Optimize by minimizing the model.
            model.minimize();
        }

        return model;
    }

}
