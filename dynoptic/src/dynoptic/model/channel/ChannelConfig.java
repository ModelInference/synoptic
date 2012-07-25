package dynoptic.model.channel;

import java.util.ArrayList;

import dynoptic.model.alphabet.Event;

/**
 * The ChannelConfig maintains the queue state for a channel along with the
 * channel's id.
 */
public class ChannelConfig {
    final ChannelId chId;
    final ArrayList<Event> queue;

    public ChannelConfig(ChannelId chId) {
        this(chId, new ArrayList<Event>());
    }

    private ChannelConfig(ChannelId chId, ArrayList<Event> queue) {
        this.chId = chId;
        this.queue = queue;
    }

    public void enqueue(Event e) {
        queue.add(e);
    }

    public Event dequeue() {
        return queue.remove(0);
    }

    public Event peek() {
        return queue.get(0);
    }

    @SuppressWarnings("unchecked")
    public ChannelConfig clone() {
        // Since ChannelId is immutable and Event is immutable all we need to do
        // is make sure to clone the ArrayList that maintains events to produce
        // a new independent deep-copy of ChannelConfig.
        return new ChannelConfig(chId, (ArrayList<Event>) queue.clone());
    }

    public int size() {
        return queue.size();
    }
}
