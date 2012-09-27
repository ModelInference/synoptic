package dynoptic.model.fifosys.channel.channelstate;

import java.util.ArrayList;

import synoptic.model.channelid.ChannelId;
import synoptic.model.event.DistEventType;

/**
 * The ChannelState maintains the queue state for a channel, identified by a
 * specific channel id.
 */
public class ChState implements Cloneable {
    final ChannelId chId;
    final ArrayList<DistEventType> queue;

    public ChState(ChannelId chId) {
        this(chId, new ArrayList<DistEventType>());
    }

    private ChState(ChannelId chId, ArrayList<DistEventType> queue) {
        assert chId != null;
        assert queue != null;

        this.chId = chId;
        this.queue = queue;
    }

    // //////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        String ret = chId.toString() + ": [";
        for (DistEventType e : queue) {
            ret = ret + e.getEType() + ", ";
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
        if (!(other instanceof ChState)) {
            return false;

        }
        ChState s = (ChState) other;
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
    public ChState clone() {
        // Since ChannelId is immutable and Event is immutable all we need to do
        // is make sure to clone the ArrayList that maintains events to produce
        // a new independent deep-copy of ChannelState.
        return new ChState(chId, (ArrayList<DistEventType>) queue.clone());
    }

    // //////////////////////////////////////////////////////////////////

    /** Adds an event to the back of the queue. */
    public void enqueue(DistEventType e) {
        assert e.isSendEvent();
        assert e.getChannelId().equals(chId);

        queue.add(e);
    }

    /** Removes and returns the event at the top of the queue. */
    public DistEventType dequeue() {
        return queue.remove(0);
    }

    /** Returns the event at the top of the queue, without removing it. */
    public DistEventType peek() {
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
