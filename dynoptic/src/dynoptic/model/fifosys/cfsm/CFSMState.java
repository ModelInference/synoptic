package dynoptic.model.fifosys.cfsm;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import dynoptic.model.alphabet.EventType;
import dynoptic.model.fifosys.AbsMultiFSMState;
import dynoptic.model.fifosys.cfsm.fsm.FSMState;

/**
 * <p>
 * This is an immutable class.
 * </p>
 * Captures the current state of a CFSM -- essentially a vector of FSMStates --
 * without the channel state.
 */
public final class CFSMState extends AbsMultiFSMState<CFSMState> {

    // List of FSMStates, ordered according to process IDs: 0 - (size-1).
    final List<FSMState> fsmStates;

    /**
     * Creates and returns a CFSMState instance for each item in the list of
     * lists of FSMState instances.
     */
    public static Set<CFSMState> CFSMStatesFromFSMListLists(
            List<List<FSMState>> list) {
        Set<CFSMState> ret = new LinkedHashSet<CFSMState>();
        for (List<FSMState> item : list) {
            ret.add(new CFSMState(item));
        }
        return ret;
    }

    // //////////////////////////////////////////////////////////////////

    /** Single FSM state. */
    public CFSMState(FSMState s) {
        super(1);
        this.fsmStates = new ArrayList<FSMState>();
        this.fsmStates.add(s);
    }

    /** Multiple FSMs state. */
    public CFSMState(List<FSMState> fsmStates) {
        super(fsmStates.size());
        this.fsmStates = fsmStates;
    }

    // //////////////////////////////////////////////////////////////////

    /**
     * Whether or not this state is an initial state for a CFSM:
     * 
     * <pre>
     * 1. all the FSMS making up the CFSM are in initial state, and
     * 2. all the queue are empty.
     * </pre>
     */
    @Override
    public boolean isInitial() {
        return statesEvalToTrue(fsmStates, fnInitialState);
    }

    /**
     * Whether or not this state is a valid accepting state for a CFSM:
     * 
     * <pre>
     * 1. all the FSMS making up the CFSM are in accept state, and
     * 2. all the queue are empty.
     * </pre>
     */
    @Override
    public boolean isAccept() {
        return statesEvalToTrue(fsmStates, fnAcceptState);
    }

    @Override
    public Set<EventType> getTransitioningEvents() {
        Set<EventType> ret = new LinkedHashSet<EventType>();
        for (FSMState p : fsmStates) {
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
    public boolean isInitialForPid(int pid) {
        assert pid >= 0 && pid < fsmStates.size();
        return fsmStates.get(pid).isInitial();
    }

    @Override
    public boolean isAcceptForPid(int pid) {
        assert pid >= 0 && pid < fsmStates.size();
        return fsmStates.get(pid).isAccept();
    }
}
