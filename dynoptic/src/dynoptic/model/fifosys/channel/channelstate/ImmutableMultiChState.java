package dynoptic.model.fifosys.channel.channelstate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import synoptic.model.channelid.ChannelId;
import synoptic.model.event.DistEventType;

/**
 * Represents the state of a set of channels that are part of a FIFO system.
 * This state _cannot_ be mutated, or modified. An instance of this state is
 * finalized at construction. Moreover, the only way to create an instance is
 * through static methods that perform instance caching and return a previously
 * created instance, if one already exists.
 */
public class ImmutableMultiChState extends AbsMultiChState<DistEventType> {

    // Global cache of channel states already created.
    private static final Map<List<ChState<DistEventType>>, ImmutableMultiChState> chCache;

    static {
        chCache = new LinkedHashMap<List<ChState<DistEventType>>, ImmutableMultiChState>();
    }

    /**
     * Call to create a new instance with empty queues. Returns a cached
     * ImmutableMultiChannelState instance, if one was previously created with
     * the given channel states. Otherwise, returns a new instance and caches
     * it.
     */
    public static ImmutableMultiChState fromChannelIds(
            List<ChannelId> channelIds) {
        List<ChState<DistEventType>> chStates = chStatesFromChIds(channelIds);
        return fromChannelStates(chStates);
    }

    /**
     * Returns a cached ImmutableMultiChannelState instance, if one was
     * previously created with the given channel states. Otherwise, returns a
     * new instance and caches it.
     */
    public static ImmutableMultiChState fromChannelStates(
            List<ChState<DistEventType>> chStates) {
        ImmutableMultiChState ret;

        if (chCache.containsKey(chStates)) {
            ret = chCache.get(chStates);
        } else {
            ret = new ImmutableMultiChState(chStates);
            chCache.put(chStates, ret);
        }
        return ret;
    }

    // //////////////////////////////////////////////////////////////////

    private ImmutableMultiChState(List<ChState<DistEventType>> chStates) {
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

        if (!(other instanceof ImmutableMultiChState)) {
            return false;
        }
        return true;
    }

    // //////////////////////////////////////////////////////////////////

    /**
     * Used to establish equality of immutable multi ch states that contain
     * ObsDistEventType instances that might have come from different traces in
     * the log (standard equals() won't work for these).
     */
    /*
     * public boolean equalsIgnoringTraceIds(Object other) { if (other == null)
     * { return false; } if (this == other) { return true; } if (!(other
     * instanceof ImmutableMultiChState)) { return false; }
     * ImmutableMultiChState mc = (ImmutableMultiChState) other; if
     * (this.channelStates.size() != mc.channelStates.size()) { return false; }
     * for (int i = 0; i < this.channelStates.size(); i++) {
     * ChState<ObsDistEventType> chS1 = this.channelStates.get(i);
     * ChState<ObsDistEventType> chS2 = mc.channelStates.get(i); if
     * (!ChState.equalsObsDistETypeIgnoringTraceIds(chS1, chS2)) { return false;
     * } } return true; }
     */

    /**
     * Merges trace ids of obs event types in each channel state in the
     * multi-state chStates into this multi-state.
     */
    /*
     * public void mergeInTraceIds(ImmutableMultiChState chStates) { assert
     * this.channelStates.size() == chStates.channelStates.size();
     * 
     * for (int i = 0; i < this.channelStates.size(); i++) {
     * ChState<DistEventType> chS1 = this.channelStates.get(i);
     * ChState<DistEventType> chS2 = chStates.channelStates.get(i);
     * chS1.mergeInTraceIds(chS2); } }
     */

    public ImmutableMultiChState getNextChState(DistEventType e) {
        if (e.isLocalEvent()) {
            // These events do not change the channel state.
            return this;
        }

        // Keep this.channelStates, except for the one that will be modified (at
        // the index indicated by the event) -- this ChannelState is cloned.
        List<ChState<DistEventType>> states = new ArrayList<ChState<DistEventType>>(
                channelStates);
        int scmId = e.getChannelId().getScmId();
        ChState<DistEventType> newState = states.get(scmId).clone();
        states.set(scmId, newState);

        // 'Execute' the event by taking the appropriate action on the queue.
        if (e.isSendEvent()) {
            newState.enqueue(e);
        } else if (e.isRecvEvent()) {
            DistEventType eRecv = newState.dequeue();
            assert e.getEType().equals(eRecv.getEType());
        } else {
            assert false : "A non-local event is not a send or a receive event.";
        }

        return ImmutableMultiChState.fromChannelStates(states);
    }

}
