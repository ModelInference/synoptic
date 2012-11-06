package dynoptic.model.fifosys.cfsm;

import java.util.List;
import java.util.Set;

import dynoptic.model.fifosys.AbsMultiFSMState;
import dynoptic.model.fifosys.cfsm.fsm.FSMState;
import dynoptic.util.Util;

import synoptic.model.event.DistEventType;

/**
 * <p>
 * This is an immutable class.
 * </p>
 * Captures the current state of a CFSM -- essentially a vector of FSMStates --
 * without the channel state.
 */
public final class CFSMState extends AbsMultiFSMState<CFSMState, DistEventType> {

    // List of FSMStates, ordered according to process IDs: 0 - (size-1).
    private final List<FSMState> fsmStates;

    /**
     * Creates and returns a CFSMState instance for each item in the list of
     * lists of FSMState instances.
     */
    public static Set<CFSMState> CFSMStatesFromFSMListLists(
            List<List<FSMState>> list) {
        Set<CFSMState> ret = Util.newSet();
        for (List<FSMState> item : list) {
            ret.add(new CFSMState(item));
        }
        return ret;
    }

    // //////////////////////////////////////////////////////////////////

    /** Single FSM state. */
    public CFSMState(FSMState s) {
        super(1);
        this.fsmStates = Util.newList();
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
        return statesEvalToTrue(fsmStates, fnIsInitialState);
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
        return statesEvalToTrue(fsmStates, fnIsAcceptState);
    }

    @Override
    public Set<DistEventType> getTransitioningEvents() {
        Set<DistEventType> ret = Util.newSet();
        for (FSMState p : fsmStates) {
            ret.addAll(p.getTransitioningEvents());
        }
        return ret;
    }

    @Override
    public Set<CFSMState> getNextStates(DistEventType event) {
        // TODO: have to take a cross product of all the possible sub-FSM states
        // to derive possible CFSM states.
        throw new RuntimeException("getNextStates unimplemented for CFSMState");
    }

    @Override
    public boolean isInitForPid(int pid) {
        assert pid >= 0 && pid < fsmStates.size();
        return fsmStates.get(pid).isInitial();
    }

    @Override
    public boolean isAcceptForPid(int pid) {
        assert pid >= 0 && pid < fsmStates.size();
        return fsmStates.get(pid).isAccept();
    }

    // //////////////////////////////////////////////////////////////////

    public FSMState getFSMState(int pid) {
        return fsmStates.get(pid);
    }
}
