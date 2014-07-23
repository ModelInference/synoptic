package csight.mc.parallelizer;

import csight.model.fifosys.gfsm.GFSM;

/**
 * Inputs to the Parallelizer that are sent with ParallelizerCommands through
 * the ParallelizerTask channel. An input contains the invariant to check, the
 * model to check the invariant against, the timeout in seconds, and the current
 * invariant counter and total invariant count for logging.
 */
public class ParallelizerInput {
    protected final InvariantTimeoutPair invTimeoutPair;
    protected final GFSM gfsm;
    protected final int invsCounter;
    protected final int totalInvs;

    public ParallelizerInput(InvariantTimeoutPair invTimeoutPair, GFSM gfsm,
            int invsCounter, int totalInvs) {
        this.invTimeoutPair = invTimeoutPair;
        this.gfsm = gfsm;
        this.invsCounter = invsCounter;
        this.totalInvs = totalInvs;
    }
}