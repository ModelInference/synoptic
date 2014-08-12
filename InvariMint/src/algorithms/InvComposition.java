package algorithms;

import java.io.IOException;
import java.util.logging.Logger;

import model.InvModel;
import model.InvsModel;

import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.TemporalInvariantSet;

public class InvComposition {

    public static Logger logger;
    static {
        logger = Logger.getLogger("InvComposition");
    }

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
        int total = invariants.numInvariants();
        int numDone = 0;

        // How many times to intersect before we minimize.
        int minimizeEveryX = 20;

        for (ITemporalInvariant invariant : invariants) {
            // logger.info("Create new invDFA instance, remaining" + remaining);
            InvModel invDFA = new InvModel(invariant, model.getEventEncodings());
            model.intersectWith(invDFA);
            invDFA = null;

            numDone += 1;

            // Optimize by minimizing the model every 20 intersections.
            if ((numDone % minimizeEveryX == 0) && minimizeDFAIntersections) {
                logger.info("Intersected " + numDone + " / " + total
                        + " invariants.");
                model.minimize();
            }
        }

        // Minimize one more time, at the end, but only if the current model
        // hasn't been minimized in the above loop.
        if ((numDone % minimizeEveryX != 0) && minimizeDFAIntersections) {
            model.minimize();
        }

        return model;
    }
}
