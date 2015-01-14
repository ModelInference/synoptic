package tests.units;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import model.EncodedAutomaton;
import model.EventTypeEncodings;
import model.InvsModel;

import org.junit.Test;

import synoptic.model.event.EventType;
import synoptic.model.event.StringEventType;

public class EncodingTests {

    public static final EventType aEvent = new StringEventType("a");

    public static final EventType bEvent = new StringEventType("b");
    public static final EventType cEvent = new StringEventType("c");
    public static final EventType dEvent = new StringEventType("d");
    public static final EventType zEvent = new StringEventType("z");

    /**
     * Returns an EventTypeEncodings object for event {a, b, c, d}.
     */
    public static EventTypeEncodings getBasicEncodings() {
        Set<EventType> events = new HashSet<EventType>();
        events.add(aEvent);
        events.add(bEvent);
        events.add(cEvent);
        events.add(dEvent);
        return new EventTypeEncodings(events);
    }

    @Test
    public void testGetEncoding() {
        final EventType a2Event = new StringEventType("a");
        final EventType xEvent = new StringEventType("x");
        final EventType yEvent = new StringEventType("y");
        final EventType z2Event = new StringEventType("z");

        EventTypeEncodings encodings = getBasicEncodings();
        // Check one event type that has been encoded.
        assertEquals(encodings.getEncoding(aEvent),
                encodings.getEncoding(aEvent));

        // Check an encoded event type and a NON-encoded event type. These are
        // different event type objects, but have the same string
        // representations.
        assertEquals(encodings.getEncoding(aEvent),
                encodings.getEncoding(a2Event));

        // Check two different event types that have been encoded.
        assertFalse(((Character) encodings.getEncoding(aEvent))
                .equals(encodings.getEncoding(bEvent)));

        // Check one event type that has NOT been encoded.
        assertEquals(encodings.getEncoding(dEvent),
                encodings.getEncoding(dEvent));

        // Check two event types that have NOT been encoded.
        assertFalse(((Character) encodings.getEncoding(xEvent))
                .equals(encodings.getEncoding(yEvent)));

        // Check encoding equality of two NON-encoded events that are different
        // event type objects, but have the same string representation.
        assertEquals(encodings.getEncoding(zEvent),
                encodings.getEncoding(z2Event));
    }

    @Test
    public void testGetString() {
        EventTypeEncodings encodings = getBasicEncodings();
        char aEncoding = encodings.getEncoding(aEvent);
        assertSame(encodings.getString(aEncoding), aEvent.toString());
    }

    @Test
    public void testGetInitialModel() {
        EventTypeEncodings encoding = getBasicEncodings();
        EncodedAutomaton model = new InvsModel(encoding);
        List<EventType> events = new ArrayList<EventType>();
        events.add(bEvent);
        events.add(bEvent);
        events.add(aEvent);
        events.add(dEvent);
        assertTrue(model.run(events));

        events.add(new StringEventType("fakeEvent"));
        assertFalse(model.run(events));
    }
}
