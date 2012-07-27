package dynoptic.model.fifosys.cfsm;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import dynoptic.model.alphabet.EventType;
import dynoptic.model.fifosys.ICFSMState;
import dynoptic.model.fifosys.cfsm.fsm.FSMState;

/**
 * <p>
 * This is an immutable class.
 * </p>
 * Captures the current state of a CFSM without the channel state.
 */
public final class CFSMState implements ICFSMState<CFSMState> {

    final Map<Integer, FSMState> fsmStates;

    public CFSMState(Map<Integer, FSMState> fsmStates) {
        this.fsmStates = fsmStates;
    }

    // //////////////////////////////////////////////////////////////////

    /**
     * Whether or not this state is a valid accepting state for a CFSM. The
     * conservative definition of this is:
     * 
     * <pre>
     * (1) all the FSMS making up the CFSM are in accept state, and
     * (2) all the queue are empty.
     * </pre>
     */
    @Override
    public boolean isAccept() {
        for (FSMState state : fsmStates.values()) {
            if (!state.isAccept()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Set<EventType> getTransitioningEvents() {
        Set<EventType> ret = new LinkedHashSet<EventType>();
        for (FSMState p : fsmStates.values()) {
            ret.addAll(p.getTransitioningEvents());
        }
        return ret;
    }

    @Override
    public Set<CFSMState> getNextStates(EventType event) {
        // TODO: have to take a cross product of all the possible sub-FSM states
        // to derive possible CFSM states.
        return null;

        // Set<CFSMState> ret = new LinkedHashSet<CFSMState>();
        // for (FSMState p : fsmStates.values()) {
        // ret.addAll(p.getNextStates(event));
        // }
        // return ret;
    }

    @Override
    public boolean isAcceptForPid(int pid) {
        assert fsmStates.containsKey(pid);

        return fsmStates.get(pid).isAccept();
    }

}
