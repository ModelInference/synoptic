package tests.units;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import model.EventTypeEncodings;
import model.InvModel;
import model.InvsModel;

import org.junit.Before;
import org.junit.Test;

import synoptic.invariants.AlwaysFollowedInvariant;
import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.NeverFollowedInvariant;
import synoptic.model.EventType;

/**
 * Basic tests for the InvsModel class -- checks that invariants are used and
 * maintained correctly.
 * 
 * @author Jenny
 */
public class InvsModelTests {

    EventTypeEncodings encodings;

    @Before
    public void setUp() {
        encodings = EncodingTests.getBasicEncodings();
    }

    /**
     * Constructs an InvsModel with encodings and the given set of invariants.
     */
    public InvsModel generateModel(Set<ITemporalInvariant> invariants) {
        InvsModel model = new InvsModel(encodings);

        for (ITemporalInvariant inv : invariants) {
            model.intersectWith(new InvModel(inv, encodings));
        }
        return model;
    }

    @Test
    public void testConstructor() {
        InvsModel model = new InvsModel(encodings);
        assertTrue(model.getInvariants().size() == 0);
    }

    @Test
    public void testGetInvariants() {
        Set<ITemporalInvariant> invariants = new HashSet<ITemporalInvariant>();
        invariants.add(new AlwaysFollowedInvariant("a", "b", "t"));
        invariants.add(new AlwaysFollowedInvariant("b", "c", "t"));
        invariants.add(new AlwaysFollowedInvariant("c", "d", "t"));
        InvsModel model = generateModel(invariants);
        assertTrue(invariants.equals(model.getInvariants()));
    }

    @Test
    public void testIntersection() {
        Set<ITemporalInvariant> invariants = new HashSet<ITemporalInvariant>();
        invariants.add(new AlwaysFollowedInvariant("a", "b", "t"));
        invariants.add(new NeverFollowedInvariant("c", "d", "t"));
        InvsModel model = generateModel(invariants);
        List<EventType> sequence = new ArrayList<EventType>();
        sequence.add(EncodingTests.aEvent);
        sequence.add(EncodingTests.dEvent);
        sequence.add(EncodingTests.cEvent);
        assertFalse(model.run(sequence));
        sequence.add(EncodingTests.bEvent);
        assertTrue(model.run(sequence));
    }
}
