package dynoptic.model.fifosys.channel.channelstate;

import java.util.List;

import dynoptic.util.Util;

import synoptic.model.channelid.ChannelId;
import synoptic.model.event.IDistEventType;

/**
 * Implements basic functionality and maintains channel states for a number of
 * processes in a FIFO system.
 */
abstract public class AbsMultiChState<TxnEType extends IDistEventType> {

    // List of channel states ordered according to the scm ID of the
    // corresponding channel ID.
    final List<ChState<TxnEType>> channelStates;

    /** Private constructor used by clone() */
    protected AbsMultiChState(List<ChState<TxnEType>> chStates) {
        assert chStates != null;

        this.channelStates = chStates;
    }

    // //////////////////////////////////////////////////////////////////

    /**
     * Initializes a list of channel state instances with empty channels based
     * on channelIds.
     * 
     * @param channelIds
     */
    protected static <EType extends IDistEventType> List<ChState<EType>> chStatesFromChIds(
            List<ChannelId> channelIds) {
        assert channelIds != null;

        List<ChState<EType>> ret = Util.newList(channelIds.size());

        // Populate the channels map based on the channelIds.
        for (ChannelId chId : channelIds) {
            ret.add(new ChState<EType>(chId));
        }
        return ret;
    }

    // //////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        String ret = "ChStates[";
        for (ChState<TxnEType> s : channelStates) {
            // channelState string includes the channelId.
            ret = ret + s.toString() + ", ";
        }
        ret = ret.substring(0, ret.length() - 2) + "]";
        return ret;
    }

    @Override
    public int hashCode() {
        return channelStates.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (this == other) {
            return true;
        }
        if (!(other instanceof AbsMultiChState)) {
            return false;

        }
        @SuppressWarnings("unchecked")
        AbsMultiChState<TxnEType> mc = (AbsMultiChState<TxnEType>) other;
        return mc.channelStates.equals(channelStates);
    }

    // //////////////////////////////////////////////////////////////////

    public List<ChannelId> getChannelIds() {
        // TODO: cache the returned channel ids.
        List<ChannelId> ret = Util.newList(channelStates.size());
        for (ChState<TxnEType> s : channelStates) {
            ret.add(s.getChannelId());
        }
        return ret;
    }

    /** Whether or not all queues are empty. */
    public boolean isEmpty() {
        for (ChState<TxnEType> s : channelStates) {
            if (!s.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /** Whether or not all queues where pid is receiver are empty. */
    public boolean isEmptyForPid(int pid) {
        // NOTE: A process is not required to be associated with a queue on
        // which it is a receiver. In this case, this always return true.
        for (ChState<TxnEType> s : channelStates) {
            if (s.getChannelId().getDstPid() == pid && (!s.isEmpty())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a hash of the list of event types at the top of all of the queues
     * in this multi-channel state.
     */
    public int topOfQueuesHash() {
        int ret = 17;
        for (ChState<TxnEType> s : channelStates) {
            if (!s.isEmpty()) {
                ret = 31 * ret + s.peek().hashCode();
            } else {
                // Empty queues have to be captured by the hash as well.
                ret = 31 * ret;
            }
        }
        return ret;
    }

}
