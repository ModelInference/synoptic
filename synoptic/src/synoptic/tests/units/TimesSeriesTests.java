package synoptic.tests.units;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import synoptic.tests.SynopticTest;
import synoptic.util.time.DTotalTime;
import synoptic.util.time.ITotalTime;
import synoptic.util.time.TimeSeries;

public class TimesSeriesTests extends SynopticTest {

    private TimeSeries<ITotalTime> times;

    @Before
    public void resettimes() {
        times = new TimeSeries<ITotalTime>();
    }

    // Does it compute the mode with only one value?
    @Test
    public void modeDeltaTestOneValue() {
        times.addDelta(new ITotalTime(1));
        assertEquals(new ITotalTime(1), times.computeMode());
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
        times.addDelta(new ITotalTime(1));
        times.addDelta(new ITotalTime(8));
        times.addDelta(new ITotalTime(2));
        times.addDelta(new ITotalTime(1));
        times.addDelta(new ITotalTime(8));
        times.addDelta(new ITotalTime(1));
        times.addDelta(new ITotalTime(16));
        times.addDelta(new ITotalTime(16));
        times.addDelta(new ITotalTime(1));
        times.addDelta(new ITotalTime(8));
        times.addDelta(new ITotalTime(1));
        assertEquals(new ITotalTime(1), times.computeMode());
    }

    // Does the median work with one value?
    @Test
    public void medianTestOneValue() {
        times.addDelta(new ITotalTime(1));
        assertEquals(new ITotalTime(1), times.computeMedian());
    }

    @Test
    public void medianTestEmpty() {
        assertNull(times.computeMedian());
    }

    @Test
    public void medianTestOddNumberOfValues() {
        for (int i = 1; i <= 5; i++) {
            times.addDelta(new ITotalTime(i));
        }

        assertEquals(new ITotalTime(3), times.computeMedian());
    }

    @Test
    public void medianTestEvenNumberOfValues() {
        TimeSeries<DTotalTime> dtimes = new TimeSeries<DTotalTime>();
        for (int i = 1; i <= 6; i++) {
            dtimes.addDelta(new DTotalTime(i));
        }

        assertEquals(new DTotalTime(3.5), dtimes.computeMedian());
    }

    @Test
    public void meanTestOneValue() {
        times.addDelta(new ITotalTime(1));
        assertEquals(new ITotalTime(1), times.computeMean());
    }

    @Test
    public void meanTestEmpty() {
        assertNull(times.computeMean());
    }

    @Test
    public void meanTestManyValues() {
        times.addDelta(new ITotalTime(1));
        times.addDelta(new ITotalTime(2));
        times.addDelta(new ITotalTime(5));
        times.addDelta(new ITotalTime(8));
        assertEquals(new ITotalTime(4), times.computeMean());
    }
}
