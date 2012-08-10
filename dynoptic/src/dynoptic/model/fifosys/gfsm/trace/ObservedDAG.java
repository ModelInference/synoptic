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

    public ObservedDAG(List<ObsStateDAGNode> initDagConfig) {
        this.initDagConfig = initDagConfig;
    }

    // //////////////////////////////////////////////////////////////////

    public ObsMultFSMState getMultiFSMStateFromDagConfig(
            List<ObsStateDAGNode> dagConfig) {
        List<ObservedFSMState> fsmStates = new ArrayList<ObservedFSMState>();

        for (ObsStateDAGNode node : dagConfig) {
            fsmStates.add(node.getObsState());
            // Simulate the initial states across all of the processes.
            node.setOccurred(true);
        }

        ObsMultFSMState ret = new ObsMultFSMState(fsmStates);
        return ret;
    }

    /** Creates a Trace FSM from the internal observed DAG. */
    public Trace getGlobalTraceFSM() {
        ObservedFifoSysState initS, termS;

        // TODO: need a MultiChannelState type that is immutable.
        // TODO: need a MultiChannelState constant that is the empty-queues
        // state.

        List<ChannelId> channelIds = new ArrayList<ChannelId>();
        ImmutableMultiChannelState initChStates = ImmutableMultiChannelState
                .fromChannelIds(channelIds);
        termS = ObservedFifoSysState.getFifoSysState(
                getMultiFSMStateFromDagConfig(termDagConfig), initChStates);
        initS = ObservedFifoSysState.getFifoSysState(
                getMultiFSMStateFromDagConfig(initDagConfig), initChStates);

        List<ObsStateDAGNode> curDagConfig = new ArrayList<ObsStateDAGNode>(
                initDagConfig);
        Set<ObsStateDAGNode> enabledNodes = getEnabledNodes(curDagConfig);

        ObservedFifoSysState prevSysState = initS;
        ImmutableMultiChannelState prevChStates = initChStates;

        ObservedFifoSysState nextSysState;
        ImmutableMultiChannelState nextChStates;

        for (ObsStateDAGNode nextNode : enabledNodes) {
            // Retrieve the event that is will cause the transition.
            ObservedEvent e = nextNode.getPrevState().getNextEvent();

            // Create the next set of channel states based off of the previous
            // channel state and the event type.
            nextChStates = prevChStates.getNextChState(e);

            // Update the DAG config by transitioning to the next node.
            curDagConfig.set(nextNode.getPid(), nextNode);

            // Look up/create the next fifo sys state.
            nextSysState = ObservedFifoSysState.getFifoSysState(
                    getMultiFSMStateFromDagConfig(curDagConfig), nextChStates);

            // Update the nextNode as having occurred.
            nextNode.setOccurred(true);

            // Remove the current node we are processing from enabled nodes.
            enabledNodes.remove(nextNode);
            // Add any new nodes that are now enabled.
            enabledNodes.addAll(getEnabledNodes(curDagConfig));

            prevSysState.addTransition(e, nextSysState);
            prevSysState = nextSysState;
            prevChStates = nextChStates;
        }

        // TODO: assert that the queues are completely empty at the terminal
        // state.

        return new Trace(initS, termS);
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
