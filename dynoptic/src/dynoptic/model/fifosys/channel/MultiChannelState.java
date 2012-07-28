package dynoptic.model.fifosys.channel;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import dynoptic.model.alphabet.EventType;

/**
 * Represents the state of a set of channels that are part of a FIFO system.
 */
public class MultiChannelState {
    final Map<ChannelId, ChannelState> channelStates;

    /**
     * Initialize the channelStates to empty based on the channelIds.
     * 
     * @param channelIds
     */
    public MultiChannelState(Set<ChannelId> channelIds) {
        assert channelIds != null;

        // Populate the channels map based on the channelIds.
        this.channelStates = new LinkedHashMap<ChannelId, ChannelState>();
        for (ChannelId chId : channelIds) {
            ChannelState chState = new ChannelState(chId);
            channelStates.put(chId, chState);
        }
    }

    /** Private constructor used by clone() */
    private MultiChannelState(Map<ChannelId, ChannelState> channelStates) {
        this.channelStates = channelStates;
    }

    // //////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        String ret = "{";
        for (ChannelId chId : channelStates.keySet()) {
            // channelState string includes the channelId.
            ret = ret + channelStates.get(chId).toString() + ", ";
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

    // //////////////////////////////////////////////////////////////////

    /** Whether or not all queues are empty. */
    public boolean isEmpty() {
        for (ChannelState chState : channelStates.values()) {
            if (chState.size() != 0) {
                return false;
            }
        }
        return true;
    }

    /** Whether or not all queues where pid is receiver are empty. */
    public boolean isEmptyForPid(int pid) {
        boolean foundPid = false;

        for (ChannelId chId : channelStates.keySet()) {
            if (chId.getDstPid() == pid) {
                if (channelStates.get(chId).size() != 0) {
                    return false;
                }
                foundPid = true;
            }
        }
        assert foundPid;

        return true;
    }

    public MultiChannelState clone() {
        // Capture the current state of all the channels.
        Map<ChannelId, ChannelState> clonedChannels = new LinkedHashMap<ChannelId, ChannelState>();
        for (ChannelId chId : channelStates.keySet()) {
            clonedChannels.put(chId, channelStates.get(chId).clone());
        }
        return new MultiChannelState(clonedChannels);
    }

    public void enqueue(EventType event) {
        assert channelStates.containsKey(event.getChannelId());

        channelStates.get(event.getChannelId()).enqueue(event);
    }

    public EventType dequeue(ChannelId chId) {
        assert channelStates.containsKey(chId);

        return channelStates.get(chId).dequeue();
    }

    public EventType peek(ChannelId chId) {
        assert channelStates.containsKey(chId);

        return channelStates.get(chId).peek();
    }

}
