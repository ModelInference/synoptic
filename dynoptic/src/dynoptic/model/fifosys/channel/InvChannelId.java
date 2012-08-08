package dynoptic.model.fifosys.channel;

import dynoptic.invariants.BinaryInvariant;

/**
 * Represent a channel that is used to track events that are relevant to model
 * checking a specific invariant.
 */
public class InvChannelId extends ChannelId {

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
        return result + inv.hashCode();
    }

}
