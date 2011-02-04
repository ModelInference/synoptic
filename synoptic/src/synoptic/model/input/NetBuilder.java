package synoptic.model.input;

import java.util.Map.Entry;

import synoptic.model.Action;
import synoptic.model.nets.Net;
import synoptic.model.nets.PetriEvent;

public class NetBuilder implements IBuilder<PetriEvent> {
    Net net = new Net();
    private PetriEvent lastEvent;
    private final boolean createEventAndPlace;

    public NetBuilder() {
        createEventAndPlace = true;
    }

    public NetBuilder(boolean b) {
        createEventAndPlace = b;
    }

    @Override
    public PetriEvent append(Action act) {
        PetriEvent oldLastEvent = lastEvent;
        if (createEventAndPlace) {
            lastEvent = net.createEvent(act.getLabel(), act.getTime());
        } else {
            lastEvent = net.createEventWithoutPlace(act.getLabel(), act
                    .getTime());
        }
        for (Entry<String, String> e : act.getStringArguments().entrySet()) {
            lastEvent.setStringArgument(e.getKey(), e.getValue());
        }
        if (oldLastEvent != null) {
            if (createEventAndPlace) {
                net.connectEvents(oldLastEvent, lastEvent);
            } else {
                net.connectEventsWithNewPlace(oldLastEvent, lastEvent);
            }
        }
        return null;
    }

    @Override
    public void split() {
        lastEvent = null;
    }

    @Override
    public PetriEvent insert(Action act) {
        PetriEvent lastEvent = null;
        if (createEventAndPlace) {
            lastEvent = net.createEvent(act.getLabel(), act.getTime());
        } else {
            lastEvent = net.createEventWithoutPlace(act.getLabel(), act
                    .getTime());
        }
        for (Entry<String, String> e : act.getStringArguments().entrySet()) {
            lastEvent.setStringArgument(e.getKey(), e.getValue());
        }
        return lastEvent;
    }

    @Override
    public void connect(PetriEvent first, PetriEvent second, String relation) {
        // relation is ignored.
        if (createEventAndPlace) {
            net.connectEvents(first, second);
        } else {
            net.connectEventsWithNewPlace(first, second);
        }
    }

    public Net getNet() {
        return net;
    }

    @Override
    public void tagTerminal(PetriEvent terminalNode) {
        net.connect(terminalNode, net.createPlace());
    }

    @Override
    public void tagInitial(PetriEvent first, String relation) {
        // TODO: this is implicit in nets
    }
}
