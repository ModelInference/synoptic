package dynoptic.model.fifosys.gfsm.observed.fifosys;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import dynoptic.invariants.BinaryInvariant;
import dynoptic.invariants.checkers.BinChecker;
import dynoptic.main.DynopticMain;
import dynoptic.model.fifosys.FifoSys;
import dynoptic.model.fifosys.gfsm.observed.ObsDistEventType;
import dynoptic.model.fifosys.gfsm.observed.ObsFSMState;
import dynoptic.model.fifosys.gfsm.observed.dag.ObsDAG;
import dynoptic.model.fifosys.gfsm.observed.dag.ObsDAGNode;

import synoptic.model.DAGsTraceGraph;
import synoptic.model.EventNode;
import synoptic.model.channelid.ChannelId;
import synoptic.model.event.DistEventType;
import synoptic.model.event.Event;

/**
 * Represents a single captured/observed trace of an execution of a distributed
 * system. The actual (parsed) event/state information is maintained by
 * ObservedFifoSysState instances. An instance of ObsFifoSys merely maintains a
 * pointer to the initial/terminal states.
 */
public class ObsFifoSys extends FifoSys<ObsFifoSysState, ObsDistEventType> {

    private static Logger logger = Logger.getLogger("ObsFifoSys");

    /**
     * Uses Synoptic event nodes and ordering constraints between these nodes to
     * generate ObsFSMStates (anonymous states), obsDAGNodes (to contain
     * obsFSMStates and encode dependencies between them), and an ObsDag per
     * execution parsed from the log. Then, this function converts each ObsDag
     * into an observed FifoSys. The list of these observed FifoSys instances is
     * then returned. Note that if the consistentInitState is set then just one
     * observed FifoSys is returned.
     * 
     * @param traceGraph
     * @param numProcesses
     * @param channelIds
     * @param consistentInitState
     * @return
     */
    public static List<ObsFifoSys> synTraceGraphToDynObsFifoSys(
            DAGsTraceGraph traceGraph, int numProcesses,
            List<ChannelId> channelIds, boolean consistentInitState) {
        assert numProcesses != -1;

        // Note: if we assume consistent per-process initial state then this
        // list will contain just 1 ObsFifoSys, even with multiple traces,
        List<ObsFifoSys> traces = new ArrayList<ObsFifoSys>();

        // Maps an observed event to the generated ObsDAGNode that emits the
        // event in the Dynoptic DAG.
        Map<Event, ObsDAGNode> preEventNodesMap = new LinkedHashMap<Event, ObsDAGNode>();

        ObsDAG dag = null;
        ObsFifoSys fifoSys = null;
        Set<ObsFifoSysState> fifoStates = null;
        // In case of consistentInitState, there is just one initial state in
        // the observed fifo sys, and there are _multiple_ terminal states. This
        // set keeps track of these terminal states.
        Set<ObsFifoSysState> termStates = new LinkedHashSet<ObsFifoSysState>();

        int numFifoStates = 0;

        // Build a Dynoptic ObsDAG for each Synoptic trace DAG.
        for (int traceId = 0; traceId < traceGraph.getNumTraces(); traceId++) {
            logger.info("Processing trace " + traceId);
            preEventNodesMap.clear();

            // These contain the initial and terminal configurations in terms of
            // process states. These are used to construct the ObsDAG.
            List<ObsDAGNode> initDagCfg = Arrays
                    .asList(new ObsDAGNode[numProcesses]);

            List<ObsDAGNode> termDagCfg = Arrays
                    .asList(new ObsDAGNode[numProcesses]);

            // Maps a pid to the first event node for that pid.
            List<EventNode> pidInitialNodes = Arrays
                    .asList(new EventNode[numProcesses]);

            // Populate the pidInitialNodes list.
            logger.info("Populating initial nodes list");
            buildInitPidEventNodes(traceGraph, traceId, pidInitialNodes);

            // Walk the per-process chain starting at the initial node, and
            // create the corresponding Dynoptic states (without remote
            // dependencies).
            genObsFSMStates(numProcesses, consistentInitState,
                    preEventNodesMap, initDagCfg, termDagCfg, pidInitialNodes);

            // Walk the same chains as above, but now record the remote
            // dependencies between events as dependencies between states.
            genRemoteStateDependencies(numProcesses, preEventNodesMap,
                    pidInitialNodes);

            logger.info("Generating ObsDAG.");
            dag = new ObsDAG(initDagCfg, termDagCfg, channelIds, traceId);

            termStates.add(dag.getTermFifoSysState());

            logger.info("Generating ObsFifoSys.");
            if (consistentInitState) {
                // Accumulate fifo sys state instances, but delay creating a
                // fifoSys until we have collected all of the instances..
                if (fifoStates == null) {
                    fifoStates = dag.genFifoStates();
                } else {
                    fifoStates.addAll(dag.genFifoStates());
                }
            } else {
                fifoSys = dag.getObsFifoSys();
                numFifoStates += fifoSys.getStates().size();
                traces.add(fifoSys);
            }
        }

        if (consistentInitState) {
            assert dag != null;
            assert fifoStates != null;

            // Since all DAGs share the initial state when consistentInitState
            // is enabled, we can just use the initial state of the last DAG.
            ObsFifoSysState initS = dag.getInitFifoSysState();
            fifoStates.add(initS);
            fifoStates.addAll(termStates);
            fifoSys = new ObsFifoSys(channelIds, initS, termStates, fifoStates);
            traces.add(fifoSys);
            logger.info("[consistentInitState] Total fifo states created: "
                    + fifoStates.size());
        } else {
            logger.info("Total fifo states created: " + numFifoStates);
        }

        return traces;
    }

