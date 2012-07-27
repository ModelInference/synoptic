package dynoptic.model.fifosys.gfsm;

import java.util.Map;
import java.util.Set;

import dynoptic.model.alphabet.EventType;
import dynoptic.model.fifosys.IMultiFSMState;
import dynoptic.model.fifosys.cfsm.fsm.FSMState;

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
    // TODO: include these.

    // CACHE optimization: whether or not any of the observed states were
    // terminal.
    boolean isAccept;

    // CACHE optimization: the set of abstract transitions induced by the
    // concrete transitions. This is merely a cached version of the ground
    // truth.
    Map<EventType, Set<FSMState>> transitions;

    public GFSMState() {
        //
    }

    // //////////////////////////////////////////////////////////////////

    @Override
    public boolean isAccept() {
        return isAccept;
    }

    @Override
    public Set<EventType> getTransitioningEvents() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<GFSMState> getNextStates(EventType event) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isAcceptForPid(int pid) {
        // TODO Auto-generated method stub
        return false;
    }

}
