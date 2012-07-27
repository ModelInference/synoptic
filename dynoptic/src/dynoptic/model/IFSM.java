package dynoptic.model;

import dynoptic.model.alphabet.FSMAlphabet;

/**
 * Describes a basic interface for an FSM.
 */
public interface IFSM<State extends IFSMState<State>> {
    /**
     * Returns the initial state for the FSM.
     * 
     * <pre>
     * TODO: expand this to allow for multiple initial states.
     * </pre>
     */
    State getInitState();

    /**
     * Returns the accept state for the FSM.
     * 
     * <pre>
     * TODO: expand this to allow for multiple initial states.
     * </pre>
     */
    State getAcceptState();

    /**
     * An FSM uses a finite alphabet of events.
     */
    FSMAlphabet getAlphabet();
}
