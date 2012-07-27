package dynoptic.model.fifosys;

import dynoptic.model.IFSMState;

public interface ICFSMState<State extends IFSMState<State>> extends
        IFSMState<State> {

    /**
     * Whether or not this CFSM state is an accept state for the specific
     * process id. That is, whether or not in this state:
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
