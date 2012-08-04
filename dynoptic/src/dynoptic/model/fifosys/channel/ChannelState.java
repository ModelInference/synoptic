package dynoptic.model.fifosys.channel;

import java.util.ArrayList;

import dynoptic.model.alphabet.EventType;

/**
 * The ChannelState maintains the queue state for a channel, identified with a
 * specific channel id.
 */
public class ChannelState implements Cloneable {
    final ChannelId chId;
    final ArrayList<EventType> queue;

    public ChannelState(ChannelId chId) {
        this(chId, new ArrayList<EventType>());
    }

    private ChannelState(ChannelId chId, ArrayList<EventType> queue) {
        assert chId != null;
        assert queue != null;

        this.chId = chId;
        this.queue = queue;
    }

    // //////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        String ret = chId.toString() + ": [";
        for (EventType e : queue) {
            ret = ret + e.getRawEventStr() + ", ";
        }
        ret = ret + "]";
        return ret;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + chId.hashCode();
        result = 31 * result + queue.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (this == other) {
            return true;
        }
        if (!(other instanceof ChannelState)) {
            return false;

        }
        ChannelState s = (ChannelState) other;
        if (!s.chId.equals(chId)) {
            return false;
        }
        return s.queue.equals(queue);
    }

    /**
     * Returns a copy of this ChannelState.
     */
    @Override
    @SuppressWarnings("unchecked")
    public ChannelState clone() {
        // Since ChannelId is immutable and Event is immutable all we need to do
        // is make sure to clone the ArrayList that maintains events to produce
        // a new independent deep-copy of ChannelState.
        return new ChannelState(chId, (ArrayList<EventType>) queue.clone());
    }

    // //////////////////////////////////////////////////////////////////

    /** Adds an event to the back of the queue. */
    public void enqueue(EventType e) {
        assert e.isSendEvent();
        assert e.getChannelId().equals(chId);

        queue.add(e);
    }

    /** Removes and returns the event at the top of the queue. */
    public EventType dequeue() {
        return queue.remove(0);
    }

    /** Returns the event at the top of the queue, without removing it. */
    public EventType peek() {
        return queue.get(0);
    }

    /** Returns the number of events in the queue. */
    public int size() {
        return queue.size();
    }

    public ChannelId getChannelId() {
        return chId;
    }
}
