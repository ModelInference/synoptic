package dynoptic.model.fifosys.channel;

import java.util.ArrayList;
import java.util.List;

import dynoptic.model.alphabet.EventType;

/**
 * Represents the state of a set of channels that are part of a FIFO system.
 * This state can be mutated, or modified. For example, a new message can be
 * enqueued and messages can be dequeued to/from channels.
 */
public class MutableMultiChannelState extends AbsMultiChannelState implements
        Cloneable {

    static public MutableMultiChannelState fromChannelIds(
            List<ChannelId> channelIds) {
        return new MutableMultiChannelState(
                AbsMultiChannelState.chStatesFromChIds(channelIds));
    }

    // //////////////////////////////////////////////////////////////////

    public MutableMultiChannelState(List<ChannelState> channelStates) {
        super(channelStates);
    }

    // //////////////////////////////////////////////////////////////////

    @Override
    public MutableMultiChannelState clone() {
        // Capture the current state of all the channels.
        List<ChannelState> clonedChannels = new ArrayList<ChannelState>(
                channelStates.size());
        for (ChannelState s : channelStates) {
            clonedChannels.add(s.clone());
        }
        return new MutableMultiChannelState(clonedChannels);
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

        if (!(other instanceof MutableMultiChannelState)) {
            return false;

        }
        return true;
    }

    // //////////////////////////////////////////////////////////////////

    public void enqueue(EventType event) {
        assert event.isSendEvent();
        assert event.getChannelId().getScmId() < channelStates.size();

        channelStates.get(event.getChannelId().getScmId()).enqueue(event);
    }

    public void dequeue(EventType expectedEvent) {
        assert expectedEvent.isRecvEvent();
        assert expectedEvent.getChannelId().getScmId() < channelStates.size();

        EventType e = channelStates
                .get(expectedEvent.getChannelId().getScmId()).dequeue();
        assert e.equals(expectedEvent);
    }

    public EventType dequeue(ChannelId chId) {
        assert chId.getScmId() < channelStates.size();

        return channelStates.get(chId.getScmId()).dequeue();
    }

    public EventType peek(ChannelId chId) {
        assert chId.getScmId() < channelStates.size();

        return channelStates.get(chId.getScmId()).peek();
    }

}
