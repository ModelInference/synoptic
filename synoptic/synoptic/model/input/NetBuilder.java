package model.input;

import java.util.Map.Entry;

import model.Action;
import model.nets.Event;
import model.nets.Net;

public class NetBuilder implements IBuilder<Event> {
	Net net = new Net();
	private Event lastEvent;
	private boolean createEventAndPlace;
	
	public NetBuilder() {
		this.createEventAndPlace = true;
	}
	public NetBuilder(boolean b) {
		this.createEventAndPlace = b;
	}

	@Override
	public Event append(Action act) {
		Event oldLastEvent = lastEvent;
		if (createEventAndPlace)
			lastEvent = net.createEvent(act.getLabel(), act.getTime());
		else 
			lastEvent = net.createEventWithoutPlace(act.getLabel(), act.getTime());
		for (Entry<String, String> e : act.getStringArguments().entrySet())
			lastEvent.setStringArgument(e.getKey(), e.getValue());
		if (oldLastEvent != null)
			if (createEventAndPlace)
				net.connectEvents(oldLastEvent, lastEvent);
			else
				net.connectEventsWithNewPlace(oldLastEvent, lastEvent);
		return null;
	}

	@Override
	public Event insertAfter(Event event, Action act) {
		Event lastEvent = null;
		if (createEventAndPlace)
			lastEvent = net.createEvent(act.getLabel(), act.getTime());
		else 
			lastEvent = net.createEventWithoutPlace(act.getLabel(), act.getTime());
		for (Entry<String, String> e : act.getStringArguments().entrySet())
			lastEvent.setStringArgument(e.getKey(), e.getValue());
		if (createEventAndPlace)
			net.connectEvents(event, lastEvent);
		else
			net.connectEventsWithNewPlace(event, lastEvent);
		return lastEvent;
	}

	@Override
	public void split() {
		lastEvent = null;
	}

	@Override
	public Event insert(Action act) {
		Event lastEvent = null;
		if (createEventAndPlace)
			lastEvent = net.createEvent(act.getLabel(), act.getTime());
		else 
			lastEvent = net.createEventWithoutPlace(act.getLabel(), act.getTime());
		for (Entry<String, String> e : act.getStringArguments().entrySet())
			lastEvent.setStringArgument(e.getKey(), e.getValue());
		return lastEvent;
	}

	@Override
	public void addInitial(Event curMessage, String relation) {
		//TODO: this is implicit in nets		
	}

	@Override
	public void connect(Event first, Event second, String relation) {
		// relation is ignored.
		if (createEventAndPlace)
			net.connectEvents(first, second);
		else
			net.connectEventsWithNewPlace(first, second);
	}

	public Net getNet() {
		return net;
	}

	@Override
	public void setTerminal(Event terminalNode) {
		net.connect(terminalNode, net.createPlace());
	}
}
