package tests.units;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import model.InvModel;
import model.InvsModel;

import org.junit.Test;

import synoptic.invariants.AlwaysFollowedInvariant;
import synoptic.invariants.ITemporalInvariant;
import synoptic.invariants.NeverFollowedInvariant;

/**
 * Basic tests for the InvsModel class -- checks that invariants are used and
 * maintained correctly.
 * 
 * @author Jenny
 */
public class InvsModelTests {

    @Test
    public void testConstructor() {
        InvsModel model = new InvsModel();
        assertTrue(model.getInvariants().size() == 0);
        assertFalse(model.run(Arrays.asList("a")));
    }

    @Test
    public void testGetInvariants() {
        Set<ITemporalInvariant> invariants = new HashSet<ITemporalInvariant>();
        invariants.add(new AlwaysFollowedInvariant("a", "b", "t"));
        invariants.add(new AlwaysFollowedInvariant("x", "y", "t"));
        invariants.add(new AlwaysFollowedInvariant("m", "n", "t"));
        InvsModel model = generateModel(invariants);
        assertTrue(invariants.equals(model.getInvariants()));
    }

    @Test
    public void testIntersection() {
        Set<ITemporalInvariant> invariants = new HashSet<ITemporalInvariant>();
        invariants.add(new AlwaysFollowedInvariant("a", "b", "t"));
        invariants.add(new NeverFollowedInvariant("x", "y", "t"));
        InvsModel model = generateModel(invariants);
        assertTrue(model.run(Arrays.asList("a", "y", "x", "b")));
        assertFalse(model.run(Arrays.asList("a", "y", "x")));
    }

    public InvsModel generateModel(Set<ITemporalInvariant> invariants) {
        InvsModel model = new InvsModel();

        for (ITemporalInvariant inv : invariants) {
            model.intersectWith(new InvModel(inv));
        }
        return model;
    }

    @Test
    public void testFinalizeAlphabet() {
        Set<ITemporalInvariant> invariants = new HashSet<ITemporalInvariant>();
        invariants.add(new AlwaysFollowedInvariant("a", "b", "t"));
        invariants.add(new NeverFollowedInvariant("x", "y", "t"));
        InvsModel model = generateModel(invariants);
        assertTrue(model.run(Arrays.asList("a", "k", "b")));
        model.finalizeAlphabet();
        assertFalse(model.run(Arrays.asList("a", "l", "b")));
    }
}
