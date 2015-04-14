package synoptic.tests.units;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import synoptic.model.EventNode;
import synoptic.model.Transition;
import synoptic.model.event.Event;
import synoptic.util.resource.AbstractResource;
import synoptic.util.resource.ITotalResource;

/**
 * Tests for verifying proper state when adding/mutating the innards of the
 * Transition object (relating to the Delta(s)). The state should adhere to
 * where there is no instance of ITimeSeries at the same time as an inner ITime
 * delta object.
 */
public class TransitionDeltaTests {

    private Transition<EventNode> sTrans;
    private AbstractResource t;

    @Before
    public void createTransitions() {
        t = new ITotalResource(1);
        sTrans = new Transition<EventNode>(new EventNode(new Event("event1")), 
                new EventNode(new Event("event2")), "");
    }

    // TODO: Is a delta series created on call?
    @Test
    public void generateDeltaSeries() {
        assertNotNull(sTrans.getDeltaSeries());
    }

    // is the single delta now null?
    @Test
    public void generateDelta() {
        assertNull(sTrans.getTimeDelta());
    }

    @Test
    public void addDeltaToSeries() {
        sTrans.addTimeDeltaToSeries(t);
        assertNotNull(sTrans.getDeltaSeries());
        assertEquals(sTrans.getDeltaSeries().computeMode(), t);
    }

    // Should not be able to add a delta after the delta
    // has been set.
    @Test(expected = IllegalStateException.class)
    public void stateExceptionOnAddDeltaAfterSet() {
        sTrans.setTimeDelta(t);
        sTrans.addTimeDeltaToSeries(t);
    }

    // Should not be able to set the delta after the
    // ITimeSeries has been initialized.
    @Test(expected = IllegalStateException.class)
    public void stateExceptionOnSetDelta() {
        sTrans.addTimeDeltaToSeries(t);
        sTrans.setTimeDelta(t);
    }

    @Test(expected = IllegalStateException.class)
    public void stateExceptionOnGetDelta() {
        sTrans.addTimeDeltaToSeries(t);
        sTrans.getTimeDelta();
    }

    @Test(expected = IllegalStateException.class)
    public void stateExceptionOnGetDeltaSeries() {
        sTrans.setTimeDelta(t);
        sTrans.getDeltaSeries();
    }
}
