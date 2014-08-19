package csight.mc.parallelizer;

import csight.model.fifosys.cfsm.CFSM;

/**
 * Inputs to the Parallelizer that are sent with ParallelizerCommands through
 * the ParallelizerTask channel. An input contains the invariant to check, the
 * model to check the invariant against, and the timeout in seconds.
 */
public class ParallelizerInput {
    protected final InvariantTimeoutPair invTimeoutPair;
    protected final CFSM cfsm;

    public ParallelizerInput(InvariantTimeoutPair invTimeoutPair, CFSM cfsm) {
        this.invTimeoutPair = invTimeoutPair;
        this.cfsm = cfsm;
    }
}