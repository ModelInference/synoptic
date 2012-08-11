package dynoptic.model.fifosys.channel.channelstate;

import java.util.ArrayList;
import java.util.List;

import dynoptic.model.alphabet.EventType;
import dynoptic.model.fifosys.channel.channelid.ChannelId;

/**
 * Represents the state of a set of channels that are part of a FIFO system.
 * This state can be mutated, or modified. For example, a new message can be
 * enqueued and messages can be dequeued to/from channels.
 */
public class MutableMultiChState extends AbsMultiChState implements
        Cloneable {

    static public MutableMultiChState fromChannelIds(
            List<ChannelId> channelIds) {
        return new MutableMultiChState(
                AbsMultiChState.chStatesFromChIds(channelIds));
    }

    // //////////////////////////////////////////////////////////////////

    public MutableMultiChState(List<ChState> channelStates) {
        super(channelStates);
    }

    // //////////////////////////////////////////////////////////////////

    @Override
    public MutableMultiChState clone() {
        // Capture the current state of all the channels.
        List<ChState> clonedChannels = new ArrayList<ChState>(
                channelStates.size());
        for (ChState s : channelStates) {
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
        assert e.getRawEventStr().equals(expectedEvent.getRawEventStr());
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
