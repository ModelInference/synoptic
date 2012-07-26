package dynoptic.model.fifosys.cfsm.fsm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import dynoptic.model.IFSMState;
import dynoptic.model.alphabet.EventType;

import synoptic.util.InternalSynopticException;

/**
 * <p>
 * Represents a state of a simple NFA FSM.
 * </p>
 * <p>
 * An FSMState maintains abstract transitions to other FSMState instances. It is
 * completely disassociated form the concrete/observed transitions and states.
 * Note that an FSMState can have multiple transitions on the same event that go
 * to different FSMState instances (the FSM can be an NFA).
 * </p>
 */
public class FSMState implements IFSMState {
    // Whether or not this state is an accepting state.
    boolean isAccept;

    // Transitions to other FSMState instances.
    Map<EventType, Set<FSMState>> transitions;

    public FSMState() {

    }

    public boolean isAccept() {
        return isAccept;
    }

    public Set<EventType> getPossibleEvents() {
        return transitions.keySet();
    }

    /**
     * Returns a single next state based on an event transition. If multiple
     * states are possible, then a state is returned non-deterministically.
     * 
     * @param event
     * @return
     */
    public FSMState getNextState(EventType event) {
        if (!transitions.containsKey(event)) {
            throw new InternalSynopticException(
                    "Cannot transition on an event that is not possible from this state.");
        }
        // Get the next state non-deterministically (randomly) based on event.
        ArrayList<FSMState> following = new ArrayList<FSMState>(
                transitions.get(event));
        int i = new Random().nextInt(following.size());
        return following.get(i);
    }

    /**
     * Returns the set of all possible following states for this FSMState.
     */
    public Set<FSMState> getPossibleFollowingStates(EventType event) {
        if (!transitions.containsKey(event)) {
            return Collections.<FSMState> emptySet();
        }
        return Collections.unmodifiableSet(transitions.get(event));
    }

    // //////////////////////////////////////////////////////////////////

    private void addTransition(EventType e, FSMState s) {
        Set<FSMState> following;
        if (transitions.get(e) == null) {
            following = new LinkedHashSet<FSMState>();
        } else {
            following = transitions.get(e);
        }
        following.add(s);
    }
}
