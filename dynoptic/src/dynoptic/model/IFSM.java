package dynoptic.model;

import java.util.Set;

import dynoptic.model.alphabet.Event;
import dynoptic.model.alphabet.FSMAlphabet;

/**
 * Describes a basic interface for an FSM.
 */
public interface IFSM<Config extends IFSMConfig> {
    /**
     * An FSM has a configuration, that captures the complete state of the FSM.
     */
    Config getConfig();

    /**
     * An FSM uses a finite alphabet of events.
     */
    FSMAlphabet getAlphabet();

    /**
     * An FSM transitions between states by processing events.
     * 
     * @param event
     * @return
     */
    Config transition(Event event);

    /**
     * Returns the set of events that are currently feasible (that this FSM can
     * transition on).
     */
    Set<Event> getEnabledEvents();
}
