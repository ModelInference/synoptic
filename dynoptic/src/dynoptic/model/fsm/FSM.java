package dynoptic.model.fsm;

import java.util.Set;

import dynoptic.model.IFSM;
import dynoptic.model.alphabet.EventType;
import dynoptic.model.alphabet.FSMAlphabet;
import dynoptic.model.cfsm.CFSM;

/**
 * This class models FSMs that make up a CFSM. A few key characteristics:
 * 
 * <pre>
 * 1. It does not maintain channel state. This is done in the parent CFSM.
 * 2. Almost all fields are immutable. Pre-construct all FSMState instances prior to constructing this FSM.
 * 3. It does not maintain inter-state transitions. These are managed by FSMState instances.
 * </pre>
 */
public class FSM implements IFSM<FSMState> {
    // An instance of CFSM that this FSM corresponds to.
    final CFSM cfsm;

    // The process id of this FSM.
    final int pid;

    // This FSM's alphabet.
    final FSMAlphabet alphabet;

    // Initial, accept, and set of all feasible states. States manage
    // transitions internally.
    final FSMState initState;
    final FSMState acceptState;
    final Set<FSMState> states;

    // The current state.
    FSMState fsmState;

    public FSM(CFSM cfsm, int pid, FSMAlphabet alphabet, FSMState initState,
            FSMState acceptState, Set<FSMState> states) {
        assert states.contains(initState);
        assert states.contains(acceptState);

        this.cfsm = cfsm;
        this.pid = pid;
        this.alphabet = alphabet;

        this.initState = initState;
        this.acceptState = acceptState;
        this.states = states;

        // We start off in the initial state.
        this.fsmState = initState;
    }

    public CFSM getCFSM() {
        return this.cfsm;
    }

    public int getPid() {
        return this.pid;
    }

    // //////////////////////////////////////////////////////////////////

    @Override
    public Set<EventType> getEnabledEvents() {
        return this.fsmState.getPossibleEvents();
    }

    @Override
    public FSMAlphabet getAlphabet() {
        return alphabet;
    }

    @Override
    public FSMState getState() {
        return fsmState;
    }

    @Override
    public FSMState transition(EventType event) {
        this.fsmState = this.fsmState.getNextState(event);
        return this.fsmState;
    }
}
