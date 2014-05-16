package synoptic.tests.units;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import synoptic.invariants.AlwaysFollowedInvariant;
import synoptic.invariants.NeverFollowedInvariant;
import synoptic.invariants.TemporalInvariantSet;
import synoptic.model.event.Event;
import synoptic.tests.SynopticTest;

/**
 * Tests for synoptic.invariants.TemporalInvariantSet class. We test invariants
 * by parsing a string representing a log, and mining invariants from the
 * resulting graph.
 * 
 */
public class TemporalInvariantSetTests extends SynopticTest {

    /**
     * Tests the sameInvariants() method.
     */
    @Test
    public void testSameInvariants() {
        TemporalInvariantSet s1 = new TemporalInvariantSet();
        TemporalInvariantSet s2 = new TemporalInvariantSet();

        assertTrue(s1.sameInvariants(s2));
        assertTrue(s2.sameInvariants(s1));

        s1.add(new AlwaysFollowedInvariant("a", "b",
                Event.defTimeRelationStr));
        assertFalse(s1.sameInvariants(s2));
        assertFalse(s2.sameInvariants(s1));

        s2.add(new AlwaysFollowedInvariant("a", "b",
                Event.defTimeRelationStr));
        assertTrue(s1.sameInvariants(s2));
        assertTrue(s2.sameInvariants(s1));

        s1.add(new NeverFollowedInvariant("b", "a",
                Event.defTimeRelationStr));
        assertFalse(s1.sameInvariants(s2));
        assertFalse(s2.sameInvariants(s1));

        // Add a similar looking invariant, but different in the B/b label.
        s2.add(new NeverFollowedInvariant("B", "a",
                Event.defTimeRelationStr));
        assertFalse(s1.sameInvariants(s2));
        assertFalse(s2.sameInvariants(s1));
    }

}
