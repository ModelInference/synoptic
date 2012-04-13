package synoptic.tests.units;

import static org.junit.Assert.*;

import org.junit.*;

import synoptic.model.Partition;
import synoptic.model.Transition;
import synoptic.util.time.ITotalTime;

// Tests for verifying proper state when adding/mutating the innards of the
// Transition object (relating to the Delta(s)).
/*
 * The state should adhere to where there is no instance of ITimeSeries at the same
 * time as an inner ITime delta object.
 */
public class TransitionDeltaTests {

    private Transition<Partition> sTrans;

    @Before
    public void createTransitions() {
        sTrans = new Transition<Partition>(null, null, "");
    }

    // Is a delta series created on call?
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
        assertEquals((ITotalTime) sTrans.getDeltaSeries().computeMode(),
                new ITotalTime(1));
    }
}
