package synoptic.tests.units;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import synoptic.model.Transition;
import synoptic.util.time.ITime;
import synoptic.util.time.ITotalTime;
import synoptic.util.time.DTotalTime;

/**
 * Tests for verifying proper state when adding/mutating the innards of the
 * Transition object (relating to the Delta(s)). The state should adhere to
 * where there is no instance of ITimeSeries at the same time as an inner ITime
 * delta object.
 */
public class TransitionDeltaTests {

    private Transition<String> sTrans;

    @Before
    public void createTransitions() {
        sTrans = new Transition<String>("node1", "node2", "");
    }

    // TODO: Is a delta series created on call?
    // is the single delta now null?
    @Test
    public void generateDelta() {
        assertNotNull(sTrans.getDeltaSeries());
        assertNull(sTrans.getDelta());
    }

    @Test
    public void addDeltaToSeries() {
        sTrans.addDelta(new ITotalTime(1));
        assertNotNull(sTrans.getDeltaSeries());
        assertEquals(sTrans.getDeltaSeries().computeMode(),
                new ITotalTime(1));
    }

    // Should not be able to add a delta after the delta
    // has been set.
    @Test (expected = IllegalStateException.class)
    public void stateExceptionOnAddDeltaAfterSet() {
        ITime t = new DTotalTime(2);
        sTrans.setDelta(t);
        sTrans.addDelta(t);
    }

    // Should not be able to set the delta after the
    // ITimeSeries has been initialized.
    @Test (expected = IllegalStateException.class)
    public void stateExceptionOnSetDelta() {
        ITime t = new DTotalTime(2);
        sTrans.addDelta(t);
        sTrans.setDelta(t);
    }
}
