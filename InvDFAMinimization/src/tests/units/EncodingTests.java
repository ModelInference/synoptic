package tests.units;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.util.HashSet;
import java.util.Set;

import model.EventTypeEncodings;

import org.junit.Test;

import synoptic.model.EventType;
import synoptic.model.StringEventType;

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
        EventTypeEncodings encodings = getBasicEncodings();

        assertSame(encodings.getEncoding(aEvent), encodings.getEncoding(aEvent));

        assertNotSame(encodings.getEncoding(aEvent),
                encodings.getEncoding(bEvent));

        assertNotSame(encodings.getEncoding(zEvent),
                encodings.getEncoding(zEvent));
    }

    @Test
    public void testGetString() {
        EventTypeEncodings encodings = getBasicEncodings();
        char aEncoding = encodings.getEncoding(aEvent);
        assertSame(encodings.getString(aEncoding), aEvent.toString());
    }
}
