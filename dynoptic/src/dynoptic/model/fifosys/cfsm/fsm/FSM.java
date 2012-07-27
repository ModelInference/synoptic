package dynoptic.model.fifosys.cfsm.fsm;

import java.util.Set;

import dynoptic.model.IFSM;
import dynoptic.model.alphabet.FSMAlphabet;
import dynoptic.model.fifosys.cfsm.CFSM;

/**
 * This class models FSMs that make up a CFSM. A few key characteristics:
 * 
 * <pre>
 * 1. It does not maintain channel state. This is done by FifoState/FifoSysExecution instances.
 * 2. Almost all fields are immutable. Pre-construct all FSMState instances prior to constructing this FSM.
 * 3. It does not maintain inter-state transitions. These are managed by FSMState instances.
 * </pre>
 */
public class FSM implements IFSM<FSMState> {
    // An instance of CFSM that this FSM corresponds to.
    final CFSM cfsm;

    // The process id of this FSM in the CFSM.
    final int pid;

    // This FSM's alphabet.
    final FSMAlphabet alphabet;

    // The set of all states associated with this FSM. This includes initial and
    // accept states. States manage transitions internally.
    final Set<FSMState> states;

    // Initial, and accept states.
    final FSMState initState;
    final FSMState acceptState;

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
    }

    public CFSM getCFSM() {
        return this.cfsm;
    }

    public int getPid() {
        return this.pid;
    }

    // //////////////////////////////////////////////////////////////////

    @Override
    public FSMAlphabet getAlphabet() {
        return alphabet;
    }

    @Override
    public FSMState getInitState() {
        // Set<FSMState> ret = new LinkedHashSet<FSMState>();
        // ret.add(initState);
        // return ret;
        return initState;
    }

    @Override
    public FSMState getAcceptState() {
        // Set<FSMState> ret = new LinkedHashSet<FSMState>();
        // ret.add(acceptState);
        // return ret;
        return acceptState;
    }
}
