package synoptic.tests.units;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.LinkedList;

import org.junit.Test;

import synoptic.tests.SynopticTest;
import synoptic.util.resource.AbstractResource;
import synoptic.util.resource.DTotalResource;
import synoptic.util.resource.ITotalResource;
import synoptic.util.resource.LTotalResource;
import synoptic.util.resource.NonComparableResourceException;
import synoptic.util.resource.VectorTime;

/**
 * Tests that exercise classes that extends the AbstractResource abstract class.
 */
public class ResourceTests extends SynopticTest {

    /**
     * Tests correctness of delta computation.
     */
    @Test
    public void testPositiveDelta() {
        DTotalResource d1 = new DTotalResource("2.0");
        DTotalResource d2 = new DTotalResource("1.0");
        AbstractResource delta = d1.computeDelta(d2);
        assertEquals(new DTotalResource("1.0"), delta);
    }

    @Test
    public void testPositiveDeltaWithKey() {
        DTotalResource d1 = new DTotalResource("2.0", "key");
        DTotalResource d2 = new DTotalResource("1.0", "key");
        AbstractResource delta = d1.computeDelta(d2);
        assertEquals(new DTotalResource("1.0", "key"), delta);
    }

    /**
     * Delta computation between two vector timestamps is not supported. Uses
     * integer vtime constructor.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testExceptionVectorTimeIntConstructor() {
        VectorTime v1 = new VectorTime(1);
        VectorTime v2 = new VectorTime(2);
        v2.computeDelta(v1);
    }

    /**
     * Delta computation between two vector timestamps is not supported. Uses
     * list of integers vtime constructor.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testExceptionVectorTimeVectorConstructor() {
        LinkedList<Integer> l1 = new LinkedList<Integer>();
        LinkedList<Integer> l2 = new LinkedList<Integer>();

        // Fill the list with some int values.
        for (int i = 0; i < 5; i++) {
            l1.add(i);
            l2.add(i);
        }

        VectorTime v1 = new VectorTime(l1);
        VectorTime v2 = new VectorTime(l2);

        v1.computeDelta(v2);
    }

    /**
     * Test that we can divide an AbstractResource instance.
     */
    @Test
    public void testDivisionITotalResource() {
        AbstractResource r1 = new ITotalResource(10);
        AbstractResource r2 = r1.divBy(2);
        AbstractResource oracle = new ITotalResource(5);
        assertEquals(oracle, r2);

        AbstractResource r1WithKey = new ITotalResource(10, "key");
        AbstractResource r2WithKey = r1WithKey.divBy(2);
        AbstractResource oracleWithKey = new ITotalResource(5, "key");
        assertEquals(oracleWithKey, r2WithKey);
    }

    /**
     * Test that we can divide an LTotalResource instance.
     */
    @Test
    public void testDivisionLTotalResource() {
        AbstractResource r1 = new LTotalResource(10);
        AbstractResource r2 = r1.divBy(2);
        AbstractResource oracle = new LTotalResource(5);
        assertEquals(oracle, r2);

        AbstractResource r1WithKey = new LTotalResource(10, "key");
        AbstractResource r2WithKey = r1WithKey.divBy(2);
        AbstractResource oracleWithKey = new LTotalResource(5, "key");
        assertEquals(oracleWithKey, r2WithKey);
    }

    /**
     * Test that we can divide a DTotalResource instance.
     */
    @Test
    public void testDivisionDTotalResource() {
        AbstractResource r1 = new DTotalResource(10);
        AbstractResource r2 = r1.divBy(2);
        AbstractResource oracle = new DTotalResource(5);
        assertEquals(oracle, r2);

        AbstractResource r1WithKey = new DTotalResource(10, "key");
        AbstractResource r2WithKey = r1WithKey.divBy(2);
        AbstractResource oracleWithKey = new DTotalResource(5, "key");
        assertEquals(oracleWithKey, r2WithKey);
    }

    /**
     * Test that we can increment an ITotalResource instance.
     */
    @Test
    public void testITotalResourceIncr() {
        AbstractResource r1 = new ITotalResource(1);
        AbstractResource r2 = new ITotalResource(5);
        AbstractResource result = r1.incrBy(r2);
        AbstractResource oracle = new ITotalResource(6);
        assertEquals(oracle, result);

        AbstractResource r1WithKey = new ITotalResource(1, "key");
        AbstractResource r2WithKey = new ITotalResource(5, "key");
        AbstractResource resultWithKey = r1WithKey.incrBy(r2WithKey);
        AbstractResource oracleWithKey = new ITotalResource(6, "key");
        assertEquals(oracleWithKey, resultWithKey);
    }

    /**
     * Test that we can increment an LTotalResource instance.
     */
    @Test
    public void testLTotalResourceIncr() {
        AbstractResource r1 = new LTotalResource(1);
        AbstractResource r2 = new LTotalResource(5);
        AbstractResource result = r1.incrBy(r2);
        AbstractResource oracle = new LTotalResource(6);
        assertEquals(oracle, result);

        AbstractResource r1WithKey = new LTotalResource(1, "key");
        AbstractResource r2WithKey = new LTotalResource(5, "key");
        AbstractResource resultWithKey = r1WithKey.incrBy(r2WithKey);
        AbstractResource oracleWithKey = new LTotalResource(6, "key");
        assertEquals(oracleWithKey, resultWithKey);
    }

