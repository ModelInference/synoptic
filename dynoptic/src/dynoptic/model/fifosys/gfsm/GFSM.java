package dynoptic.model.fifosys.gfsm;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import mcscm.McScMCExample;
import dynoptic.main.DynopticMain;
import dynoptic.model.AbsFSMState;
import dynoptic.model.fifosys.FifoSys;
import dynoptic.model.fifosys.cfsm.CFSM;
import dynoptic.model.fifosys.cfsm.fsm.FSM;
import dynoptic.model.fifosys.cfsm.fsm.FSMState;
import dynoptic.model.fifosys.gfsm.observed.fifosys.ObsFifoSys;
import dynoptic.model.fifosys.gfsm.observed.fifosys.ObsFifoSysState;
import dynoptic.util.Util;

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
public class GFSM extends FifoSys<GFSMState, DistEventType> {

    public static Logger logger = Logger.getLogger("GFSM");

    /** Used when converting GFSM to a CFSM representation. */
    private int nextScmId = 0;

    /** Used when converting GFSM to a CFSM representation. */
    private int nextFsmStateId = 0;

    /**
     * Creates a new GFSM from observed ObsFifoSys traces, using default initial
     * partitioning strategy (by the list of elements at the head of all of the
     * queues in the system), from a list of traces.
     * 
     * @param traces
     * @return
     */
    public GFSM(List<ObsFifoSys> traces) {
        super(traces.get(0).getNumProcesses(), traces.get(0).getChannelIds());

        // Compute the initial partitioning of the observed states by using the
        // queue contents associated with each globally observed state.
        Map<Integer, Set<ObsFifoSysState>> qTopHashToPartition = Util.newMap();
        Set<ObsFifoSysState> visited = Util.newSet();
        for (ObsFifoSys t : traces) {
            assert t.getNumProcesses() == numProcesses;
            assert t.getChannelIds().equals(channelIds);

            // DFS traversal to perform initial partitioning.
            ObsFifoSysState init = t.getInitState();
            addToQueueContentsHashMap(qTopHashToPartition, init);
            traverseAndPartition(init, qTopHashToPartition, visited);
            visited.clear();
        }

        Set<ObsFifoSysState> allObs = null;
        if (DynopticMain.assertsOn) {
            allObs = Util.newSet();
        }

        // Create the GFSMState partitions based off of sets of observations.
        for (Set<ObsFifoSysState> set : qTopHashToPartition.values()) {
            states.add(new GFSMState(numProcesses, set));
            if (DynopticMain.assertsOn) {
                allObs.addAll(set);
            }
        }
        recomputeAlphabet();

        // Now, assert that if two ObsFifoSysStates are identical, then they are
        // assigned to the same partition.
        if (DynopticMain.assertsOn) {
            checkPartitioningConsistency(allObs);
        }

    }

    /**
     * Checks that the following property is true for a set of observations: if
     * two observations have identical process states, then belong to the same
     * GFSMState partition.
     */
    private static void checkPartitioningConsistency(
            Set<ObsFifoSysState> obsToCheck) {
        for (ObsFifoSysState s1 : obsToCheck) {
            for (ObsFifoSysState s2 : obsToCheck) {
                if (s1.getFSMStates().equals(s2.getFSMStates())) {
                    assert s1.getParent().equals(s2.getParent());
                }
            }
        }
    }

    /**
     * Constructor helper -- adds an observation to the map, by hashing on its
     * top of queue event types.
     */
    private void addToQueueContentsHashMap(
            Map<Integer, Set<ObsFifoSysState>> qTopHashToPartition,
            ObsFifoSysState obs) {
        int hash = obs.getChannelStates().topOfQueuesHash();
        if (!qTopHashToPartition.containsKey(hash)) {
            logger.info("Creating a new partition for ch-states like: "
                    + obs.getChannelStates().toString());
            Set<ObsFifoSysState> partition = Util.newSet();
            qTopHashToPartition.put(hash, partition);
        }
        qTopHashToPartition.get(hash).add(obs);
    }

