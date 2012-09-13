package dynoptic.model.fifosys.gfsm.observed.dag;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import dynoptic.DynopticTest;
import dynoptic.model.fifosys.gfsm.observed.ObsFSMState;

import synoptic.model.channelid.ChannelId;
import synoptic.model.event.DistEventType;
import synoptic.model.event.Event;

public class ObsDagTests extends DynopticTest {

    public ObsDAG getSimpleDag() {
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

        Event e = new Event(DistEventType.LocalEvent("e", 0));
        Event f = new Event(DistEventType.LocalEvent("f", 1));

        node0I.addTransition(e, node0T);
        node1I.addTransition(f, node1T);

        List<ObsDAGNode> termDagConfig = new ArrayList<ObsDAGNode>();
        termDagConfig.add(node0T);
        termDagConfig.add(node1T);

        List<ChannelId> channelIds = new ArrayList<ChannelId>();
        ObsDAG dag = new ObsDAG(initDagConfig, termDagConfig, channelIds);
        return dag;
    }

    @Test
    public void create() {
        getSimpleDag();
    }

    @Test
    public void getObsFifoSys() {
        ObsDAG dag = getSimpleDag();
        // FIXME
        // ObsFifoSys sys = dag.getObsFifoSys();

        // TODO: check properties of sys.
    }

}
