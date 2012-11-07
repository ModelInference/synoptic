package dynoptic.model.fifosys.gfsm.observed;

import java.util.Map;

import dynoptic.util.Util;

import synoptic.model.event.DistEventType;
import synoptic.util.Pair;

/**
 * <p>
 * Represents the state observed at a _single_ FSM, without any context -- i.e.,
 * no transition events or following states.
 * </p>
 * <p>
 * An ObservedFSMState instance is maintained by an ObsDAGNode. There is exactly
 * one instance of this class per observed state so as to minimize number of
 * instances. The corresponding ObsDAGNode instance maintains transitions,
 * separately from this class.
 * </p>
 */
public class ObsFSMState {

    private static int prevAnonId = -1;

    private static String getNextAnonName() {
        prevAnonId++;
        return Integer.toString(prevAnonId);
    }

    // //////////////////////////////////////////////////////////////////

    // The process id of the FSM that generated this state.
    final private int pid;

    // Whether or not this state is an anonymous state -- it was synthesized
    // because no concrete state name was given in the trace between two events.
    final boolean isAnon;

    // Whether or not this state is an initial state.
    private boolean isInitial;

    // Whether or not this state is a terminal state in a trace.
    // _not_ final because this field might change if we learn that process
    // can terminate in a state that we've created for a previous trace.
    private boolean isTerminal;

    // The string representation of this state.
    final private String name;

    // TODO: For non-anon states include things like line number and filename,
    // and so forth.

    // A cache of previously created ObsFSMState instances. This is used by code
    // that generates consistent process states -- the state of a process is
    // determined by the sequence of events (= previous state + next event) that
    // it executed, and each process begins execution in the same initial state.
    private static final Map<Pair<ObsFSMState, DistEventType>, ObsFSMState> prevStateAndEventMap;
    private static final Map<Integer, ObsFSMState> initialProcessStatesMap;

    static {
        prevStateAndEventMap = Util.newMap();
        initialProcessStatesMap = Util.newMap();
    }

    // Used by tests and DynopticMain to clear the states cache.
    public static void clearCache() {
        prevStateAndEventMap.clear();
        initialProcessStatesMap.clear();
        prevAnonId = -1;
    }

    /** Anonymous, globally-unique states. */
    public static ObsFSMState anonObsFSMState(int pid, boolean isInit,
            boolean isTerm) {
        return new ObsFSMState(pid, isInit, isTerm, getNextAnonName(), true);
    }

    /** Named states. */
    public static ObsFSMState namedObsFSMState(int pid, String name,
            boolean isInit, boolean isTerm) {
        return new ObsFSMState(pid, isInit, isTerm, name, false);
    }

    /** Returns the _initial_ consistent anonymous state for a process. */
    public static ObsFSMState consistentAnonInitObsFSMState(int pid) {
        if (initialProcessStatesMap.containsKey(pid)) {
            return initialProcessStatesMap.get(pid);
        }

        String name = Integer.toString(pid);
        ObsFSMState state = new ObsFSMState(pid, true, false, name, true);
        initialProcessStatesMap.put(pid, state);
        return state;
    }

    /** Consistent anonymous (non-initial) state. */
    public static ObsFSMState consistentAnonObsFSMState(ObsFSMState prevState,
            DistEventType prevEvent) {
        assert prevState != null;
        assert prevEvent != null;
        // The event must correspond to the process of prevState.
        assert prevEvent.getPid() == prevState.getPid();

        Pair<ObsFSMState, DistEventType> key = Util.newPair(prevState,
                prevEvent);
        if (prevStateAndEventMap.containsKey(key)) {
            return prevStateAndEventMap.get(key);
        }

        String name = prevState.getName() + "." + prevEvent.toString();
        ObsFSMState state = new ObsFSMState(prevState.getPid(), false, false,
                name, true);
        prevStateAndEventMap.put(key, state);
        return state;
    }

    // //////////////////////////////////////////////////////////////////

    private ObsFSMState(int pid, boolean isInit, boolean isTerminal,
            String name, boolean isAnon) {
        assert name != null;

        this.pid = pid;
        this.isInitial = isInit;
        this.isTerminal = isTerminal;
        this.name = name;
        this.isAnon = isAnon;
    }

    // //////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        return ((isInitial) ? "i_" : "") + ((isAnon) ? "a" : "") + name
                + ((isTerminal) ? "_t" : "");
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + (isInitial ? 1231 : 1237);
        result = 31 * result + (isTerminal ? 1231 : 1237);
        result = 31 * result + pid;
        result = 31 * result + name.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (this == other) {
            return true;
        }
        if (!(other instanceof ObsFSMState)) {
            return false;
        }
        ObsFSMState otherF = (ObsFSMState) other;
        if (otherF.isInitial() != isInitial) {
            return false;
        }

        if (otherF.isTerminal() != isTerminal) {
            return false;
        }

        if (otherF.getPid() != pid) {
            return false;
        }

        if (!otherF.getName().equals(name)) {
            return false;
        }
        return true;
    }

    // //////////////////////////////////////////////////////////////////

    public int getPid() {
        return pid;
    }

    public void markTerm() {
        this.isTerminal = true;
    }

    public boolean isInitial() {
        return isInitial;
    }

    public boolean isTerminal() {
        return isTerminal;
    }

    public String getName() {
        return name;
    }
}