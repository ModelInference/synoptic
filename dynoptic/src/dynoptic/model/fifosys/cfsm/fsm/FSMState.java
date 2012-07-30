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
    final boolean isAccept;
    final boolean isInitial;

    // Transitions to other FSMState instances.
    final Map<EventType, Set<FSMState>> transitions;

    // The process that this state is associated with. Initially this is -1, but
    // once a transition on an event is added, the pid is set based on the event
    // type.
    int pid = -1;

    public FSMState(boolean isAccept, boolean isInitial, int pid) {
        this.isAccept = isAccept;
        this.isInitial = isInitial;
        this.pid = pid;
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

        if (!transitions.containsKey(event)) {
            return Collections.<FSMState> emptySet();
        }
        return Collections.unmodifiableSet(transitions.get(event));
    }

    // //////////////////////////////////////////////////////////////////

    /** Returns the pid that this state is associated with. */
    public int getPid() {
        return pid;
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
}
