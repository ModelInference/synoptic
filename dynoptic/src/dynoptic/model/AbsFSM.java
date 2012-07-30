package dynoptic.model;

import java.util.LinkedHashSet;
import java.util.Set;

import dynoptic.model.alphabet.FSMAlphabet;

/**
 * Describes a basic interface for an FSM.
 */
public abstract class AbsFSM<State extends AbsFSMState<State>> {

    // The set of all states associated with this FSM. This includes initial and
    // accept states. States manage transitions internally.
    protected final Set<State> states;

    // This FSM's alphabet.
    protected final FSMAlphabet alphabet;

    // Initial, and accept states.
    protected final Set<State> initStates;
    protected final Set<State> acceptStates;

    public AbsFSM() {
        this.states = new LinkedHashSet<State>();
        this.alphabet = new FSMAlphabet();
        this.initStates = new LinkedHashSet<State>();
        this.acceptStates = new LinkedHashSet<State>();
    }

    /**
     * Returns the initial states for the FSM.
     */
    public Set<State> getInitStates() {
        return initStates;
    }

    /**
     * Returns the accept states for the FSM.
     */
    public Set<State> getAcceptStates() {
        return acceptStates;
    }

    /**
     * An FSM uses a finite alphabet of events.
     */
    public FSMAlphabet getAlphabet() {
        return alphabet;
    }

    // //////////////////////////////////////////////////////////////////

    protected void recomputeAlphabet() {
        this.alphabet.clear();
        for (State s : states) {
            this.alphabet.addAll(s.getTransitioningEvents());
        }
    }

}
