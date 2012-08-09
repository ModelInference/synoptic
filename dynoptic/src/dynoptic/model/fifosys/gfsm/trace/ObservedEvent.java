package dynoptic.model.fifosys.gfsm.trace;

import dynoptic.model.alphabet.EventType;
import dynoptic.model.fifosys.channel.ChannelId;

/**
 * Represents an event that was observed or mined from a log of an execution of
 * a FIFO system.
 */
public class ObservedEvent extends EventType {

    public static ObservedEvent LocalEvent(String event, int pid) {
        return new ObservedEvent(event, pid, EventClass.LOCAL, null);
    }

    public static ObservedEvent SendEvent(String event, ChannelId channel) {
        return new ObservedEvent(event, channel.getSrcPid(), EventClass.SEND,
                channel);
    }

    public static ObservedEvent RecvEvent(String event, ChannelId channel) {
        return new ObservedEvent(event, channel.getDstPid(), EventClass.RECV,
                channel);
    }

    // //////////////////////////////////////////////////////////////////

    private ObservedEvent(String event, int pid, EventClass eventType,
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
