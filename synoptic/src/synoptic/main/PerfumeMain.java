package synoptic.main;

import synoptic.invariants.TemporalInvariantSet;
import synoptic.invariants.miners.ConstrainedInvMiner;
import synoptic.model.ChainsTraceGraph;

/**
 * Contains entry points for the command line version of Perfume.
 */
public class PerfumeMain extends AbstractMain {

    @Override
    public TemporalInvariantSet mineTOInvariants(
            boolean useTransitiveClosureMining, ChainsTraceGraph traceGraph) {

        if (useTransitiveClosureMining) {
            logger.warning("Using transitive closure mining was requested, but this is not supported by Perfume. Continuing without transitive closure mining.");
        }

        // Mine unconstrained Synoptic invariants
        TemporalInvariantSet unconstrainedInvs = mineTOInvariantsCommon(false,
                traceGraph);

        // Mine performance-constrained invariants
        long startTime = loggerInfoStart("Mining performance-constrained invariants...");
        ConstrainedInvMiner constrainedMiner = new ConstrainedInvMiner();

        // Augment unconstrained invariants with performance information. A
        // 'false' parameter is hard-coded because Perfume does not support the
        // multipleRelations flag.
        TemporalInvariantSet allInvs = constrainedMiner.computeInvariants(
                traceGraph, false, unconstrainedInvs);

        loggerInfoEnd("Constrained mining took ", startTime);

        return allInvs;
    }
}
