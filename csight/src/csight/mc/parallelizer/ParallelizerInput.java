package csight.mc.parallelizer;

import csight.invariants.BinaryInvariant;
import csight.model.fifosys.gfsm.GFSM;

/**
 * Inputs to the Parallelizer that are sent with ParallelizerCommands through
 * the ParallelizerTask channel.
 */
public class ParallelizerInput {
    protected final BinaryInvariant inv;
    protected final GFSM gfsm;
    protected final int timeout;
    protected final int invsCounter;
    protected final int totalInvs;

    public ParallelizerInput(BinaryInvariant inv, GFSM gfsm, int timeout,
            int invsCounter, int totalInvs) {
        this.inv = inv;
        this.gfsm = gfsm;
        this.timeout = timeout;
        this.invsCounter = invsCounter;
        this.totalInvs = totalInvs;
    }
}