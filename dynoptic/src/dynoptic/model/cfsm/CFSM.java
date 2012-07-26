package dynoptic.model.cfsm;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import dynoptic.model.IFSM;
import dynoptic.model.alphabet.EventType;
import dynoptic.model.alphabet.FSMAlphabet;
import dynoptic.model.channel.ChannelState;
import dynoptic.model.channel.ChannelId;
import dynoptic.model.fsm.FSM;
import dynoptic.model.fsm.FSMState;

import synoptic.util.InternalSynopticException;

/**
 * Represents a CFSM that consists of some number of FSM processes, which
 * communicate through some number of channels with each other. Channels are
 * uni-directional, involve just two end-points, and do not have to exist
 * between any two processes.
 */
public class CFSM implements IFSM<CFSMState> {
    // Total number of processes in this CFSM. These are numbered 0 through
    // numProcesses - 1.
    final int numProcesses;

    // srcPid_i -> [(dstPid_j, channel), ... ]
    final Map<ChannelId, ChannelState> channels;

    // Keeps track of the total number of channels.
    final int numChannels;

    // pid -> FSM_pid
    final Map<Integer, FSM> fsms;

    // A count of the number of processes/FSMs that still remain to be
    // added/specified.
    int unSpecifiedPids;

    public CFSM(int numProcesses, Set<ChannelId> channelIds) {
        this.numProcesses = numProcesses;

        // Populate the channels map based on the connections map.
        int chCount = 0;
        this.channels = new HashMap<ChannelId, ChannelState>();
        for (ChannelId chId : channelIds) {
            ChannelState chState = new ChannelState(chId);
            channels.put(chId, chState);
            chCount++;
        }
        numChannels = chCount;

        fsms = new HashMap<Integer, FSM>();
        unSpecifiedPids = numProcesses;
    }

    /**
     * Adds a new FSM instance to the CFSM. Once all the FSMs have been added,
     * the CFSM is considered initialized.
     * 
     * @param fsm
     */
    public void addFSM(FSM fsm) {
        assert fsm != null;
        int pid = fsm.getPid();

        // Must be a valid pid (in the right range).
        assert (pid >= 0 && pid < numProcesses);
        // Only allow to set the FSM for a pid once.
        assert !fsms.containsKey(pid);
        // Check that the FSM thinks it is part of the right CFSM (this).
        assert fsm.getCFSM() == this;

        fsms.put(pid, fsm);

        unSpecifiedPids -= 1;
        assert unSpecifiedPids > 0;
    }

    // //////////////////////////////////////////////////////////////////

    @Override
    public CFSMState getState() {
        assert unSpecifiedPids == 0;

        // Capture the current state of all FSMs.
        Map<Integer, FSMState> fsmStates = new LinkedHashMap<Integer, FSMState>();
        for (Integer pid : fsms.keySet()) {
            fsmStates.put(pid, fsms.get(pid).getState());
        }
        // Capture the current state of all the channels.
        Map<ChannelId, ChannelState> clonedChannels = new HashMap<ChannelId, ChannelState>();
        for (ChannelId chId : channels.keySet()) {
            clonedChannels.put(chId, channels.get(chId).clone());
        }
        return new CFSMState(fsmStates, clonedChannels);
    }

    @Override
    public FSMAlphabet getAlphabet() {
        assert unSpecifiedPids == 0;

        // Return the union of the alphabets of all of the FSMs.
        FSMAlphabet ret = new FSMAlphabet();
        for (Integer pid : fsms.keySet()) {
            ret.addAll(fsms.get(pid).getAlphabet());
        }
        return ret;
    }

    @Override
    public Set<EventType> getEnabledEvents() {
        assert unSpecifiedPids == 0;

        Set<EventType> ret = new LinkedHashSet<EventType>();

        // Iterate through all the FSMs and determine the events that they can
        // perform.
        for (FSM fsm : fsms.values()) {
            // Get all the possible events that the FSM thinks it can perform.
            Set<EventType> enabled = fsm.getEnabledEvents();

            // Add events, but filter out those events that cannot be received
            // because of incompatible FIFO queue state (i.e., cannot receive
            // 'm' if 'm' is not at the head of the queue).
            for (EventType e : enabled) {
                if (e.isRecvEvent()) {
                    if (!channels.get(e.getChannelId()).peek().equals(e)) {
                        continue;
                    }
                }
                ret.add(e);
            }
        }
        return ret;
    }

    @Override
    public CFSMState transition(EventType event) {
        assert unSpecifiedPids == 0;

        // Execute the transition on the corresponding FSM/channel.
        int pid;
        if (!event.isCommEvent()) {
            pid = event.getEventPid();
            fsms.get(pid).transition(event);
        } else {
            assert event.isCommEvent();
            ChannelId chId = event.getChannelId();

            if (event.isRecvEvent()) {
                // 1. Update the receiving FSM.
                pid = chId.getDstPid();
                fsms.get(pid).transition(event);

                // 2. Consume a message from the top of the queue.
                EventType recvdEvent = channels.get(chId).dequeue();
                if (!event.equals(recvdEvent)) {
                    // The recv event we transitioned on is not an event
                    // that was at the top of the channel queue. Since we should
                    // have checked for this earlier, this is an internal error.
                    throw new InternalSynopticException(
                            "The recv event we transitioned on is not an event at the top of the channel queue");
                }
            } else {
                assert event.isSendEvent();

                // 1. Update the sending FSM.
                pid = chId.getSrcPid();
                fsms.get(pid).transition(event);

                // 2. Add the new event/message into the queue.
                channels.get(chId).enqueue(event);
            }
        }

        return getState();
    }
}
