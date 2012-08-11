package dynoptic.model.fifosys.gfsm.observed.dag;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import dynoptic.DynopticTest;
import dynoptic.model.fifosys.gfsm.observed.ObsFSMState;

public class ObsDagTests extends DynopticTest {

    @Test
    public void create() {
        ObsFSMState state0I = ObsFSMState.ObservedInitialFSMState(0);
        ObsFSMState state1I = ObsFSMState.ObservedInitialFSMState(1);
        ObsDAGNode node0I = new ObsDAGNode(state0I);
        ObsDAGNode node1I = new ObsDAGNode(state1I);
        List<ObsDAGNode> initDagConfig = new ArrayList<ObsDAGNode>();
        initDagConfig.add(node0I);
        initDagConfig.add(node1I);

        ObsFSMState state0T = ObsFSMState.ObservedTerminalFSMState(0);
        ObsFSMState state1T = ObsFSMState.ObservedTerminalFSMState(1);
        ObsDAGNode node0T = new ObsDAGNode(state0T);
        ObsDAGNode node1T = new ObsDAGNode(state1T);
        List<ObsDAGNode> termDagConfig = new ArrayList<ObsDAGNode>();
        termDagConfig.add(node0T);
        termDagConfig.add(node1T);

        ObsDAG dag = new ObsDAG(initDagConfig, termDagConfig);
    }

}
