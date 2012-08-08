package dynoptic.model.fifosys.channel;

import java.util.LinkedHashMap;
import java.util.Map;

import dynoptic.model.alphabet.EventType;

/** Represent a channel that simulates local events in a CFSM. */
public class LocalEventsChannelId extends ChannelId {

    /**
     * This map allows us to look up the local event types based on their string
     * representations in the McScM counter-example output.
     */
    private Map<String, EventType> eventStrToEventType;

    public LocalEventsChannelId(int scmId) {
        super(Integer.MAX_VALUE, Integer.MAX_VALUE, scmId, "ch-locals");
        eventStrToEventType = new LinkedHashMap<String, EventType>();
    }

    public void addLocalEventString(EventType eventType, String eventStr) {
        assert !eventStrToEventType.containsKey(eventStr);

        eventStrToEventType.put(eventStr, eventType);
    }

    public EventType getEventType(String eventStr) {
        assert eventStrToEventType.containsKey(eventStr);

        return eventStrToEventType.get(eventStr);
    }

}
