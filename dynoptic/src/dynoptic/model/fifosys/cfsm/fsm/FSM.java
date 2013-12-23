package dynoptic.model.fifosys.cfsm.fsm;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;
import dynoptic.main.DynopticMain;
import dynoptic.model.AbsFSM;
import dynoptic.model.automaton.EncodedAutomaton;
import dynoptic.model.automaton.EventTypeEncodings;
import dynoptic.model.fifosys.channel.channelid.LocalEventsChannelId;
import dynoptic.util.Util;

import synoptic.model.event.DistEventType;

/**
 * This class models FSMs that make up a CFSM. A few key characteristics:
 * 
 * <pre>
 * 1. It does not maintain channel state. This is done by FifoState/FifoSysExecution instances.
 * 2. Almost all fields are immutable. Pre-construct all FSMState instances prior to constructing this FSM.
 * 3. It does not maintain inter-state transitions. These are managed by FSMState instances.
 * </pre>
 */
public class FSM extends AbsFSM<FSMState, DistEventType> {
    // The process id of this FSM in the CFSM.
    final int pid;

    // This keeps track of the scmId to be used by FSMState instances associated
    // with this FSM.
    private int nextScmFSMStateId = 0;

    public FSM(int pid, FSMState initState, FSMState acceptState,
            Set<FSMState> states, int nextScmFSMStateId) {
        this(pid, Collections.singleton(initState), Collections
                .singleton(acceptState), states, nextScmFSMStateId);
    }

    public FSM(int pid, Set<FSMState> initStates, Set<FSMState> acceptStates,
            Collection<FSMState> states, int nextScmFSMStateId) {
        super();

        assert states != null;
        assert initStates != null;
        assert acceptStates != null;
        assert states.containsAll(initStates);
        assert states.containsAll(acceptStates);
        assert nextScmFSMStateId >= 0;

        if (DynopticMain.assertsOn) {
            // Check that:
            // 1. all states transition only to states in the states collection
            // 2. all states have a pid that matches the pid of this FSM
            // 3. all init/accept states are in fact init/accept
            Set<Integer> scmIds = Util.newSet();
            for (FSMState s : states) {
                // NOTE: states might contain duplicates!
                scmIds.add(s.getStateId());
                assert (states.containsAll(s.getNextStates()));
                assert s.getPid() == pid;
                assert nextScmFSMStateId > s.getStateId();
            }

            for (FSMState s : initStates) {
                assert s.isInitial();
            }
            for (FSMState s : acceptStates) {
                assert s.isAccept();
            }
        }

        this.pid = pid;
        this.nextScmFSMStateId = nextScmFSMStateId;
        this.states.addAll(states);
        this.initStates.addAll(initStates);
        this.acceptStates.addAll(acceptStates);

        // Construct the alphabet from the events associated with each state.
        this.recomputeAlphabet();
    }

    // //////////////////////////////////////////////////////////////////

    /** Adds a new synthetic state for tracking events for invariants checking. */
    public void addSyntheticState(FSMState parent, FSMState child,
            DistEventType eToTrace, DistEventType eTracer1,
            DistEventType eTracer2) {
        assert this.states.contains(parent);
        assert this.states.contains(child);

        parent.rmTransition(eToTrace, child);

        FSMState synthState1 = new FSMState(false, false, pid,
                nextScmFSMStateId);
        nextScmFSMStateId++;

        FSMState synthState2 = new FSMState(false, false, pid,
                nextScmFSMStateId);
        nextScmFSMStateId++;

        // The tracer events t1 and t2 flank the event to trace so that a queue
        // sequence of 't1t2' would indicate exactly when the event to trace
        // occurred.
        parent.addSynthTransition(eTracer1, synthState1);
        synthState1.addTransition(eToTrace, synthState2);
        synthState2.addSynthTransition(eTracer2, child);

        this.states.add(synthState1);
        this.states.add(synthState2);
    }

    @Override
    public Set<FSMState> getStates() {
        return this.states;
    }

    public int getPid() {
        return this.pid;
    }

