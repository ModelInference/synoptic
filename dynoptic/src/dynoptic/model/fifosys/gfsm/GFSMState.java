package dynoptic.model.fifosys.gfsm;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import dynoptic.model.alphabet.EventType;
import dynoptic.model.fifosys.AbsMultiFSMState;
import dynoptic.model.fifosys.gfsm.trace.ObservedFifoSysState;

/**
 * <p>
 * An GFSMState is a partitioning of the concrete observations. It maintains a
 * set of these observations, but this set may change over time (e.g., as more
 * partitioning occurs).
 * </p>
 * <p>
 * The transitions of a GFSMState are abstract -- they are induced by the
 * transitions of the concrete states that the GFSMState maintains. Note that a
 * GFSMState can have multiple transitions on the same event that go to
 * different GFSMState instances (GFSM can be an NFA).
 * </p>
 * <p>
 * In many ways this class mimics a Synoptic Partition class/concept.
 * </p>
 */
public class GFSMState extends AbsMultiFSMState<GFSMState> {
    // This is the set of observed state instances.
    final Set<ObservedFifoSysState> observedStates;

    // CACHE optimization: the set of abstract transitions induced by the
    // concrete transitions. This is merely a cached version of the ground
    // truth.
    final Map<EventType, Set<GFSMState>> transitions;

    // Fn: (ObservedFifoSysState s, pid p) -> "s accept for pid"
    private IStatePidToBooleanFn fnIsAcceptForPid;
    // Fn: (ObservedFifoSysState s, pid p) -> "s initial for pid"
    private IStatePidToBooleanFn fnIsInitialForPid;

    public GFSMState(int numProcesses) {
        super(numProcesses);
        observedStates = new LinkedHashSet<ObservedFifoSysState>();
        transitions = new LinkedHashMap<EventType, Set<GFSMState>>();

        fnIsAcceptForPid = new IStatePidToBooleanFn() {
            @Override
            public boolean eval(ObservedFifoSysState s, int pid) {
                return s.isAcceptForPid(pid);
            }
        };

        fnIsInitialForPid = new IStatePidToBooleanFn() {
            @Override
            public boolean eval(ObservedFifoSysState s, int pid) {
                return s.isInitialForPid(pid);
            }
        };
    }

    // //////////////////////////////////////////////////////////////////

    @Override
    public boolean isInitial() {
        for (int pid = 0; pid < numProcesses; pid++) {
            if (!atLeastOneObsStateEvalTrueForPid(fnIsInitialForPid, pid)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isAccept() {
        for (int pid = 0; pid < numProcesses; pid++) {
            if (!atLeastOneObsStateEvalTrueForPid(fnIsAcceptForPid, pid)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Set<EventType> getTransitioningEvents() {
        return transitions.keySet();
    }

    @Override
    public Set<GFSMState> getNextStates(EventType event) {
        assert transitions.containsKey(event);

        return transitions.get(event);
    }

    @Override
    public boolean isAcceptForPid(int pid) {
        assert pid >= 0 && pid < numProcesses;

        return atLeastOneObsStateEvalTrueForPid(fnIsAcceptForPid, pid);
    }

    // //////////////////////////////////////////////////////////////////

    /** Adds a new observed state to this partition. */
    public void add(ObservedFifoSysState s) {
        assert !observedStates.contains(s);
        assert s.getNumProcesses() == this.numProcesses;

        observedStates.add(s);
        cacheObservedParentTransitions(s);
    }

    /** Adds a new observed state to this partition. */
    public void addAll(Set<ObservedFifoSysState> states) {
        for (ObservedFifoSysState s : states) {
            add(s);
        }
    }

    /** Removes an observed state from this partition. */
    public void removeObservedState(ObservedFifoSysState s) {
        assert observedStates.contains(s);
        observedStates.remove(s);
        recreateCachedTransitions();
    }

    /** Creates the transitions cache from scratch. */
    private void recreateCachedTransitions() {
        transitions.clear();

        // Update the cached transitions for each observed state in this
        // partition.
        for (ObservedFifoSysState s : observedStates) {
            cacheObservedParentTransitions(s);
        }
    }

    /** Updates the cached transitions for a particular observed state. */
    private void cacheObservedParentTransitions(ObservedFifoSysState s) {
        for (EventType e : s.getTransitioningEvents()) {
            GFSMState nextPartition = s.getNextState(e).getParent();
            Set<GFSMState> partitions;
            if (!transitions.containsKey(e)) {
                partitions = new LinkedHashSet<GFSMState>();
                transitions.put(e, partitions);
            } else {
                partitions = transitions.get(e);
            }
            partitions.add(nextPartition);
        }
    }

    /** Used for functional calls to atLeastOneStateEvalTruePerPid. */
    private interface IStatePidToBooleanFn {
        boolean eval(ObservedFifoSysState s, int pid);
    }

    /** Used to evaluate whether this GFSMState is accept/initial. */
    private boolean atLeastOneObsStateEvalTrueForPid(IStatePidToBooleanFn fn,
            int pid) {
        for (ObservedFifoSysState s : observedStates) {
            if (fn.eval(s, pid)) {
                return true;
            }
        }
        return false;
    }
}
