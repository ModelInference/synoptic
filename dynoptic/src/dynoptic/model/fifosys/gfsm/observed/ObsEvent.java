package dynoptic.model.fifosys.gfsm.observed;

import dynoptic.model.alphabet.EventType;
import dynoptic.model.fifosys.channel.channelid.ChannelId;

/**
 * Represents an event that was observed or mined from a log of an execution of
 * a FIFO system. An observed event is an _instance_ of an EventType. It is
 * associated with a particular log, location in the log, etc.
 */
public class ObsEvent extends EventType {

    // TODO: include contextual information, such as the log filename, the line
    // number at which this event was observed/parsed, etc.

    public static ObsEvent LocalEvent(String event, int pid) {
        return new ObsEvent(event, pid, EventClass.LOCAL, null);
    }

    public static ObsEvent SendEvent(String event, ChannelId channel) {
        return new ObsEvent(event, channel.getSrcPid(), EventClass.SEND,
                channel);
    }

    public static ObsEvent RecvEvent(String event, ChannelId channel) {
        return new ObsEvent(event, channel.getDstPid(), EventClass.RECV,
                channel);
    }

    // //////////////////////////////////////////////////////////////////

    private ObsEvent(String event, int pid, EventClass eventType,
            ChannelId channel) {
        super(event, pid, eventType, channel);
    }

    // //////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        return "obs_" + super.toString();
    }

    /**
     * It is critical that EventType and ObservedEvent have the same
     * hashCode/equals methods. Otherwise, we would not be able to map abstract
     * event types in MC counter-examples to concrete transitions in the GFSM.
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return super.equals(other);
    }

}
