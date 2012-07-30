package dynoptic.model.fifosys;

import java.util.LinkedHashSet;
import java.util.Set;

import dynoptic.model.AbsFSM;
import dynoptic.model.fifosys.channel.ChannelId;
import dynoptic.model.fifosys.exec.FifoSysExecution;

/**
 * <p>
 * Represents an FSM that contains multiple processes that communicate through
 * message passing over channels. This is a common representation for a CFSM, as
 * well as for a GFSM (which represents the global state space of a CFSM).
 * </p>
 * <p>
 * Note that a FifoSys is merely a representation of a machine, and does not
 * maintain execution instance state. FifoSysExecution does this.
 * </p>
 * 
 * @param <MultiFSMState>
 *            Represents the state of _all_ the processes participating in the
 *            system. This does _not_ include channel states.
 */
abstract public class FifoSys<MultiFSMState extends AbsMultiFSMState<MultiFSMState>>
        extends AbsFSM<MultiFSMState> {
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

        // Make sure the channel IDs reference valid process IDs.
        for (ChannelId chId : channelIds) {
            assert chId.getSrcPid() >= 0 && chId.getSrcPid() < numProcesses;
            assert chId.getDstPid() >= 0 && chId.getDstPid() < numProcesses;
        }

        this.numProcesses = numProcesses;
        this.channelIds = channelIds;
        this.numChannels = channelIds.size();
    }

    /**
     * Returns a new set of executions of this FIFO. Each execution is
     * initialized to an init state of this FIFO system. Multiple executions are
     * returned when there are multiple possible init states.
     */
    public Set<FifoSysExecution<MultiFSMState>> newExecution() {
        Set<FifoSysExecution<MultiFSMState>> ret = new LinkedHashSet<FifoSysExecution<MultiFSMState>>();
        for (MultiFSMState init : this.getInitStates()) {
            ret.add(new FifoSysExecution<MultiFSMState>(this, init));
        }
        return ret;
    }

    public Set<ChannelId> getChannelIds() {
        return channelIds;
    }
}
