package dynoptic.model.fifosys.cfsm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import dynoptic.DynopticTest;
import dynoptic.model.fifosys.cfsm.fsm.FSMState;
import dynoptic.util.Util;

import synoptic.model.event.DistEventType;

public class CFSMStateTests extends DynopticTest {

    // Initial state for pid 0.
    FSMState i_0;
    // Non-initial and non-accepting state for pid 0.
    FSMState q_0;
    // Accepting state at pid 1.
    FSMState a_1;
    // Local event at pid 0.
    DistEventType e_0;

    @Override
    public void setUp() {
        i_0 = new FSMState(false, true, 0, 0);
        q_0 = new FSMState(false, false, 0, 1);
        a_1 = new FSMState(true, false, 1, 2);
        e_0 = DistEventType.LocalEvent("e", 0);
    }

    @Test
    public void createCFSMState() {
        CFSMState tmp = new CFSMState(new FSMState(true, false, 0, 0));

        List<FSMState> states = Util.newList();
        states.add(i_0);
        states.add(a_1);
        CFSMState c = new CFSMState(states);

        assertFalse(c.isAccept());
        assertFalse(c.isAcceptForPid(0));
        assertTrue(c.isAcceptForPid(1));

        assertFalse(c.isInitial());
        assertFalse(c.isInitForPid(1));
        assertTrue(c.isInitForPid(0));

        assertEquals(c.getTransitioningEvents().size(), 0);
    }

    @Test
    public void createCFSMStateWithTxns() {
        i_0.addTransition(e_0, q_0);

        List<FSMState> states = Util.newList();
        states.add(i_0);
        states.add(a_1);
        CFSMState c = new CFSMState(states);

        assertEquals(c.getTransitioningEvents().size(), 1);
        assertTrue(c.getTransitioningEvents().contains(e_0));

        // TODO: test c.getNextStates()
    }
}
