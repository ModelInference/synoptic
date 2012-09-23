package dynoptic.model.fifosys.gfsm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import mcscm.McScMCExample;
import dynoptic.model.fifosys.FifoSys;
import dynoptic.model.fifosys.cfsm.CFSM;
import dynoptic.model.fifosys.cfsm.fsm.FSM;
import dynoptic.model.fifosys.cfsm.fsm.FSMState;
import dynoptic.model.fifosys.gfsm.observed.fifosys.ObsFifoSys;
import dynoptic.model.fifosys.gfsm.observed.fifosys.ObsFifoSysState;

import synoptic.model.channelid.ChannelId;
import synoptic.model.event.DistEventType;

/**
 * <p>
 * A GFSM captures the execution space of a CFSM. We use this model to (1)
 * maintain the observed states/event, and (2) to carry out complex operations
 * like refinement.
 * </p>
 * <p>
 * A GFSM is composed of GFSMStates, which are _partitions_ of the observed
 * global configurations. Refinement causes a re-shuffling of the observations,
 * and new partitions to be created and added to the GFSM. Therefore, a GFSM is
 * highly mutable. Each mutation of the GFSM is a single complete step of the
 * Dynoptic algorithm.
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
 */
public class GFSM extends FifoSys<GFSMState> {

    public static Logger logger = Logger.getLogger("GFSM");

    /** Used when converting GFSM to a CFSM representation. */
    private int scmId = 0;

    /** Used when converting GFSM to a CFSM representation. */
    private int nextFsmStateId = 0;

    /**
     * Creates a new GFSM from observed traces, using default initial
     * partitioning strategy (by the list of elements at the head of all of the
     * queues in the system), from a list of traces.
     * 
     * @param traces
     * @return
     */
    public GFSM(List<ObsFifoSys> traces) {
        super(traces.get(0).getNumProcesses(), traces.get(0).getChannelIds());

        Map<Integer, Set<ObsFifoSysState>> qTopHashToPartition = new LinkedHashMap<Integer, Set<ObsFifoSysState>>();

        for (ObsFifoSys t : traces) {
            assert t.getNumProcesses() == numProcesses;
            assert t.getChannelIds().equals(channelIds);

            // DFS traversal to perform initial partitioning.
            ObsFifoSysState init = t.getInitState();
            addToMap(qTopHashToPartition, init);
            traverseAndPartition(init, qTopHashToPartition);
        }
        // Create the GFSMState partitions based off of sets of observations.
        for (Set<ObsFifoSysState> set : qTopHashToPartition.values()) {
            states.add(new GFSMState(numProcesses, set));
        }
        recomputeAlphabet();
    }

    /**
     * Constructor helper -- adds an observation to the map, by hashing on its
     * top of queue event types.
     */
    private void addToMap(
            Map<Integer, Set<ObsFifoSysState>> qTopHashToPartition,
            ObsFifoSysState obs) {
        int hash = obs.getChannelStates().topOfQueuesHash();
        if (!qTopHashToPartition.containsKey(hash)) {
            Set<ObsFifoSysState> partition = new LinkedHashSet<ObsFifoSysState>();
            qTopHashToPartition.put(hash, partition);
        }
        qTopHashToPartition.get(hash).add(obs);
    }

    /**
     * Constructor helper -- DFS traversal of the observed traces, building up
     * an initial partitioning.
     */
    private void traverseAndPartition(ObsFifoSysState curr,
            Map<Integer, Set<ObsFifoSysState>> qTopHashToPartition) {
        for (ObsFifoSysState next : curr.getNextStates()) {
            addToMap(qTopHashToPartition, next);
            traverseAndPartition(next, qTopHashToPartition);
        }
    }

    /** Creates an empty GFSM. */
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

