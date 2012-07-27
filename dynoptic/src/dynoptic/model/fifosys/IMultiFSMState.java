package dynoptic.model.fifosys;

import dynoptic.model.IFSMState;

/**
 * Interface for the non-channel state of multiple FSM processes.
 * 
 * @param <State>
 *            The state of a single FSM process.
 */
public interface IMultiFSMState<State extends IFSMState<State>> extends
        IFSMState<State> {

    /**
     * Whether or not this state is an accept state for the specific process id.
     * For processes that communicate over queues, this is defined as:
     * 
     * <pre>
     * (1) the specified pid is in an accept state, and
     * (2) all queues where this pid is a _receiver_ are empty.
     * </pre>
     * 
     * @param pid
     * @return
     */
    boolean isAcceptForPid(int pid);
}
