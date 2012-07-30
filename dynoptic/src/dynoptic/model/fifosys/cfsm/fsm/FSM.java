package dynoptic.model.fifosys.cfsm.fsm;

import java.util.Set;

import dynoptic.model.AbsFSM;
import dynoptic.model.alphabet.EventType;

/**
 * This class models FSMs that make up a CFSM. A few key characteristics:
 * 
 * <pre>
 * 1. It does not maintain channel state. This is done by FifoState/FifoSysExecution instances.
 * 2. Almost all fields are immutable. Pre-construct all FSMState instances prior to constructing this FSM.
 * 3. It does not maintain inter-state transitions. These are managed by FSMState instances.
 * </pre>
 */
public class FSM extends AbsFSM<FSMState> {
    // The process id of this FSM in the CFSM.
    final int pid;

    public FSM(int pid, FSMState initState, FSMState acceptState,
            Set<FSMState> states) {
        super();

        assert states != null;
        assert states.contains(initState);
        assert states.contains(acceptState);
        assert acceptState.isAccept();

        this.pid = pid;
        this.states.addAll(states);
        this.initStates.add(initState);
        this.acceptStates.add(acceptState);

        // Construct the alphabet from the events associated with each state.
        for (FSMState s : states) {
            assert s.getPid() == pid;
            Set<EventType> events = s.getTransitioningEvents();
            if (events.size() != 0) {
                alphabet.addAll(events);
            }
        }
    }

    // //////////////////////////////////////////////////////////////////

    public int getPid() {
        return this.pid;
    }
}
