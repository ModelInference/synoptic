package dynoptic.model.fifosys.gfsm.observed;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import dynoptic.DynopticTest;

public class ObsFSMStateTests extends DynopticTest {

    @Test
    public void create() {
        ObsFSMState p;
        p = ObsFSMState.anonObsFSMState(0, false, false);
        p = ObsFSMState.anonObsFSMState(0, true, false);
        p = ObsFSMState.anonObsFSMState(0, true, true);
        p = ObsFSMState.anonObsFSMState(0, false, true);

        p = ObsFSMState.namedObsFSMState(0, "a", false, false);
        p = ObsFSMState.namedObsFSMState(0, "b", true, false);
        p = ObsFSMState.namedObsFSMState(0, "b", true, true);
        p = ObsFSMState.namedObsFSMState(0, "c", false, true);

        assertFalse(p.equals(null));
        assertFalse(p.equals(""));
        assertTrue(p.equals(p));

        logger.info(p.toString());
    }
}
