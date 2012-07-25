package dynoptic.model.alphabet;

import dynoptic.model.channel.ChannelId;

/**
 * <p>
 * This is an immutable class.
 * </p>
 * An event represents a possible transition/action that the CFSM, or one of the
 * FSMs that make up a CFSM can handle.
 */
public final class Event {
    enum EventType {
        LOCAL, SEND, RECV
    }

    final EventType eventType;
    final String event;

    // LOCAL event types are associated with a pid:
    final int pid;

    // SEND and RECV event types are associated with a channel id (i.e., a
    // sender and a receiver pid):
    final ChannelId channelId;

    // Used to cache the hashCode.
    final int hashCode;

    public static Event LocalEvent(String event, int pid) {
        return new Event(event, pid, EventType.LOCAL, null);
    }

    public static Event SendEvent(String event, ChannelId channel) {
        return new Event(event, -1, EventType.SEND, channel);
    }

    public static Event RecvEvent(String event, ChannelId channel) {
        return new Event(event, -1, EventType.RECV, channel);
    }

    // //////////////////////////////////////////////////////////////////

    private Event(String event, int pid, EventType eventType, ChannelId channel) {
        if (eventType == EventType.LOCAL) {
            assert channel == null;
        } else if (eventType == EventType.SEND || eventType == EventType.RECV) {
            assert channel != null;
            pid = -1;
        } else {
            throw new IllegalArgumentException("Invalid EventType.");
        }

        this.eventType = eventType;
        this.event = event;
        this.pid = pid;
        this.channelId = channel;
        this.hashCode = initHashCode();
    }

    public String getEventStr() {
        return event;
    }

    public int getEventPid() {
        return pid;
    }

    public ChannelId getChannelId() {
        return channelId;
    }

    public boolean isCommEvent() {
        return eventType == EventType.SEND || eventType == EventType.RECV;
    }

    public boolean isRecvEvent() {
        return eventType == EventType.RECV;
    }

    public boolean isSendEvent() {
        return eventType == EventType.SEND;
    }

    public String toScmString() {
        return toString();
    }

    // //////////////////////////////////////////////////////////////////

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (this == other) {
            return true;
        }
        if (!(other instanceof Event)) {
            return false;

        }
        Event otherE = (Event) other;
        if (!otherE.getEventStr().equals(event)) {
            return false;
        }
        if (otherE.getEventPid() != pid) {
            return false;
        }
        if (otherE.isRecvEvent() != isRecvEvent()) {
            return false;
        }
        if (otherE.isSendEvent() != isSendEvent()) {
            return false;
        }
        if (otherE.getChannelId() != this.getChannelId()) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        if (isSendEvent()) {
            assert channelId != null;
            return channelId.toString() + " ! " + event;
        } else if (isRecvEvent()) {
            assert channelId != null;
            return channelId.toString() + " ? " + event;
        }
        return event + "_" + Integer.toString(pid);
    }

    // //////////////////////////////////////////////////////////////////

    private int initHashCode() {
        int result = 17;
        result = 31 * result + eventType.hashCode();
        result = 31 * result + event.hashCode();
        result = 31 * result + pid;
        result = 31 * result + channelId.hashCode();
        return result;
    }
}
