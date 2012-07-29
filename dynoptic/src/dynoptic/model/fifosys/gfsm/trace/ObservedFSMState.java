package dynoptic.model.fifosys.gfsm.trace;

/**
 * Represents the state observed at a _single_ FSM.
 */
public class ObservedFSMState {

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
    final private boolean isInitial;

    // Whether or not this state is a terminal state in a trace.
    final private boolean isTerminal;

    // The string representation of this state.
    final private String name;

    // The event that was observed to follow this state _locally_ (i.e., at this
    // process). For terminal states, this is null.
    private ObservedEvent nextEvent = null;

    // The state that was observed to follow this state locally.
    private ObservedFSMState nextState = null;

    // TODO: For non-anon states include things like line number and filename,
    // and so forth.

    public static ObservedFSMState ObservedTerminalFSMState(int pid, String name) {
        if (name == null) {
            name = getNextAnonName();
        }
        return new ObservedFSMState(pid, false, true, name);
    }

    public static ObservedFSMState ObservedInitialFSMState(int pid, String name) {
        if (name == null) {
            name = getNextAnonName();
        }
        return new ObservedFSMState(pid, false, true, name);
    }

    public static ObservedFSMState ObservedIntermediateFSMState(int pid,
            String name) {
        if (name == null) {
            name = getNextAnonName();
        }
        return new ObservedFSMState(pid, false, false, name);
    }

    // //////////////////////////////////////////////////////////////////

    private ObservedFSMState(int pid, boolean isInit, boolean isTerminal,
            String name) {
        this.pid = pid;
        this.isInitial = isInit;
        this.isTerminal = isTerminal;
        this.name = name;
    }

    // //////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        return name;
    }

    /** Use this method to set the event and state that followed this state. */
    public void addTransition(ObservedEvent event, ObservedFSMState followState) {
        this.nextEvent = event;
        this.nextState = followState;
    }

    public boolean isInitial() {
        return isInitial;
    }

    public boolean isTerminal() {
        return isTerminal;
    }

    public ObservedEvent getNextEvent() {
        return nextEvent;
    }

    public ObservedFSMState getNextState() {
        return nextState;
    }
}