    /**
     * Constructor helper -- DFS traversal of the observed traces, building up
     * an initial partitioning.
     */
    private void traverseAndPartition(ObsFifoSysState curr,
            Map<Integer, Set<ObsFifoSysState>> qTopHashToPartition,
            Set<ObsFifoSysState> visited) {
        visited.add(curr);
        for (ObsFifoSysState next : curr.getNextStates()) {
            // Ignore branches we've already visited.
            if (visited.contains(next)) {
                continue;
            }
            addToQueueContentsHashMap(qTopHashToPartition, next);
            traverseAndPartition(next, qTopHashToPartition, visited);
        }
    }

    /** Creates an empty GFSM. */
    public GFSM(int numProcesses, List<ChannelId> channelIds) {
        super(numProcesses, channelIds);
    }

    // //////////////////////////////////////////////////////////////////

    @Override
    public Set<GFSMState> getInitStates() {
        Set<GFSMState> ret = Util.newSet();
        for (GFSMState s : states) {
            if (s.isInitial()) {
                ret.add(s);
            }
        }
        return ret;
    }

    @Override
    public Set<GFSMState> getAcceptStates() {
        Set<GFSMState> ret = Util.newSet();
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
        Set<GFSMState> ret = Util.newSet();
        for (GFSMState s : states) {
            if (s.isAcceptForPid(pid)) {
                ret.add(s);
            }
        }
        return ret;
    }

