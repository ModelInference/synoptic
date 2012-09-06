package dynoptic.model.fifosys.channel.channelid;

import dynoptic.invariants.BinaryInvariant;

/**
 * Represent a channel that is used to track events that are relevant to model
 * checking a specific invariant. Such channels are synthetic -- that is, they
 * are used only internally by Dynoptic and are not part of the actual system
 * being modeled.
 */
public class InvChannelId extends ChannelId {

    // The invariant that this channel corresponds to.
    private BinaryInvariant inv;

    public InvChannelId(BinaryInvariant inv, int scmId) {
        super(inv.getFirst().getEventPid(), inv.getFirst().getEventPid(),
                scmId, "ch-[" + inv.toString() + "]");
        this.inv = inv;
    }

    @Override
    public boolean equals(Object other) {
        if (!super.equals(other)) {
            return false;
        }

        if (!(other instanceof InvChannelId)) {
            return false;

        }

        InvChannelId iCid = (InvChannelId) other;
        if (!inv.equals(iCid.inv)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        return (31 * result) + inv.hashCode();
    }

}
