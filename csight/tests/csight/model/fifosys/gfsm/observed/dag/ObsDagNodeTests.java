package csight.model.fifosys.gfsm.observed.dag;

import org.junit.Test;

import csight.CSightTest;
import csight.model.fifosys.gfsm.observed.ObsFSMState;
import csight.model.fifosys.gfsm.observed.dag.ObsDAGNode;

public class ObsDagNodeTests extends CSightTest {

    @Test
    public void create() {
        ObsFSMState state = ObsFSMState.anonObsFSMState(0, false, false);
        ObsDAGNode node = new ObsDAGNode(state);
    }
}
