package dynoptic.model.fifosys.gfsm.observed.dag;

import org.junit.Test;

import dynoptic.DynopticTest;
import dynoptic.model.fifosys.gfsm.observed.ObsFSMState;

public class ObsDagNodeTests extends DynopticTest {

    @Test
    public void create() {
        ObsFSMState state = ObsFSMState.ObservedIntermediateFSMState(0);
        ObsDAGNode node = new ObsDAGNode(state);
    }
}
