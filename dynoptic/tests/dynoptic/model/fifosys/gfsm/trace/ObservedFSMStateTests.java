package dynoptic.model.fifosys.gfsm.trace;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import dynoptic.DynopticTest;

public class ObservedFSMStateTests extends DynopticTest {

    @Test
    public void create() {
        ObservedFSMState p;
        p = ObservedFSMState.ObservedIntermediateFSMState(0);
        p = ObservedFSMState.ObservedInitialFSMState(0);
        p = ObservedFSMState.ObservedInitialTerminalFSMState(0);
        p = ObservedFSMState.ObservedTerminalFSMState(0);

        p = ObservedFSMState.ObservedIntermediateFSMState(0, "a");
        p = ObservedFSMState.ObservedInitialFSMState(0, "b");
        p = ObservedFSMState.ObservedInitialTerminalFSMState(0, "b");
        p = ObservedFSMState.ObservedTerminalFSMState(0, "c");

        assertFalse(p.equals(null));
        assertFalse(p.equals(""));
        assertTrue(p.equals(p));

        logger.info(p.toString());
    }
}
