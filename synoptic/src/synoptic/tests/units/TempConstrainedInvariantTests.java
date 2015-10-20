package synoptic.tests.units;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import synoptic.invariants.AlwaysFollowedInvariant;
import synoptic.invariants.AlwaysPrecedesInvariant;
import synoptic.invariants.constraints.IThresholdConstraint;
import synoptic.invariants.constraints.TempConstrainedInvariant;
import synoptic.invariants.constraints.UpperBoundConstraint;
import synoptic.main.parser.ParseException;
import synoptic.tests.PynopticTest;
import synoptic.util.resource.DTotalResource;

/**
 * Tests for the TempConstrainedInvariant class.
 */
public class TempConstrainedInvariantTests extends PynopticTest {

    public void setUp() throws ParseException {
        super.setUp();
    }

    /**
     * Tests that an unconstrained and constrained invariant don't equal each
     * other.
     */
    @Test
    public void unconstrainedAndConstrainedInvEqualityTest() {
        AlwaysFollowedInvariant unconstrainedInv = new AlwaysFollowedInvariant(
                "a", "b", "t");

        IThresholdConstraint upper = new UpperBoundConstraint(new DTotalResource("2.0"));
        TempConstrainedInvariant<AlwaysFollowedInvariant> constrInv = new TempConstrainedInvariant<AlwaysFollowedInvariant>(
                unconstrainedInv, upper, false);

        assertFalse(unconstrainedInv.equals(constrInv));
    }

    /**
     * Tests that two of the same constrained invariants with the same
     * constraints equal each other.
     */
    @Test
    public void sameConstrainedInvsEqualityTest() {
        AlwaysFollowedInvariant unconstrainedInv = new AlwaysFollowedInvariant(
                "a", "b", "t");

        IThresholdConstraint upper1 = new UpperBoundConstraint(new DTotalResource("2.0"));
        IThresholdConstraint upper2 = new UpperBoundConstraint(new DTotalResource("2.0"));

        TempConstrainedInvariant<AlwaysFollowedInvariant> constrInv1 = new TempConstrainedInvariant<AlwaysFollowedInvariant>(
                unconstrainedInv, upper1, false);
        TempConstrainedInvariant<AlwaysFollowedInvariant> constrInv2 = new TempConstrainedInvariant<AlwaysFollowedInvariant>(
                unconstrainedInv, upper2, false);

        assertTrue(constrInv1.equals(constrInv2));
    }

    /**
     * Tests that two of the same constrained invariants with different
     * constraints don't equal each other.
     */
    @Test
    public void diffConstraintSameInvsEqualityTest() {
        AlwaysFollowedInvariant unconstrainedInv = new AlwaysFollowedInvariant(
                "a", "b", "t");

        IThresholdConstraint upper1 = new UpperBoundConstraint(new DTotalResource("2.0"));
        IThresholdConstraint upper2 = new UpperBoundConstraint(new DTotalResource("1.0"));

        TempConstrainedInvariant<AlwaysFollowedInvariant> constrInv1 = new TempConstrainedInvariant<AlwaysFollowedInvariant>(
                unconstrainedInv, upper1, false);
        TempConstrainedInvariant<AlwaysFollowedInvariant> constrInv2 = new TempConstrainedInvariant<AlwaysFollowedInvariant>(
                unconstrainedInv, upper2, false);

        assertFalse(constrInv1.equals(constrInv2));
    }

    /**
     * Tests that two constrained invariants augmented from two different
     * unconstrained invariants don't equal each other.
     */
    @Test
    public void diffConstrainedInvsEqualityTest() {
        AlwaysFollowedInvariant unconstrainedInv1 = new AlwaysFollowedInvariant(
                "a", "b", "t");
        AlwaysPrecedesInvariant unconstrainedInv2 = new AlwaysPrecedesInvariant(
                "a", "b", "t");

        IThresholdConstraint upper1 = new UpperBoundConstraint(new DTotalResource("2.0"));
        IThresholdConstraint upper2 = new UpperBoundConstraint(new DTotalResource("1.0"));

        TempConstrainedInvariant<AlwaysFollowedInvariant> constrInv1 = new TempConstrainedInvariant<AlwaysFollowedInvariant>(
                unconstrainedInv1, upper1, false);
        TempConstrainedInvariant<AlwaysPrecedesInvariant> constrInv2 = new TempConstrainedInvariant<AlwaysPrecedesInvariant>(
                unconstrainedInv2, upper2, false);

        assertFalse(constrInv1.equals(constrInv2));
    }
}
