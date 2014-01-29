package dynoptic.model.fifosys.gfsm.observed.dag;

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import dynoptic.model.fifosys.channel.channelstate.ImmutableMultiChState;
import dynoptic.model.fifosys.gfsm.observed.ObsDistEventType;
import dynoptic.model.fifosys.gfsm.observed.ObsFSMState;
import dynoptic.model.fifosys.gfsm.observed.ObsMultFSMState;
import dynoptic.model.fifosys.gfsm.observed.fifosys.ObsFifoSys;
import dynoptic.model.fifosys.gfsm.observed.fifosys.ObsFifoSysState;
import dynoptic.util.Util;

import synoptic.model.channelid.ChannelId;
import synoptic.model.event.DistEventType;
import synoptic.model.event.Event;

/**
 * Maintains pointers to the set of initial DAG nodes and implements conversion
 * from ObsDAG FSM to an ObsFifoSys.
 */
public class ObsDAG {

    private static Logger logger = Logger.getLogger("ObsDAG");

    // Ordered list of initial (root) DAG nodes, ordered by process id.
    private final List<ObsDAGNode> initDagConfig;
    // Ordered list of terminal (leaf) DAG nodes, ordered by process id.
    private final List<ObsDAGNode> termDagConfig;
    // The channel ids of the system that generated this DAG execution.
    List<ChannelId> channelIds;

    private final ObsFifoSysState initS, termS;
    private final ImmutableMultiChState emptyChannelsState;

    private final int traceId;

    public ObsDAG(List<ObsDAGNode> initDagConfig,
            List<ObsDAGNode> termDagConfig, List<ChannelId> channelIds,
            int traceId) {
        assert initDagConfig != null;
        assert termDagConfig != null;
        assert channelIds != null;

        assert !initDagConfig.isEmpty();
        assert !termDagConfig.isEmpty();

        this.initDagConfig = initDagConfig;
        this.termDagConfig = termDagConfig;
        this.channelIds = channelIds;
        this.traceId = traceId;

        emptyChannelsState = ImmutableMultiChState.fromChannelIds(channelIds);

        initS = ObsFifoSysState.getFifoSysState(
                fsmStatesFromDagConfig(initDagConfig), emptyChannelsState);
        termS = ObsFifoSysState.getFifoSysState(
                fsmStatesFromDagConfig(termDagConfig), emptyChannelsState);
    }

    // //////////////////////////////////////////////////////////////////

    public ObsFifoSysState getInitFifoSysState() {
        return initS;
    }

    public ObsFifoSysState getTermFifoSysState() {
        return termS;
    }

    /**
     * Performs a DFS exploration to generated ObsFifoSysState instances and
     * returns the set of these. NOTE: this set does _not_ include the initial
     * and terminal instances (initS, termS).
     */
    public Set<ObsFifoSysState> genFifoStates() {
        // This will keep track of all states we've created thus far.
        Set<ObsFifoSysState> fifoStates = Util.newSet();

        // Mark all the nodes in the initial config as having occurred.
        for (ObsDAGNode node : initDagConfig) {
            node.setOccurred(true);
        }

        // Copy the initDagConfig into curDagconfig, since we will be modifying
        // this config to track where we are in the space of possible DAG
        // configurations.
        List<ObsDAGNode> currDagConfig = Util.newList(initDagConfig);

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
        return fifoStates;
    }

    /** Creates an observed fifo system from the observed DAG. */
    public ObsFifoSys getObsFifoSys() {
        Set<ObsFifoSysState> fifoStates = genFifoStates();
        fifoStates.add(initS);
        fifoStates.add(termS);
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

        DistEventType eType = (DistEventType) e.getEType();

        // Create the next set of channel states based off of the previous
        // channel state and the event type.
        ImmutableMultiChState nextChStates = currChStates.getNextChState(eType);

        // Update the DAG config by transitioning to the next node.
        curDagConfig.set(nextNode.getPid(), nextNode);

        // Look up/create the next FIFO sys state.
        ObsFifoSysState nextSysState = ObsFifoSysState.getFifoSysState(
                fsmStatesFromDagConfig(curDagConfig), nextChStates);
        // This boolean determines if we've already visited this state during
        // _this_ traceid exploration. This is independent of other traces.
        boolean nextSysStatePreviouslyExplored = false;
        if (states.contains(nextSysState)) {
            nextSysStatePreviouslyExplored = true;
        } else {
            states.add(nextSysState);
        }

        // Add a transition between the FIFO states, if this kind of
        // transitioning event type has not been observed previously.

        // currSysState might already have a transition e if we are maintaining
        // only 1 ObsFifoSys.
        ObsDistEventType existingTxn = currSysState
                .getObsTransitionByEType(eType);

        if (existingTxn == null) {
            // NOTE: we do not want to cache the ObsDistEventType, since each
            // edge must have its own instance.
            ObsDistEventType obsEType = new ObsDistEventType(eType, traceId);
            currSysState.addTransition(obsEType, nextSysState);
        } else {
            // 1. Make sure that the state we're transitioning to already is the
            // one we are supposed to be transitioning to according to the
            // current traversal.
            assert currSysState.getNextState(existingTxn).equals(nextSysState);

            // 2. Merge in the trace-ids of the observed event instances that
            // generated the transition.
            existingTxn.addTraceId(traceId);
        }

        if (nextSysStatePreviouslyExplored) {
            // Optimization to not re-explore previous fifo states.
            curDagConfig.set(nextNode.getPid(), nextNode.getPrevState());
            return;
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
        List<ObsFSMState> fsmStates = Util.newList();

        int pid = 0;
        for (ObsDAGNode node : dagConfig) {
            ObsFSMState s = node.getObsState();
            assert (s.getPid() == pid);
            fsmStates.add(s);
            pid += 1;
        }

        ObsMultFSMState ret = ObsMultFSMState.getMultiFSMState(fsmStates);
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
        Set<ObsDAGNode> ret = Util.newSet();
        for (ObsDAGNode node : curConfig) {
            if (node.getNextState() != null && node.getNextState().isEnabled()) {
                ret.add(node.getNextState());
            }
        }
        return ret;
    }
}
