package synoptic.model.nets;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


import synoptic.model.IEvent;
import synoptic.model.input.VectorTime;
import synoptic.util.IIterableIterator;

public class Event implements IEvent {
	private String name;
	private Set<Place> successors = new HashSet<Place>();
	private VectorTime time;
	private HashMap<String, String> stringArguments = new HashMap<String, String>();
	
	public Event(String name, VectorTime vectorTime) {
		this.name = name;
		this.time = vectorTime;
	} 
	
	public String toString() {
		return name;
	}
	
	public IIterableIterator<Edge<Event, Place>> getEdgeIterator() {
		return new SuccessorToEdgeIterator<Event, Place>(this, successors.iterator(), 1);
	}

	public Set<Place> getPost() {
		return successors;
	}

	public boolean remove(Place p) {
		return successors.remove(p);
	}

	public void add(Place p) {
		successors.add(p);
	}

	public Set<Event> getPostEvents() {
		Set<Event> postEvents = new HashSet<Event>();
		for (Place p : getPost()) {
			postEvents.addAll(p.getPost());
		}
		return postEvents;
	}

	@Override
	public VectorTime getTime() {
		return time;
	}

	@Override
	public String getStringArgument(String name) {
		return stringArguments .get(name);
	}
	
	@Override
	public void setStringArgument(String name, String value) {
		stringArguments.put(name, value);
	}

	public String getName() {
		return name;
	}

	@Override
	public Set<String> getStringArguments() {
		return stringArguments.keySet();
	}
}
