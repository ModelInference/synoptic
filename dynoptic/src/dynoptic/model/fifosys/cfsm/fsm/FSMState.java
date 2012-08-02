package dynoptic.model.fifosys.cfsm.fsm;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import dynoptic.model.AbsFSMState;
import dynoptic.model.alphabet.EventType;

/**
 * <p>
 * Represents a state of a simple NFA FSM.
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
    private final Map<EventType, Set<FSMState>> transitions;

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
        transitions = new LinkedHashMap<EventType, Set<FSMState>>();
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
    public Set<EventType> getTransitioningEvents() {
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
    public Set<FSMState> getNextStates(EventType event) {
        assert event != null;
        assert transitions.containsKey(event);

        return Collections.unmodifiableSet(transitions.get(event));
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
    public void addTransition(EventType e, FSMState s) {
        assert e != null;
        assert s != null;
        assert pid == e.getEventPid();

        Set<FSMState> following;
        if (transitions.get(e) == null) {
            following = new LinkedHashSet<FSMState>();
            transitions.put(e, following);
        } else {
            following = transitions.get(e);
        }

        // Make sure that we haven't added this transition to s on e before.
        assert !following.contains(s);
        following.add(s);
    }

    /**
     * Removes an existing transition to a state s on event e from this state.
     * 
     * @param e
     * @param s
     */
    public void rmTransition(EventType e, FSMState s) {
        assert e != null;
        assert s != null;
        assert pid == e.getEventPid();
        assert transitions.containsKey(e);
        assert transitions.get(e).contains(s);

        transitions.get(e).remove(s);
    }

    /** Returns an SCM representation of this FSMStates. */
    public String toScmString() {
        String ret = "state " + scmId + " :\n";

        for (EventType e : transitions.keySet()) {
            String eStr = e.toScmString();
            for (FSMState next : transitions.get(e)) {
                ret += "to " + next.getScmId() + " : when true , " + eStr
                        + " ;\n";
            }
        }

        return ret;
    }

}
