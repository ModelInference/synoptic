package dynoptic.model.fsm;

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
 * An FSMState maintains "abstract" transitions to other FSMState instances,
 * these are induced based on the "concrete" (observed) transitions from the
 * observed states corresponding to (grouped into) this FSMState. Note that the
 * FSM can be an NFA -- that is, an FSMState can have multiple transitions on
 * the same event that go to different FSMState instances.
 * </p>
 * <p>
 * In many ways this class mimics a Synoptic Partition class/concept.
 * </p>
 */
public class FSMState implements IFSMState {
    // This is the set of observed state instances.
    // TODO: include these.

    // Whether or not this state is an accepting state = whether or not any of
    // the observed states were terminal.
    boolean isAccept;

    // CACHE optimization: the set of abstract transitions induced by the
    // concrete transitions. This is merely a cached version of the ground
    // truth.
    Map<EventType, Set<FSMState>> transitions;

    public FSMState() {

    }

    public boolean isAccept() {
        return isAccept;
    }

    public Set<EventType> getPossibleEvents() {
        return transitions.keySet();
    }

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
        return transitions.get(event);
    }

    // //////////////////////////////////////////////////////////////////

    private void cacheTransition(EventType e, FSMState s) {
        Set<FSMState> following;
        if (transitions.get(e) == null) {
            following = new LinkedHashSet<FSMState>();
        } else {
            following = transitions.get(e);
        }
        following.add(s);
    }
}
