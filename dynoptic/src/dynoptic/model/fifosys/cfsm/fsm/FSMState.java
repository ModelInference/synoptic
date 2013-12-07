package dynoptic.model.fifosys.cfsm.fsm;

import java.util.Map;
import java.util.Set;

import dynoptic.model.AbsFSMState;
import dynoptic.model.fifosys.channel.channelid.LocalEventsChannelId;
import dynoptic.util.Util;

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
public class FSMState extends AbsFSMState<FSMState, DistEventType> {
    // Whether or not this state is an accepting/initial state.
    private boolean isAccept;
    private boolean isInitial;

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
        transitions = Util.newMap();
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

        // NOTE: Unfortunately, we can't return
        // Collections.unmodifiableSet(transitions.get(event))
        // because transitions are iterated over and at the same time modified
        // in CFSM.recurseAddSendToEventTx(). This is a potential, but
        // difficult, FIXME.
        return Util.newSet(transitions.get(event));
    }

    public String toLongString() {
        return "FSM_state: init[" + isInitial + "], accept[" + isAccept
                + "] id[" + scmId + "]";
    }

    public String toShortIntString() {
        // return String.valueOf(pid) + "." + String.valueOf(scmId);
        return String.valueOf(scmId);
    }

    @Override
    public String toString() {
        return toShortIntString();
    }

    @Override
    public int hashCode() {
        int ret = 31;
        ret = ret * 31 + (isAccept ? 1 : 0);
        ret = ret * 31 + (isInitial ? 1 : 0);
        // FIXME: Issue 276
        // Not using transitions because they cause a stack overflow.
        ret = ret * 31 + pid;
        ret = ret * 31 + scmId;
        return 1;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof FSMState)) {
            return false;
        }

        FSMState state = (FSMState) other;

        if (state.isAccept != this.isAccept) {
            return false;
        }

        if (state.isInitial != this.isInitial) {
            return false;
        }

        // FIXME: Issue 276
        // Not using transitions because they cause a stack overflow.

        if (state.pid != this.pid) {
            return false;
        }

        if (state.scmId != this.scmId) {
            return false;
        }

        return true;
    }

    // //////////////////////////////////////////////////////////////////

    public void setAccept() {
        isAccept = true;
    }

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
        assert pid == e.getPid();

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
        assert pid == e.getPid();
        assert transitions.containsKey(e);

        Set<FSMState> children = transitions.get(e);
        assert children.contains(s);

        children.remove(s);
        if (children.isEmpty()) {
            transitions.remove(e);
        }
    }

    /**
     * Returns an SCM representation of this FSMState. Updates the internal
     * mapping maintained by localEventsChId to account for any local event
     * transitions from this state.
     */
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
                String eTypeStr = e.getScmEventFullString();
                localEventsChId.addLocalEventString(e, eTypeStr);
                eStr = localEventsChId.getScmId() + " ! " + eTypeStr;
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
            following = Util.newSet();
            transitions.put(e, following);
        } else {
            following = transitions.get(e);
        }

        // Since following is a set, it's okay if we have added the transition
        // to s on e before.
        following.add(s);
    }

}
