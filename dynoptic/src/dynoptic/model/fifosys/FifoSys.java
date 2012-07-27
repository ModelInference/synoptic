package dynoptic.model.fifosys;

import java.util.Set;

import dynoptic.model.IFSM;
import dynoptic.model.fifosys.channel.ChannelId;

/**
 * Represents an FSM that contains multiple processes that communicate through
 * message passing over channels. This is a common representation for a CFSM, as
 * well as for a GFSM (which represents the global state space of a CFSM).
 */
abstract public class FifoSys<State extends ICFSMState<State>> implements
        IFSM<State> {
    // Total number of processes in the system. These are numbered 0 through
    // numProcesses - 1.
    final protected int numProcesses;

    // The set of all the channels, which are just <pid_i, pid_j> pairs.
    final protected Set<ChannelId> channelIds;

    // Keeps track of the total number of channels.
    final protected int numChannels;

    // //////////////////////////////////////////////////////////////////

    public FifoSys(int numProcesses, Set<ChannelId> channelIds) {
        assert numProcesses > 0;
        assert channelIds != null;

        this.numProcesses = numProcesses;
        this.channelIds = channelIds;
        this.numChannels = channelIds.size();
    }

    /**
     * Returns a new execution of this FIFO that is initialized with the initial
     * state.
     */
    public FifoSysExecution<State> newExecution() {
        return new FifoSysExecution<State>(this);
    }
}
