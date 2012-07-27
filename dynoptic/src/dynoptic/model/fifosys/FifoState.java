package dynoptic.model.fifosys;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import dynoptic.model.alphabet.EventType;
import dynoptic.model.fifosys.channel.ChannelId;
import dynoptic.model.fifosys.channel.ChannelState;

import synoptic.util.InternalSynopticException;

/**
 * This class bundles together the state representation of all of the processes
 * in a FIFO systems, along with state of all of the channels in the system.
 * 
 * @param <State>
 */
public class FifoState<State extends ICFSMState<State>> implements
        ICFSMState<FifoState<State>> {

    // The current state of the system.
    State processStates;

    Map<ChannelId, ChannelState> channelStates;

    public FifoState(State processStates, Set<ChannelId> channelIds) {
        this.processStates = processStates;

        // Populate the channels map based on the channelIds.
        this.channelStates = new HashMap<ChannelId, ChannelState>();
        for (ChannelId chId : channelIds) {
            ChannelState chState = new ChannelState(chId);
            channelStates.put(chId, chState);
        }
    }

    public FifoState(State processStates,
            Map<ChannelId, ChannelState> channelStates) {
        this.processStates = processStates;
        this.channelStates = channelStates;
    }

    // //////////////////////////////////////////////////////////////////

    @Override
    public boolean isAccept() {
        if (!processStates.isAccept()) {
            return false;
        }
        for (ChannelState chState : channelStates.values()) {
            if (chState.size() != 0) {
                return false;
            }
        }
        return true;
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
    @Override
    public boolean isAcceptForPid(int pid) {
        if (!processStates.isAcceptForPid(pid)) {
            return false;
        }

        for (ChannelId chId : channelStates.keySet()) {
            if (chId.getDstPid() == pid && channelStates.get(chId).size() != 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Set<EventType> getTransitioningEvents() {
        return processStates.getTransitioningEvents();
    }

    @Override
    public Set<FifoState<State>> getNextStates(EventType event) {
        Set<FifoState<State>> ret = new LinkedHashSet<FifoState<State>>();

        Set<EventType> enabledEvents = this.getEnabledEvents();

        // If the event is not enabled, then no next states are possible.
        if (!enabledEvents.contains(event)) {
            return Collections.<FifoState<State>> emptySet();
        }

        // Next states are going to have their own version of channels (possible
        // updated with event).
        Map<ChannelId, ChannelState> clonedChannelStates = cloneChannelStates();

        // Only send and receive events modify channel states.
        if (event.isSendEvent()) {
            // Add the new event/message into the queue.
            clonedChannelStates.get(event.getChannelId()).enqueue(event);
        } else if (event.isRecvEvent()) {
            // Consume a message from the top of the queue.
            EventType recvdEvent = clonedChannelStates
                    .get(event.getChannelId()).dequeue();

            if (!event.equals(recvdEvent)) {
                // The recv event we transitioned on is not an event
                // that was at the top of the channel queue. Since this was
                // checked for in getEnabledEvents, this is an internal error.
                throw new InternalSynopticException(
                        "The recv event we transitioned on is not an event at the top of the channel queue");
            }
        }

        Set<State> newPStates = processStates.getNextStates(event);

        for (State newS : newPStates) {
            // WARNING: we are returning possible next states that share channel
            // states. This is potentially dangerous, but we are expecting a
            // usage in which just one of these states is used and the rest are
            // discarded.
            ret.add(new FifoState<State>(newS, clonedChannelStates));
        }

        return ret;
    }

    // //////////////////////////////////////////////////////////////////

    // public FifoState clone() {
    // Capture the current state of all the channels.
    // Map<ChannelId, ChannelState> clonedChannels = new HashMap<ChannelId,
    // ChannelState>();
    // for (ChannelId chId : channelStates.keySet()) {
    // clonedChannels.put(chId, channelStates.get(chId).clone());
    // }
    // return new CFSMState(processStates, clonedChannels);
    // return new FifoState(processStates, clonedChannels);
    // }

    /**
     * Returns the set of events that are _feasible_ from this FIFO state. That
     * is, those events that this FSM can transition on based on its current
     * state.
     */
    public Set<EventType> getEnabledEvents() {
        // The Set of possible events -- these are the only events that we have
        // to consider.
        Set<EventType> potentialEvents = getTransitioningEvents();

        // Traverse potentialEvents and build ret from all events that are
        // feasible -- all local/send events, and some receive events.
        Set<EventType> ret = new LinkedHashSet<EventType>();
        for (EventType e : potentialEvents) {
            // Filter out those events that cannot be received because of
            // incompatible FIFO queue state (i.e., cannot receive 'm' if 'm' is
            // not at the head of the queue).
            if (e.isRecvEvent()) {
                ChannelId chId = e.getChannelId();
                if (!channelStates.get(chId).peek().equals(e)) {
                    continue;
                }
            }
            ret.add(e);
        }
        return ret;
    }

    // //////////////////////////////////////////////////////////////////

    /**
     * Clone and return the FIFO channel states.
     */
    private Map<ChannelId, ChannelState> cloneChannelStates() {
        // Capture the current state of all the channels.
        Map<ChannelId, ChannelState> clonedChannels = new HashMap<ChannelId, ChannelState>();
        for (ChannelId chId : channelStates.keySet()) {
            clonedChannels.put(chId, channelStates.get(chId).clone());
        }
        return clonedChannels;
    }
}
