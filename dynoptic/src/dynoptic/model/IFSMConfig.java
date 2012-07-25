package dynoptic.model;

/**
 * Captures complete current state of an FSM.
 */
public interface IFSMConfig {
    /**
     * Whether or not the FSM configuration represents a valid terminal state
     * for the FSM.
     */
    public boolean isAccept();
}
