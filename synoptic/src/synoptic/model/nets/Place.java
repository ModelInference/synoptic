package synoptic.model.nets;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import synoptic.util.IIterableIterator;

public class Place {
    String name;
    Set<Event> successors = new HashSet<Event>();

    public Place(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public IIterableIterator<Edge<Place, Event>> getEdgeIterator(Net net) {
        return new SuccessorToEdgeIterator<Place, Event>(this, successors
                .iterator(), Math.max(1, net.getPre(this).size()));
    }

    public void add(Event e) {
        successors.add(e);
    }

    public Set<Event> getPost() {
        return successors;
    }

    public void addAll(Collection<Event> events) {
        successors.addAll(events);
    }

    public void remove(Event event) {
        successors.remove(event);
    }

    public Set<Place> getPostPlaces() {
        Set<Place> postEvents = new HashSet<Place>();
        for (Event p : getPost()) {
            postEvents.addAll(p.getPost());
        }
        return postEvents;
    }
}
