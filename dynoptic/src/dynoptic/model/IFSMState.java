package dynoptic.model;

/**
 * Captures complete state of an FSM at some instant.
 */
public interface IFSMState {
    /**
     * Whether or not the FSM state is a valid terminal state for the FSM.
     */
    public boolean isAccept();
}