    /**
     * Vector timestamps do not support incrementing.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testExceptionVectorTimeIncr() {
        VectorTime v1 = new VectorTime(1);
        VectorTime v2 = new VectorTime(2);
        v1.incrBy(v2);
    }

    /**
     * Test equals and hashcode for equal resources.
     */
    @Test
    public void testEqualsHashcodeEqualResources() {
        AbstractResource r1;
        AbstractResource r2;

        r1 = new DTotalResource(4);
        r2 = new DTotalResource(4);
        assertEquals(r1.hashCode(), r2.hashCode());
        assertTrue(r1.equals(r2));

        r1 = new ITotalResource(4);
        r2 = new ITotalResource(4);
        assertEquals(r1.hashCode(), r2.hashCode());
        assertTrue(r1.equals(r2));

        r1 = new LTotalResource(4);
        r2 = new LTotalResource(4);
        assertEquals(r1.hashCode(), r2.hashCode());
        assertTrue(r1.equals(r2));
    }

    /**
     * Test equals and hashcode for equal resources with resource key.
     */
    @Test
    public void testEqualsHashcodeEqualResourcesWithKey() {
        AbstractResource r1;
        AbstractResource r2;

        r1 = new DTotalResource(4, "key");
        r2 = new DTotalResource(4, "key");
        assertEquals(r1.hashCode(), r2.hashCode());
        assertTrue(r1.equals(r2));

        r1 = new ITotalResource(4, "key");
        r2 = new ITotalResource(4, "key");
        assertEquals(r1.hashCode(), r2.hashCode());
        assertTrue(r1.equals(r2));

        r1 = new LTotalResource(4, "key");
        r2 = new LTotalResource(4, "key");
        assertEquals(r1.hashCode(), r2.hashCode());
        assertTrue(r1.equals(r2));
    }

    /**
     * Test equals and hashcode for unequal resources.
     */
    @Test
    public void testEqualsHashcodeUnequalResources() {
        AbstractResource r1;
        AbstractResource r2;

        r1 = new DTotalResource(4);
        r2 = new DTotalResource(2);
        assertTrue(r1.hashCode() != r2.hashCode());
        assertFalse(r1.equals(r2));

        r1 = new ITotalResource(4);
        r2 = new ITotalResource(2);
        assertTrue(r1.hashCode() != r2.hashCode());
        assertFalse(r1.equals(r2));

        r1 = new LTotalResource(4);
        r2 = new LTotalResource(2);
        assertTrue(r1.hashCode() != r2.hashCode());
        assertFalse(r1.equals(r2));

        r1 = new LTotalResource(4);
        r2 = new DTotalResource(4);
        assertTrue(r1.hashCode() != r2.hashCode());
        assertFalse(r1.equals(r2));
    }

    /**
     * Test equals and hashcode for unequal resources with resource key.
     */
    @Test
    public void testEqualsHashcodeUnequalResourcesWithKey() {
        AbstractResource r1;
        AbstractResource r2;

        r1 = new DTotalResource(4, "key");
        r2 = new DTotalResource(4, "notkey");
        assertTrue(r1.hashCode() != r2.hashCode());
        assertFalse(r1.equals(r2));

        r1 = new ITotalResource(4, "key");
        r2 = new ITotalResource(2, "key");
        assertTrue(r1.hashCode() != r2.hashCode());
        assertFalse(r1.equals(r2));

        r1 = new LTotalResource(4, "key");
        r2 = new DTotalResource(4, "key");
        assertTrue(r1.hashCode() != r2.hashCode());
        assertFalse(r1.equals(r2));

        r1 = new LTotalResource(4, "key");
        r2 = new LTotalResource(4);
        assertTrue(r1.hashCode() != r2.hashCode());
        assertFalse(r1.equals(r2));
    }

    @Test
    public void testIncompatibleResourceOperation() {
        AbstractResource r1;
        AbstractResource r2;

        r1 = new DTotalResource(4, "key");
        r2 = new DTotalResource(4, "notkey");
        try {
            r1.compareTo(r2);
            fail("NonComparableResourceException expected");
        } catch (NonComparableResourceException e) {
            // Success
        }

        r1 = new DTotalResource(4, "key");
        r2 = new DTotalResource(4);
        try {
            r1.computeDelta(r2);
            fail("NonComparableResourceException expected");
        } catch (NonComparableResourceException e) {
            // Success
        }

        r1 = new DTotalResource(4, "key");
        r2 = new ITotalResource(4, "key");
        try {
            r1.incrBy(r2);
            fail("NonComparableResourceException expected");
        } catch (NonComparableResourceException e) {
            // Success
        }

        r1 = new DTotalResource(4);
        r2 = new ITotalResource(4);
        try {
            r1.compareTo(r2);
            fail("NonComparableResourceException expected");
        } catch (NonComparableResourceException e) {
            // Success
        }
    }
}
