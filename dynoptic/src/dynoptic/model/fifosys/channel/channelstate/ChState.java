package dynoptic.model.fifosys.channel.channelstate;

import java.util.ArrayList;

import dynoptic.model.fifosys.gfsm.observed.ObsDistEventType;

import synoptic.model.channelid.ChannelId;
import synoptic.model.event.IDistEventType;

/**
 * The ChannelState maintains the queue state for a channel, identified by a
 * specific channel id.
 */
public class ChState<TxnEType extends IDistEventType> implements Cloneable {

    public static boolean equalsObsDistETypeIgnoringTraceIds(
            ChState<ObsDistEventType> chS1, ChState<ObsDistEventType> chS2) {
        if (chS1 == null || chS2 == null) {
            return false;
        }

        if (!chS1.chId.equals(chS2.chId)) {
            return false;
        }

        if (chS1.queue.size() != chS2.queue.size()) {
            return false;
        }

        for (int i = 0; i < chS1.queue.size(); i++) {
            if (chS1.queue.get(i).equalsIgnoringTraceIds(chS2.queue.get(i))) {
                return false;
            }
        }
        return true;
    }

    // //////////////////////////////////////////////////////////////////

    private final ChannelId chId;
    private final ArrayList<TxnEType> queue;

    public ChState(ChannelId chId) {
        this(chId, new ArrayList<TxnEType>());
    }

    private ChState(ChannelId chId, ArrayList<TxnEType> queue) {
        assert chId != null;
        assert queue != null;

        this.chId = chId;
        this.queue = queue;
    }

    // //////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        String ret = chId.toString() + ": [";
        for (TxnEType e : queue) {
            ret = ret + e.toString() + ", ";
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
        ChState<?> s = (ChState<?>) other;
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
    public ChState<TxnEType> clone() {
        // Since ChannelId is immutable and Event is immutable all we need to do
        // is make sure to clone the ArrayList that maintains events to produce
        // a new independent deep-copy of ChannelState.
        return new ChState<TxnEType>(chId, (ArrayList<TxnEType>) queue.clone());
    }

    // //////////////////////////////////////////////////////////////////

    /** Adds an event to the back of the queue. */
    public void enqueue(TxnEType e) {
        assert e.isSendEvent();
        assert e.getChannelId().equals(chId);

        queue.add(e);
    }

    /** Removes and returns the event at the top of the queue. */
    public TxnEType dequeue() {
        return queue.remove(0);
    }

    /** Returns the event at the top of the queue, without removing it. */
    public TxnEType peek() {
        return queue.get(0);
    }

    /** Returns the number of events in the queue. */
    public int size() {
        return queue.size();
    }

    public ChannelId getChannelId() {
        return chId;
    }

    /** Merges trace ids of obs event types in chS into this. */
    public void mergeInTraceIds(ChState<ObsDistEventType> chS) {
        assert chS != null;
        assert this.chId.equals(chS.chId);
        assert this.queue.size() == chS.queue.size();

        for (int i = 0; i < this.queue.size(); i++) {
            ((ObsDistEventType) this.queue.get(i))
                    .addTraceIds(chS.queue.get(i));
        }
    }
}
