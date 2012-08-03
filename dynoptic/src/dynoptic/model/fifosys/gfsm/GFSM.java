package dynoptic.model.fifosys.gfsm;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dynoptic.model.alphabet.EventType;
import dynoptic.model.fifosys.FifoSys;
import dynoptic.model.fifosys.cfsm.CFSM;
import dynoptic.model.fifosys.cfsm.fsm.FSM;
import dynoptic.model.fifosys.cfsm.fsm.FSMState;
import dynoptic.model.fifosys.channel.ChannelId;
import dynoptic.model.fifosys.gfsm.trace.ObservedFifoSysState;
import dynoptic.model.fifosys.gfsm.trace.Trace;

/**
 * <p>
 * A GFSM captures the execution space of a CFSM. We use this model to (1)
 * maintain the observed states/event, and (2) to carry out complex operations
 * like refinement.
 * </p>
 * <p>
 * This model is easier to deal with than a CFSM because it captures all global
 * information in a single place (e.g., all enabled transitions from a single
 * global configuration). A CFSM can be thought of as an abstraction of a GFSM
 * -- a CFSM does not deal with concrete observations. The CFSM model is useful
 * for visualization and for input to the McScM model checker.
 * </p>
 * <p>
 * A GFSM can also be thought of as a representation of the operational
 * semantics, or some number of executions of some hidden/abstract CFSM. Note
 * that although it captures or describes prior executions, it cannot actually
 * be executed or maintain instantaneous execution state -- for this, use
 * FifoSysExecution.
 * </p>
 * <p>
 * A GFSM is composed of GFSMStates, which are actually _partitions_ of the
 * observed global configurations. Refinement causes a re-shuffling of the
 * observations, and new partitions to be created and added to the GFSM.
 * Therefore, a GFSM is highly mutable. Each mutation of the GFSM is a single
 * complete step of the Dynoptic algorithm.
 * </p>
 */
public class GFSM extends FifoSys<GFSMState> {

    /**
     * Creates a new GFSM, using default initial partitioning strategy (by the
     * list of elements at the head of all of the queues in the system), from a
     * list of traces.
     * 
     * @param traces
     * @return
     */
    public GFSM(List<Trace> traces) {
        super(traces.get(0).getNumProcesses(), traces.get(0).getChannelIds());

        Map<Integer, Set<ObservedFifoSysState>> qTopHashToPartition = new LinkedHashMap<Integer, Set<ObservedFifoSysState>>();

        for (Trace t : traces) {
            assert t.getNumProcesses() == numProcesses;
            assert t.getChannelIds().equals(channelIds);

            // DFS traversal to perform initial partitioning.
            ObservedFifoSysState init = t.getInitState();
            addToMap(qTopHashToPartition, init);
            traverseAndPartition(init, qTopHashToPartition);
        }
        // Create the GFSMState partitions based off of sets of observations.
        for (Set<ObservedFifoSysState> set : qTopHashToPartition.values()) {
            states.add(new GFSMState(numProcesses, set));
        }
        recomputeAlphabet();
    }

    /**
     * Constructor helper -- adds an observation to the map, by hashing on its
     * top of queue event types.
     */
    private void addToMap(
            Map<Integer, Set<ObservedFifoSysState>> qTopHashToPartition,
            ObservedFifoSysState obs) {
        int hash = obs.getChannelStates().topOfQueuesHash();
        if (qTopHashToPartition.containsKey(hash)) {
            qTopHashToPartition.get(hash).add(obs);
        } else {
            Set<ObservedFifoSysState> partition = new LinkedHashSet<ObservedFifoSysState>();
            partition.add(obs);
            qTopHashToPartition.put(hash, partition);
        }
    }

    /**
     * Constructor helper -- DFS traversal of the observed traces, building up
     * an initial partitioning.
     */
    private void traverseAndPartition(ObservedFifoSysState curr,
            Map<Integer, Set<ObservedFifoSysState>> qTopHashToPartition) {
        for (ObservedFifoSysState next : curr.getNextStates()) {
            addToMap(qTopHashToPartition, next);
            traverseAndPartition(next, qTopHashToPartition);
        }
    }

    public GFSM(int numProcesses, List<ChannelId> channelIds) {
        super(numProcesses, channelIds);
    }

    // //////////////////////////////////////////////////////////////////

    @Override
    public Set<GFSMState> getInitStates() {
        Set<GFSMState> ret = new LinkedHashSet<GFSMState>();
        for (GFSMState s : states) {
            if (s.isInitial()) {
                ret.add(s);
            }
        }
        return ret;
    }

    @Override
    public Set<GFSMState> getAcceptStates() {
        Set<GFSMState> ret = new LinkedHashSet<GFSMState>();
        for (GFSMState s : states) {
            if (s.isAccept()) {
                ret.add(s);
            }
        }
        return ret;
    }

    // //////////////////////////////////////////////////////////////////

    /** Returns the set of partitions that are accepting for a pid. */
    public Set<GFSMState> getAcceptStatesForPid(int pid) {
        Set<GFSMState> ret = new LinkedHashSet<GFSMState>();
        for (GFSMState s : states) {
            if (s.isAcceptForPid(pid)) {
                ret.add(s);
            }
        }
        return ret;
    }

