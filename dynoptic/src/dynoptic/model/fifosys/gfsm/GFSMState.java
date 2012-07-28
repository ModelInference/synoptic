package dynoptic.model.fifosys.gfsm;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import dynoptic.model.alphabet.EventType;
import dynoptic.model.fifosys.IMultiFSMState;
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
public class GFSMState implements IMultiFSMState<GFSMState> {
    // This is the set of observed state instances.
    final Set<ObservedFifoSysState> observedStates;

    // CACHE optimization: the set of abstract transitions induced by the
    // concrete transitions. This is merely a cached version of the ground
    // truth.
    final Map<EventType, Set<GFSMState>> transitions;

    public GFSMState() {
        observedStates = new LinkedHashSet<ObservedFifoSysState>();
        transitions = new LinkedHashMap<EventType, Set<GFSMState>>();
    }

    // //////////////////////////////////////////////////////////////////

    @Override
    public boolean isAccept() {
        // We need at least one observed state as accepting/terminal for this
        // partition to be accepting.
        for (ObservedFifoSysState s : observedStates) {
            if (s.isAccept()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<EventType> getTransitioningEvents() {
        return transitions.keySet();
    }

    @Override
    public Set<GFSMState> getNextStates(EventType event) {
        Set<GFSMState> ret = new LinkedHashSet<GFSMState>();
        for (Set<GFSMState> following : transitions.values()) {
            ret.addAll(following);
        }
        return ret;
    }

    @Override
    public boolean isAcceptForPid(int pid) {
        for (ObservedFifoSysState s : observedStates) {
            if (s.isAcceptForPid(pid)) {
                return true;
            }
        }
        return false;
    }

    // //////////////////////////////////////////////////////////////////

    /** Adds a new observed state to this partition. */
    public void add(ObservedFifoSysState s) {
        assert !observedStates.contains(s);
        observedStates.add(s);
        cacheObservedParentTransitions(s);
    }

    /** Adds a new observed state to this partition. */
    public void addAll(Set<ObservedFifoSysState> states) {
        for (ObservedFifoSysState s : states) {
            add(s);
        }
    }

    /** Adds a new observed state to this partition. */
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

}
