package dynoptic.model.alphabet;

import dynoptic.model.fifosys.channel.ChannelId;

/**
 * <p>
 * This is an immutable class.
 * </p>
 * An event type represents a possible transition/action that the CFSM, or one
 * of the FSMs that make up a CFSM can handle. Event types that are
 * communicating events (send, receive) are associated with a channel Id, while
 * local events are associated with a process id of the corresponding process.
 */
public class EventType {
    protected enum EventClass {
        LOCAL, SEND, RECV
    }

    final EventClass eventType;
    final String event;

    // LOCAL event types are associated with a pid:
    // SEND/RECV events have pid set to the sender/receiver
    final int pid;

    // SEND and RECV event types are associated with a channel id (i.e., a
    // sender and a receiver pid):
    final ChannelId channelId;

    // Used to cache the hashCode.
    final int hashCode;

    public static EventType LocalEvent(String event, int pid) {
        return new EventType(event, pid, EventClass.LOCAL, null);
    }

    public static EventType SendEvent(String event, ChannelId channel) {
        return new EventType(event, channel.getSrcPid(), EventClass.SEND,
                channel);
    }

    public static EventType RecvEvent(String event, ChannelId channel) {
        return new EventType(event, channel.getDstPid(), EventClass.RECV,
                channel);
    }

    // //////////////////////////////////////////////////////////////////

    protected EventType(String event, int pid, EventClass eventType,
            ChannelId channel) {
        if (eventType == EventClass.LOCAL) {
            assert channel == null;
        } else if (eventType == EventClass.SEND || eventType == EventClass.RECV) {
            assert channel != null;
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
        return eventType == EventClass.SEND || eventType == EventClass.RECV;
    }

    public boolean isRecvEvent() {
        return eventType == EventClass.RECV;
    }

    public boolean isSendEvent() {
        return eventType == EventClass.SEND;
    }

    /**
     * Returns an scm representation of this EventType, based on channelId to
     * int map.
     */
    public String toScmString() {
        if (channelId != null) {
            return toString(Integer.toString(channelId.getScmId()));
        }
        return toString("");
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
        if (!(other instanceof EventType)) {
            return false;

        }
        EventType otherE = (EventType) other;
        if (!otherE.getEventStr().equals(event)) {
            return false;
        }
        if (otherE.getEventPid() != pid) {
            return false;
        }

        if (otherE.isCommEvent() != this.isCommEvent()) {
            return false;
        }

        if (this.isCommEvent()) {
            if (otherE.isRecvEvent() != isRecvEvent()) {
                return false;
            }
            if (otherE.isSendEvent() != isSendEvent()) {
                return false;
            }
            if (!otherE.getChannelId().equals(this.getChannelId())) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String toString() {
        if (channelId != null) {
            return toString(channelId.toString());
        }
        return toString("");
    }

    // //////////////////////////////////////////////////////////////////

    private int initHashCode() {
        int result = 17;
        result = 31 * result + eventType.hashCode();
        result = 31 * result + event.hashCode();
        result = 31 * result + pid;
        if (channelId != null) {
            result = 31 * result + channelId.hashCode();
        }
        return result;
    }

    private String toString(String cidString) {
        if (isSendEvent()) {
            return cidString + " ! " + event;
        } else if (isRecvEvent()) {
            return cidString + " ? " + event;
        }
        return event + "_" + Integer.toString(pid);
    }
}
