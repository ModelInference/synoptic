package synoptic.tests.units;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;

import org.junit.Test;

import synoptic.tests.SynopticTest;
import synoptic.util.resource.AbstractResource;
import synoptic.util.resource.DTotalResource;
import synoptic.util.resource.ITotalResource;
import synoptic.util.resource.LTotalTime;
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
        DTotalResource d1 = new DTotalResource(2.0);
        DTotalResource d2 = new DTotalResource(1.0);
        AbstractResource delta = d1.computeDelta(d2);
        assertEquals(new DTotalResource(1.0), delta);
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
    public void testDivisionITotalTIme() {
        AbstractResource t1 = new ITotalResource(10);
        AbstractResource t2 = t1.divBy(2);
        AbstractResource oracle = new ITotalResource(5);
        assertEquals(oracle, t2);
    }

    /**
     * Test that we can divide an LTotalTime instance.
     */
    @Test
    public void testDivisionLTotalTIme() {
        AbstractResource t1 = new LTotalTime(10);
        AbstractResource t2 = t1.divBy(2);
        AbstractResource oracle = new LTotalTime(5);
        assertEquals(oracle, t2);
    }

    /**
     * Test that we can divide a DTotalTime instance.
     */
    @Test
    public void testDivisionDTotalTime() {
        AbstractResource t1 = new DTotalResource(10);
        AbstractResource t2 = t1.divBy(2);
        AbstractResource oracle = new DTotalResource(5);
        assertEquals(oracle, t2);
    }

    /**
     * Test that we can increment an ITotalTime instance.
     */
    @Test
    public void testITotalTimeIncr() {
        AbstractResource t1 = new ITotalResource(1);
        AbstractResource t2 = new ITotalResource(5);
        AbstractResource result = t1.incrBy(t2);
        AbstractResource oracle = new ITotalResource(6);
        assertEquals(oracle, result);
    }

    /**
     * Test that we can increment an LTotalTime instance.
     */
    @Test
    public void testLTotalTimeIncr() {
        AbstractResource t1 = new LTotalTime(1);
        AbstractResource t2 = new LTotalTime(5);
        AbstractResource result = t1.incrBy(t2);
        AbstractResource oracle = new LTotalTime(6);
        assertEquals(oracle, result);
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
}
