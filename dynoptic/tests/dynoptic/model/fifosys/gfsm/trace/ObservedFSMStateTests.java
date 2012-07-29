package dynoptic.model.fifosys.gfsm.trace;

import org.junit.Test;

import dynoptic.DynopticTest;

public class ObservedFSMStateTests extends DynopticTest {

    @Test
    public void create() {
        ObservedFSMState p;
        p = ObservedFSMState.ObservedIntermediateFSMState(0, null);
        p = ObservedFSMState.ObservedInitialFSMState(0, null);
        p = ObservedFSMState.ObservedTerminalFSMState(0, null);

        p = ObservedFSMState.ObservedIntermediateFSMState(0, "a");
        p = ObservedFSMState.ObservedInitialFSMState(0, "b");
        p = ObservedFSMState.ObservedTerminalFSMState(0, "c");
    }
}
