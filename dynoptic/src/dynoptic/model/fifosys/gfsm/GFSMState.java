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
    private final Set<ObservedFifoSysState> observedStates;

    // CACHE optimization: the set of abstract transitions induced by the
    // concrete transitions. This is merely a cached version of the ground
    // truth.
    private final Map<EventType, Set<GFSMState>> transitions;

    public GFSMState(int numProcesses) {
        super(numProcesses);
        observedStates = new LinkedHashSet<ObservedFifoSysState>();
        transitions = new LinkedHashMap<EventType, Set<GFSMState>>();
    }

    // //////////////////////////////////////////////////////////////////

    @Override
    public boolean isInitial() {
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

        return atLeastOneStatePidEvalTrue(this.observedStates,
                fnIsAcceptForPid, pid);
    }

    @Override
    public boolean isInitialForPid(int pid) {
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
    public void addObs(ObservedFifoSysState s) {
        assert !observedStates.contains(s);
        assert s.getNumProcesses() == this.numProcesses;
        assert s.getParent() == null;

        observedStates.add(s);
        s.setParent(this);
        cacheObservedParentTransitions(s);
    }

    /** Adds a new observed state to this partition. */
    public void addAllObs(Set<ObservedFifoSysState> states) {
        for (ObservedFifoSysState s : states) {
            addObs(s);
        }
    }

    /** Removes an observed state from this partition. */
    public void removeObs(ObservedFifoSysState s) {
        assert observedStates.contains(s);
        assert s.getParent() == this;

        observedStates.remove(s);
        s.setParent(null);
        recreateCachedTransitions();
    }

    // //////////////////////////////////////////////////////////////////

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

}