    @Override
    public String toString() {
        String ret = "GFSM[num-states=" + states.size() + "] : ";
        for (GFSMState s : states) {
            ret += "\n\t" + s.toString();
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

    /**
     * Splits, or refines, the partition into two sets of observations --
     * setExtract and the remaining set of events in partition part.
     */
    public void refine(GFSMState part, Set<ObsFifoSysState> setExtract) {
        part.removeAllObs(setExtract);
        states.add(new GFSMState(numProcesses, setExtract));
    }

    /**
     * Checks if the partitions and events paths associated with cExample are
     * feasible in this GFSM.
     */
    public boolean feasible(GFSMCExample cExample) {
        // 1. Check that all partitions in path are in pGraph
        for (GFSMState s : cExample.getPartitionPath()) {
            if (!this.states.contains(s)) {
                return false;
            }
        }

        // 2. Check that event transitions between partitions in path are still
        // as specified by cExample are still valid in pGraph.
        List<GFSMState> path = cExample.getPartitionPath();

        int i = 0;
        for (DistEventType e : cExample.getMcScMCExample().getEvents()) {
            if (!path.get(i).getNextStates(e).contains(path.get(i + 1))) {
                return false;
            }
            i += 1;
        }

        return true;
    }

    /**
     * Refines partition part into setLeft and setRight -- keeping setLeft in
     * part, and extracting the events in setRight. If (setLeft \\Union setRight
     * != part) then the observations in part that are in neither of the two
     * sets are assigned at random to setLeft or setRight with uniform
     * probability.
     * 
     * @param part
     * @param setLeft
     * @param setRight
     */
    public void refineWithRandNonRelevantObsAssignment(GFSMState part,
            Set<ObsFifoSysState> setLeft, Set<ObsFifoSysState> setRight) {
        // We know that setLeft and setRight have to be isolated, but what
        // about the observations in part that in neither of these two sets?
        // Our strategy is to assign them at random, either to setLeft or
        // setRight (and hope for the best).
        Random rand = new Random();

        for (ObsFifoSysState s : part.getObservedStates()) {
            if (!setLeft.contains(s) && !setRight.contains(s)) {
                // Assign s to setLeft or setRight at random.
                if (rand.nextInt(2) == 0) {
                    setLeft.add(s);
                } else {
                    setRight.add(s);
                }
            }
        }

        // Perform the complete refinement.
        this.refine(part, setRight);
    }

    /**
     * Constructs a CFSM from a GFSM. It performs the necessary traversal of the
     * GFSM to construct/specify all the process FSMs that should be part of the
     * CFSM.
     * 
     * @param gfsm
     * @return
     */
    public CFSM getCFSM() {
        Map<GFSMState, Integer> stateMap = new LinkedHashMap<GFSMState, Integer>();
        Map<Integer, FSMState> idToFSMState = new LinkedHashMap<Integer, FSMState>();
        Set<FSMState> initFSMStates = new LinkedHashSet<FSMState>();
        Set<FSMState> acceptFSMStates = new LinkedHashSet<FSMState>();
        Set<GFSMState> visited = new LinkedHashSet<GFSMState>();

        // This is the CFSM that we will return, once we populate it with all
        // the process FSMs.
        CFSM cfsm = new CFSM(numProcesses, channelIds);

        logger.info("GFSM -> CFSM: " + this.toString() + "\n");

        // Create an FSM per pid.
        for (int pid = 0; pid < numProcesses; pid++) {
            logger.info("Building FSM for pid " + pid);
            // States in each FSM have to be uniquely numbered in the scm
            // output.
            scmId = 0;

            for (GFSMState gInit : getInitStatesForPid(pid)) {
                logger.info("Exploring from " + gInit.toShortString());
                // We might have visited the current gInit in a prior iteration,
                // from another gInit, in which case we don't need to
                // re-explore.
                if (!stateMap.containsKey(gInit)) {
                    populateStateMap(stateMap, gInit, pid);
                }
            }

            // Generate the FSM states based on the FSMState transitive closure
            // equivalence classes captured by stateMap.
            for (Entry<GFSMState, Integer> entry : stateMap.entrySet()) {
                GFSMState g = entry.getKey();
                int gId = entry.getValue();
                FSMState gFsmS = null;
                if (!idToFSMState.containsKey(gId)) {
                    gFsmS = new FSMState(false, false, pid, scmId);
                    scmId += 1;
                    idToFSMState.put(gId, gFsmS);
                } else {
                    gFsmS = idToFSMState.get(gId);
                }
                gFsmS.setAccept(gFsmS.isAccept() || g.isAcceptForPid(pid));
                gFsmS.setInitial(gFsmS.isInitial() || g.isInitForPid(pid));
            }

            // Generate inter-FSM-State transitions based on the underlying
            // GFSMStates that these correspond to (we can't do this in the
            // above loop because all the FSMStates have to first exist).
            for (Entry<GFSMState, Integer> entry : stateMap.entrySet()) {
                GFSMState g = entry.getKey();
                int gId = entry.getValue();
                FSMState gFsmS = idToFSMState.get(gId);

                for (DistEventType e : g.getTransitioningEvents()) {
                    // Only create this pid's event transitions.
                    if (e.getPid() != pid) {
                        continue;
                    }
                    for (GFSMState gNext : g.getNextStates(e)) {
                        int gNextFsmSId = stateMap.get(gNext);
                        FSMState gNextFsmS = idToFSMState.get(gNextFsmSId);
                        gFsmS.addTransition(e, gNextFsmS);
                    }
                }
            }

            // Determine the initial/accept FSM states for FSM construction
            // below.
            for (FSMState s : idToFSMState.values()) {
                if (s.isInitial()) {
                    initFSMStates.add(s);
                }
                if (s.isAccept()) {
                    acceptFSMStates.add(s);
                }
            }

            // Create the FSM for this pid, and add it to the CFSM.
            FSM fsm = new FSM(pid, initFSMStates, acceptFSMStates,
                    idToFSMState.values(), scmId);

            if (fsm.getAcceptStates().isEmpty()) {
                logger.info("GFSM: " + this.toString() + "\n");
                logger.info("Problem FSM: " + fsm.toString() + "\n");

                String s = "";
                // for (Entry<GFSMState, FSMState> e : stateMap.entrySet()) {
                // s += e.getKey().toShortString() + " : "
                // + e.getValue().toString() + "\n";
                // }
                logger.info("stateMap: \n" + s);

                assert !fsm.getAcceptStates().isEmpty();
            }
            assert !fsm.getAcceptStates().isEmpty();
            assert !fsm.getInitStates().isEmpty();

            cfsm.addFSM(fsm);

            stateMap.clear();
            idToFSMState.clear();
            initFSMStates.clear();
            acceptFSMStates.clear();
            visited.clear();
        }
        return cfsm;
    }

    /**
     * Returns all counter-example paths in GFSM that correspond to the
     * event-based McScMCExample. Since a GFSM is an NFA, we might have multiple
     * matching paths. Constructs the paths using DFS exploration of the GFSM.
     * 
     * @param cExample
     */
    public List<GFSMCExample> getCExamplePaths(McScMCExample cExample) {
        List<GFSMCExample> paths = new ArrayList<GFSMCExample>();
        // Explore potential paths from each global initial state in the GFSM.
        for (GFSMState parent : getInitStates()) {
            List<GFSMCExample> newPaths = buildCExamplePaths(cExample, 0,
                    parent);
            if (newPaths != null) {
                paths.addAll(newPaths);
            }
        }
        return paths;
    }

    /**
     * Returns the longest _partial_ counter-example path in the GFSM that
     * correspond to the event-based McScMCExample. If a complete
     * counter-example is possible, then an AssertionError is thrown: Use
     * getCExamplePaths() to first to determine if a complete counter-example
     * path is impossible and it is necessary to construct a partial
     * counter-example path for refinement.
     * 
     * @param cExample
     */
    public PartialGFSMCExample getLongestPartialCExamplePath(
            McScMCExample cExample) {
        // Explore potential paths from each global initial state in the GFSM.
        PartialGFSMCExample maxPath = null;
        PartialGFSMCExample newPath = null;
        for (GFSMState parent : getInitStates()) {
            newPath = buildCExamplePartialPaths(cExample, 0, parent);
            if (newPath == null) {
                continue;
            }
            if (maxPath == null || maxPath.pathLength() < newPath.pathLength()) {
                maxPath = newPath;
            }
        }

        assert maxPath != null;
        // If maxPath length is 1 then we haven't even matched the first event
        // in cExample. Therefore we cannot consider maxPath to be a partial
        // path of cExample. And, at least one partial path must exist, based on
        // GFSM->CFSM conversion.
        if (!(maxPath.pathLength() > 1)) {
            assert maxPath.pathLength() > 1;
        }

        return maxPath;
    }

    // //////////////////////////////////////////////////////////////////

    /**
     * Returns a single longest partial counter-example path corresponding to
     * cExample, starting at event index, and the corresponding GFSM partition
     * parent. This method is based on buildCExamplePaths() below.
     */
    private PartialGFSMCExample buildCExamplePartialPaths(
            McScMCExample cExample, int eventIndex, GFSMState parent) {
        PartialGFSMCExample path = null, newPath = null;

        // Check that we did not reach the end of the events list (so a complete
        // counter-example is possible).
        assert (eventIndex != cExample.getEvents().size());

        DistEventType e = cExample.getEvents().get(eventIndex);

        if (!parent.getTransitioningEvents().contains(e)) {
            // We can't make further progress along this partitions path with e
            // as the next event.
            path = new PartialGFSMCExample(cExample);
            path.addToFrontOfPath(parent);
            return path;
        }

        // Recursively build the possible partial paths by traversing through
        // each child that is reachable on e from parent.
        for (GFSMState child : parent.getNextStates(e)) {
            newPath = buildCExamplePartialPaths(cExample, eventIndex + 1, child);
            if (path == null || (newPath.pathLength() > path.pathLength())) {
                path = newPath;
            }
        }

        // Add the current partition to the front of the longest partial path
        // constructed and return.
        if (path == null) {
            path = new PartialGFSMCExample(cExample);
        }
        path.addToFrontOfPath(parent);

        return path;
    }

    /**
     * Returns a set of counter-example paths that correspond to cExample,
     * starting at event index, and the corresponding GFSM partition parent.
     */
    private List<GFSMCExample> buildCExamplePaths(McScMCExample cExample,
            int eventIndex, GFSMState parent) {
        List<GFSMCExample> paths = null, newPaths = null;

        // We reached the end of the events list: construct a new GFSMCExample
        // instance containing just the parent partition and return it.
        if (eventIndex == cExample.getEvents().size()) {
            // A counter-example is only valid if it ends at an accepting state.
            if (!parent.isAccept()) {
                return null;
            }
            GFSMCExample path = new GFSMCExample(cExample);
            path.addToFrontOfPath(parent);
            newPaths = new ArrayList<GFSMCExample>();
            newPaths.add(path);
            return newPaths;
        }
        DistEventType e = cExample.getEvents().get(eventIndex);

        if (!parent.getTransitioningEvents().contains(e)) {
            // We can't make further progress along this partitions path with e
            // as the next event.
            return null;
        }

        // Recursively build the paths by traversing through each child that is
        // reachable on e from parent.
        for (GFSMState child : parent.getNextStates(e)) {
            newPaths = buildCExamplePaths(cExample, eventIndex + 1, child);
            if (paths == null) {
                paths = newPaths;
            } else if (newPaths != null) {
                paths.addAll(newPaths);
            }
        }

        // If the rest of the counter-example does not result in any valid
        // sub-paths then we can't generate one.
        if (paths == null) {
            return null;
        }

        // Otherwise, add the current partition to the front of the paths
        // constructed and return.
        for (GFSMCExample path : paths) {
            path.addToFrontOfPath(parent);
        }
        return paths;
    }

    /**
     * Depth-first recursive traversal of the GFSM state/transition graph to
     * construct an equivalent CFSM (though in practice the CFSM may accept more
     * behaviors). We back-out when we reach a node that we've visited before.
     * As we traverse, we build up the FSMState states for the specific pid,
     * which are only dependent on event types that are relevant to this pid.
     */

    /**
     * Depth-first recursive traversal of the GFSM state/transition graph to
     * determine transitive closure of GFSMState instances that are reachable
     * from s through non-pid event transitions. GFSMState instances belonging
     * to the same transitive closure map to the same integer in the stateMap
     * structure. After a record is created in stateMap, it may later be updated
     * based on state reachability in the GFSM.
     */
    private void populateStateMap(Map<GFSMState, Integer> stateMap,
            GFSMState s, int pid) {
        assert !stateMap.containsKey(s);

        // Find the transitive closure from s in the subgraph generated by
        // considering all partitions and transitions on events that
        // are _not_ associated with pid. At the same time determine the
        // boundary composed of states that _are_ reachable with pid
        // event transitions. We use this boundary for recursive calls to
        // populateStateMap().
        Set<GFSMState> nonPidTxClosureStates = new LinkedHashSet<GFSMState>();
        Set<GFSMState> pidBoundaryStates = new LinkedHashSet<GFSMState>();

        findNonPidTransitiveClosure(pid, s, new LinkedHashSet<GFSMState>(),
                nonPidTxClosureStates, pidBoundaryStates);

        // Determine the corresponding FSMState for s. Since all partitions
        // in the computed transitive closure must have the same FSMState, we
        // first check if a partition in this set has a mapping to an FSMState.
        int txClosureFsmSId = nextFsmStateId;

        for (GFSMState part : nonPidTxClosureStates) {
            if (stateMap.containsKey(part)) {
                int existingId = stateMap.get(part);
                if (existingId < txClosureFsmSId) {
                    txClosureFsmSId = existingId;
                }
            }

        }

        if (txClosureFsmSId == nextFsmStateId) {
            nextFsmStateId += 1;
        }

        // Now, update all the neighbors to map to the lowest state id
        // corresponding to this transitive closure.
        for (GFSMState neighbor : nonPidTxClosureStates) {
            stateMap.put(neighbor, txClosureFsmSId);
        }
        // Record the mapping for s.
        stateMap.put(s, txClosureFsmSId);

        // Recursively explore states at the transitive closure boundary.
        for (GFSMState boundaryG : pidBoundaryStates) {
            if (!stateMap.containsKey(boundaryG)) {
                populateStateMap(stateMap, boundaryG, pid);
            }
        }
    }

    /**
     * Returns a set of GFSMState nodes that are reachable from s though event
     * transitions that are non-pid transitions. This set does not include s.
     * 
     * @param visited
     */
    private void findNonPidTransitiveClosure(int pid, GFSMState s,
            Set<GFSMState> visited, Set<GFSMState> nonPidTxClosureStates,
            Set<GFSMState> pidBoundaryStates) {
        // Record that we have visited s.
        visited.add(s);

        // States reachable from s by non-pid events.
        Set<GFSMState> nonPidStates = new LinkedHashSet<GFSMState>();
        // States reachable from s by pid events.
        Set<GFSMState> pidStates = new LinkedHashSet<GFSMState>();
        for (DistEventType e : s.getTransitioningEvents()) {
            if (e.getPid() != pid) {
                nonPidStates.addAll(s.getNextStates(e));
            } else {
                pidStates.addAll(s.getNextStates(e));
            }
        }
        pidBoundaryStates.addAll(pidStates);

        // Now nonPidStates contains just those states that are reachable
        // exclusively through non-pid events (some may have been reachable
        // through both pid, and non-pid events).
        nonPidStates.removeAll(pidStates);

        // Set<GFSMState> reachables = new LinkedHashSet<GFSMState>();
        nonPidTxClosureStates.addAll(nonPidStates);

        // Recursively build up the transitive set of non-pid-event-reachable
        // states from s.
        // Set<GFSMState> newReachables = null;
        for (GFSMState child : nonPidStates) {
            if (!visited.contains(child)) {
                // newReachables =
                findNonPidTransitiveClosure(pid, child, visited,
                        nonPidTxClosureStates, pidBoundaryStates);
                // reachables.addAll(newReachables);
            }
        }
        // return reachables;
    }

}
