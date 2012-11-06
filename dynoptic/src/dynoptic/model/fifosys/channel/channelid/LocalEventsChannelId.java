package dynoptic.model.fifosys.channel.channelid;

import java.util.Map;

import dynoptic.util.Util;

import synoptic.model.channelid.ChannelId;
import synoptic.model.event.DistEventType;

/**
 * Represent a channel that simulates local events in a CFSM. This channel is
 * synthetic -- that is, it is used internally by dynoptic due to limitations of
 * the model checker. This channel is not part of the system being modeled by
 * dynoptic.
 */
public class LocalEventsChannelId extends ChannelId {

    /**
     * This map allows us to look up the local event types based on their string
     * representations in the McScM counter-example output.
     */
    private Map<String, DistEventType> eventStrToDistEventType;

    public LocalEventsChannelId(int scmId) {
        // NOTE: srcPid and dstPid of 0 are arbitrary values.
        super(0, 0, scmId, "ch-locals");
        eventStrToDistEventType = Util.newMap();
    }

    // //////////////////////////////////////////////////////////////////

    @Override
    public boolean equals(Object other) {
        if (!super.equals(other)) {
            return false;
        }

        if (!(other instanceof LocalEventsChannelId)) {
            return false;
        }

        LocalEventsChannelId leCid = (LocalEventsChannelId) other;
        if (!eventStrToDistEventType.equals(leCid.eventStrToDistEventType)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        return (31 * result) + eventStrToDistEventType.hashCode();
    }

    // //////////////////////////////////////////////////////////////////

    public void addLocalEventString(DistEventType eventType, String eventStr) {
        assert eventType.isLocalEvent();
        if (eventStrToDistEventType.containsKey(eventStr)) {
            assert eventStrToDistEventType.get(eventStr).equals(eventType);
        } else {
            eventStrToDistEventType.put(eventStr, eventType);
        }
    }

    public DistEventType getEventType(String eventStr) {
        assert eventStrToDistEventType.containsKey(eventStr);

        return eventStrToDistEventType.get(eventStr);
    }

}
