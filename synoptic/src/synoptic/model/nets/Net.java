package synoptic.model.nets;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import synoptic.model.input.VectorTime;

public class Net {
    Set<Place> places = new HashSet<Place>();
    Set<PetriEvent> events = new HashSet<PetriEvent>();

    public PetriEvent createEventWithoutPlace(String name, VectorTime vectorTime) {
        PetriEvent event = new PetriEvent(name, vectorTime);
        events.add(event);
        return event;
    }

    public PetriEvent createEvent(String name, VectorTime vectorTime) {
        Place place = new Place("P-" + name);
        places.add(place);
        PetriEvent event = new PetriEvent(name, vectorTime);
        events.add(event);
        place.add(event);
        return event;
    }

    public void connectEventsWithNewPlace(PetriEvent event, Set<PetriEvent> events) {
        Place place = new Place("");
        Set<PetriEvent> postEvents = event.getPostEvents();
        events.removeAll(postEvents);
        if (events.size() == 0) {
            return;
        }
        place.addAll(events);
        places.add(place);
        event.add(place);
    }

    public void connectEventsWithNewPlace(PetriEvent from, PetriEvent to) {
        Place place = new Place("");
        from.add(place);
        places.add(place);
        place.add(to);
    }

    public void connectEvents(PetriEvent first, PetriEvent last) {
        for (Place p : getPre(last)) {
            first.add(p);
        }
    }

    public Set<Place> getPre(PetriEvent event) {
        Set<Place> set = new HashSet<Place>();
        for (Place p : places) {
            if (p.getPost().contains(event)) {
                set.add(p);
            }
        }
        return set;
    }

    public Set<Place> getPost(PetriEvent e) {
        return e.getPost();
    }

    public Set<PetriEvent> getPre(Place place) {
        Set<PetriEvent> set = new HashSet<PetriEvent>();
        for (PetriEvent e : events) {
            if (e.getPost().contains(place)) {
                set.add(e);
            }
        }
        return set;
    }

    public Set<PetriEvent> getPost(Place p) {
        return p.getPost();
    }

    public Set<PetriEvent> getEvents() {
        return events;
    }

    public Set<Place> getPlaces() {
        return places;
    }

    public void makeExclusive(PetriEvent e1, PetriEvent e2) {
        for (Place p : getPre(e1)) {
            p.add(e2);
        }
        for (Place p : getPre(e2)) {
            p.add(e1);
        }
    }

    public void makeExclusive(PetriEvent e1, PetriEvent e2, PetriEvent e3) {
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

    public void addNF(PetriEvent e1, PetriEvent e2) {
        for (Place p : getPre(e1)) {
            p.add(e2);
        }
    }

    public void removePlace(Place p) {
        places.remove(p);
        for (PetriEvent e : events) {
            e.remove(p);
        }
    }

    public Set<Place> getInitalPlaces() {
        Set<Place> initialPlaces = new HashSet<Place>();
        out: for (Place p : places) {
            for (PetriEvent e : events) {
                if (e.getPost().contains(p)) {
                    continue out;
                }
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

    public void connect(PetriEvent key, Place finalPlace) {
        key.add(finalPlace);
    }

    public Set<PetriEvent> getPreEvents(PetriEvent first) {
        Set<PetriEvent> pre = new HashSet<PetriEvent>();
        for (Place p : getPre(first)) {
            pre.addAll(getPre(p));
        }
        return pre;
    }

    public void replace(ArrayList<PetriEvent> seq, String string) {
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
        PetriEvent e = new PetriEvent(string, null);
        initial.add(e);
        e.add(end);
        events.add(e);
    }

    public Set<PetriEvent> getInitalEvents() {
        Set<PetriEvent> set = new HashSet<PetriEvent>();
        for (Place p : getInitalPlaces()) {
            set.addAll(p.getPost());
        }
        return set;
    }

    public Set<Place> getTerminalPlaces() {
        Set<Place> set = new HashSet<Place>();
        for (Place p : places) {
            if (p.getPost().size() == 0) {
                set.add(p);
            }
        }
        return set;
    }

    public Set<PetriEvent> getTerminalEvents() {
        Set<PetriEvent> set = new HashSet<PetriEvent>();
        for (PetriEvent p : events) {
            if (p.getPost().size() == 0) {
                set.add(p);
            }
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
            for (PetriEvent e : getPre(p)) {
                e.add(survivor);
                e.remove(p);
            }
        }

    }

    public void contract(Place p, Place next) {
        for (PetriEvent e : getPre(next)) {
            e.remove(next);
        }
        for (PetriEvent e : getPre(p)) {
            e.add(next);
        }
        for (PetriEvent e : p.getPost()) {
            for (Place p2 : next.getPostPlaces()) {
                e.add(p2);
                // if (next.getPost().size() == 0)
                // removePlace(next);
            }
        }
    }

    public void mergeInitialPlaces() {
        Place first = null;
        ArrayList<Place> remove = new ArrayList<Place>();
        for (Place p : places) {
            if (getPre(p).size() == 0) {
                if (first == null) {
                    first = p;
                } else {
                    first.addAll(p.getPost());
                    remove.add(p);
                }
            }
        }
        places.removeAll(remove);
    }
}
