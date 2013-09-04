package dynoptic.model.fifosys.gfsm.observed;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import dynoptic.DynopticTest;
import dynoptic.util.Util;

public class ObsMultFSMStateTests extends DynopticTest {
    ObsMultFSMState p, q;

    @Test
    public void create() {
        List<ObsFSMState> P = Util.newList();
        ObsFSMState p0 = ObsFSMState.namedObsFSMState(0, "p", false, true);
        ObsFSMState p1 = ObsFSMState.namedObsFSMState(1, "q", false, true);
        P.add(p0);
        P.add(p1);

        p = ObsMultFSMState.getMultiFSMState(P);
        assertFalse(p.equals(null));
        assertTrue(p.isAccept());
        assertTrue(p.isAcceptForPid(0));
        assertTrue(p.isAcceptForPid(1));
        assertFalse(p.isInitial());
        assertFalse(p.isInitForPid(0));
        assertFalse(p.isInitForPid(1));
        assertTrue(p.getNumProcesses() == 2);
        logger.info(p.toString());

        q = ObsMultFSMState.getMultiFSMState(P);
        assertTrue(p == q);
        assertTrue(p.equals(q));
    }

}
