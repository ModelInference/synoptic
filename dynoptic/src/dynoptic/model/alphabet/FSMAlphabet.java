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
        assert !events.contains(arg0);

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

    public String toScmParametersString() {
        String ret = "parameters :\n";

        Set<String> seenEventStrs = new LinkedHashSet<String>();
        for (EventType e : events) {
            String eStr = e.getRawEventStr();
            if (!seenEventStrs.contains(eStr)) {
                ret += "real " + eStr + " ;\n";
                seenEventStrs.add(eStr);
            }
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

    private String scmQRe(EventType ignoreE) {
        String ret = "(";
        Iterator<EventType> iter = events.iterator();

        while (iter.hasNext()) {
            EventType e = iter.next();
            // Skip the ignored event.
            if (ignoreE != null && e.equals(ignoreE)) {
                continue;
            }
            ret = ret + e.getScmEventString() + " . ";
        }

        // The re encodes no events -- return empty string re.
        if (ret.length() == 1) {
            return EMPTY_STR_SCM_RE;
        }

        // Remove the dangling "."
        ret = ret.substring(0, ret.length() - 3);

        return ret + ")";
    }
}