    /** Returns the set of partitions that are initial for a pid. */
    public Set<GFSMState> getInitStatesForPid(int pid) {
        Set<GFSMState> ret = Util.newSet();
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
     * Checks if the partitions and events paths associated with cExample are
     * feasible in this GFSM.
     */
    public boolean feasible(List<GFSMState> pStates, List<DistEventType> pEvents) {
        assert !pStates.isEmpty();

        // 1. Check that all partitions in path are in pGraph
        for (GFSMState s : pStates) {
            if (!this.states.contains(s)) {
                return false;
            }
        }

        // 2. Check that there are event transitions between pStates that
        // correspond to pEvents.
        for (int i = 0; i < pStates.size() - 1; i++) {
            GFSMState s = pStates.get(i);
            GFSMState sNext = pStates.get(i + 1);
            DistEventType e = pEvents.get(i);

            if (!s.getTransitioningEvents().contains(e)) {
                return false;
            }
            if (!s.getNextStates(e).contains(sNext)) {
                return false;
            }
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
        assert !setLeft.isEmpty();
        assert !setRight.isEmpty();

        // We know that setLeft and setRight have to be isolated, but what
        // about the observations in part that are in neither of these two sets?
        // Our strategy is to assign these at random, either to setLeft or
        // setRight.
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
     * @param minimize
     *            - whether to minimize each of the process FSMs
     * @return
     */
    public CFSM getCFSM(boolean minimize) {
        // This is the CFSM that we will return, once we populate it with all
        // the process FSMs.
        CFSM cfsm = new CFSM(numProcesses, channelIds);

        logger.info("GFSM -> CFSM: " + this.toString() + "\n");

        Set<FSMState> initFSMStates = Util.newSet();
        Set<FSMState> acceptFSMStates = Util.newSet();
        Set<GFSMState> nonPidTxClosureStates = Util.newSet();
        Map<GFSMState, FSMState> stateMap = Util.newMap();

        Set<GFSMState> gvisited = Util.newSet();

        Set<FSMState> txClosure = Util.newSet();
        Set<FSMState> fvisited = Util.newSet();

        // Create an FSM per pid.
        for (int pid = 0; pid < numProcesses; pid++) {
            logger.info("Building FSM for pid " + pid);
            // States in each FSM have to be uniquely numbered in the scm
            // output.
            nextScmId = 0;

            // Create a new FSM state corresponding to each GFSMState.
            for (GFSMState gstate : states) {
                // NOTE: FSMState accept flag is further computed below.
                FSMState fstate = new FSMState(gstate.isAcceptForPid(pid),
                        gstate.isInitForPid(pid), pid, nextScmId);
                if (fstate.isInitial()) {
                    initFSMStates.add(fstate);
                }
                nextScmId += 1;
                stateMap.put(gstate, fstate);
            }

            logger.info("GFSMState->FSMState[pid=" + pid + "] stateMap : "
                    + stateMap.toString());

            // Create transitions between FSMState instances based on
            // corresponding GFSMState transitions, as well as the non-pid
            // transition transitive closure for each GFSMState.
            for (GFSMState gstate : states) {
                // Find the states that can be reached through non-pid
                // transitions (we treat them as epsilon transitions).
                nonPidTxClosureStates.clear();
                gvisited.clear();
                // TODO: cache reachability for explored states and re-use this
                // information.
                AbsFSMState.findNonPidTransitiveClosure(pid, gstate, gvisited,
                        nonPidTxClosureStates);

                nonPidTxClosureStates.add(gstate);

                FSMState fstate = stateMap.get(gstate);

                for (GFSMState g : nonPidTxClosureStates) {
                    // Accept computation: If we can reach an accepting g from
                    // gstate using non-pid (epsilon) transitions, then this
                    // process can terminate at gstate, or equivalently, at the
                    // corresponding fstate.
                    if (g.isAcceptForPid(pid)) {
                        fstate.setAccept();
                        acceptFSMStates.add(fstate);
                    }

                    // Add the GFSMState transitions to fstate.
                    for (DistEventType e : g.getTransitioningEvents()) {
                        // Only create this pid's event transitions.
                        if (e.getPid() != pid) {
                            continue;
                        }
                        for (GFSMState gNext : g.getNextStates(e)) {
                            FSMState fNext = stateMap.get(gNext);
                            fstate.addTransition(e, fNext);
                        }
                    }
                }
            }

            // Remove any non-reachable FSM states.

            // 1. Build the transitive closure of state from all initial states.
            for (FSMState istate : initFSMStates) {
                fvisited.clear();
                AbsFSMState.findTransitiveClosure(istate, fvisited, txClosure);
            }

            // 2. Remove those FSM states that are not reachable from any
            // initial state -- that are not in txClosure -- from stateMap.
            Set<GFSMState> keySet = stateMap.keySet();
            Iterator<GFSMState> keyItr = keySet.iterator();
            while (keyItr.hasNext()) {
                GFSMState gstate = keyItr.next();
                FSMState fstate = stateMap.get(gstate);
                // Skip states that are initial -- we can always reach these.
                if (fstate.isInitial()) {
                    continue;
                }

                if (!txClosure.contains(fstate)) {
                    // Remove from stateMap, as well as accepting set of states.
                    keyItr.remove();
                    acceptFSMStates.remove(fstate);
                }
            }

            // We want to create the smallest possible FSM for efficiency (McScM
            // runs faster on smaller models) and so that the models are simple
            // to inspect.

            // 1. Merge any FSM states that are bisimular --- if their behavior
            // is indistinguishable.
            boolean merged = false;
            do {
                merged = false;
                keySet = stateMap.keySet();
                keyItr = keySet.iterator();
                while (keyItr.hasNext()) {
                    GFSMState gstate = keyItr.next();
                    GFSMState gstate2 = findBisimularFSMState(gstate, stateMap);

                    // Did not find a corresponding gstate2 that maps to fstate2
                    // that is bisimular to fstate.
                    if (gstate2 == null) {
                        continue;
                    }

                    FSMState fstate = stateMap.get(gstate);
                    FSMState fstate2 = stateMap.get(gstate2);

                    // Merges fstate INTO fstate2.
                    remapPredTxns(fstate, fstate2, stateMap);

                    // Remove gstate/fstate from the stateMap, and remove
                    // fstate from accepting set
                    keyItr.remove();
                    acceptFSMStates.remove(fstate);
                    initFSMStates.remove(fstate);
                    merged = true;
                    break;
                }
                // Re-try all possible n^2 merges if we've just merged two
                // states, since this might induce further state equivalence and
                // merging.
            } while (merged);

            // 2. Remove states when there is a state that simulates it --
            // TODO: INCOMPLETE.
            // mergeSimulatingStates(initFSMStates, acceptFSMStates, stateMap);

            assert !acceptFSMStates.isEmpty();
            assert !initFSMStates.isEmpty();

            // Create the FSM for this pid.
            FSM fsm = new FSM(pid, initFSMStates, acceptFSMStates,
                    stateMap.values(), nextScmId);

            // 3. Check if after bisimulation merging above the FSM is
            // now a DFA and we can use standard minimization to further
            // minimize the FSM.
            if (minimize && fsm.isDeterministic()) {
                fsm.minimize();
            }

            // Add FSM to the CFSM.
            cfsm.addFSM(fsm);

            stateMap.clear();
            initFSMStates.clear();
            acceptFSMStates.clear();
            txClosure.clear();
        }
        return cfsm;
    }

    /**
     * Incomplete, see TODO below.
     * 
     * @param initFSMStates
     * @param acceptFSMStates
     * @param stateMap
     */
    private void mergeSimulatingStates(Set<FSMState> initFSMStates,
            Set<FSMState> acceptFSMStates, Map<GFSMState, FSMState> stateMap) {
        Set<GFSMState> keySet;
        Iterator<GFSMState> keyItr;
        boolean merged;
        // 2. Merge any FSM states where one state is subsumed by the
        // behavior of another state. More specifically, if there are three
        // FSM states f1,f2,f3 such that f1 --e--> f2, f1 --e--> f3, and the
        // set transitions from f2 is a subset of transitions from f3 (both
        // in events and states reached along those events) then merge f2
        // and f3.
        merged = false;
        do {
            merged = false;
            keySet = stateMap.keySet();
            keyItr = keySet.iterator();
            while (keyItr.hasNext()) {
                GFSMState gparent = keyItr.next();
                GFSMState gchild = null, gchild2 = null;

                outer:
                for (DistEventType e : gparent.getTransitioningEvents()) {
                    Set<GFSMState> children = gparent.getNextStates(e);
                    // TODO: have to consider only children gchild_,
                    // child2_
                    // that have NO other parents besides gparent.
                    for (GFSMState gchild_ : children) {
                        for (GFSMState gchild2_ : children) {
                            if (gchild_ == gchild2_) {
                                continue;
                            }

                            if (checkSubsuming(gchild_, gchild2_, stateMap)) {
                                gchild = gchild_;
                                gchild2 = gchild2_;
                                continue outer;
                            }

                        }
                    }
                }

                if (gchild == null) {
                    continue;
                }

                FSMState fstate, fstate2;
                fstate = stateMap.get(gchild);
                fstate2 = stateMap.get(gchild2);

                // Merges fstate INTO fstate2.
                remapPredTxns(fstate, fstate2, stateMap);

                // Remove gstate/fstate from the stateMap, and remove
                // fstate from accepting set
                stateMap.remove(gchild);
                acceptFSMStates.remove(fstate);
                initFSMStates.remove(fstate);
                merged = true;
                break;

            }
        } while (merged);
    }

    /**
     * Changes transitions from all predecessors of fstate to transition instead
     * to fstate2.
     */
    private void remapPredTxns(FSMState fstate, FSMState fstate2,
            Map<GFSMState, FSMState> stateMap) {
        // Find predecessors of fstate.

        // TODO: we need a better way of doing this -- need a map from children
        // to parents
        for (FSMState fPred : stateMap.values()) {
            if (fPred == fstate) {
                continue;
            }
            if (!fPred.getNextStates().contains(fstate)) {
                continue;
            }

            // TODO: need a better way of doing this, too -- have a way to
            // update transitions to states in bulk.
            for (DistEventType e : Util.newSet(fPred.getTransitioningEvents())) {
                if (fPred.getNextStates(e).contains(fstate)) {
                    fPred.rmTransition(e, fstate);
                    fPred.addTransition(e, fstate2);
                }
            }
        }
    }

    /** Checks if gchild2 SUBSUMEs gchild in the FSMState world. */
    private boolean checkSubsuming(GFSMState gchild, GFSMState gchild2,
            Map<GFSMState, FSMState> stateMap) {

        FSMState fstate = stateMap.get(gchild);
        FSMState fstate2 = stateMap.get(gchild2);

        // States must have identical initial/accept properties
        // to be mergeable.
        if (fstate.isInitial() != fstate2.isInitial()) {
            return false;
        }
        if (fstate.isAccept() != fstate2.isAccept()) {
            return false;
        }

        // TODO.

        return true;
    }

    /**
     * Attempts to find an FSM state, fstate2, that is bisimular (behaviorally
     * indistinguishable) from the FSM state stateMap[gstate]. Returns gstate2
     * such that fstate2 = stateMap[gstate2]. If no such fstate2 exists, then
     * returns null.
     */
    private GFSMState findBisimularFSMState(GFSMState gstate,
            Map<GFSMState, FSMState> stateMap) {
        FSMState fstate = stateMap.get(gstate);

        for (GFSMState gstate2 : stateMap.keySet()) {
            if (gstate == gstate2) {
                continue;
            }
            FSMState fstate2 = stateMap.get(gstate2);

            // States must have identical initial/accept properties
            // to be mergeable.
            if (fstate.isInitial() != fstate2.isInitial()) {
                continue;
            }
            if (fstate.isAccept() != fstate2.isAccept()) {
                continue;
            }

            // Compare transitions of fstate and fstate2
            Set<DistEventType> txns1 = fstate.getTransitioningEvents();
            Set<DistEventType> txns2 = fstate2.getTransitioningEvents();
            if (!txns1.equals(txns2)) {
                continue;
            }

            boolean txns_identical = true;
            for (DistEventType tx : txns1) {
                if (!fstate.getNextStates(tx).equals(fstate2.getNextStates(tx))) {
                    // States reachable along tx are different,
                    // therefore fstate and fstate2 are not
                    // bisimular.
                    txns_identical = false;
                    break;
                }
            }
            if (!txns_identical) {
                continue;
            }

            return gstate2;
        }
        return null;
    }

    public Set<GFSMPath> getCExamplePaths(McScMCExample cExample, int pid) {
        Set<GFSMPath> paths = Util.newSet();

        // Initialize paths with all the initial states in the model.
        for (GFSMState initS : getInitStates()) {
            paths.add(new GFSMPath(initS, pid));
        }

        // States visited during suffix paths construction.
        Set<GFSMState> visitedStates = Util.newSet();
        // Suffix paths that we use to build up the paths.
        Set<GFSMPath> suffixPaths = null;
        // Temporary holding for new set of paths.
        Set<GFSMPath> newPaths = Util.newSet();
        // The events sequence representing a counter-example in the CFSM.
        List<DistEventType> events = cExample.getEvents();

        // Find the index of the last event for pid. We'll require below that
        // the suffix corresponding to this last event must end in a partition
        // that is an accepting partition for pid.
        int lastPidEventIndx = -1;
        int numPidEvents = 0;
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i).getPid() == pid) {
                lastPidEventIndx = i;
                numPidEvents += 1;
            }
        }

        // Build paths for sub-sequence of process pid events in the
        // counter-example.
        for (int eventIndx = 0; eventIndx <= lastPidEventIndx; eventIndx++) {
            DistEventType e = events.get(eventIndx);

            // Skip non-process pid events.
            if (e.getPid() != pid) {
                continue;
            }

            // Extend the constructed paths. The new paths are in newPaths.
            for (GFSMPath path : paths) {
                // Populate suffix paths with extensions to path that end with e
                // as the last event, and only contain non pid transitions
                // before the e transition.
                GFSMState firstState = path.lastState();
                suffixPaths = getSuffixPaths(firstState, e, visitedStates, pid);
                visitedStates.clear();
                if (suffixPaths == null) {
                    continue;
                }

                // path becomes a prefix.
                GFSMPath prefix = new GFSMPath(path);
                // Iterate through nextPaths, using these as suffixes to
                // construct extensions to the prefix.
                Iterator<GFSMPath> iter = suffixPaths.iterator();
                while (iter.hasNext()) {
                    GFSMPath suffix = iter.next();

                    // If we are processing the last event for this process,
                    // then we require that the transitive closure of non-pid
                    // events starting at (and including) the last state in this
                    // suffix includes a state is a terminal state for pid.
                    if (eventIndx == lastPidEventIndx
                            && !pidTermStateReachableFromPartitionViaNonPidTxns(
                                    suffix.lastState(), pid)) {
                        continue;
                    }

                    // Construct a new path: prefix + suffix.
                    GFSMPath newPath = new GFSMPath(prefix, suffix);
                    newPaths.add(newPath);
                }
            }

            // NOTE: if we cannot find any matching paths, then there are none
            // because we have eliminated them through previous refinement!
            if (newPaths.isEmpty()) {
                // assert !newPaths.isEmpty();
                return null;
            }

            paths.clear();
            paths.addAll(newPaths);
            newPaths.clear();
        }

        // There must always be a path for pid.
        assert !paths.isEmpty();

        if (lastPidEventIndx == -1) {
            // If there are no pid events, then all paths should have a single
            // state and no events.
            for (GFSMPath path : paths) {
                assert path.numEvents() == 0;
                assert path.numStates() == 1;
            }
        } else {
            // Otherwise, check on path length.
            for (GFSMPath path : paths) {
                assert path.numEvents() >= numPidEvents;
                assert path.numStates() >= numPidEvents + 1;
            }
        }

        return paths;
    }