    /**
     * Walks the DAG and records the remote dependencies between events as
     * dependencies between states.
     * 
     * @param numProcesses
     * @param preEventNodesMap
     * @param pidInitialNodes
     */
    private static void genRemoteStateDependencies(int numProcesses,
            Map<Event, ObsDAGNode> preEventNodesMap,
            List<EventNode> pidInitialNodes) {
        for (int pid = 0; pid < numProcesses; pid++) {
            logger.info("Walking process[" + pid
                    + "] chain to record remote dependencies");

            EventNode eNode = pidInitialNodes.get(pid);

            while (eNode != null) {
                Event e = eNode.getEvent();

                // Record remote dependencies.
                for (EventNode eNodeSucc : eNode.getAllSuccessors()) {
                    if (eNodeSucc.isTerminal()) {
                        continue;
                    }
                    Event eSucc = eNodeSucc.getEvent();
                    int eSuccPid = ((DistEventType) eSucc.getEType()).getPid();

                    if (eSuccPid != pid) {
                        assert preEventNodesMap.containsKey(e);
                        assert preEventNodesMap.containsKey(eSucc);

                        // post-state of eSucc depends on the post-state of
                        // e having occurred.
                        ObsDAGNode eSuccPost = preEventNodesMap.get(eSucc)
                                .getNextState();
                        ObsDAGNode ePost = preEventNodesMap.get(e)
                                .getNextState();
                        eSuccPost.addRemoteDependency(ePost);
                    }
                }

                eNode = eNode.getProcessLocalSuccessor();
            }
        }
    }

    /**
     * Walks the per-process chain starting at the initial node, and creates the
     * corresponding Dynoptic states (without remote dependencies).
     * 
     * @param numProcesses
     * @param consistentInitState
     * @param preEventNodesMap
     * @param initDagCfg
     * @param termDagCfg
     * @param pidInitialNodes
     */
    private static void genObsFSMStates(int numProcesses,
            boolean consistentInitState,
            Map<Event, ObsDAGNode> preEventNodesMap,
            List<ObsDAGNode> initDagCfg, List<ObsDAGNode> termDagCfg,
            List<EventNode> pidInitialNodes) {

        for (int pid = 0; pid < numProcesses; pid++) {
            logger.info("Walking process[" + pid
                    + "] chain to create ObsFSMState instances.");

            EventNode eNode = pidInitialNodes.get(pid);
            assert eNode != null;

            ObsFSMState obsState;

            if (consistentInitState) {
                // Every process starts in the same (anonymous) state across
                // all executions.
                obsState = ObsFSMState.consistentAnonInitObsFSMState(pid);
            } else {
                // Every process starts in a unique anonymous state across
                // all executions.
                obsState = ObsFSMState.anonObsFSMState(pid, true, false);
            }

            ObsDAGNode prevNode = new ObsDAGNode(obsState);

            initDagCfg.set(pid, prevNode);

            while (eNode != null) {
                Event e = eNode.getEvent();

                if (consistentInitState) {
                    // A new state is a function of its previous state and
                    // previous event.
                    DistEventType prevEvent = (DistEventType) e.getEType();
                    obsState = ObsFSMState.consistentAnonObsFSMState(obsState,
                            prevEvent);
                } else {
                    // A new state is globally new.
                    obsState = ObsFSMState.anonObsFSMState(pid, false, false);
                }

                ObsDAGNode nextNode = new ObsDAGNode(obsState);

                prevNode.addTransition(e, nextNode);
                preEventNodesMap.put(e, prevNode);

                prevNode = nextNode;
                eNode = eNode.getProcessLocalSuccessor();
            }
            termDagCfg.set(pid, prevNode);
            // Terminal is an accumulating property -- obsState might not
            // have been terminal for prior traces, but it is in this trace,
            // and so it will remain for this log.
            obsState.markTerm();
        }
    }

