package dynoptic.model.fifosys;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import dynoptic.main.DynopticMain;
import dynoptic.model.AbsFSM;
import dynoptic.model.fifosys.channel.channelid.ChannelId;
import dynoptic.model.fifosys.exec.FifoSysExecution;

/**
 * <p>
 * Represents an FSM that contains multiple processes that communicate through
 * message passing over channels. This is a common representation for a CFSM, as
 * well as for a GFSM, which represents the global state space of a CFSM.
 * </p>
 * <p>
 * Note that a FifoSys is merely a representation of a machine, and does not
 * maintain execution instance state. For example, it does not keep track of
 * queue states. Instances of FifoSysExecution maintain this kind of state.
 * </p>
 * 
 * @param <MultiFSMState>
 *            Represents the state of _all_ the processes participating in the
 *            system. This does _not_ include channel states.
 */
abstract public class FifoSys<MultiFSMState extends AbsMultiFSMState<MultiFSMState>>
        extends AbsFSM<MultiFSMState> {
    // Total number of processes in the system. These are numbered 0 through
    // (numProcesses - 1).
    final protected int numProcesses;

    // The list of all the channels, which are <pid_i, pid_j> pairs. This
    // list is ordered according to the scmIds used by each of the channelIds.
    final protected List<ChannelId> channelIds;

    // //////////////////////////////////////////////////////////////////

    public FifoSys(int numProcesses, List<ChannelId> channelIds) {
        super();
        assert numProcesses > 0;
        assert channelIds != null;

        if (DynopticMain.assertsOn) {
            // 1. Make sure the channel IDs reference valid process IDs.
            // 2. Check that channel IDs are ordered according to scmIds.
            for (int i = 0; i < channelIds.size(); i++) {
                ChannelId chId = channelIds.get(i);
                assert chId.getSrcPid() >= 0 && chId.getSrcPid() < numProcesses;
                assert chId.getDstPid() >= 0 && chId.getDstPid() < numProcesses;
                assert chId.getScmId() == i;
            }
        }

        this.numProcesses = numProcesses;
        this.channelIds = channelIds;
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

    public List<ChannelId> getChannelIds() {
        return channelIds;
    }

    public int getNumProcesses() {
        return numProcesses;
    }
}
