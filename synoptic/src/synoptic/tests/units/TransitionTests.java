package synoptic.tests.units;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import synoptic.model.EventNode;
import synoptic.model.StringEventType;
import synoptic.model.Transition;
import synoptic.util.time.ITotalTime;

public class TransitionTests {

    private Transition<Integer> trans;

    @Before
    public void resetTrans() {
        trans = new Transition(1, 2, "");
    }

    // Does it compute the mode with only one value?
    @Test
    public void modeDeltaTestOneValue() {
        trans.addDelta(new ITotalTime(1));
        assertEquals(new ITotalTime(1), trans.computeModeDelta());
    }

    // Does it compute the mode with no values?
    @Test
    public void modeDeltaTestEmpty() {
        assertNull(trans.computeModeDelta());
    }

    // Does it compute the mode with many arbitrary values
    // (one is the most used).
    @Test
    public void modeDeltaTestManyValues() {
        trans.addDelta(new ITotalTime(1));
        trans.addDelta(new ITotalTime(8));
        trans.addDelta(new ITotalTime(2));
        trans.addDelta(new ITotalTime(1));
        trans.addDelta(new ITotalTime(8));
        trans.addDelta(new ITotalTime(1));
        trans.addDelta(new ITotalTime(16));
        trans.addDelta(new ITotalTime(16));
        trans.addDelta(new ITotalTime(1));
        trans.addDelta(new ITotalTime(8));
        trans.addDelta(new ITotalTime(1));
        assertEquals(new ITotalTime(1), trans.computeModeDelta());
    }

    // Does the median work with one value?
    @Test
    public void medianTestOneValue() {
        trans.addDelta(new ITotalTime(1));
        assertEquals(new ITotalTime(1), trans.computeMedianDelta());
    }

    @Test
    public void medianTestEmpty() {
        assertNull(trans.computeMedianDelta());
    }

    @Test
    public void medianTestManyValues() {
        for (int i = 1; i <= 5; i++) {
            trans.addDelta(new ITotalTime(i));
        }

        assertEquals(new ITotalTime(3), trans.computeMedianDelta());
    }
}