    /** Returns the set of partitions that are initial for a pid. */
    public Set<GFSMState> getInitStatesForPid(int pid) {
        Set<GFSMState> ret = new LinkedHashSet<GFSMState>();
        for (GFSMState s : states) {
            if (s.isInitForPid(pid)) {
                ret.add(s);
            }
        }
        return ret;
    }

    /** Adds a new partition/state s to this GFSM. */
    public void addAllGFSMStates(Collection<GFSMState> newStates) {
        assert !states.containsAll(newStates);

        states.addAll(newStates);
        recomputeAlphabet();
    }

    /** Adds a new partition/state s to this GFSM. */
    public void addGFSMState(GFSMState s) {
        assert !states.contains(s);

        states.add(s);
        recomputeAlphabet();
    }

    /** Removes the partition/state s from this GFSM. */
    public void removeGFSMState(GFSMState s) {
        assert states.contains(s);

        states.remove(s);
        recomputeAlphabet();
    }

    // //////////////////////////////////////////////////////////////////

    /**
     * Constructs a CFSM from a GFSM. It performs the necessary traversal of the
     * GFSM to construct/specify all the process FSMs that should be part of the
     * CFSM.
     * 
     * @param gfsm
     * @return
     */
    public CFSM getCFSM() {
        Map<GFSMState, FSMState> stateMap = new LinkedHashMap<GFSMState, FSMState>();
        Set<FSMState> initFSMStates = new LinkedHashSet<FSMState>();
        Set<FSMState> acceptFSMStates = new LinkedHashSet<FSMState>();
        Set<GFSMState> visited = new LinkedHashSet<GFSMState>();

        // This is the CFSM that we will return, once we populate it with all
        // the process FSMs.
        CFSM c = new CFSM(numProcesses, channelIds);

        // Create an FSM per pid.
        for (int pid = 0; pid < numProcesses; pid++) {

            // States in each FSM have to be uniquely numbered in the scm
            // output.
            int scmId = 0;

            // Generate the FSM states and inter-state transitions.
            for (GFSMState gInit : getInitStatesForPid(pid)) {
                FSMState fInit;
                if (stateMap.containsKey(gInit)) {
                    fInit = stateMap.get(gInit);
                } else {
                    fInit = new FSMState(gInit.isAcceptForPid(pid), true, pid,
                            scmId);
                    scmId++;
                    stateMap.put(gInit, fInit);
                }
                // We might have visited the current gInit in a prior iteration,
                // from another gInit, in which case we don't need to
                // re-explore.
                if (!visited.contains(gInit)) {
                    scmId = cfsmBuilderVisit(stateMap, gInit, fInit, visited,
                            pid, scmId);
                }
            }

            // Determine the initial/accept FSM states for FSM construction
            // below.
            for (FSMState s : stateMap.values()) {
                if (s.isInitial()) {
                    initFSMStates.add(s);
                }
                if (s.isAccept()) {
                    acceptFSMStates.add(s);
                }
            }

            // Create the FSM for this pid, and add it to the CFSM.
            FSM f = new FSM(pid, initFSMStates, acceptFSMStates,
                    stateMap.values(), scmId);
            c.addFSM(f);

            stateMap.clear();
            initFSMStates.clear();
            acceptFSMStates.clear();
            visited.clear();
        }
        return c;
    }

    /**
     * Depth-first recursive traversal of the GFSM state/transition graph. We
     * back-out when we reach a node that we've visited before. As we traverse,
     * we build up the FSMState states for the specific pid, which are only
     * dependent on event types that are relevant to this pid.
     * 
     * @param stateMap
     * @param gParent
     * @param fParent
     * @param visited
     * @param pid
     */
    private int cfsmBuilderVisit(Map<GFSMState, FSMState> stateMap,
            GFSMState gParent, FSMState fParent, Set<GFSMState> visited,
            int pid, int scmId) {
        visited.add(gParent);

        // Recurse on each (e,gNext) transition from this parent.
        for (EventType e : gParent.getTransitioningEvents()) {
            for (GFSMState gNext : gParent.getNextStates(e)) {

                // In the FSM we only include transitions, and optionally create
                // new FSMStates, for events that match the pid.
                if (e.getEventPid() == pid) {
                    FSMState fNext;
                    // Look-up and optionally create the next FSMState
                    // corresponding to gNext.
                    if (stateMap.containsKey(gNext)) {
                        fNext = stateMap.get(gNext);
                    } else {
                        fNext = new FSMState(gNext.isAcceptForPid(pid),
                                gNext.isInitForPid(pid), pid, scmId);
                        scmId++;
                        stateMap.put(gNext, fNext);
                    }
                    // Add the transition in the FSM-space.
                    fParent.addTransition(e, fNext);

                    // Recurse with fNext as parent and updated visited set.
                    if (!visited.contains(gNext)) {
                        scmId = cfsmBuilderVisit(stateMap, gNext, fNext,
                                visited, pid, scmId);
                    }

                } else {
                    // Because the event e does not impact this pid, we recurse
                    // with gNext as g-parent, but with the _old_ fParent
                    // FSMState. That is, the pid did not transition in the FSM
                    // state space, even though we did transition the GFSM state
                    // space.
                    if (!visited.contains(gNext)) {
                        scmId = cfsmBuilderVisit(stateMap, gNext, fParent,
                                visited, pid, scmId);
                    }
                }
            }
        }
        return scmId;
    }
}
