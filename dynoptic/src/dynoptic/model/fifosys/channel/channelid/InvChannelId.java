package dynoptic.model.fifosys.channel.channelid;

import dynoptic.invariants.BinaryInvariant;

import synoptic.model.channelid.ChannelId;

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
        // NOTE: since the McScM model checker allows all processes to access
        // all channels, it does not matter which pids we use here, so we use
        // the arbitrary value of 0 for both srcPid and dstPid.
        super(0, 0, scmId, "ch-[" + inv.toString() + "]");
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
