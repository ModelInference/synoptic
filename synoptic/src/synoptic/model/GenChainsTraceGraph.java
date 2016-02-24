package synoptic.model;

import java.util.Collection;

import synoptic.model.event.Event;

/**
 * 
 */
public class GenChainsTraceGraph extends ChainsTraceGraph {
    static Event initGenericEvent = Event.newInitialGenericEvent();
    static Event termGenericEvent = Event.newTerminalGenericEvent();

    public GenChainsTraceGraph(Collection<EventNode> nodes) {
        super(nodes, initGenericEvent, termGenericEvent);
    }
}