    /**
     * Returns true if the non-pid transitive closure from gstate includes a
     * state that is a terminal state for pid.
     */
    private boolean pidTermStateReachableFromPartitionViaNonPidTxns(
            GFSMState gstate, int pid) {
        if (gstate.isAcceptForPid(pid)) {
            return true;
        }

        Set<GFSMState> nonPidTxClosureStates = Util.newSet();
        Set<GFSMState> gvisited = Util.newSet();

        AbsFSMState.findNonPidTransitiveClosure(pid, gstate, gvisited,
                nonPidTxClosureStates);
        for (GFSMState reachable : nonPidTxClosureStates) {
            if (reachable.isAcceptForPid(pid)) {
                return true;
            }
        }
        return false;
    }

    // //////////////////////////////////////////////////////////////////

    /**
     * Adds new GFSMPath instances to suffixPaths that begin at state, end in
     * event e, and have non-pid process event types before e.
     * 
     * @param state
     * @param e
     * @param pid
     * @param suffixPaths
     */
    private Set<GFSMPath> getSuffixPaths(GFSMState state, DistEventType e,
            Set<GFSMState> visited, int pid) {

        if (visited.contains(state)) {
            return null;
        }
        visited.add(state);

        Set<GFSMPath> suffixPaths = null;
        for (DistEventType e_ : state.getTransitioningEvents()) {
            if (e_.equals(e)) {
                if (suffixPaths == null) {
                    suffixPaths = Util.newSet();
                }
                for (GFSMState stateFinal : state.getNextStates(e)) {
                    GFSMPath p = new GFSMPath(pid);
                    p.prefixEventAndState(e, stateFinal);
                    // Note, we add current state to prefix of p, but only once
                    // we are done with the outer loop.
                    suffixPaths.add(p);
                }
                continue;
            }

            if (e_.getPid() == pid) {
                // Matching pid, but wrong type (checked for above).
                continue;
            }

            // Otherwise, we recurse, and add current state to prefix of
            // whatever paths we get back (at end of the recursive call).
            for (GFSMState stateNext : state.getNextStates(e_)) {
                Set<GFSMPath> newSuffixPaths = getSuffixPaths(stateNext, e,
                        visited, pid);
                // Make sure to add the -- e_ --> stateNext to all suffixPaths
                // we get back (if there were any that terminate with e)
                if (newSuffixPaths != null) {
                    for (GFSMPath p : newSuffixPaths) {
                        p.prefixEventAndState(e_, stateNext);
                    }
                    if (suffixPaths == null) {
                        suffixPaths = Util.newSet();
                    }
                    suffixPaths.addAll(newSuffixPaths);
                }
            }
        }

        return suffixPaths;
    }

    /**
     * Splits, or refines, the partition into two sets of observations --
     * setExtract and the remaining set of events in partition part. Add a newly
     * created partition composed of observations in setExtract.
     */
    private void refine(GFSMState part, Set<ObsFifoSysState> setExtract) {
        assert states.contains(part);
        assert !setExtract.isEmpty();

        Set<ObsFifoSysState> obsToCheck = null;
        if (DynopticMain.assertsOn) {
            obsToCheck = part.getObservedStates();
        }

        part.removeAllObs(setExtract);
        states.add(new GFSMState(numProcesses, setExtract));

        part.recreateCachedTransitions();

        // We need to reset inter-partition transitions that contain preceding
        // ObsFifoSysState instances to setExtract. Unfortunately, there is no
        // way to do this in a targeted way -- we have to re-create all
        // transitions if any are suspected of being stale (i.e., need to point
        // to the new GFSMState created above).
        for (GFSMState s : states) {
            if (s != part && s.getNextStates().contains(part)) {
                s.recreateCachedTransitions();
            }
        }

        // Check consistency of just the observations that belong to the
        // original (unrefined) partition.
        if (DynopticMain.assertsOn) {
            checkPartitioningConsistency(obsToCheck);
        }

    }

}
