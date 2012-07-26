package dynoptic.model.fifosys.cfsm;

import java.util.Map;

import dynoptic.model.IFSMState;
import dynoptic.model.fifosys.cfsm.fsm.FSMState;
import dynoptic.model.fifosys.channel.ChannelId;
import dynoptic.model.fifosys.channel.ChannelState;

/**
 * <p>
 * This is an immutable class.
 * </p>
 * Captures the current state of a CFSM.
 */
public final class CFSMState implements IFSMState {

    final Map<Integer, FSMState> fsmStates;
    final Map<ChannelId, ChannelState> channels;

    public CFSMState(Map<Integer, FSMState> fsmStates,
            Map<ChannelId, ChannelState> channels) {
        this.fsmStates = fsmStates;
        this.channels = channels;
    }

    /**
     * Whether or not in this state:
     * 
     * <pre>
     * (1) the specified pid is in an accept state, and
     * (2) all queues where this pid is a receiver are empty.
     * </pre>
     * 
     * @param pid
     * @return
     */
    public boolean isAcceptForPid(int pid) {
        if (!fsmStates.get(pid).isAccept()) {
            return false;
        }
        for (ChannelId chId : channels.keySet()) {
            if (chId.getDstPid() == pid && channels.get(chId).size() != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Whether or not this state is a valid accepting state for a CFSM. The
     * conservative definition of this is:
     * 
     * <pre>
     * (1) all the FSMS making up the CFSM are in accept state, and
     * (2) all the queue are empty.
     * </pre>
     */
    public boolean isAccept() {
        for (FSMState state : fsmStates.values()) {
            if (!state.isAccept()) {
                return false;
            }
        }
        for (ChannelState chState : channels.values()) {
            if (chState.size() != 0) {
                return false;
            }
        }
        return true;
    }
}
