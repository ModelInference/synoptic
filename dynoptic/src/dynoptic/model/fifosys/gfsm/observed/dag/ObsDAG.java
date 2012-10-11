package dynoptic.model.fifosys.gfsm.observed.dag;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import dynoptic.model.fifosys.channel.channelstate.ImmutableMultiChState;
import dynoptic.model.fifosys.gfsm.observed.ObsFSMState;
import dynoptic.model.fifosys.gfsm.observed.ObsMultFSMState;
import dynoptic.model.fifosys.gfsm.observed.fifosys.ObsFifoSys;
import dynoptic.model.fifosys.gfsm.observed.fifosys.ObsFifoSysState;

import synoptic.model.channelid.ChannelId;
import synoptic.model.event.DistEventType;
import synoptic.model.event.Event;

/**
 * Maintains pointers to the set of initial DAG nodes and implements conversion
 * from ObsDAG FSM to an ObsFifoSys.
 */
public class ObsDAG {

    // Ordered list of initial (root) DAG nodes, ordered by process id.
    List<ObsDAGNode> initDagConfig;
    // Ordered list of terminal (leaf) DAG nodes, ordered by process id.
    List<ObsDAGNode> termDagConfig;
    // The channel ids of the system that generated this DAG execution.
    List<ChannelId> channelIds;

    public ObsDAG(List<ObsDAGNode> initDagConfig,
            List<ObsDAGNode> termDagConfig, List<ChannelId> channelIds) {
        assert initDagConfig != null;
        assert termDagConfig != null;
        assert channelIds != null;

        assert initDagConfig.size() > 0;
        assert termDagConfig.size() > 0;

        this.initDagConfig = initDagConfig;
        this.termDagConfig = termDagConfig;
        this.channelIds = channelIds;
    }

    // //////////////////////////////////////////////////////////////////

    /** Creates an observed fifo system from the observed DAG. */
    public ObsFifoSys getObsFifoSys() {
        ObsFifoSysState initS, termS;

        ImmutableMultiChState emptyChannelsState = ImmutableMultiChState
                .fromChannelIds(channelIds);
        initS = ObsFifoSysState.getFifoSysState(
                fsmStatesFromDagConfig(initDagConfig), emptyChannelsState);
        termS = ObsFifoSysState.getFifoSysState(
                fsmStatesFromDagConfig(termDagConfig), emptyChannelsState);

        // This will keep track of all states we've created thus far.
        Set<ObsFifoSysState> fifoStates = new LinkedHashSet<ObsFifoSysState>();
        fifoStates.add(initS);
        fifoStates.add(termS);

        // Mark all the nodes in the initial config as having occurred.
        for (ObsDAGNode node : initDagConfig) {
            node.setOccurred(true);
        }

        // Copy the initDagConfig into curDagconfig, since we will be modifying
        // this config to track where we are in the space of possible DAG
        // configurations.
        List<ObsDAGNode> currDagConfig = new ArrayList<ObsDAGNode>(
                initDagConfig);

        // Iterate through all the nodes enabled from the current configuration,
        // and explore each of them DFS-style.
        for (ObsDAGNode nextDAGNode : getEnabledNodes(currDagConfig)) {
            exploreNextDagNode(currDagConfig, initS, emptyChannelsState,
                    nextDAGNode, fifoStates);
        }

        // Cleanup: mark all nodes in the init config as _not_ having occurred.
        for (ObsDAGNode node : initDagConfig) {
            node.setOccurred(false);
        }

        return new ObsFifoSys(channelIds, initS, termS, fifoStates);
    }

    // //////////////////////////////////////////////////////////////////

    /**
     * A recursive DFS exploration of the DAG structure, all the while building
     * up the FIFO system states based on the execution paths we explore.
     * 
     * @param curDagConfig
     * @param currSysState
     * @param currChStates
     * @param nextNode
     * @param states
     */
    private void exploreNextDagNode(List<ObsDAGNode> curDagConfig,
            ObsFifoSysState currSysState, ImmutableMultiChState currChStates,
            ObsDAGNode nextNode, Set<ObsFifoSysState> states) {

        // Retrieve the event that will cause the transition.
        Event e = nextNode.getPrevState().getNextEvent();

        // Create the next set of channel states based off of the previous
        // channel state and the event type.
        ImmutableMultiChState nextChStates = currChStates
                .getNextChState((DistEventType) e.getEType());

        // Update the DAG config by transitioning to the next node.
        curDagConfig.set(nextNode.getPid(), nextNode);

        // Look up/create the next FIFO sys state.
        ObsFifoSysState nextSysState = ObsFifoSysState.getFifoSysState(
                fsmStatesFromDagConfig(curDagConfig), nextChStates);
        states.add(nextSysState);

        // Add a transition between the FIFO states.
        DistEventType eType = (DistEventType) e.getEType();

        // currSysState might already have a transition e if we are maintaining
        // only 1 ObsFifoSys.
        if (!currSysState.getTransitioningEvents().contains(eType)) {
            currSysState.addTransition(eType, nextSysState);
        } else {
            // Make sure that the state we're transitioning to already is the
            // one we are supposed to be transitioning to according to the
            // current traversal.
            assert currSysState.getNextState(eType).equals(nextSysState);
        }

        // Update the nextNode as having occurred.
        nextNode.setOccurred(true);

        // Iterate through all the nodes enabled from this configuration, and
        // transition to them.
        for (ObsDAGNode nextNextNode : getEnabledNodes(curDagConfig)) {
            exploreNextDagNode(curDagConfig, nextSysState, nextChStates,
                    nextNextNode, states);
        }

        // Revert the nextNode to not having occurred.
        nextNode.setOccurred(false);

        // Revert the DAG config to previous node (at the nextNode process).
        curDagConfig.set(nextNode.getPid(), nextNode.getPrevState());
    }

    /**
     * Takes a list of DAG nodes and returns a list of corresponding ObsFSMState
     * instances.
     * 
     * @param dagConfig
     * @return
     */
    private ObsMultFSMState fsmStatesFromDagConfig(List<ObsDAGNode> dagConfig) {
        List<ObsFSMState> fsmStates = new ArrayList<ObsFSMState>();

        for (ObsDAGNode node : dagConfig) {
            fsmStates.add(node.getObsState());
        }

        ObsMultFSMState ret = new ObsMultFSMState(fsmStates);
        return ret;
    }

    /**
     * Returns the set of DAG nodes that can be "executed" next based on the
     * current configuration -- basically determines which next nodes from the
     * current configuration are "enabled" or had their dependencies satisfied.
     * 
     * @param curConfig
     * @return
     */
    private Set<ObsDAGNode> getEnabledNodes(List<ObsDAGNode> curConfig) {
        Set<ObsDAGNode> ret = new LinkedHashSet<ObsDAGNode>();
        for (ObsDAGNode node : curConfig) {
            if (!node.isTermState() && node.getNextState().isEnabled()) {
                ret.add(node.getNextState());
            }
        }
        return ret;
    }
}
