package model.nets;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import model.input.VectorTime;

public class Net {
	Set<Place> places = new HashSet<Place>();
	Set<Event> events = new HashSet<Event>();

	public Event createEventWithoutPlace(String name, VectorTime vectorTime) {
		Event event = new Event(name, vectorTime);
		events.add(event);
		return event;
	}

	public Event createEvent(String name, VectorTime vectorTime) {
		Place place = new Place("P-" + name);
		places.add(place);
		Event event = new Event(name, vectorTime);
		events.add(event);
		place.add(event);
		return event;
	}

	public void connectEventsWithNewPlace(Event event, Set<Event> events) {
		Place place = new Place("");
		Set<Event> postEvents = event.getPostEvents();
		events.removeAll(postEvents);
		if (events.size() == 0)
			return;
		place.addAll(events);
		places.add(place);
		event.add(place);
	}

	public void connectEventsWithNewPlace(Event from, Event to) {
		Place place = new Place("");
		from.add(place);
		places.add(place);
		place.add(to);
	}

	public void connectEvents(Event first, Event last) {
		for (Place p : getPre(last)) {
			first.add(p);
		}
	}

	public Set<Place> getPre(Event event) {
		Set<Place> set = new HashSet<Place>();
		for (Place p : places) {
			if (p.getPost().contains(event))
				set.add(p);
		}
		return set;
	}

	public Set<Place> getPost(Event e) {
		return e.getPost();
	}

	public Set<Event> getPre(Place place) {
		Set<Event> set = new HashSet<Event>();
		for (Event e : events) {
			if (e.getPost().contains(place))
				set.add(e);
		}
		return set;
	}

	public Set<Event> getPost(Place p) {
		return p.getPost();
	}

	public Set<Event> getEvents() {
		return events;
	}

	public Set<Place> getPlaces() {
		return places;
	}

	public void makeExclusive(Event e1, Event e2) {
		for (Place p : getPre(e1)) {
			p.add(e2);
		}
		for (Place p : getPre(e2)) {
			p.add(e1);
		}
	}

	public void makeExclusive(Event e1, Event e2, Event e3) {
		for (Place p : getPre(e1)) {
			if (e3.getPost().contains(p)) {
				p.add(e2);
				p.add(e1);
			}
		}
		for (Place p : getPre(e2)) {
			if (e3.getPost().contains(p)) {
				p.add(e2);
				p.add(e1);
			}
		}
	}

	public void addNF(Event e1, Event e2) {
		for (Place p : getPre(e1))
			p.add(e2);
	}

	public void removePlace(Place p) {
		places.remove(p);
		for (Event e : events) {
			e.remove(p);
		}
	}

	public Set<Place> getInitalPlaces() {
		Set<Place> initialPlaces = new HashSet<Place>();
		out: for (Place p : places) {
			for (Event e : events) {
				if (e.getPost().contains(p))
					continue out;
			}
			initialPlaces.add(p);
		}
		return initialPlaces;
	}

	public Place createPlace() {
		Place p = new Place("");
		places.add(p);
		return p;
	}

	public void connect(Event key, Place finalPlace) {
		key.add(finalPlace);
	}

	public Set<Event> getPreEvents(Event first) {
		Set<Event> pre = new HashSet<Event>();
		for (Place p : getPre(first)) {
			pre.addAll(getPre(p));
		}
		return pre;
	}

	public void replace(ArrayList<Event> seq, String string) {
		Place initial = null;
		Place end = null;
		for (int i = 0; i < seq.size(); ++i) {
			if (i == 0) {
				initial = getPre(seq.get(i)).iterator().next();
				events.remove(seq.get(i));
				initial.remove(seq.get(i));
				continue;
			}
			if (i == seq.size() - 1) {
				end = seq.get(i).getPost().iterator().next();
			}
			events.remove(seq.get(i));
			places.remove(getPre(seq.get(i)).iterator().next());
		}
		Event e = new Event(string, null);
		initial.add(e);
		e.add(end);
		events.add(e);
	}

	public Set<Event> getInitalEvents() {
		Set<Event> set = new HashSet<Event>();
		for (Place p : getInitalPlaces()) {
			set.addAll(p.getPost());
		}
		return set;
	}

	public Set<Place> getTerminalPlaces() {
		Set<Place> set = new HashSet<Place>();
		for (Place p : places) {
			if (p.getPost().size() == 0)
				set.add(p);
		}
		return set;
	}

	public Set<Event> getTerminalEvents() {
		Set<Event> set = new HashSet<Event>();
		for (Event p : events) {
			if (p.getPost().size() == 0)
				set.add(p);
		}
		return set;
	}

	public void mergeTerminalPlaces() {
		Place survivor = null;
		for (Place p : getTerminalPlaces()) {
			if (survivor == null) {
				survivor = p;
				continue;
			}
			places.remove(p);
			for (Event e : getPre(p)) {
				e.add(survivor);
				e.remove(p);
			}
		}

	}

	public void contract(Place p, Place next) {
		for (Event e : getPre(next)) {
			e.remove(next);
		}
		for (Event e : getPre(p)) {
			e.add(next);
		}
		for (Event e : p.getPost())
			for (Place p2 : next.getPostPlaces())
				e.add(p2);
		//if (next.getPost().size() == 0)
		//	removePlace(next);
	}

	public void mergeInitialPlaces() {
		Place first = null;
		ArrayList<Place> remove = new ArrayList<Place>();
		for (Place p : places) {
			if (getPre(p).size() == 0)
			{
				if (first == null)
					first = p;
				else {
					first.addAll(p.getPost());
					remove.add(p);
				}
			}
		}
		places.removeAll(remove);
	}
}
