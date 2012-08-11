package dynoptic.model.fifosys.gfsm.trace;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import dynoptic.model.fifosys.channel.ChannelId;
import dynoptic.model.fifosys.channel.ImmutableMultiChannelState;

/**
 * Maintains pointers to the set of initial DAG nodes and implements conversion
 * from ObservedDAG FSM to a Trace FSM.
 */
public class ObservedDAG {

    // Ordered list of initial (root) DAG nodes, ordered by process id.
    List<ObsStateDAGNode> initDagConfig;
    // Ordered list of terminal (leaf) DAG nodes, ordered by process id.
    List<ObsStateDAGNode> termDagConfig;

    public ObservedDAG(List<ObsStateDAGNode> initDagConfig,
            List<ObsStateDAGNode> termDagConfig) {
        this.initDagConfig = initDagConfig;
        this.termDagConfig = termDagConfig;
    }

    // //////////////////////////////////////////////////////////////////

    /** Creates a Trace FSM from the internal observed DAG. */
    public TraceFSM getGlobalTraceFSM() {
        ObservedFifoSysState initS, termS;

        List<ChannelId> channelIds = new ArrayList<ChannelId>();
        ImmutableMultiChannelState initChStates = ImmutableMultiChannelState
                .fromChannelIds(channelIds);
        termS = ObservedFifoSysState.getFifoSysState(
                fsmStatesFromDagConfig(termDagConfig), initChStates);
        initS = ObservedFifoSysState.getFifoSysState(
                fsmStatesFromDagConfig(initDagConfig), initChStates);

        // Copy the initDagConfig into curDagconfig, since we will be modifying
        // this config to tack where we are in the space of possible DAG
        // configurations.
        List<ObsStateDAGNode> initDagConfigCopy = new ArrayList<ObsStateDAGNode>(
                initDagConfig);

        // This will keep track of all states we've created thus far.
        Set<ObservedFifoSysState> states = new LinkedHashSet<ObservedFifoSysState>();
        states.add(initS);
        states.add(termS);

        // Mark all the nodes in the initial config as having occurred.
        for (ObsStateDAGNode node : initDagConfigCopy) {
            node.setOccurred(true);
        }

        // Iterate through all enabled nodes, and explore them in DFS-style.
        for (ObsStateDAGNode nextNode : getEnabledNodes(initDagConfigCopy)) {
            exploreNextDagNode(initDagConfigCopy, initS, initChStates,
                    nextNode, states);
        }

        // Cleanup: mark all nodes in the init config as _not_ having occurred.
        for (ObsStateDAGNode node : initDagConfigCopy) {
            node.setOccurred(false);
        }

        return new TraceFSM(initS.getChannelIds(), initS, termS, states);
    }

    /**
     * A DFS exploration of the DAG structure, all the while building up the
     * FIFO system states based on the execution paths.
     * 
     * @param curDagConfig
     * @param currSysState
     * @param currChStates
     * @param nextNode
     * @param states
     */
    public void exploreNextDagNode(List<ObsStateDAGNode> curDagConfig,
            ObservedFifoSysState currSysState,
            ImmutableMultiChannelState currChStates, ObsStateDAGNode nextNode,
            Set<ObservedFifoSysState> states) {

        // Retrieve the event that will cause the transition.
        ObservedEvent e = nextNode.getPrevState().getNextEvent();

        // Create the next set of channel states based off of the previous
        // channel state and the event type.
        ImmutableMultiChannelState nextChStates = currChStates
                .getNextChState(e);

        // Update the DAG config by transitioning to the next node.
        curDagConfig.set(nextNode.getPid(), nextNode);

        // Look up/create the next FIFO sys state.
        ObservedFifoSysState nextSysState = ObservedFifoSysState
                .getFifoSysState(fsmStatesFromDagConfig(curDagConfig),
                        nextChStates);
        states.add(nextSysState);

        // Add a transition between the FIFO states.
        currSysState.addTransition(e, nextSysState);

        // Update the nextNode as having occurred.
        nextNode.setOccurred(true);

        // Iterate through all the nodes enabled from this configuration, and
        // transition to them.
        for (ObsStateDAGNode nextNextNode : getEnabledNodes(curDagConfig)) {
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
    public ObsMultFSMState fsmStatesFromDagConfig(
            List<ObsStateDAGNode> dagConfig) {
        List<ObservedFSMState> fsmStates = new ArrayList<ObservedFSMState>();

        for (ObsStateDAGNode node : dagConfig) {
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
    public Set<ObsStateDAGNode> getEnabledNodes(List<ObsStateDAGNode> curConfig) {
        Set<ObsStateDAGNode> ret = new LinkedHashSet<ObsStateDAGNode>();
        for (ObsStateDAGNode node : curConfig) {
            if (node.getNextState().isEnabled()) {
                ret.add(node);
            }
        }
        return ret;
    }
}
