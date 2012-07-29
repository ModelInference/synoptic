package dynoptic.model.fifosys.gfsm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import dynoptic.DynopticTest;

public class GFSMStateTests extends DynopticTest {

    @Override
    public void setUp() {
        //
    }

    @Test
    public void createGFSMState() {
        GFSMState s = new GFSMState();
        assertFalse(s.isAccept());
        assertFalse(s.isAcceptForPid(0));
        assertEquals(s.getTransitioningEvents().size(), 0);
    }

}
