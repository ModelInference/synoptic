package dynoptic.model.fifosys;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import dynoptic.model.alphabet.EventType;

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
 * @param <State>
 *            Represents the state of _all_ the processes participating in the
 *            system. This does _not_ include channel states (channel state is
 *            maintained by the FifoState<State> instance).
 */
public class FifoSysExecution<State extends IMultiFSMState<State>> {

    // The FIFO system for which this is an execution.
    final FifoSys<State> fifoSys;

    // The current state of the FIFO system -- this includes the state of all
    // the processes and state of all the channels.
    FifoState<State> currentState;

    // Sequence of states that we have executed before (includes currentState)
    final List<FifoState<State>> stateSequence;

    public FifoSysExecution(FifoSys<State> fifoSys) {
        this.fifoSys = fifoSys;

        // We start with the initial state and empty queues.
        this.currentState = new FifoState<State>(fifoSys.getInitState(),
                fifoSys.channelIds);
        this.stateSequence = new LinkedList<FifoState<State>>();
        this.stateSequence.add(currentState);
    }

    // //////////////////////////////////////////////////////////////////

    /**
     * Returns a single next state based on an event transition. If multiple
     * states are possible, then a state is returned non-deterministically.
     * 
     * @param event
     * @return
     */
    public FifoState<State> transition(EventType event) {
        Set<FifoState<State>> following = this.currentState
                .getNextStates(event);
        if (following.size() == 0) {
            throw new InternalSynopticException(
                    "Cannot transition on an event that is not possible from this state.");
        }

        // Get the next state non-deterministically (randomly) based on event.
        ArrayList<FifoState<State>> followingArray = new ArrayList<FifoState<State>>(
                following);
        int i = new Random().nextInt(following.size());
        currentState = followingArray.get(i);
        return currentState;
    }

}
