package dynoptic.model.fifosys.gfsm.observed;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import dynoptic.DynopticTest;

import synoptic.model.event.DistEventType;

public class ObsFSMStateTests extends DynopticTest {
    ObsFSMState p, q, q1, q2, q3;

    @Test
    public void createAnon() {
        p = ObsFSMState.anonObsFSMState(0, false, false);
        assertTrue(p.getPid() == 0);
        assertTrue(p.isAnon);
        assertFalse(p.isInitial());
        assertFalse(p.isTerminal());

        q = ObsFSMState.anonObsFSMState(0, true, false);
        assertFalse(p.equals(q));

        q = ObsFSMState.anonObsFSMState(0, true, true);
        assertFalse(p.equals(q));

        q = ObsFSMState.anonObsFSMState(0, false, true);
        assertFalse(p.equals(q));
    }

    @Test
    public void createNamed() {
        p = ObsFSMState.namedObsFSMState(0, "a", false, false);
        assertTrue(p.getPid() == 0);
        assertFalse(p.isAnon);
        assertFalse(p.isInitial());
        assertFalse(p.isTerminal());
        assertTrue("a".equals(p.getName()));

        q = ObsFSMState.namedObsFSMState(0, "b", true, false);
        assertFalse(p.equals(q));

        q = ObsFSMState.namedObsFSMState(0, "b", true, true);
        assertFalse(p.equals(q));

        q = ObsFSMState.namedObsFSMState(0, "c", false, true);
        assertFalse(p.equals(q));

        assertFalse(p.equals(null));
        assertFalse(p.equals(""));
        assertTrue(p.equals(p));

        logger.info(p.toString());
    }

    @Test
    public void createConsistentAnonInit() {
        p = ObsFSMState.consistentAnonInitObsFSMState(0);
        logger.info(p.toString());
        assertTrue(p.getPid() == 0);
        assertTrue(p.isAnon);
        assertTrue(p.isInitial());
        assertFalse(p.isTerminal());

        q = ObsFSMState.consistentAnonInitObsFSMState(0);
        assertTrue(p.equals(q));

        q = ObsFSMState.consistentAnonInitObsFSMState(1);
        assertFalse(p.equals(q));
    }

    @Test
    public void createConsistentAnonIntermediate() {
        p = ObsFSMState.consistentAnonInitObsFSMState(0);
        DistEventType e = DistEventType.LocalEvent("e", 0);
        // p -> e = q1
        q1 = ObsFSMState.consistentAnonObsFSMState(p, e);
        assertFalse(q1.isInitial());
        assertFalse(q1.isTerminal());
        logger.info(q1.toString());
        assertFalse(p.equals(q1));

        // p -> f = q2
        DistEventType f = DistEventType.LocalEvent("f", 0);
        q2 = ObsFSMState.consistentAnonObsFSMState(p, f);
        assertFalse(q1.equals(q2));

        // p -> e = q3
        DistEventType e_ = DistEventType.LocalEvent("e", 0);
        q3 = ObsFSMState.consistentAnonObsFSMState(p, e_);
        assertTrue(q1.equals(q3));

        q3.markTerm();
        assertTrue(q3.isTerminal());
    }
}
