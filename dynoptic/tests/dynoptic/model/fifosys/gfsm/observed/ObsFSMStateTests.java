package dynoptic.model.fifosys.gfsm.observed;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import dynoptic.DynopticTest;
import dynoptic.model.fifosys.gfsm.observed.ObsFSMState;

public class ObsFSMStateTests extends DynopticTest {

    @Test
    public void create() {
        ObsFSMState p;
        p = ObsFSMState.ObservedIntermediateFSMState(0);
        p = ObsFSMState.ObservedInitialFSMState(0);
        p = ObsFSMState.ObservedInitialTerminalFSMState(0);
        p = ObsFSMState.ObservedTerminalFSMState(0);

        p = ObsFSMState.ObservedIntermediateFSMState(0, "a");
        p = ObsFSMState.ObservedInitialFSMState(0, "b");
        p = ObsFSMState.ObservedInitialTerminalFSMState(0, "b");
        p = ObsFSMState.ObservedTerminalFSMState(0, "c");

        assertFalse(p.equals(null));
        assertFalse(p.equals(""));
        assertTrue(p.equals(p));

        logger.info(p.toString());
    }
}
