package dynoptic.model.automaton;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Test;

import dynoptic.DynopticTest;

import synoptic.model.event.DistEventType;

/**
 * To test that EventType encodings of 2 equal sets of events are equal, and
 * that of 2 different sets of events are not equal.
 */
public class EventTypeEncodingsTests extends DynopticTest {

    @Test
    public void encodeZeroEvent() {
        Set<DistEventType> eventSet1 = new LinkedHashSet<DistEventType>();
        Set<DistEventType> eventSet2 = new LinkedHashSet<DistEventType>();

        checkEncodingEquality(eventSet1, eventSet2);
    }

    @Test
    public void encodeOneEvent() {
        DistEventType e = DistEventType.LocalEvent("a", 0);
        DistEventType f = DistEventType.LocalEvent("a", 0);
        Set<DistEventType> eventSet1 = toSet(e);
        Set<DistEventType> eventSet2 = toSet(f);

        checkEncodingEquality(eventSet1, eventSet2);
    }

    @Test
    public void encodeTwoEvents() {
        DistEventType e = DistEventType.LocalEvent("x", 0);
        DistEventType f = DistEventType.LocalEvent("y", 0);
        DistEventType g = DistEventType.LocalEvent("x", 0);
        DistEventType h = DistEventType.LocalEvent("y", 0);
        Set<DistEventType> eventSet1 = toSet(e, f);
        Set<DistEventType> eventSet2 = toSet(g, h);

        checkEncodingEquality(eventSet1, eventSet2);
    }

    @Test
    public void encodeThreeEvents() {
        DistEventType a = DistEventType.LocalEvent("a", 0);
        DistEventType b = DistEventType.LocalEvent("b", 0);
        DistEventType c = DistEventType.LocalEvent("c", 0);
        DistEventType d = DistEventType.LocalEvent("a", 0);
        DistEventType e = DistEventType.LocalEvent("b", 0);
        DistEventType f = DistEventType.LocalEvent("c", 0);
        Set<DistEventType> eventSet1 = toSet(a, b, c);
        Set<DistEventType> eventSet2 = toSet(d, e, f);

        checkEncodingEquality(eventSet1, eventSet2);
    }

    @Test
    public void encodeDifferentEvents() {
        DistEventType e = DistEventType.LocalEvent("x", 0);
        DistEventType f = DistEventType.LocalEvent("y", 0);
        DistEventType g = DistEventType.LocalEvent("x", 0);
        DistEventType h = DistEventType.LocalEvent("z", 0);
        Set<DistEventType> eventSet1 = toSet(e, f);
        Set<DistEventType> eventSet2 = toSet(g, h);

        checkEncodingEquality(eventSet1, eventSet2);
    }

    /**
     * Converts a list of DistEventType instances into a set, and returns the
     * set.
     */
    private Set<DistEventType> toSet(DistEventType... events) {
        Set<DistEventType> eventSet = new LinkedHashSet<DistEventType>();
        for (DistEventType event : events) {
            eventSet.add(event);
        }
        return eventSet;
    }

    private void checkEncodingEquality(Set<DistEventType> eventSet1,
            Set<DistEventType> eventSet2) {
        EventTypeEncodings encoding1 = new EventTypeEncodings(eventSet1);
        EventTypeEncodings encoding2 = new EventTypeEncodings(eventSet2);

        if (eventSet1.equals(eventSet2)) {
            assertEquals(encoding1, encoding2);
        } else {
            assertFalse(encoding1.equals(encoding2));
        }
    }
}
