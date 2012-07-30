package dynoptic.model.fifosys.exec;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import dynoptic.model.alphabet.EventType;
import dynoptic.model.fifosys.AbsMultiFSMState;
import dynoptic.model.fifosys.FifoSys;

import synoptic.util.InternalSynopticException;

/**
 * Represents the execution of a FIFO system. An execution consists of:
 * 
 * <pre>
 * (1) FifoState = the current state of the FSMs making up the FIFO system that is +
 *                 executing the current state of all the channels in the FIFO system.
 *                 
 * (2) A cache of the sequence of FifoState instances
 *    that we have passed through on this execution
 * </pre>
 * 
 * @param <MultiFSMState>
 *            Represents the state of _all_ the processes participating in the
 *            system. This does _not_ include channel states (channel state is
 *            maintained by the FifoState<State> instance).
 */
public class FifoSysExecution<MultiFSMState extends AbsMultiFSMState<MultiFSMState>> {

    // The FIFO system for which this is an execution.
    final FifoSys<MultiFSMState> fifoSys;

    // The current state of the FIFO system -- this includes the state of all
    // the processes and state of all the channels.
    FifoSysExecState<MultiFSMState> currState;

    // Sequence of states that we have executed before (includes currentState)
    final List<FifoSysExecState<MultiFSMState>> stateSequence;

    public FifoSysExecution(FifoSys<MultiFSMState> fifoSys,
            MultiFSMState initState) {
        this.fifoSys = fifoSys;

        // We start with the initial state and empty queues.
        this.currState = new FifoSysExecState<MultiFSMState>(initState,
                fifoSys.getChannelIds());
        this.stateSequence = new LinkedList<FifoSysExecState<MultiFSMState>>();
        this.stateSequence.add(currState);
    }

    // //////////////////////////////////////////////////////////////////

    public FifoSysExecState<MultiFSMState> getCurrentState() {
        return currState;
    }

    /**
     * Returns a single next state based on an event transition. If multiple
     * states are possible, then a state is returned non-deterministically.
     * 
     * @param event
     * @return
     */
    public FifoSysExecState<MultiFSMState> transition(EventType event) {
        Set<FifoSysExecState<MultiFSMState>> following = this.currState
                .getNextStates(event);
        if (following.size() == 0) {
            throw new InternalSynopticException(
                    "Cannot transition on an event that is not possible from this state.");
        }

        // Get the next state non-deterministically (randomly) based on event.
        ArrayList<FifoSysExecState<MultiFSMState>> followingArray = new ArrayList<FifoSysExecState<MultiFSMState>>(
                following);
        int i = new Random().nextInt(following.size());
        currState = followingArray.get(i);

        this.stateSequence.add(currState);
        return currState;
    }

}
