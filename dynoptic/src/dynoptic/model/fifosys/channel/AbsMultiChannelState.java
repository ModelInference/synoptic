package dynoptic.model.fifosys.channel;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements some basic functionality and maintains channel states for a number
 * of processes in a FIFO system.
 */
abstract public class AbsMultiChannelState {

    // List of channel states ordered according to the scm ID of the
    // corresponding channel ID.
    final List<ChannelState> channelStates;

    /** Private constructor used by clone() */
    protected AbsMultiChannelState(List<ChannelState> chStates) {
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
    protected static List<ChannelState> chStatesFromChIds(
            List<ChannelId> channelIds) {
        assert channelIds != null;

        List<ChannelState> ret = new ArrayList<ChannelState>(channelIds.size());

        // Populate the channels map based on the channelIds.
        for (ChannelId chId : channelIds) {
            ret.add(new ChannelState(chId));
        }
        return ret;
    }

    // //////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        String ret = "{";
        for (ChannelState s : channelStates) {
            // channelState string includes the channelId.
            ret = ret + s.toString() + ", ";
        }
        ret = ret + "}";
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
        if (!(other instanceof AbsMultiChannelState)) {
            return false;

        }
        AbsMultiChannelState mc = (AbsMultiChannelState) other;
        return mc.channelStates.equals(channelStates);
    }

    // //////////////////////////////////////////////////////////////////

    public List<ChannelId> getChannelIds() {
        // TODO: cache the returned channel ids.
        List<ChannelId> ret = new ArrayList<ChannelId>(channelStates.size());
        for (ChannelState s : channelStates) {
            ret.add(s.getChannelId());
        }
        return ret;
    }

    /** Whether or not all queues are empty. */
    public boolean isEmpty() {
        for (ChannelState s : channelStates) {
            if (s.size() != 0) {
                return false;
            }
        }
        return true;
    }

    /** Whether or not all queues where pid is receiver are empty. */
    public boolean isEmptyForPid(int pid) {
        // NOTE: A process is not required to be associated with a queue on
        // which it is a receiver. In this case, this always return true.
        for (ChannelState s : channelStates) {
            if (s.getChannelId().getDstPid() == pid && s.size() != 0) {
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
        for (ChannelState s : channelStates) {
            if (s.size() != 0) {
                ret = 31 * ret + s.peek().hashCode();
            } else {
                ret = 31 * ret;
            }
        }
        return ret;
    }

}
