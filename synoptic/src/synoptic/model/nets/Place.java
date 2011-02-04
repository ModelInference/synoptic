package synoptic.model.nets;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import synoptic.util.IIterableIterator;

public class Place {
    String name;
    Set<PetriEvent> successors = new HashSet<PetriEvent>();

    public Place(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public IIterableIterator<Edge<Place, PetriEvent>> getEdgeIterator(Net net) {
        return new SuccessorToEdgeIterator<Place, PetriEvent>(this, successors
                .iterator(), Math.max(1, net.getPre(this).size()));
    }

    public void add(PetriEvent e) {
        successors.add(e);
    }

    public Set<PetriEvent> getPost() {
        return successors;
    }

    public void addAll(Collection<PetriEvent> events) {
        successors.addAll(events);
    }

    public void remove(PetriEvent event) {
        successors.remove(event);
    }

    public Set<Place> getPostPlaces() {
        Set<Place> postEvents = new HashSet<Place>();
        for (PetriEvent p : getPost()) {
            postEvents.addAll(p.getPost());
        }
        return postEvents;
    }
}
