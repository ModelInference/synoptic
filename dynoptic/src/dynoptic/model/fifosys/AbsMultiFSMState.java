package dynoptic.model.fifosys;

import java.util.Collection;

import dynoptic.model.AbsFSMState;

/**
 * Interface for the non-channel state of multiple FSM processes.
 * 
 * @param <State>
 *            The state of a single FSM process.
 */
public abstract class AbsMultiFSMState<State extends AbsFSMState<State>>
        extends AbsFSMState<State> {

    /** Used for functional calls to atLeastOneStateEvalTruePerPid. */
    protected interface IStatePidToBooleanFn {
        boolean eval(AbsMultiFSMState<?> s, int pid);
    }

    // Fn: (ObservedFifoSysState s, pid p) -> "s accept for pid"
    protected static IStatePidToBooleanFn fnIsAcceptForPid = new IStatePidToBooleanFn() {
        @Override
        public boolean eval(AbsMultiFSMState<?> s, int pid) {
            return s.isAcceptForPid(pid);
        }
    };

    // Fn: (ObservedFifoSysState s, pid p) -> "s initial for pid"
    protected static IStatePidToBooleanFn fnIsInitialForPid = new IStatePidToBooleanFn() {
        @Override
        public boolean eval(AbsMultiFSMState<?> s, int pid) {
            return s.isInitialForPid(pid);
        }
    };

    /** Used to evaluate whether this GFSMState is accept/initial. */
    protected static boolean atLeastStatePidEvalTrue(
            Collection<? extends AbsMultiFSMState<?>> states,
            IStatePidToBooleanFn fn, int pid) {
        // for (ObservedFifoSysState s : observedStates) {
        for (AbsMultiFSMState<?> s : states) {
            if (fn.eval(s, pid)) {
                return true;
            }
        }
        return false;
    }

    // //////////////////////////////////////////////////////////////////

    /** The total number of processes that this multi-FSM state captures, */
    protected int numProcesses;

    public AbsMultiFSMState(int numProcesses) {
        this.numProcesses = numProcesses;
    }

    public int getNumProcesses() {
        return numProcesses;
    }

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
    abstract public boolean isAcceptForPid(int pid);

    /** Similar to accept state definition above. */
    abstract public boolean isInitialForPid(int pid);
}