    /**
     * Populates the pidInitialNodes list with the first event for each process.
     * We identify the first node by it's timestamp -- the node that has the
     * earliest timestamp is the first node.
     * 
     * @param traceGraph
     * @param traceId
     * @param pidInitialNodes
     */
    private static void buildInitPidEventNodes(DAGsTraceGraph traceGraph,
            int traceId, List<EventNode> pidInitialNodes) {

        for (EventNode eNode : traceGraph.getNodes()) {
            // Skip nodes from other traces.
            if (eNode.getTraceID() != traceId) {
                continue;
            }

            // Skip special nodes.
            if (eNode.isInitial() || eNode.isTerminal()) {
                continue;
            }

            // Retrieve the pid of the process that generated the event.
            Event e = eNode.getEvent();
            int ePid = ((DistEventType) e.getEType()).getPid();

            assert ePid < pidInitialNodes.size();

            // If we have no event for this pid, or if this event has an earlier
            // timestamp than the node we know about, then set it to be the
            // earliest event for this pid.
            if (pidInitialNodes.get(ePid) == null
                    || eNode.getTime().lessThan(
                            pidInitialNodes.get(ePid).getTime())) {
                pidInitialNodes.set(ePid, eNode);
            }
        }

        // Make sure that all of the processes have an "earliest" event node
        // set.
        for (EventNode eNode : pidInitialNodes) {
            assert eNode != null;
        }
    }

    // //////////////////////////////////////////////////////////////////

    private final ObsFifoSysState initState;
    private final Set<ObsFifoSysState> termStates;

    public ObsFifoSys(List<ChannelId> channelIds, ObsFifoSysState initState,
            ObsFifoSysState termState, Set<ObsFifoSysState> states) {
        this(channelIds, initState, Collections.singleton(termState), states);
    }

    public ObsFifoSys(List<ChannelId> channelIds, ObsFifoSysState initState,
            Set<ObsFifoSysState> termStates, Set<ObsFifoSysState> states) {
        super(initState.getNumProcesses(), channelIds);
        assert initState.isInitial();
        for (ObsFifoSysState termS : termStates) {
            assert termS.isAccept();
        }

        assert states.contains(initState);
        assert states.containsAll(termStates);

        if (DynopticMain.assertsOn) {
            for (ObsFifoSysState s : states) {
                assert s.getNumProcesses() == this.numProcesses;
                assert s.getChannelIds().equals(channelIds);
                // There can only be one initial and one accept state in a
                // trace.
                if (s.isAccept()) {
                    assert termStates.contains(s);
                }
                if (s.isInitial()) {
                    assert initState == s;
                }
            }
        }

        this.initState = initState;
        this.termStates = termStates;
        this.states.addAll(states);
    }

    // //////////////////////////////////////////////////////////////////

    public ObsFifoSysState getInitState() {
        return initState;
    }

    public Set<ObsFifoSysState> getTermStates() {
        return termStates;
    }

    public int getNumProcesses() {
        return initState.getNumProcesses();
    }

    public List<ChannelId> getChannelIds() {
        return initState.getChannelIds();
    }

    @Override
    public String toString() {
        return "ObsFifoSys[" + this.states.size() + "]";
    }

    // //////////////////////////////////////////////////////////////////

    /**
     * This method model checks ObsFifoSys against minedInvs and returns the set
     * of invariants that are violated by the ObsFifoSys. The model checking is
     * simplistic: (1) since ObsFifoSys is a DAG, we don't have to worry about
     * cycles and re-visiting nodes, (2) we only care about removing stitching,
     * and not the complete path -- once we find a violation, we percolate up
     * until we see that there is a stitching edge (with a different trace id),
     * (3) we only care about the three basic invariant types.
     * 
     * @param minedInvs
     */
    public Set<BinaryInvariant> findInvalidatedInvariants(
            List<BinaryInvariant> minedInvs) {
        Set<BinaryInvariant> ret = new LinkedHashSet<BinaryInvariant>();
        for (BinaryInvariant inv : minedInvs) {
            BinChecker invChecker = BinChecker.newChecker(inv);
            if (!checkInvariant(invChecker)) {
                ret.add(inv);
            }
        }
        return ret;
    }

    /**
     * Runs the invChecker over this instance of observed fifo sys to check if
     * it satisfied the corresponding invariant.
     */
    private boolean checkInvariant(BinChecker invChecker) {
        return false;
    }

}
