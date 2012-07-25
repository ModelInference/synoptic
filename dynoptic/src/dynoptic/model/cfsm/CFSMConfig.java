package dynoptic.model.cfsm;

import java.util.Map;

import dynoptic.model.IFSMConfig;
import dynoptic.model.channel.ChannelConfig;
import dynoptic.model.channel.ChannelId;
import dynoptic.model.fsm.FSMState;

/**
 * <p>
 * This is an immutable class.
 * </p>
 * Captures the current state of a CFSM.
 */
public final class CFSMConfig implements IFSMConfig {

    final Map<Integer, FSMState> fsmStates;
    final Map<ChannelId, ChannelConfig> channels;

    public CFSMConfig(Map<Integer, FSMState> fsmStates,
            Map<ChannelId, ChannelConfig> channels) {
        this.fsmStates = fsmStates;
        this.channels = channels;
    }

    /**
     * Whether or not this configuration represents a valid accepting
     * configuration for a CFSM. The conservative definition of this is:
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
        for (ChannelConfig chConfig : channels.values()) {
            if (chConfig.size() != 0) {
                return false;
            }
        }
        return true;
    }
}
