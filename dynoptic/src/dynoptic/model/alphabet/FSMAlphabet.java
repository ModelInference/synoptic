package dynoptic.model.alphabet;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A collection of events that can be processed by an FSM that is part of a
 * CFSM. This includes both the local events as well as send/receive
 * (communication) events.
 */
public class FSMAlphabet implements Set<EventType> {
    Set<EventType> events;

    public FSMAlphabet() {
        events = new LinkedHashSet<EventType>();
    }

    // //////////////////////////////////////////////////////////////////
    // Methods for Set<>, which we simply pass onto the internal events set.

    @Override
    public boolean add(EventType arg0) {
        return events.add(arg0);
    }

    @Override
    public boolean addAll(Collection<? extends EventType> arg0) {
        return events.addAll(arg0);
    }

    @Override
    public void clear() {
        events.clear();
    }

    @Override
    public boolean contains(Object arg0) {
        return events.contains(arg0);
    }

    @Override
    public boolean containsAll(Collection<?> arg0) {
        return events.containsAll(arg0);
    }

    @Override
    public boolean isEmpty() {
        return events.isEmpty();
    }

    @Override
    public Iterator<EventType> iterator() {
        return events.iterator();
    }

    @Override
    public boolean remove(Object arg0) {
        return events.remove(arg0);
    }

    @Override
    public boolean removeAll(Collection<?> arg0) {
        return events.removeAll(arg0);
    }

    @Override
    public boolean retainAll(Collection<?> arg0) {
        return events.retainAll(arg0);
    }

    @Override
    public int size() {
        return events.size();
    }

    @Override
    public Object[] toArray() {
        return events.toArray();
    }

    @Override
    public <T> T[] toArray(T[] arg0) {
        return events.toArray(arg0);
    }
}
