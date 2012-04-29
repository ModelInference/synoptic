package synoptic.model.event;

/**
 * A class to encapsulate the abstract notion of an event type. This depends on
 * whether we're dealing with totally ordered or partially ordered logs. As
 * well, event types that are synthetic (INITIAL\TERMINAL event nodes) are
 * fundamentally different from other event types, so this class differentiates
 * between these.
 */
public abstract class EventType implements Comparable<EventType> {
    /**
     * The label used to distinguish the dummy initial node -- constructed to
     * transition to all initial trace log events.
     */
    protected static final String initialNodeLabel = "INITIAL";

    /**
     * The label used to distinguish the dummy terminal node -- constructed so
     * that all terminal trace log events transition to it.
     */
    protected static final String terminalNodeLabel = "TERMINAL";

    /**
     * Whether or not this EventType instance is the INITIAL node event type.
     */
    protected final boolean isInitialEventType;
    /**
     * Whether or not this EventType instance is the TERMINAL node event type.
     */
    protected final boolean isTerminalEventType;

    /**
     * Instantiates a new EventType that can be an INITIAL, a TERMINAL, or
     * neither. It cannot be both an INITIAL and a TERMINAL.
     * 
     * @param isInitialEventType
     *            whether or not the event type is an INITIAL
     * @param isTerminalEventType
     *            whether or not the event type is a TERMINAL
     */
    public EventType(boolean isInitialEventType, boolean isTerminalEventType) {
        assert (!(isInitialEventType && isTerminalEventType));
        this.isInitialEventType = isInitialEventType;
        this.isTerminalEventType = isTerminalEventType;
    }

    public boolean isInitialEventType() {
        return isInitialEventType;
    }

    public boolean isTerminalEventType() {
        return isTerminalEventType;
    }

    public boolean isSpecialEventType() {
        return isInitialEventType || isTerminalEventType;
    }

    // //////////////////////
    // These methods implement basic functionality for the two
    // boolean base data members:

    @Override
    public int compareTo(EventType eother) {
        int initCmp = ((Boolean) isInitialEventType)
                .compareTo(eother.isInitialEventType);
        if (initCmp != 0) {
            return initCmp;
        }
        return ((Boolean) isTerminalEventType)
                .compareTo(eother.isTerminalEventType);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (other == this) {
            return true;
        }
        if (this.getClass() != other.getClass()) {
            return false;
        }
        EventType eother = (EventType) other;
        return (isInitialEventType == eother.isInitialEventType)
                && (isTerminalEventType == eother.isTerminalEventType);
    }

    @Override
    public int hashCode() {
        return (isInitialEventType ? 1231 : 1301)
                + (isTerminalEventType ? 1831 : 1907);
    }
}
