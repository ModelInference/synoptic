package synoptic.model.nets;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import synoptic.model.IEvent;
import synoptic.model.input.VectorTime;
import synoptic.util.IIterableIterator;

public class PetriEvent implements IEvent {
    private final String name;
    private final Set<Place> successors = new HashSet<Place>();
    private final VectorTime time;
    private final HashMap<String, String> stringArguments = new HashMap<String, String>();

    public PetriEvent(String name, VectorTime vectorTime) {
        this.name = name;
        time = vectorTime;
    }

    @Override
    public String toString() {
        return name;
    }

    public IIterableIterator<Edge<PetriEvent, Place>> getEdgeIterator() {
        return new SuccessorToEdgeIterator<PetriEvent, Place>(this, successors
                .iterator(), 1);
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

    public Set<PetriEvent> getPostEvents() {
        Set<PetriEvent> postEvents = new HashSet<PetriEvent>();
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
        return stringArguments.get(name);
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
