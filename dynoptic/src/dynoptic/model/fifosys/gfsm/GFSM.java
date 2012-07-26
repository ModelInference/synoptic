package dynoptic.model.fifosys.gfsm;

import java.util.Map;
import java.util.Set;

import dynoptic.model.IFSM;
import dynoptic.model.alphabet.EventType;
import dynoptic.model.alphabet.FSMAlphabet;
import dynoptic.model.fifosys.channel.ChannelId;
import dynoptic.model.fifosys.channel.ChannelState;

/**
 * <p>
 * A GFSM captures the execution space of a CFSM. We use this model to (1)
 * maintain the observed states/event, and (2) to carry out complex operations
 * like refinement.
 * </p>
 * <p>
 * This model is easier to deal with than a CFSM because it captures all global
 * information in a single place (e.g., all enabled transitions from a single
 * global configuration). A CFSM can be thought of as an abstraction of a GFSM
 * -- a CFSM does not deal with concrete observations. The CFSM model is useful
 * for visualization and for input to the McScM model checker.
 * </p>
 * <p>
 * A GFSM can also be thought of as a representation of the operational
 * semantics, or _the_ execution of a CFSM. Note that all CFSM executions are
 * captured by a GFSM.
 * </p>
 * <p>
 * A GFSM is composed of GFSMStates, which are actually _partitions_ of the
 * observed global configurations. Refinement causes a re-shuffling of the
 * observations, and new partitions to be created and added to the GFSM.
 * Therefore, a GFSM is highly mutable. Each mutation of the GFSM is a single
 * complete step of the Dynoptic algorithm.
 * </p>
 */
public class GFSM implements IFSM<GFSMState> {

    // Total number of processes in this CFSM. These are numbered 0 through
    // numProcesses - 1.
    final int numProcesses;

    // srcPid_i -> [(dstPid_j, channel), ... ]
    final Map<ChannelId, ChannelState> channels;

    // Keeps track of the total number of channels.
    final int numChannels;

    public GFSM(int numProcesses, Set<ChannelId> channelIds) {
        //
    }

    // //////////////////////////////////////////////////////////////////

    @Override
    public GFSMState getState() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FSMAlphabet getAlphabet() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public GFSMState transition(EventType event) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<EventType> getEnabledEvents() {
        // TODO Auto-generated method stub
        return null;
    }

}
