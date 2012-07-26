package dynoptic.model.fifosys.channel;

import java.util.ArrayList;

import dynoptic.model.alphabet.EventType;

/**
 * The ChannelState maintains the queue state for a channel, identified with a
 * specific channel id.
 */
public class ChannelState {
    final ChannelId chId;
    final ArrayList<EventType> queue;

    public ChannelState(ChannelId chId) {
        this(chId, new ArrayList<EventType>());
    }

    private ChannelState(ChannelId chId, ArrayList<EventType> queue) {
        this.chId = chId;
        this.queue = queue;
    }

    public void enqueue(EventType e) {
        queue.add(e);
    }

    public EventType dequeue() {
        return queue.remove(0);
    }

    public EventType peek() {
        return queue.get(0);
    }

    @SuppressWarnings("unchecked")
    public ChannelState clone() {
        // Since ChannelId is immutable and Event is immutable all we need to do
        // is make sure to clone the ArrayList that maintains events to produce
        // a new independent deep-copy of ChannelState.
        return new ChannelState(chId, (ArrayList<EventType>) queue.clone());
    }

    public int size() {
        return queue.size();
    }
}
