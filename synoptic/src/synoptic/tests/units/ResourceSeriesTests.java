package synoptic.tests.units;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import synoptic.tests.SynopticTest;
import synoptic.util.resource.DTotalResource;
import synoptic.util.resource.ITotalResource;
import synoptic.util.resource.ResourceSeries;

public class ResourceSeriesTests extends SynopticTest {

    private ResourceSeries<ITotalResource> resources;

    @Before
    public void resettimes() {
        resources = new ResourceSeries<ITotalResource>();
    }

    // Does it compute the mode with only one value?
    @Test
    public void modeDeltaTestOneValue() {
        resources.addDelta(new ITotalResource(1));
        assertEquals(new ITotalResource(1), resources.computeMode());
    }

    // Does it compute the mode with no values?
    @Test
    public void modeDeltaTestEmpty() {
        assertNull(resources.computeMode());
    }

    // Does it compute the mode with many arbitrary values
    // (one is the most used).
    @Test
    public void modeDeltaTestManyValues() {
        resources.addDelta(new ITotalResource(1));
        resources.addDelta(new ITotalResource(8));
        resources.addDelta(new ITotalResource(2));
        resources.addDelta(new ITotalResource(1));
        resources.addDelta(new ITotalResource(8));
        resources.addDelta(new ITotalResource(1));
        resources.addDelta(new ITotalResource(16));
        resources.addDelta(new ITotalResource(16));
        resources.addDelta(new ITotalResource(1));
        resources.addDelta(new ITotalResource(8));
        resources.addDelta(new ITotalResource(1));
        assertEquals(new ITotalResource(1), resources.computeMode());
    }

    // Does the median work with one value?
    @Test
    public void medianTestOneValue() {
        resources.addDelta(new ITotalResource(1));
        assertEquals(new ITotalResource(1), resources.computeMedian());
    }

    @Test
    public void medianTestEmpty() {
        assertNull(resources.computeMedian());
    }

    @Test
    public void medianTestOddNumberOfValues() {
        for (int i = 1; i <= 5; i++) {
            resources.addDelta(new ITotalResource(i));
        }

        assertEquals(new ITotalResource(3), resources.computeMedian());
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
        resources.addDelta(new ITotalResource(1));
        assertEquals(new ITotalResource(1), resources.computeMean());
    }

    @Test
    public void meanTestEmpty() {
        assertNull(resources.computeMean());
    }

    @Test
    public void meanTestManyValues() {
        resources.addDelta(new ITotalResource(1));
        resources.addDelta(new ITotalResource(2));
        resources.addDelta(new ITotalResource(5));
        resources.addDelta(new ITotalResource(8));
        assertEquals(new ITotalResource(4), resources.computeMean());
    }
}
