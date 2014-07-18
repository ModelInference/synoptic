package csight.mc.parallelizer;

import csight.invariants.BinaryInvariant;

/**
 * Inputs to the Parallelizer that are sent with ParallelizerCommands through
 * the ParallelizerTask channel.
 */
public class ParallelizerInput {
    protected final BinaryInvariant inv;
    protected final int timeout;

    public ParallelizerInput(BinaryInvariant inv, int timeout) {
        this.inv = inv;
        this.timeout = timeout;
    }
}