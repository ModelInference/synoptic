package synoptic.model.event;

/**
 * Implements an EventType for a totally ordered log. In this case an event type
 * is essentially a unique string.
 */
public class StringEventType extends EventType {
    private final String eType;

    /**
     * Most expressive constructor that is used internally.
     */
    private StringEventType(String type, boolean isInitialEventType,
            boolean isTerminalEventType) {
        super(isInitialEventType, isTerminalEventType);
        eType = type;
    }

    /**
     * Creates a new StringEventType that is a non-INITIAL and non-TERMINAL.
     */
    public StringEventType(String type) {
        this(type, false, false);
    }

    /**
     * Creates a new StringEventType that is an INITIAL.
     */
    static public StringEventType newInitialStringEventType() {
        return new StringEventType(EventType.initialNodeLabel, true, false);
    }

    /**
     * Creates a new StringEventType that is an TERMINAL.
     */
    static public StringEventType newTerminalStringEventType() {
        return new StringEventType(EventType.terminalNodeLabel, false, true);
    }

    // ///////////////////////////////////////////////////////////////////////

    @Override
    public String getETypeLabel() {
        return eType;
    }

    @Override
    public int compareTo(EventType eother) {
        int baseCmp = super.compareTo(eother);
        if (baseCmp != 0) {
            return baseCmp;
        }
        return eType.compareTo(((StringEventType) eother).eType);
    }

    @Override
    public boolean equals(Object other) {
        if (!super.equals(other)) {
            return false;
        }
        return eType.equals(((StringEventType) other).eType);
    }

    @Override
    public String toString() {
        return eType;
    }

    @Override
    public int hashCode() {
        return eType.hashCode() + super.hashCode();
    }
}
