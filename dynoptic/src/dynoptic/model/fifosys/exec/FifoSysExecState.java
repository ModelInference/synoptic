package dynoptic.model.fifosys.exec;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import dynoptic.model.fifosys.AbsMultiFSMState;
import dynoptic.model.fifosys.channel.channelstate.MutableMultiChState;
import dynoptic.util.Util;

import synoptic.model.channelid.ChannelId;
import synoptic.model.event.DistEventType;
import synoptic.util.InternalSynopticException;

/**
 * This class bundles together the state representation of all of the processes
 * in a FIFO systems, along with state of all of the channels in the system.
 * 
 * @param <MultiFSMState>
 *            Represents the state of _all_ the processes participating in the
 *            system. This does _not_ include channel states.
 */
public class FifoSysExecState<MultiFSMState extends AbsMultiFSMState<MultiFSMState, DistEventType>>
        extends
        AbsMultiFSMState<FifoSysExecState<MultiFSMState>, DistEventType> {

    // The current state of the system.
    MultiFSMState processStates;
    MutableMultiChState channelStates;

    /**
     * Initializes with processStates and empty queues.
     * 
     * @param processStates
     * @param channelIds
     */
    public FifoSysExecState(MultiFSMState processStates,
            List<ChannelId> channelIds) {
        this(processStates, MutableMultiChState.fromChannelIds(channelIds));
    }

    /**
     * Initializes with processStates and specific channelStates.
     * 
     * @param processStates
     * @param channelStates
     */
    public FifoSysExecState(MultiFSMState processStates,
            MutableMultiChState channelStates) {
        super(processStates.getNumProcesses());
        this.processStates = processStates;
        this.channelStates = channelStates;
    }

    // //////////////////////////////////////////////////////////////////

    @Override
    public boolean isInitial() {
        return processStates.isInitial() && channelStates.isEmpty();
    }

    @Override
    public boolean isAccept() {
        return processStates.isAccept() && channelStates.isEmpty();
    }

    /**
     * Returns true if: <br/>
     * 1. the specified pid is in an initial state, and<br/>
     * 2. all queues where this pid is a receiver are empty.
     */
    @Override
    public boolean isInitForPid(int pid) {
        return processStates.isInitForPid(pid)
                && channelStates.isEmptyForPid(pid);
    }

    /**
     * Returns true if: <br/>
     * 1. the specified pid is in an accept state, and<br/>
     * 2. all queues where this pid is a receiver are empty.
     */
    @Override
    public boolean isAcceptForPid(int pid) {
        return processStates.isAcceptForPid(pid)
                && channelStates.isEmptyForPid(pid);
    }

    @Override
    public Set<DistEventType> getTransitioningEvents() {
        return processStates.getTransitioningEvents();
    }

    @Override
    public Set<FifoSysExecState<MultiFSMState>> getNextStates(
            DistEventType event) {
        Set<FifoSysExecState<MultiFSMState>> ret = Util.newSet();

        Set<DistEventType> enabledEvents = this.getEnabledEvents();

        // If the event is not enabled, then no next states are possible.
        if (!enabledEvents.contains(event)) {
            return Collections.<FifoSysExecState<MultiFSMState>> emptySet();
        }

        // Next states are going to have their own version of channels (possible
        // updated with event).
        MutableMultiChState clonedChannelStates = channelStates.clone();

        // Only send and receive events modify channel states.
        if (event.isSendEvent()) {
            // Add the new event/message into the queue.
            clonedChannelStates.enqueue(event);
        } else if (event.isRecvEvent()) {
            // Consume a message from the top of the queue.
            DistEventType recvdEvent = clonedChannelStates.dequeue(event
                    .getChannelId());

            if (!event.equals(recvdEvent)) {
                // The recv event we transitioned on is not an event
                // that was at the top of the channel queue. Since this was
                // checked for in getEnabledEvents, this is an internal error.
                throw new InternalSynopticException(
                        "The recv event we transitioned on is not an event at the top of the channel queue");
            }
        }

        Set<MultiFSMState> newPStates = processStates.getNextStates(event);

        for (MultiFSMState newS : newPStates) {
            // WARNING: we are returning possible next states that share channel
            // states. This is potentially dangerous, but we are expecting a
            // usage in which just one of these states is used and the rest are
            // discarded.
            ret.add(new FifoSysExecState<MultiFSMState>(newS,
                    clonedChannelStates));
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
    public Set<DistEventType> getEnabledEvents() {
        // The Set of possible events -- these are the only events that we have
        // to consider.
        Set<DistEventType> potentialEvents = getTransitioningEvents();

        // Traverse potentialEvents and build ret from all events that are
        // feasible -- all local/send events, and some receive events.
        Set<DistEventType> ret = Util.newSet();
        for (DistEventType e : potentialEvents) {
            // Filter out those events that cannot be received because of
            // incompatible FIFO queue state (i.e., cannot receive 'm' if 'm' is
            // not at the head of the queue).
            if (e.isRecvEvent()) {
                ChannelId chId = e.getChannelId();
                if (!channelStates.peek(chId).equals(e)) {
                    continue;
                }
            }
            ret.add(e);
        }
        return ret;
    }

}
