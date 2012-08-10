package dynoptic.model.fifosys.channel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import dynoptic.model.fifosys.gfsm.trace.ObservedEvent;

/**
 * Represents the state of a set of channels that are part of a FIFO system.
 * This state _cannot_ be mutated, or modified. An instance of this state is
 * finalized at construction. Moreover, the only way to create an instance is
 * through static methods that perform instance caching and will return a
 * previosly created instance, if one exists.
 */
public class ImmutableMultiChannelState extends AbsMultiChannelState {

    private static final Map<List<ChannelId>, ImmutableMultiChannelState> emptyQueuesMap;
    private static final Map<List<ChannelState>, ImmutableMultiChannelState> queuesMap;

    static {
        emptyQueuesMap = new LinkedHashMap<List<ChannelId>, ImmutableMultiChannelState>();
        queuesMap = new LinkedHashMap<List<ChannelState>, ImmutableMultiChannelState>();
    }

    /**
     * Returns a cached ImmutableMultiChannelState instance, if one was
     * previously created with the given channel states. Otherwise, returns a
     * new instance and caches it.
     */
    public static ImmutableMultiChannelState getFifoSysState(
            List<ChannelState> chStates) {
        ImmutableMultiChannelState ret;
        if (queuesMap.containsKey(chStates)) {
            ret = queuesMap.get(chStates);
        } else {
            ret = new ImmutableMultiChannelState(chStates);
            queuesMap.put(chStates, ret);
        }
        return ret;
    }

    public static ImmutableMultiChannelState fromChannelIds(
            List<ChannelId> channelIds) {
        ImmutableMultiChannelState ret;
        if (emptyQueuesMap.containsKey(channelIds)) {
            ret = emptyQueuesMap.get(channelIds);
        } else {
            ret = new ImmutableMultiChannelState(chStatesFromChIds(channelIds));
            emptyQueuesMap.put(channelIds, ret);
        }
        return ret;
    }

    // //////////////////////////////////////////////////////////////////

    private ImmutableMultiChannelState(List<ChannelState> chStates) {
        super(chStates);
    }

    // //////////////////////////////////////////////////////////////////

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (!super.equals(other)) {
            return false;
        }

        if (!(other instanceof ImmutableMultiChannelState)) {
            return false;

        }
        return true;
    }

    // //////////////////////////////////////////////////////////////////

    public ImmutableMultiChannelState getNextChState(ObservedEvent e) {
        if (e.isLocalEvent()) {
            // These events do not change the channel state.
            return this;
        }

        // Keep this.channelStates, except for the one that will be modified (at
        // the index indicated by the event) -- this ChannelState is cloned.
        List<ChannelState> states = new ArrayList<ChannelState>(channelStates);
        ChannelState newState = states.get(e.getEventPid()).clone();
        states.set(e.getEventPid(), newState);

        // 'Execute' the event by taking the appropriate action on the queue.
        if (e.isSendEvent()) {
            newState.enqueue(e);
        } else if (e.isRecvEvent()) {
            newState.dequeue();
        } else {
            assert false : "A non-local event is not a send or a receive event, either.";
        }

        return ImmutableMultiChannelState.getFifoSysState(states);
    }

}
