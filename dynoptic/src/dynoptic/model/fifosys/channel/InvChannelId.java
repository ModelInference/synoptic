package dynoptic.model.fifosys.channel;

import dynoptic.invariants.BinaryInvariant;

/**
 * Represent a channel that is used to track events that are relevant to model
 * checking a specific invariant.
 */
public class InvChannelId extends ChannelId {

    BinaryInvariant inv;

    public InvChannelId(BinaryInvariant inv, int scmId) {
        super(inv.getFirst().getEventPid(), inv.getFirst().getEventPid(),
                scmId, "ch-[" + inv.toString() + "]");
        this.inv = inv;
    }

}
