package synoptic.tests.units;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import synoptic.tests.SynopticTest;
import synoptic.util.resource.DTotalResource;
import synoptic.util.resource.ITotalResource;
import synoptic.util.resource.ResourceSeries;

public class TimesSeriesTests extends SynopticTest {

    private ResourceSeries<ITotalResource> times;

    @Before
    public void resettimes() {
        times = new ResourceSeries<ITotalResource>();
    }

    // Does it compute the mode with only one value?
    @Test
    public void modeDeltaTestOneValue() {
        times.addDelta(new ITotalResource(1));
        assertEquals(new ITotalResource(1), times.computeMode());
    }

    // Does it compute the mode with no values?
    @Test
    public void modeDeltaTestEmpty() {
        assertNull(times.computeMode());
    }

    // Does it compute the mode with many arbitrary values
    // (one is the most used).
    @Test
    public void modeDeltaTestManyValues() {
        times.addDelta(new ITotalResource(1));
        times.addDelta(new ITotalResource(8));
        times.addDelta(new ITotalResource(2));
        times.addDelta(new ITotalResource(1));
        times.addDelta(new ITotalResource(8));
        times.addDelta(new ITotalResource(1));
        times.addDelta(new ITotalResource(16));
        times.addDelta(new ITotalResource(16));
        times.addDelta(new ITotalResource(1));
        times.addDelta(new ITotalResource(8));
        times.addDelta(new ITotalResource(1));
        assertEquals(new ITotalResource(1), times.computeMode());
    }

    // Does the median work with one value?
    @Test
    public void medianTestOneValue() {
        times.addDelta(new ITotalResource(1));
        assertEquals(new ITotalResource(1), times.computeMedian());
    }

    @Test
    public void medianTestEmpty() {
        assertNull(times.computeMedian());
    }

    @Test
    public void medianTestOddNumberOfValues() {
        for (int i = 1; i <= 5; i++) {
            times.addDelta(new ITotalResource(i));
        }

        assertEquals(new ITotalResource(3), times.computeMedian());
    }

    @Test
    public void medianTestEvenNumberOfValues() {
        ResourceSeries<DTotalResource> dtimes = new ResourceSeries<DTotalResource>();
        for (int i = 1; i <= 6; i++) {
            dtimes.addDelta(new DTotalResource(i));
        }

        assertEquals(new DTotalResource(3.5), dtimes.computeMedian());
    }

    @Test
    public void meanTestOneValue() {
        times.addDelta(new ITotalResource(1));
        assertEquals(new ITotalResource(1), times.computeMean());
    }

    @Test
    public void meanTestEmpty() {
        assertNull(times.computeMean());
    }

    @Test
    public void meanTestManyValues() {
        times.addDelta(new ITotalResource(1));
        times.addDelta(new ITotalResource(2));
        times.addDelta(new ITotalResource(5));
        times.addDelta(new ITotalResource(8));
        assertEquals(new ITotalResource(4), times.computeMean());
    }
}
