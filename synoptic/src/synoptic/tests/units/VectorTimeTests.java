package synoptic.tests.units;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import synoptic.tests.SynopticTest;
import synoptic.util.NotComparableVectorsException;
import synoptic.util.VectorTime;

/**
 * Tests for synoptic.model.input.VectorTime class.
 * 
 * @author ivan
 */
public class VectorTimeTests extends SynopticTest {

    /**
     * Make sure we can create vector time objects without errors.
     */
    @Test
    public void constructorNoExceptionsTest() {
        new VectorTime("1,2,3");
        new VectorTime(Arrays.asList(new Integer[] { 1, 2, 3 }));
    }

    /**
     * Make sure that vector time construction fails if a time value at some
     * index is a negative integer.
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructorWithException1Test() {
        new VectorTime("-1,2,3");
    }

    /**
     * Make sure that the negative integer error also stops us when we construct
     * a vector time from a list.
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructorWithException2Test() {
        new VectorTime(Arrays.asList(new Integer[] { 1, -2, 3 }));
    }

    /**
     * Test various equality permutations.
     */
    @Test
    public void equalityTest() {
        VectorTime v1, v2, v3;

        v1 = new VectorTime("1,2,3");
        v2 = new VectorTime(Arrays.asList(new Integer[] { 1, 2, 3 }));
        assertTrue(v1.equals(v1));
        assertTrue(v1.equals(v2));

        assertFalse(v1.equals("1"));
        assertFalse(v1.equals(null));

        v3 = new VectorTime(Arrays.asList(new Integer[] { 1, 2, 4 }));
        assertFalse(v1.equals(v3));
        assertFalse(v3.equals(v1));
    }

    /**
     * Vectors of different length cannot be compared -- this throws an
     * exception.
     */
    @Test(expected = NotComparableVectorsException.class)
    public void equalityDiffLengthVectorsTest() {
        VectorTime v1, v2;

        v1 = new VectorTime("1,2,3");

        v2 = new VectorTime(Arrays.asList(new Integer[] { 1, 2, 3, 5 }));
        v1.equals(v2);
    }

    /**
     * Test the step() method.
     */
    @Test
    public void stepTest() {
        VectorTime v1, v2;
        v1 = new VectorTime("1,2,3");
        v2 = new VectorTime("1,2,4");

        assertFalse(v1.equals(v2));
        v1 = v1.step(2);
        assertTrue(v1.equals(v2));
    }

    /**
     * Test the isSingular() method.
     */
    @Test
    public void isSingularTest() {
        VectorTime v1, v2;
        v1 = new VectorTime("1,2,3");
        v2 = new VectorTime("1");
        assertFalse(v1.isSingular());
        assertTrue(v2.isSingular());
    }

    /**
     * Test the isUnitVector() method.
     */
    @Test
    public void isUnitVectorTest() {
        VectorTime v1;
        v1 = new VectorTime("0");
        assertFalse(v1.isUnitVector());
        v1 = new VectorTime("0,1,0");
        assertTrue(v1.isUnitVector());
        v1 = new VectorTime("1");
        assertTrue(v1.isUnitVector());
        v1 = new VectorTime("0,1,1");
        assertFalse(v1.isUnitVector());
        v1 = new VectorTime("2");
        assertFalse(v1.isUnitVector());
        v1 = new VectorTime("0,0,2");
        assertFalse(v1.isUnitVector());
        v1 = new VectorTime("1,2,3");
        assertFalse(v1.isUnitVector());
    }

    /**
     * Test the lessThan() method.
     */
    @Test
    public void lessThanTest() {
        VectorTime v1, v2;
        // Vectors of same lengths.
        v1 = new VectorTime("1,2,3");
        v2 = new VectorTime("1,2,4");
        assertTrue(v1.lessThan(v2));
        assertFalse(v2.lessThan(v1));

        v2 = new VectorTime("1,2,3");
        assertFalse(v1.lessThan(v2));
        assertFalse(v2.lessThan(v1));
    }

    /**
     * Vectors of different length cannot be compared -- this throws an
     * exception.
     */
    @Test(expected = NotComparableVectorsException.class)
    public void lessThanDiffLengthVectorsTest() {
        VectorTime v1, v2;
        v1 = new VectorTime("1,2,3,0");
        v2 = new VectorTime("1,2,4");
        v1.lessThan(v2);
    }

    /**
     * Test the toString() method.
     */
    @Test
    public void toStringTest() {
        VectorTime v1;
        v1 = new VectorTime("1,2,3,1");
        String s = v1.toString();
        assertEquals("[1, 2, 3, 1]", s);
    }

    /**
     * Test the hashCode() method.
     */
    @Test
    public void hashCodeTest() {
        VectorTime v1, v2;
        v1 = new VectorTime("1,2,3");
        v2 = new VectorTime("1,2,4");

        assertTrue(v1.hashCode() == v1.hashCode());
        assertTrue(v1.hashCode() != v2.hashCode());
    }
}
