package dynoptic.model.fifosys.cfsm.fsm;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import dynoptic.model.AbsFSMState;
import dynoptic.model.fifosys.channel.channelid.LocalEventsChannelId;

import synoptic.model.event.DistEventType;

/**
 * <p>
 * Represents a state of a simple FSM that is an NFA.
 * </p>
 * <p>
 * An FSMState maintains abstract transitions to other FSMState instances. It is
 * completely disassociated from the observed transitions and states. Note that
 * an FSMState can have multiple transitions on the same event that go to
 * different FSMState instances (the FSM can be an NFA).
 * </p>
 */
public class FSMState extends AbsFSMState<FSMState> {
    // Whether or not this state is an accepting/initial state.
    private final boolean isAccept;
    private final boolean isInitial;

    // Transitions to other FSMState instances.
    private final Map<DistEventType, Set<FSMState>> transitions;

    // The process that this state is associated with. Initially this is -1, but
    // once a transition on an event is added, the pid is set based on the event
    // type.
    private int pid = -1;

    // The id used by this FSMState in scm output.
    private final int scmId;

    public FSMState(boolean isAccept, boolean isInitial, int pid, int scmId) {
        this.isAccept = isAccept;
        this.isInitial = isInitial;
        this.pid = pid;
        this.scmId = scmId;
        transitions = new LinkedHashMap<DistEventType, Set<FSMState>>();
    }

    // //////////////////////////////////////////////////////////////////

    @Override
    public boolean isInitial() {
        return isInitial;
    }

    @Override
    public boolean isAccept() {
        return isAccept;
    }

    @Override
    public Set<DistEventType> getTransitioningEvents() {
        return transitions.keySet();
    }

    /**
     * Returns the set of all possible following states for this FSMState and an
     * event.
     * 
     * @param event
     * @return
     */
    @Override
    public Set<FSMState> getNextStates(DistEventType event) {
        assert event != null;
        assert transitions.containsKey(event);

        return Collections.unmodifiableSet(transitions.get(event));
    }

    @Override
    public String toString() {
        return "FSM_state: init[" + isInitial + "], accept[" + isAccept
                + "] id[" + scmId + "]";
    }

    // //////////////////////////////////////////////////////////////////

    /** Returns the pid that this state is associated with. */
    public int getPid() {
        return pid;
    }

    /** Returns the scmId that this state is associated with. */
    public int getScmId() {
        return scmId;
    }

    /**
     * Adds a new transition to a state s on event e from this state.
     * 
     * @param e
     * @param s
     */
    public void addTransition(DistEventType e, FSMState s) {
        assert e != null;
        assert s != null;
        assert pid == e.getEventPid();

        addTransitionNoChecks(e, s);
    }

    /**
     * Adds a new synthetic transition that is associated with an invariant
     * channel. For now, this is similar to addTransition and omits a check that
     * disallow two processes from sending to the same queue.
     * 
     * @param e
     * @param s
     */
    public void addSynthTransition(DistEventType e, FSMState s) {
        assert e != null;
        assert s != null;
        assert e.isSynthSendEvent();

        addTransitionNoChecks(e, s);
    }

    /**
     * Removes an existing transition to a state s on event e from this state.
     * 
     * @param e
     * @param s
     */
    public void rmTransition(DistEventType e, FSMState s) {
        assert e != null;
        assert s != null;
        assert pid == e.getEventPid();
        assert transitions.containsKey(e);
        assert transitions.get(e).contains(s);

        transitions.get(e).remove(s);
    }

    /** Returns an SCM representation of this FSMStates. */
    public String toScmString(LocalEventsChannelId localEventsChId) {
        String ret = "state " + scmId + " :\n";

        String eStr;
        for (DistEventType e : transitions.keySet()) {
            // Build an scm representation of this event type.
            if (e.isCommEvent()) {
                eStr = e.toString(
                        Integer.toString(e.getChannelId().getScmId()), ' ');
            } else {
                // Local event: use local queue for local events.
                localEventsChId.addLocalEventString(e,
                        e.getScmEventFullString());
                eStr = localEventsChId.getScmId() + " ! "
                        + e.getScmEventFullString();
            }

            for (FSMState next : transitions.get(e)) {
                ret += "to " + next.getScmId() + " : when true , " + eStr
                        + " ;\n";
            }
        }

        return ret;
    }

    // //////////////////////////////////////////////////////////////////

    /** Adds a transition from this state on e to state s. */
    private void addTransitionNoChecks(DistEventType e, FSMState s) {
        Set<FSMState> following;
        if (transitions.get(e) == null) {
            following = new LinkedHashSet<FSMState>();
            transitions.put(e, following);
        } else {
            following = transitions.get(e);
        }

        // Make sure that we haven't added this transition to s on e before.
        if (following.contains(s)) {
            // assert !following.contains(s);
            return;
        }
        following.add(s);
    }

}
