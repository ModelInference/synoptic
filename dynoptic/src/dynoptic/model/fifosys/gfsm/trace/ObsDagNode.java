package dynoptic.model.fifosys.gfsm.trace;

/**
 * Represents an observed state, or a state that could have been instantaneously
 * observed, based on the DAG of observed events.
 */
public class ObsDagNode {
    // The event that was observed to follow this state _locally_ (i.e., at this
    // process). For terminal states, this is null.
    private ObservedEvent nextEvent = null;

    // The state that was observed to follow this state locally.
    private ObservedFSMState nextState = null;

    /** Use this method to set the event and state that followed this state. */
    public void addTransition(ObservedEvent event, ObservedFSMState followState) {
        this.nextEvent = event;
        this.nextState = followState;
    }

    public ObservedEvent getNextEvent() {
        return nextEvent;
    }

    public ObservedFSMState getNextState() {
        return nextState;
    }
}
