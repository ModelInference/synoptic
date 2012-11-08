package dynoptic.model.fifosys.gfsm;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import dynoptic.main.DynopticMain;
import dynoptic.model.fifosys.AbsMultiFSMState;
import dynoptic.model.fifosys.gfsm.observed.ObsDistEventType;
import dynoptic.model.fifosys.gfsm.observed.fifosys.ObsFifoSysState;
import dynoptic.util.Util;

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
public class GFSMState extends AbsMultiFSMState<GFSMState, DistEventType> {
    // Set of observed state instances.
    private final Set<ObsFifoSysState> observedStates;

    // CACHE optimization: the set of abstract transitions induced by the
    // concrete transitions. This is a cache of the ground truth.
    private final Map<DistEventType, Set<GFSMState>> transitions;

    public GFSMState(int numProcesses) {
        this(numProcesses, Util.<ObsFifoSysState> newSet());
    }

    /**
     * Creates a GFSMState based off an observations set that will be _used_
     * internally.
     */
    public GFSMState(int numProcesses, Set<ObsFifoSysState> observedStates) {
        super(numProcesses);
        // NOTE: we do not create a new set for observed states.
        this.observedStates = observedStates;
        this.transitions = Util.newMap();

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
        return getStatesThatEvalToTrue(observedStates, fnIsInitialState);
    }

    /**
     * Returns the set of all observations that are accepting/terminal in this
     * partition.
     */
    public Set<ObsFifoSysState> getTerminalObs() {
        return getStatesThatEvalToTrue(observedStates, fnIsAcceptState);
    }

    /**
     * Returns the set of all observations that are accepting/terminal in this
     * partition.
     */
    public Set<ObsFifoSysState> getTerminalObsForPid(int pid) {
        return getStatesThatEvalToTrueWithPid(observedStates, fnIsAcceptForPid,
                pid);

    }

    public Set<ObsFifoSysState> getInitialObsForPid(int pid) {
        return getStatesThatEvalToTrueWithPid(observedStates,
                fnIsInitialForPid, pid);
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
        if (this.transitions.isEmpty()) {
            recreateCachedTransitions();
        }
        return transitions.keySet();
    }

    @Override
    public Set<GFSMState> getNextStates(DistEventType event) {
        if (this.transitions.isEmpty()) {
            recreateCachedTransitions();
        }

        assert transitions.containsKey(event);

        return transitions.get(event);
    }

    public Set<GFSMState> getNextStates(ObsDistEventType event) {
        return this.getNextStates(event.getDistEType());
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

    public String toLongString() {
        String ret = toString();
        ret += ((isInitial()) ? "_i" : "");
        ret += ((isAccept()) ? "_t" : "");
        recreateCachedTransitions();
        for (Entry<DistEventType, Set<GFSMState>> tx : transitions.entrySet()) {
            ret += "\n\t -- " + tx.getKey().toString() + " --> [";
            for (GFSMState child : tx.getValue()) {
                ret += child.toString() + ", ";
            }
            ret += "]";
        }
        return ret;
    }

    public String toIntermediateString() {
        return "Part-" + observedStates.size() + "-" + this.hashCode();
    }

    @Override
    public String toString() {
        return this.observedStates.toString();
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

        // TODO: ideally we would also clear transitions of partitions that
        // contain ObsFifoSysState instances that precede s. However, currently,
        // we only maintain children of ObsFifoSysState instances, and not their
        // parents.
    }

    /** Removes an observed state from this partition. */
    public void removeAllObs(Set<ObsFifoSysState> toRemove) {
        // Make sure that this partition will contain at least one observation
        // after we remove the toRemove observations.
        assert observedStates.size() > toRemove.size();

        for (ObsFifoSysState s : toRemove) {
            this.removeObs(s);
        }
    }

    /**
     * Returns the set of all observations mapped to this partition that emit e.
     */
    public Set<ObsFifoSysState> getObservedStatesWithTransition(DistEventType e) {
        Set<ObsFifoSysState> ret = Util.newSet();
        for (ObsFifoSysState s : observedStates) {
            if (s.hasTransitionOn(e)) {
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

    /** Creates the transitions cache from scratch. */
    public void recreateCachedTransitions() {
        transitions.clear();

        // Update the cached transitions for each observed state in this
        // partition.
        for (ObsFifoSysState s : observedStates) {
            cacheObservedParentTransitions(s);
        }
    }

    // //////////////////////////////////////////////////////////////////

    /** Updates the cached transitions for a particular observed state. */
    private void cacheObservedParentTransitions(ObsFifoSysState s) {
        for (DistEventType e : s.getTransitioningEvents()) {
            GFSMState nextPartition = s.getNextState(e).getParent();
            assert nextPartition != null;

            Set<GFSMState> partitions;
            if (!transitions.containsKey(e)) {
                partitions = Util.newSet();
                transitions.put(e, partitions);
            } else {
                partitions = transitions.get(e);
            }
            partitions.add(nextPartition);
        }
    }
}
