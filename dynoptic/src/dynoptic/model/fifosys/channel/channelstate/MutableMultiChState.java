package dynoptic.model.fifosys.channel.channelstate;

import java.util.List;

import dynoptic.util.Util;

import synoptic.model.channelid.ChannelId;
import synoptic.model.event.DistEventType;

/**
 * Represents the state of a set of channels that are part of a FIFO system.
 * This state can be mutated, or modified. For example, a new message can be
 * enqueued and messages can be dequeued to/from channels.
 */
public class MutableMultiChState extends AbsMultiChState<DistEventType>
        implements Cloneable {

    static public MutableMultiChState fromChannelIds(List<ChannelId> channelIds) {
        List<ChState<DistEventType>> chStates = AbsMultiChState
                .chStatesFromChIds(channelIds);
        return new MutableMultiChState(chStates);
    }

    // //////////////////////////////////////////////////////////////////

    public MutableMultiChState(List<ChState<DistEventType>> channelStates) {
        super(channelStates);
    }

    // //////////////////////////////////////////////////////////////////

    @Override
    public MutableMultiChState clone() {
        // Capture the current state of all the channels.
        List<ChState<DistEventType>> clonedChannels = Util
                .newList(channelStates.size());
        for (ChState<DistEventType> s : channelStates) {
            clonedChannels.add(s.clone());
        }
        return new MutableMultiChState(clonedChannels);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (!super.equals(other)) {
            return false;
        }

        if (!(other instanceof MutableMultiChState)) {
            return false;

        }
        return true;
    }

    // //////////////////////////////////////////////////////////////////

    public void enqueue(DistEventType event) {
        assert event.isSendEvent();
        assert event.getChannelId().getScmId() < channelStates.size();

        channelStates.get(event.getChannelId().getScmId()).enqueue(event);
    }

    public void dequeue(DistEventType expectedEvent) {
        assert expectedEvent.isRecvEvent();
        assert expectedEvent.getChannelId().getScmId() < channelStates.size();

        DistEventType e = channelStates.get(
                expectedEvent.getChannelId().getScmId()).dequeue();
        assert e.getEType().equals(expectedEvent.getEType());
    }

    public DistEventType dequeue(ChannelId chId) {
        assert chId.getScmId() < channelStates.size();

        return channelStates.get(chId.getScmId()).dequeue();
    }

    public DistEventType peek(ChannelId chId) {
        assert chId.getScmId() < channelStates.size();

        return channelStates.get(chId.getScmId()).peek();
    }

}