    /**
     * @return true if this FSM is deterministic.
     */
    public boolean isDeterministic() {
        if (initStates.size() > 1) {
            return false;
        }
        for (FSMState state : states) {
            Set<DistEventType> events = state.getTransitioningEvents();

            for (DistEventType event : events) {
                if (state.getNextStates(event).size() > 1) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Performs Hopcroft's algorithm to minimize this FSM.
     */
    public void minimize() {
        // minimize encoded automaton
        EventTypeEncodings encodings = getEventTypeEncodings();
        EncodedAutomaton encodedAutomaton = getEncodedAutomaton(encodings);
        encodedAutomaton.minimize();
        Automaton minAutomaton = encodedAutomaton.getAutomaton();

        /* minimize this FSM */
        // clear all states (states might be collapsed after minimized)
        states.clear();
        initStates.clear();
        acceptStates.clear();

        State initState = minAutomaton.getInitialState();
        FSMState fsmInitState = new FSMState(initState.isAccept(), true, pid, 0);
        // populate states of this FSM again
        states.add(fsmInitState);
        initStates.add(fsmInitState);
        if (initState.isAccept()) {
            acceptStates.add(fsmInitState);
        }
        // map: automaton state -> FSM state
        Map<State, FSMState> visited = new LinkedHashMap<State, FSMState>();
        DFS(initState, fsmInitState, visited, encodings);

        recomputeAlphabet();
    }

    /**
     * Traverses the Automaton while constructing an equivalent FSM.
     * 
     * @param state
     *            - Automaton state to begin DFS
     * @param fsmState
     *            - FSM state equivalent to the Automaton state
     * @param visited
     *            - mapping from visited Automaton states to their corresponding
     *            FSM states
     * @param encodings
     *            - EventType encodings
     */
    private void DFS(State state, FSMState fsmState,
            Map<State, FSMState> visited, EventTypeEncodings encodings) {
        visited.put(state, fsmState);
        Set<Transition> transitions = state.getTransitions();

        for (Transition transition : transitions) {
            State nextState = transition.getDest();

            FSMState nextFSMState = visited.get(nextState);

            if (nextFSMState == null) {
                // Note: automaton has only 1 initial state
                nextFSMState = new FSMState(nextState.isAccept(), false, pid,
                        visited.size());
                // populate states of this FSM again
                states.add(nextFSMState);
                if (nextState.isAccept()) {
                    acceptStates.add(nextFSMState);
                }

                // nextState has not been visited
                DFS(nextState, nextFSMState, visited, encodings);
            }

            // Interval min and max are inclusive.
            char min = transition.getMin();
            char max = transition.getMax();
            for (char c = min; c <= max; c++) {
                DistEventType e = encodings.getEventType(c);
                fsmState.addTransition(e, nextFSMState);
            }

        }
    }

    /**
     * Creates EventType encodings for all transitioning events in this FSM.
     * Note that, when comparing any 2 FSMs, only encodings from one of them is
     * used.
     * 
     * @return EventType encodings
     */
    public EventTypeEncodings getEventTypeEncodings() {
        recomputeAlphabet(); // events of this FSM might have changed
        return new EventTypeEncodings(alphabet);
    }

    /**
     * Creates an EncodedAutomaton for this FSM using the given EventType
     * encodings.
     * 
     * @return EncodedAutomaton
     */
    public EncodedAutomaton getEncodedAutomaton(
            EventTypeEncodings eventEncodings) {
        return new EncodedAutomaton(eventEncodings, this);
    }

    /**
     * @return true if the language of this FSM is equal to the language of the
     *         given FSM.
     */
    @Override
    public int hashCode() {
        EventTypeEncodings eventEncodings = getEventTypeEncodings();
        EncodedAutomaton thisAutomaton = getEncodedAutomaton(eventEncodings);
        int ret = 31;
        ret = ret * 31 + thisAutomaton.hashCode();
        ret = ret * 31 + pid;
        return ret;
    }

    /**
     * @return true if the language of this FSM is equal to the language of the
     *         given FSM.
     */
    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (this == other) {
            return true;
        }
        if (!(other instanceof FSM)) {
            return false;
        }
        FSM fsm = (FSM) other;

        if (pid != fsm.pid) {
            return false;
        }

        // Use encodings of this.
        EventTypeEncodings eventEncodings = getEventTypeEncodings();
        EncodedAutomaton thisAutomaton = getEncodedAutomaton(eventEncodings);
        EncodedAutomaton otherAutomaton = fsm
                .getEncodedAutomaton(eventEncodings);
        return thisAutomaton.equals(otherAutomaton);
    }

    @Override
    public String toString() {
        String ret = "FSM[pid=" + pid + "]";
        ret += "\n\tstates: " + states.toString();
        ret += "\n\tinits: " + initStates.toString();
        ret += "\n\taccepts: " + acceptStates.toString();
        return ret;
    }

    /**
     * Generate SCM representation of this FSM, using a specific channelIds
     * ordering.
     */
    public String toScmString(LocalEventsChannelId localEventsChId) {
        assert !initStates.isEmpty();

        String ret;

        ret = null;
        for (FSMState s : initStates) {
            if (ret == null) {
                ret = "initial : " + s.getStateId();
            } else {
                ret += " , " + s.getStateId();
            }
        }
        ret += "\n";

        for (FSMState s : states) {
            ret += s.toScmString(localEventsChId);
            ret += "\n\n";
        }

        return ret;
    }

    /**
     * Generate Promela representation of this FSM.
     */
    public String toPromelaString(String stateVar) {
        assert !initStates.isEmpty();

        String ret = "";

        // If we have more than one initial state, then we choose
        // non-deterministically between the available initial states.
        if (initStates.size() > 1) {
            ret += "select(" + stateVar + " : 0 .. "
                    + Integer.toString(initStates.size() - 1) + ")";
        }

        ret += "do\n";
        ret += " :: ";
        for (FSMState s : states) {
            ret += s.toPromelaString(stateVar);
            ret += "\n\n";
        }

        ret += "od\n";
        return ret;
    }
}
