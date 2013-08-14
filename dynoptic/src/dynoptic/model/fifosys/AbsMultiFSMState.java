package dynoptic.model.fifosys;

import java.util.Collection;
import java.util.Set;

import dynoptic.model.AbsFSMState;
import dynoptic.util.Util;

import synoptic.model.event.IDistEventType;

/**
 * Partial specification for the non-channel state of multiple FSM processes.
 * 
 * @param <State>
 *            The state of a single FSM process.
 */
public abstract class AbsMultiFSMState<State extends AbsFSMState<State, TxnEType>, TxnEType extends IDistEventType>
        extends AbsFSMState<State, TxnEType> {

    /** Used for functional calls to e.g., atLeastOneStateEvalTruePerPid. */
    protected interface IStatePidToBooleanFn<T> {
        boolean eval(T s, int pid);
    }

    // Fn: (ObservedFifoSysState s, pid p) -> "s accept for pid"
    protected static IStatePidToBooleanFn<AbsMultiFSMState<?, ?>> fnIsAcceptForPid = new IStatePidToBooleanFn<AbsMultiFSMState<?, ?>>() {
        @Override
        public boolean eval(AbsMultiFSMState<?, ?> s, int pid) {
            return s.isAcceptForPid(pid);
        }
    };

    // Fn: (ObservedFifoSysState s, pid p) -> "s initial for pid"
    protected static IStatePidToBooleanFn<AbsMultiFSMState<?, ?>> fnIsInitialForPid = new IStatePidToBooleanFn<AbsMultiFSMState<?, ?>>() {
        @Override
        public boolean eval(AbsMultiFSMState<?, ?> s, int pid) {
            return s.isInitForPid(pid);
        }
    };

    /** Used to evaluate whether this GFSMState is accept/initial. */
    protected static boolean atLeastOneStatePidEvalTrue(
            Collection<? extends AbsMultiFSMState<?, ?>> states,
            IStatePidToBooleanFn<AbsMultiFSMState<?, ?>> fn, int pid) {
        for (AbsMultiFSMState<?, ?> s : states) {
            if (fn.eval(s, pid)) {
                return true;
            }
        }
        return false;
    }

    /** Returns a set of states that evaluate to true through fn. */
    protected static <S extends AbsMultiFSMState<?, ?>> Set<S> getStatesThatEvalToTrueWithPid(
            Collection<S> states,
            IStatePidToBooleanFn<AbsMultiFSMState<?, ?>> fn, int pid) {
        Set<S> ret = Util.newSet();
        for (S s : states) {
            if (fn.eval(s, pid)) {
                ret.add(s);
            }
        }
        return ret;
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
    abstract public boolean isInitForPid(int pid);
}
