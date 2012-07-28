package dynoptic.model.fifosys.cfsm.fsm;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import dynoptic.model.IFSMState;
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
public class FSMState implements IFSMState<FSMState> {
    // Whether or not this state is an accepting state.
    final boolean isAccept;

    // Transitions to other FSMState instances.
    final Map<EventType, Set<FSMState>> transitions;

    public FSMState(boolean isAccept) {
        this.isAccept = isAccept;
        transitions = new LinkedHashMap<EventType, Set<FSMState>>();
    }

    // //////////////////////////////////////////////////////////////////

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

    /**
     * Adds a new transition to a state s on event e from this state.
     * 
     * @param e
     * @param s
     */
    public void addTransition(EventType e, FSMState s) {
        assert s != null;

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
