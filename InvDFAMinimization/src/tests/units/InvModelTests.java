package tests.units;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import model.InvModel;

import org.junit.Test;

import synoptic.invariants.AlwaysFollowedInvariant;
import synoptic.invariants.AlwaysPrecedesInvariant;
import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.NeverFollowedInvariant;

/**
 * Basic tests for the InvModel class = checks that the invariant is used and
 * maintained correctly.
 * 
 * @author Jenny
 */
public class InvModelTests {

    @Test
    public void testConstructor() {
        ITemporalInvariant inv = new AlwaysFollowedInvariant("a", "b", "t");
        InvModel model = new InvModel(inv);
        assertTrue(model.run(Arrays.asList("a", "b")));
        assertFalse(model.run(Arrays.asList("a")));
        assertTrue(model.run(Arrays.asList("b")));

        inv = new NeverFollowedInvariant("a", "b", "t");
        model = new InvModel(inv);
        assertFalse(model.run(Arrays.asList("a", "b")));
        assertTrue(model.run(Arrays.asList("a")));
        assertTrue(model.run(Arrays.asList("b")));

        inv = new AlwaysPrecedesInvariant("a", "b", "t");
        model = new InvModel(inv);
        assertTrue(model.run(Arrays.asList("a", "b")));
        assertTrue(model.run(Arrays.asList("a")));
        assertFalse(model.run(Arrays.asList("b")));
    }

    @Test
    public void testGetInvariant() {
        ITemporalInvariant inv = new AlwaysFollowedInvariant("a", "b", "t");
        InvModel model = new InvModel(inv);
        assertSame(inv, model.getInvariant());

        ITemporalInvariant inv2 = new NeverFollowedInvariant("a", "b", "t");
        model = new InvModel(inv2);
        assertSame(inv2, model.getInvariant());
        assertNotSame(inv, model.getInvariant());

        inv = new AlwaysPrecedesInvariant("a", "b", "t");
        model = new InvModel(inv);
        assertSame(inv, model.getInvariant());
        assertNotSame(inv2, model.getInvariant());
    }
}
