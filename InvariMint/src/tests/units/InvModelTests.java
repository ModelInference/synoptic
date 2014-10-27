package tests.units;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import model.EventTypeEncodings;
import model.InvModel;

import org.junit.Before;
import org.junit.Test;

import synoptic.invariants.AlwaysFollowedInvariant;
import synoptic.invariants.AlwaysPrecedesInvariant;
import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.NeverFollowedInvariant;
import synoptic.model.event.EventType;

/**
 * Basic tests for the InvModel class - checks that the invariant is used and
 * maintained correctly.
 * 
 */
public class InvModelTests {
    EventTypeEncodings encodings;

    @Before
    public void setUp() {
        encodings = EncodingTests.getBasicEncodings();
    }

    @Test
    public void testConstructor() {
        ITemporalInvariant inv = new AlwaysFollowedInvariant("a", "b", "t");
        InvModel model = new InvModel(inv, encodings);

        List<EventType> sequence = new ArrayList<EventType>();
        sequence.add(EncodingTests.aEvent);
        assertFalse(model.run(sequence));
        sequence.add(EncodingTests.bEvent);
        assertTrue(model.run(sequence));
        sequence.remove(0);
        assertTrue(model.run(sequence));

        inv = new NeverFollowedInvariant("a", "b", "t");
        model = new InvModel(inv, encodings);

        sequence = new ArrayList<EventType>();
        sequence.add(EncodingTests.aEvent);
        assertTrue(model.run(sequence));
        sequence.add(EncodingTests.bEvent);
        assertFalse(model.run(sequence));
        sequence.remove(0);
        assertTrue(model.run(sequence));

        inv = new AlwaysPrecedesInvariant("a", "b", "t");
        model = new InvModel(inv, encodings);
        sequence = new ArrayList<EventType>();
        sequence.add(EncodingTests.aEvent);
        assertTrue(model.run(sequence));
        sequence.add(EncodingTests.bEvent);
        assertTrue(model.run(sequence));
        sequence.remove(0);
        assertFalse(model.run(sequence));
    }

    @Test
    public void testGetInvariant() {
        ITemporalInvariant inv = new AlwaysFollowedInvariant("a", "b", "t");
        InvModel model = new InvModel(inv, encodings);
        assertSame(inv, model.getInvariant());

        ITemporalInvariant inv2 = new NeverFollowedInvariant("a", "b", "t");
        model = new InvModel(inv2, encodings);
        assertSame(inv2, model.getInvariant());
        assertNotSame(inv, model.getInvariant());

        inv = new AlwaysPrecedesInvariant("a", "b", "t");
        model = new InvModel(inv, encodings);
        assertSame(inv, model.getInvariant());
        assertNotSame(inv2, model.getInvariant());
    }
}
