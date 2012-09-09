package dynoptic.model.alphabet;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import dynoptic.main.DynopticMain;

/**
 * A collection of events that can be processed by an FSM that is part of a
 * CFSM. This includes both the local events as well as send/receive
 * (communication) events.
 */
public class FSMAlphabet implements Set<EventType> {
    private static final String EMPTY_STR_SCM_RE = "(_)";
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
        if (DynopticMain.assertsOn) {
            // Make sure that the new set of events we are adding
            // does not include any events already in the alphabet
            for (EventType e1 : arg0) {
                assert !events.contains(e1);
            }
        }

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

    // //////////////////////////////////////////////////////////////////

    public Set<String> getLocalEventScmStrings() {
        Set<String> ret = new LinkedHashSet<String>();
        for (EventType e : events) {
            if (e.isLocalEvent()) {
                ret.add(e.getScmEventFullString());
            }
        }
        return ret;
    }

    public String toScmParametersString() {
        String ret = "";

        for (String eStr : getUniqueEventStrings(null)) {
            ret += "real " + eStr + " ;\n";
        }
        return ret;
    }

    public String anyEventScmQRe() {
        return scmQRe(null);
    }

    public String anyEventExceptOneScmQRe(EventType ignoreE) {
        assert events.contains(ignoreE);

        return scmQRe(ignoreE);
    }

    // //////////////////////////////////////////////////////////////////

    /**
     * Concatenates a list of event strings into a re expression representing a
     * set of strings.
     */
    private String scmQRe(EventType ignoreE) {
        String ret = "(";
        Set<String> eventStrings = getUniqueEventStrings(ignoreE);
        Iterator<String> iter = eventStrings.iterator();

        while (iter.hasNext()) {
            String e = iter.next();
            ret = ret + e + " | ";
        }

        // The re encodes no events -- return empty string re.
        if (ret.length() == 1) {
            return EMPTY_STR_SCM_RE;
        }

        // Remove the dangling "."
        ret = ret.substring(0, ret.length() - 3);

        return ret + ")";
    }

    /**
     * This function exists because we might have duplication of event strings,
     * event though the event types are unique. For example, the two event types
     * '0!m' and '0?m' are unique, but the event string for both event types is
     * 'm'.
     */
    private Set<String> getUniqueEventStrings(EventType ignoreE) {
        Set<String> ret = new LinkedHashSet<String>();
        for (EventType e : events) {
            if ((ignoreE != null) && e.equals(ignoreE)) {
                continue;
            }
            ret.add(e.getScmEventString());
        }
        return ret;
    }
}
