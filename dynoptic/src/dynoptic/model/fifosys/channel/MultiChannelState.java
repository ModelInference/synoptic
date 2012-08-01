package dynoptic.model.fifosys.channel;

import java.util.ArrayList;
import java.util.List;

import dynoptic.model.alphabet.EventType;

/**
 * Represents the state of a set of channels that are part of a FIFO system.
 */
public class MultiChannelState implements Cloneable {
    // List of channel states ordered according to the scm ID of the
    // corresponding channel ID.
    final List<ChannelState> channelStates;

    /**
     * Initialize and returns a MultiChannelState instances with empty channels
     * baed on channelIds.
     * 
     * @param channelIds
     */
    static public MultiChannelState fromChannelIds(List<ChannelId> channelIds) {
        assert channelIds != null;

        List<ChannelState> channelStates = new ArrayList<ChannelState>(
                channelIds.size());

        // Populate the channels map based on the channelIds.
        for (ChannelId chId : channelIds) {
            channelStates.add(new ChannelState(chId));
        }
        return new MultiChannelState(channelStates);
    }

    /** Private constructor used by clone() */
    private MultiChannelState(List<ChannelState> chStates) {
        assert chStates != null;

        this.channelStates = chStates;
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
        if (!(other instanceof MultiChannelState)) {
            return false;

        }
        MultiChannelState mc = (MultiChannelState) other;
        return mc.channelStates.equals(channelStates);
    }

    @Override
    public MultiChannelState clone() {
        // Capture the current state of all the channels.
        List<ChannelState> clonedChannels = new ArrayList<ChannelState>(
                channelStates.size());
        for (ChannelState s : channelStates) {
            clonedChannels.add(s.clone());
        }
        return new MultiChannelState(clonedChannels);
    }

    // //////////////////////////////////////////////////////////////////

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

    public void enqueue(EventType event) {
        assert event.isCommEvent();
        assert event.getChannelId().getScmId() < channelStates.size();

        channelStates.get(event.getChannelId().getScmId()).enqueue(event);
    }

    public EventType dequeue(ChannelId chId) {
        assert chId.getScmId() < channelStates.size();

        return channelStates.get(chId.getScmId()).dequeue();
    }

    public EventType peek(ChannelId chId) {
        assert chId.getScmId() < channelStates.size();

        return channelStates.get(chId.getScmId()).peek();
    }

    public List<ChannelId> getChannelIds() {
        List<ChannelId> ret = new ArrayList<ChannelId>(channelStates.size());
        for (ChannelState s : channelStates) {
            ret.add(s.getChannelId());
        }
        return ret;
    }

}
