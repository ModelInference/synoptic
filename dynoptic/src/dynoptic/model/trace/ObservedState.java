package dynoptic.model.trace;

import java.util.Set;

import dynoptic.model.IFSMState;
import dynoptic.model.alphabet.EventType;

/**
 * Represents a state that was observed or mined from a log.
 */
public class ObservedState implements IFSMState<ObservedState> {

    // Whether or not this state is an anonymous state -- it was synthesized
    // because no concrete state name was given in the trace between two events.
    boolean isAnon;

    // Whether or not this state is an initial state.
    boolean isInitial;

    // Whether or not this state is an accept state.
    boolean isAccept;

    // The string representation of this state.
    String name;

    // TODO: include things like line number and filename, and so forth.

    private static int prevAnonId = -1;

    private static String getNextAnonName() {
        prevAnonId++;
        return "a" + Integer.toString(prevAnonId);
    }

    public ObservedState(boolean isInit, boolean isAccept) {
        this(isInit, isAccept, getNextAnonName());
    }

    public ObservedState(boolean isInit, boolean isAccept, String name) {
        this.isInitial = isInit;
        this.isAccept = isAccept;
        this.name = name;
    }

    // //////////////////////////////////////////////////////////////////

    @Override
    public boolean isAccept() {
        return isAccept;
    }

    @Override
    public Set<EventType> getTransitioningEvents() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<ObservedState> getNextStates(EventType event) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String toString() {
        return name;
    }

    // //////////////////////////////////////////////////////////////////
    // @Override
    public boolean isInitial() {
        return isInitial;
    }
}
