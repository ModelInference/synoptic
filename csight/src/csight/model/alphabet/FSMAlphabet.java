package csight.model.alphabet;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import csight.util.Util;

import synoptic.model.event.IDistEventType;

/**
 * A collection of events that can be processed by an FSM that is part of a
 * CFSM. This includes both the local events as well as send/receive
 * (communication) events.
 */
public class FSMAlphabet<TxnEType extends IDistEventType> implements
        Set<TxnEType> {
    // private static final String EMPTY_STR_SCM_RE = "(_)";
    Set<TxnEType> events;

    public FSMAlphabet() {
        events = Util.newSet();
    }

    // //////////////////////////////////////////////////////////////////
    // Methods for Set<>, which we simply pass onto the internal events set.

    @Override
    public boolean add(TxnEType arg0) {
        return events.add(arg0);
    }

    @Override
    public boolean addAll(Collection<? extends TxnEType> arg0) {
        // NOTE: we might end up adding the same event multiple times. This is
        // okay, since the event might occur on different transitions.
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
    public Iterator<TxnEType> iterator() {
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

    @Override
    public String toString() {
        return events.toString();
    }

    // //////////////////////////////////////////////////////////////////

    public Set<String> getLocalEventScmStrings() {
        Set<String> ret = Util.newSet();
        for (TxnEType e : events) {
            if (e.isLocalEvent()) {
                ret.add(e.getScmEventFullString());
            }
        }
        return ret;
    }

    public String toScmParametersString() {
        String ret = "";

        for (String eStr : getUniqueEventStrings()) {
            ret += "real " + eStr + " ;\n";
        }
        return ret;
    }

    // //////////////////////////////////////////////////////////////////

    /**
     * This function exists because we might have duplication of event strings,
     * though the event types are unique. For example, the two event types '0!m'
     * and '0?m' are unique, but the event string for both event types is 'm'.
     */
    private Set<String> getUniqueEventStrings() {
        Set<String> ret = Util.newSet();
        for (TxnEType e : events) {
            ret.add(e.getScmEventString());
        }
        return ret;
    }
}
