package dynoptic.model.fifosys.gfsm;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import dynoptic.main.DynopticMain;
import dynoptic.model.fifosys.AbsMultiFSMState;
import dynoptic.model.fifosys.gfsm.observed.fifosys.ObsFifoSysState;

import synoptic.model.event.DistEventType;

/**
 * <p>
 * An GFSMState is a partitioning of the concrete observations (ObsFifoSysState
 * instances). It maintains a set of these observations, but this set may change
 * over time (e.g., as more partitioning occurs).
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
    // Set of observed state instances.
    private final Set<ObsFifoSysState> observedStates;

    // CACHE optimization: the set of abstract transitions induced by the
    // concrete transitions. This is a cache of the ground truth.
    private final Map<DistEventType, Set<GFSMState>> transitions;

    public GFSMState(int numProcesses) {
        this(numProcesses, new LinkedHashSet<ObsFifoSysState>());
    }

    /**
     * Creates a GFSMState based off an observations set that will be _used_
     * internally.
     */
    public GFSMState(int numProcesses, Set<ObsFifoSysState> observedStates) {
        super(numProcesses);
        // NOTE: we do not create a new set for observed states.
        this.observedStates = observedStates;
        this.transitions = new LinkedHashMap<DistEventType, Set<GFSMState>>();

        for (ObsFifoSysState obs : this.observedStates) {
            if (DynopticMain.assertsOn) {
                assert obs.getNumProcesses() == this.numProcesses;
                assert obs.getParent() == null;
            }
            obs.setParent(this);
        }
    }

    /** Returns the set of all observations that are initial in this partition. */
    public Set<ObsFifoSysState> getInitialObservations() {
        Set<ObsFifoSysState> ret = new LinkedHashSet<ObsFifoSysState>();
        for (ObsFifoSysState s : observedStates) {
            if (s.isInitial()) {
                ret.add(s);
            }
        }
        return ret;
    }

    /**
     * Returns the set of all observations that are accepting/terminal in this
     * partition.
     */
    public Set<ObsFifoSysState> getTerminalObservations() {
        Set<ObsFifoSysState> ret = new LinkedHashSet<ObsFifoSysState>();
        for (ObsFifoSysState s : observedStates) {
            if (s.isAccept()) {
                ret.add(s);
            }
        }
        return ret;
    }

    // //////////////////////////////////////////////////////////////////

    @Override
    public boolean isInitial() {
        // For each pid, this partition must include an initial state
        // observation for the process with this pid.
        for (int pid = 0; pid < numProcesses; pid++) {
            if (!atLeastOneStatePidEvalTrue(this.observedStates,
                    fnIsInitialForPid, pid)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isAccept() {
        for (int pid = 0; pid < numProcesses; pid++) {
            if (!atLeastOneStatePidEvalTrue(this.observedStates,
                    fnIsAcceptForPid, pid)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Set<DistEventType> getTransitioningEvents() {
        if (this.transitions.size() == 0) {
            recreateCachedTransitions();
        }
        return transitions.keySet();
    }

    @Override
    public Set<GFSMState> getNextStates(DistEventType event) {
        if (this.transitions.size() == 0) {
            recreateCachedTransitions();
        }

        assert transitions.containsKey(event);

        return transitions.get(event);
    }

    @Override
    public boolean isAcceptForPid(int pid) {
        assert pid >= 0 && pid < numProcesses;

        return atLeastOneStatePidEvalTrue(this.observedStates,
                fnIsAcceptForPid, pid);
    }

    @Override
    public boolean isInitForPid(int pid) {
        assert pid >= 0 && pid < numProcesses;

        return atLeastOneStatePidEvalTrue(this.observedStates,
                fnIsInitialForPid, pid);
    }

    @Override
    public String toString() {
        String ret = "Obs_[" + observedStates.size() + "]";
        ret += ((isInitial()) ? "_i" : "");
        ret += ((isAccept()) ? "_t" : "");
        return ret;
    }

    // //////////////////////////////////////////////////////////////////

    /** Adds a new observed state to this partition. */
    public void addObs(ObsFifoSysState obs) {
        assert !observedStates.contains(obs);
        assert obs.getNumProcesses() == this.numProcesses;
        assert obs.getParent() == null;

        obs.setParent(this);
        observedStates.add(obs);
        cacheObservedParentTransitions(obs);
    }

    /** Adds a new observed state to this partition. */
    public void addAllObs(Set<ObsFifoSysState> states) {
        for (ObsFifoSysState obs : states) {
            addObs(obs);
        }
    }

    /** Removes an observed state from this partition. */
    public void removeObs(ObsFifoSysState s) {
        assert observedStates.contains(s);
        assert s.getParent() == this;

        observedStates.remove(s);
        s.setParent(null);
        // We cannot re-create the cached transitions at this point since the
        // other GFSMStates might be in indeterminate state.
        transitions.clear();
    }

    /** Removes an observed state from this partition. */
    public void removeAllObs(Set<ObsFifoSysState> states) {
        for (ObsFifoSysState s : states) {
            this.removeObs(s);
        }
    }

    /**
     * Returns the set of all observations mapped to this partition that emit e.
     */
    public Set<ObsFifoSysState> getObservedStatesWithTransition(DistEventType e) {
        Set<ObsFifoSysState> ret = new LinkedHashSet<ObsFifoSysState>();
        for (ObsFifoSysState s : observedStates) {
            if (s.getTransitioningEvents().contains(e)) {
                ret.add(s);
            }
        }
        return ret;
    }

    /**
     * Returns the set of all observations mapped to this partition.
     */
    public Set<ObsFifoSysState> getObservedStates() {
        return observedStates;
    }

    // //////////////////////////////////////////////////////////////////

    /** Creates the transitions cache from scratch. */
    private void recreateCachedTransitions() {
        transitions.clear();

        // Update the cached transitions for each observed state in this
        // partition.
        for (ObsFifoSysState s : observedStates) {
            cacheObservedParentTransitions(s);
        }
    }

    /** Updates the cached transitions for a particular observed state. */
    private void cacheObservedParentTransitions(ObsFifoSysState s) {
        for (DistEventType e : s.getTransitioningEvents()) {
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

}
