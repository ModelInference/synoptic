package dynoptic.model.fifosys.gfsm.observed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import synoptic.model.event.DistEventType;

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
        return "a" + Integer.toString(prevAnonId);
    }

    // //////////////////////////////////////////////////////////////////

    // The process id of the FSM that generated this state.
    final private int pid;

    // Whether or not this state is an anonymous state -- it was synthesized
    // because no concrete state name was given in the trace between two events.
    // final boolean isAnon;

    // Whether or not this state is an initial state.
    private boolean isInitial;

    // Whether or not this state is a terminal state in a trace.
    private boolean isTerminal;

    // The string representation of this state.
    final private String name;

    // TODO: For non-anon states include things like line number and filename,
    // and so forth.
    
    // A global cache of previously created ObsFSMState instances. This takes
    // advantage of having consistent initial states.
    // Note: Key is a list [state_name, is_initial, is_terminal].
    private static final Map<List<String>, ObsFSMState> obsFsmStatesMap;
    
    static {
        obsFsmStatesMap = new HashMap<List<String>, ObsFSMState>();
    }

    // /////////// Terminal states:

    /**
     * Creates anonymous globally-unique terminal state.
     */
    public static ObsFSMState ObservedTerminalFSMState(int pid) {
        return new ObsFSMState(pid, false, true, getNextAnonName());
    }

    public static ObsFSMState ObservedTerminalFSMState(int pid, String name) {
        return new ObsFSMState(pid, false, true, name);
    }
    
    /**
     * Creates anonymous per-process terminal state.
     */
    public static ObsFSMState ObservedTerminalFSMState(int pid, ObsFSMState prevState,
            DistEventType prevEvent) {
        int nameHash = prevState.hashCode();
        nameHash = 31 * nameHash + prevEvent.hashCode();
        String name = "a" + Integer.toString(nameHash);
        return getObsFSMState(pid, false, true, name);
    }

    // /////////// Initial states:

    /**
     * Creates anonymous globally-unique initial state.
     */
    public static ObsFSMState ObservedInitialFSMState(int pid) {
        return new ObsFSMState(pid, true, false, getNextAnonName());
    }

    public static ObsFSMState ObservedInitialFSMState(int pid, String name) {
        return new ObsFSMState(pid, true, false, name);
    }
    
    /**
     * Creates anonymous per-process initial state.
     */
    public static ObsFSMState ObservedPerProcessInitialFSMState(int pid) {
        String name = "a" + Integer.toString(pid);
        return getObsFSMState(pid, true, false, name);
    }

    // /////////// Terminal+Initial states:

    /**
     * Creates anonymous globally-unique initial/terminal state.
     */
    public static ObsFSMState ObservedInitialTerminalFSMState(int pid) {
        return new ObsFSMState(pid, true, true, getNextAnonName());
    }

    public static ObsFSMState ObservedInitialTerminalFSMState(int pid,
            String name) {
        return new ObsFSMState(pid, true, true, name);
    }
    
    /**
     * Creates anonymous per-process initial/terminal state.
     */
    public static ObsFSMState ObservedPerProcessInitialTerminalFSMState(int pid) {
        String name = "a" + Integer.toString(pid);
        return getObsFSMState(pid, true, true, name);
    }

    // /////////// Intermediate states:

    /**
     * Creates anonymous globally-unique intermediate state.
     */
    public static ObsFSMState ObservedIntermediateFSMState(int pid) {
        return new ObsFSMState(pid, false, false, getNextAnonName());
    }

    public static ObsFSMState ObservedIntermediateFSMState(int pid, String name) {
        return new ObsFSMState(pid, false, false, name);
    }
    
    /**
     * Creates anonymous per-process intermediate state.
     */
    public static ObsFSMState ObservedIntermediateFSMState(int pid, ObsFSMState prevState,
            DistEventType prevEvent) {
        int nameHash = prevState.hashCode();
        nameHash = 31 * nameHash + prevEvent.hashCode();
        String name = "a" + Integer.toString(nameHash);
        return getObsFSMState(pid, false, false, name);
    }

    // //////////////////////////////////////////////////////////////////

    // Returns a cached ObsFSMState if one was previously created.
    // Otherwise, creates a new ObsFSMState, caches and returns it.
    private static ObsFSMState getObsFSMState(int pid, boolean isInit, boolean isTerm,
            String name) {
        List<String> key = new ArrayList<String>(3);
        key.add(name);
        key.add(Boolean.toString(isInit));
        key.add(Boolean.toString(isTerm));
        
        if (obsFsmStatesMap.containsKey(key)) {
            return obsFsmStatesMap.get(key);
        }
        
        ObsFSMState state = new ObsFSMState(pid, false, true, name);
        obsFsmStatesMap.put(key, state);
        return state;
    }
    
    private ObsFSMState(int pid, boolean isInit, boolean isTerminal, String name) {
        assert name != null;

        this.pid = pid;
        this.isInitial = isInit;
        this.isTerminal = isTerminal;
        this.name = name;
    }

    // //////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        return ((isInitial) ? "i_" : "") + name + ((isTerminal) ? "_t" : "");
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

        if (!otherF.isTerminal() != isTerminal) {
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

    public void markInit() {
        this.isInitial = true;
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