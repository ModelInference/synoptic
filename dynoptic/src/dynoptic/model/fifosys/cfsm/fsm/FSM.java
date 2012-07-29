package dynoptic.model.fifosys.cfsm.fsm;

import java.util.Set;

import dynoptic.model.IFSM;
import dynoptic.model.alphabet.EventType;
import dynoptic.model.alphabet.FSMAlphabet;

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

    public FSM(int pid, FSMState initState, FSMState acceptState,
            Set<FSMState> states) {
        assert states != null;
        assert states.contains(initState);
        assert states.contains(acceptState);
        assert acceptState.isAccept();

        // Construct the alphabet from the events associated with each state.
        alphabet = new FSMAlphabet();
        for (FSMState s : states) {
            assert s.getPid() == pid;
            Set<EventType> events = s.getTransitioningEvents();
            if (events.size() != 0) {
                alphabet.addAll(events);
            }
        }

        this.pid = pid;

        this.initState = initState;
        this.acceptState = acceptState;
        this.states = states;
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
